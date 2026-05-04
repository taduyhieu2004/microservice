# Authorization Matrix — TaskFlow

## 1. Mô hình phân quyền

### 1.1 Vai trò

| Role | Phạm vi | Mô tả |
|---|---|---|
| `OWNER` | per-project | Người tạo project. Toàn quyền + xoá project + chuyển owner |
| `ADMIN` | per-project | Quản lý thành viên, board, sprint, label. Không xoá project |
| `EDITOR` | per-project | Tạo/sửa/xoá task, board, list, attachment, dependency |
| `COMMENTER` | per-project | Xem + comment (không sửa task) |
| `VIEWER` | per-project | Chỉ đọc |
| `GUEST` | global | User đã login nhưng KHÔNG là member của project đang truy cập |
| `ANONYMOUS` | global | Chưa đăng nhập |

Thứ tự cấp độ (cao → thấp): `OWNER > ADMIN > EDITOR > COMMENTER > VIEWER > GUEST > ANONYMOUS`. Khi spec ghi `EDITOR+` nghĩa là "EDITOR trở lên".

### 1.2 Hai cấp kiểm tra

**Cấp 1 — Authentication (Gateway)**: verify JWT, gắn `X-User-Id`, `X-Username`, `X-User-Email`. Endpoint `PUBLIC` bỏ qua bước này.

**Cấp 2 — Authorization (mỗi service)**: dựa vào `projectId` của resource đang truy cập, gọi **Project Service** `GET /internal/projects/{id}/members/{userId}/role` để biết role. Cache Redis 5 phút (key `role:<projectId>:<userId>`).

Ngoại lệ: User Service tự kiểm tra chủ sở hữu (chỉ user X được sửa profile của X).

### 1.3 Endpoint không thuộc project nào

`/auth/*`, `/users/*` (trừ search), `/notifications/*`, dashboard cá nhân `/tasks/me`. Các endpoint này chỉ cần `AUTH` (đã login), không cần check role per-project.

---

## 2. Matrix theo Service

Ký hiệu: ✓ = cho phép, ✗ = deny (HTTP 403), `–` = N/A. Cột `OWNER` luôn ⊇ `ADMIN`, cột `ADMIN` luôn ⊇ `EDITOR`, … (kế thừa cấp). Cột `GUEST` áp dụng khi user login nhưng không phải member.

### 2.1 User Service

| Endpoint | ANON | AUTH | Tự thân (X) |
|---|---|---|---|
| POST `/auth/register` | ✓ | ✓ | – |
| POST `/auth/login` | ✓ | ✓ | – |
| POST `/auth/refresh` | ✓ (cần refresh token hợp lệ) | – | – |
| POST `/auth/logout` | ✗ | ✓ | – |
| POST `/auth/forgot-password` | ✓ | ✓ | – |
| POST `/auth/reset-password` | ✓ (cần reset token) | – | – |
| POST `/auth/change-password` | ✗ | ✓ (biết mật khẩu cũ) | – |
| GET `/users/me` | ✗ | ✓ | – |
| PUT `/users/me` | ✗ | ✓ | – |
| GET `/users/{id}` | ✗ | ✓ | – |
| GET `/users?q=` | ✗ | ✓ | – |
| POST `/users/me/avatar` | ✗ | ✓ | – |

Internal:
- `/internal/users/*` — chỉ chấp nhận từ Docker network (Gateway chặn).

### 2.2 Project Service

#### Project

| Endpoint | OWNER | ADMIN | EDITOR | COMMENTER | VIEWER | GUEST |
|---|---|---|---|---|---|---|
| POST `/projects` (tạo) | – | – | – | – | – | ✓ (caller thành OWNER) |
| GET `/projects` (list của tôi) | – | – | – | – | – | ✓ |
| GET `/projects/{id}` | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ |
| PUT `/projects/{id}` | ✓ | ✓ | ✗ | ✗ | ✗ | ✗ |
| DELETE `/projects/{id}` | ✓ | ✗ | ✗ | ✗ | ✗ | ✗ |
| POST `/projects/{id}/transfer-ownership` | ✓ | ✗ | ✗ | ✗ | ✗ | ✗ |
| GET `/projects/search` | – | – | – | – | – | ✓ (chỉ trong project mình tham gia) |

#### Members

| Endpoint | OWNER | ADMIN | EDITOR | COMMENTER | VIEWER | GUEST |
|---|---|---|---|---|---|---|
| GET `/projects/{id}/members` | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ |
| POST `/projects/{id}/members` | ✓ | ✓ | ✗ | ✗ | ✗ | ✗ |
| PATCH `/projects/{id}/members/{userId}/role` | ✓ | ✓¹ | ✗ | ✗ | ✗ | ✗ |
| DELETE `/projects/{id}/members/{userId}` | ✓ | ✓² | ✗ | ✗ | ✗ | ✗ |

¹ ADMIN chỉ đổi role giữa EDITOR/COMMENTER/VIEWER. Đặt role ADMIN/OWNER chỉ OWNER được.  
² ADMIN không xoá được OWNER hoặc ADMIN khác. Owner không thể tự xoá mình (phải transfer trước).

#### Board / List

| Endpoint | OWNER | ADMIN | EDITOR | COMMENTER | VIEWER | GUEST |
|---|---|---|---|---|---|---|
| POST `/projects/{id}/boards` | ✓ | ✓ | ✓ | ✗ | ✗ | ✗ |
| GET `/projects/{id}/boards` | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ |
| GET `/boards/{id}` | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ |
| PUT `/boards/{id}` | ✓ | ✓ | ✓ | ✗ | ✗ | ✗ |
| DELETE `/boards/{id}` | ✓ | ✓ | ✗ | ✗ | ✗ | ✗ |
| POST `/boards/{id}/lists` | ✓ | ✓ | ✓ | ✗ | ✗ | ✗ |
| PUT/DELETE `/lists/{id}` | ✓ | ✓ | ✓ | ✗ | ✗ | ✗ |
| PATCH `/boards/{id}/lists/reorder` | ✓ | ✓ | ✓ | ✗ | ✗ | ✗ |

#### Sprint / Reports

| Endpoint | OWNER | ADMIN | EDITOR | COMMENTER | VIEWER | GUEST |
|---|---|---|---|---|---|---|
| POST `/projects/{id}/sprints` | ✓ | ✓ | ✗ | ✗ | ✗ | ✗ |
| GET `/projects/{id}/sprints` | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ |
| PATCH `/sprints/{id}` | ✓ | ✓ | ✗ | ✗ | ✗ | ✗ |
| GET `/projects/{id}/reports/*` | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ |

### 2.3 Task Service

#### Task CRUD

| Endpoint | OWNER | ADMIN | EDITOR | COMMENTER | VIEWER | GUEST |
|---|---|---|---|---|---|---|
| POST `/tasks` | ✓ | ✓ | ✓ | ✗ | ✗ | ✗ |
| GET `/tasks/{id}` | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ |
| PUT `/tasks/{id}` | ✓ | ✓ | ✓ | ✗ | ✗ | ✗ |
| DELETE `/tasks/{id}` | ✓ | ✓ | ✓¹ | ✗ | ✗ | ✗ |
| POST `/tasks/{id}/restore` | ✓ | ✓ | ✓¹ | ✗ | ✗ | ✗ |
| POST `/tasks/{id}/move` | ✓ | ✓ | ✓ | ✗ | ✗ | ✗ |
| GET `/tasks?...` | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ |

¹ EDITOR chỉ xoá/khôi phục task do mình tạo (`reporter_id == userId`) hoặc được giao (`assignee_id == userId`). ADMIN+ xoá được mọi task.

#### Checklist / Dependencies / Watchers / Labels

| Endpoint | OWNER | ADMIN | EDITOR | COMMENTER | VIEWER | GUEST |
|---|---|---|---|---|---|---|
| POST/DELETE checklist & items | ✓ | ✓ | ✓ | ✗ | ✗ | ✗ |
| POST/DELETE `/tasks/{id}/dependencies/*` | ✓ | ✓ | ✓ | ✗ | ✗ | ✗ |
| GET `/tasks/{id}/dependencies` | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ |
| POST/DELETE `/tasks/{id}/watch` | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ |
| GET `/projects/{id}/labels` | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ |
| POST/PUT/DELETE label | ✓ | ✓ | ✗ | ✗ | ✗ | ✗ |

#### Dashboard cá nhân

| Endpoint | AUTH |
|---|---|
| GET `/tasks/me?type=*` | ✓ (chỉ trả task user là assignee) |

### 2.4 Collaboration Service

#### Comment

| Endpoint | OWNER | ADMIN | EDITOR | COMMENTER | VIEWER | GUEST |
|---|---|---|---|---|---|---|
| POST `/tasks/{taskId}/comments` | ✓ | ✓ | ✓ | ✓ | ✗ | ✗ |
| GET `/tasks/{taskId}/comments` | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ |
| PUT `/comments/{id}` | author¹ | ✓ | author¹ | author¹ | ✗ | ✗ |
| DELETE `/comments/{id}` | author¹ | ✓ | author¹ | author¹ | ✗ | ✗ |

¹ Author = người tạo comment (`author_id == userId`). ADMIN+ luôn có thể sửa/xoá comment của người khác (mod role).

#### Attachment

| Endpoint | OWNER | ADMIN | EDITOR | COMMENTER | VIEWER | GUEST |
|---|---|---|---|---|---|---|
| POST `/tasks/{taskId}/attachments` | ✓ | ✓ | ✓ | ✗ | ✗ | ✗ |
| GET `/tasks/{taskId}/attachments` | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ |
| GET `/attachments/{id}/download` | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ |
| DELETE `/attachments/{id}` | uploader | ✓ | uploader | ✗ | ✗ | ✗ |

#### Activity Log

| Endpoint | OWNER | ADMIN | EDITOR | COMMENTER | VIEWER | GUEST |
|---|---|---|---|---|---|---|
| GET `/projects/{projectId}/activities` | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ |
| GET `/tasks/{taskId}/activities` | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ |

### 2.5 Notification Service

| Endpoint | AUTH |
|---|---|
| GET `/notifications` | ✓ (chỉ của chính user) |
| PATCH `/notifications/{id}/read` | ✓ (owner notification) |
| DELETE `/notifications/{id}` | ✓ (owner notification) |
| GET/PUT `/notifications/preferences` | ✓ |
| WS `/ws/notifications` | ✓ |
| WS `/ws/board/{boardId}` | MEMBER (mọi role trong project) |

---

## 3. Logic kiểm tra phân quyền (pseudo-code)

Mỗi service gắn 1 helper `AuthorizationService.requireRole(projectId, userId, minRole)`:

```java
public void requireRole(Long projectId, Long userId, Role minRole) {
    Role role = projectRoleClient.getRole(projectId, userId);  // gọi Project Service, cache Redis
    if (role == null) throw new ForbiddenException("not_a_member");
    if (role.compareTo(minRole) < 0) throw new ForbiddenException("insufficient_role");
}

public void requireSelfOrAdmin(Long projectId, Long userId, Long resourceOwnerId) {
    if (userId.equals(resourceOwnerId)) return;
    requireRole(projectId, userId, Role.ADMIN);
}
```

Controller / Facade gọi `requireRole` ngay đầu method:

```java
public TaskResponse update(Long taskId, UpdateTaskRequest req, Long userId) {
    Task task = taskRepository.findByIdAndDeletedFalse(taskId)
        .orElseThrow(() -> new NotFoundException(taskId.toString(), "Task"));
    authorizationService.requireRole(task.getProjectId(), userId, Role.EDITOR);
    // ... mapping + save
}
```

## 4. Cache Strategy

| Cache key | Value | TTL | Invalidate khi |
|---|---|---|---|
| `role:<projectId>:<userId>` | role string | 300s | event `project.member.added`, `project.member.removed`, `project.member.role_changed` |
| `proj:exists:<projectId>` | true/false | 600s | event `project.created`, `project.deleted` |
| `user:contact:<userId>` | `{email, name}` | 600s | event `user.updated` |

Mọi service consume các event tương ứng để **evict cache** ngay khi có thay đổi (eventual consistency).

## 5. Edge Cases

| Trường hợp | Xử lý |
|---|---|
| Owner tự rời project | Bị chặn — phải `transfer-ownership` trước |
| Xoá project khi còn task | Soft delete cascade qua event `project.deleted` |
| 2 user cùng kéo 1 task vào 2 list khác | Optimistic lock qua `version` — request sau nhận 409 `conflict` |
| Khi đổi role từ EDITOR xuống VIEWER, user đang mở edit form | Lần submit tiếp theo nhận 403, FE redirect sang view-only |
| Kết nối WebSocket sau khi bị remove khỏi project | Server check role → đóng connection (`board.member.removed`) |
| Reset password token bị tái sử dụng | Bảng `password_resets.used` = true sau lần đầu, lần 2 trả 400 |
| Idempotency key dùng lại với payload khác | 409 `idempotency_conflict` |

## 6. Kiểm thử phân quyền

Mỗi service phải có test class `AuthorizationIT.java` cover 5 role × các endpoint chính. Test pattern:

```java
@Test
void editorCanCreateTask() {
    setRole(projectId, userId, Role.EDITOR);
    var resp = mvc.perform(post("/api/v1/tasks")
        .header("X-User-Id", userId.toString())
        .contentType(JSON).content(payload))
      .andExpect(status().isCreated());
}

@Test
void viewerCannotCreateTask() {
    setRole(projectId, userId, Role.VIEWER);
    mvc.perform(post("/api/v1/tasks")
        .header("X-User-Id", userId.toString())
        .contentType(JSON).content(payload))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.data.code").value("insufficient_role"));
}
```
