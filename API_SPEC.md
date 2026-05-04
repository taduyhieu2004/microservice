# API Specification — TaskFlow

Quy ước chung:
- **Base URL** qua Gateway: `https://api.taskflow.local/api/v1`
- **Auth**: header `Authorization: Bearer <ACCESS_TOKEN>` (trừ các endpoint public).
- **i18n**: header `Accept-Language: vi|en` (default `vi`).
- **Idempotency**: các POST tạo resource đỡ trùng (task / comment / attachment) yêu cầu header `Idempotency-Key: <uuid>`.
- Response wrapper: `ApiResponse<T>` (snake_case, fields: `status`, `message`, `data`, `timestamp`).
- Pagination response: `PageResponse<T>` (`content`, `total_elements`, `page`, `size`).
- Filter pagination params: `?page=0&size=20&sort=created_at,desc`.
- Lỗi: response wrapper với `data = { code, detail }`. HTTP status theo bản chất lỗi.

Cột "Auth":
- `PUBLIC` — không cần token.
- `AUTH` — chỉ cần đăng nhập.
- `MEMBER` — phải là member của project.
- `EDITOR+` — phải là Editor / Admin / Owner trong project.
- `ADMIN+` — phải là Admin / Owner.
- `OWNER` — chỉ Owner.

Chi tiết role → endpoint xem file [AUTHORIZATION_MATRIX.md](./AUTHORIZATION_MATRIX.md).

---

## 1. User Service (`/api/v1/auth`, `/api/v1/users`)

### 1.1 Authentication

| # | Method | URL | Auth | Mô tả |
|---|---|---|---|---|
| 1 | POST | `/auth/register` | PUBLIC | Đăng ký |
| 2 | POST | `/auth/login` | PUBLIC | Login, trả access + refresh token |
| 3 | POST | `/auth/refresh` | PUBLIC (cần refresh token) | Đổi access token |
| 4 | POST | `/auth/logout` | AUTH | Revoke token (xoá Redis key) |
| 5 | POST | `/auth/forgot-password` | PUBLIC | Gửi email kèm reset token |
| 6 | POST | `/auth/reset-password` | PUBLIC (cần reset token) | Đổi mật khẩu |
| 7 | POST | `/auth/change-password` | AUTH | Đổi mật khẩu (biết mật khẩu cũ) |

**Login example**

`POST /auth/login`
```json
{
  "username": "alice",
  "password": "secret123"
}
```

Response:
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": 42,
    "access_token": "eyJ...",
    "refresh_token": "eyJ...",
    "token_expired_seconds": 3600,
    "refresh_expired_seconds": 2592000,
    "token_type": "Bearer",
    "change_password": false
  },
  "timestamp": "2026-05-04T10:30:45Z"
}
```

### 1.2 Profile

| # | Method | URL | Auth | Mô tả |
|---|---|---|---|---|
| 8 | GET | `/users/me` | AUTH | Profile chính mình |
| 9 | PUT | `/users/me` | AUTH | Cập nhật profile (full_name, bio, dob, avatar_url) |
| 10 | GET | `/users/{id}` | AUTH | Public profile của user khác |
| 11 | GET | `/users?q=alice` | AUTH | Search user theo username/email (cho UI mời thành viên) |
| 12 | POST | `/users/me/avatar` | AUTH | Upload avatar (multipart) |

### 1.3 Internal endpoint (chỉ service nội bộ gọi)

| # | Method | URL | Mô tả |
|---|---|---|---|
| 13 | GET | `/internal/users/{id}/contact` | Lấy email + display name (cho Notification Service) |
| 14 | POST | `/internal/users/exists` | Body: `{user_ids: [...]}` — kiểm tra batch |

> Endpoint `/internal/*` không expose qua Gateway, chỉ cho service-to-service trong Docker network.

---

## 2. Project Service (`/api/v1/projects`)

### 2.1 Project CRUD

| # | Method | URL | Auth | Mô tả |
|---|---|---|---|---|
| 1 | POST | `/projects` | AUTH | Tạo project (caller thành Owner) |
| 2 | GET | `/projects` | AUTH | List project mà mình tham gia |
| 3 | GET | `/projects/{id}` | MEMBER | Chi tiết |
| 4 | PUT | `/projects/{id}` | ADMIN+ | Cập nhật |
| 5 | DELETE | `/projects/{id}` | OWNER | Xóa (soft) |
| 6 | GET | `/projects/search?q=...` | AUTH | Tìm trong các project mình đang tham gia |
| 7 | POST | `/projects/{id}/transfer-ownership` | OWNER | Chuyển Owner cho user khác |

**Create project**

`POST /projects`
```json
{
  "name": "TaskFlow Mobile App",
  "key": "TFM",
  "description": "Mobile client",
  "type": "SOFTWARE"
}
```

### 2.2 Members

| # | Method | URL | Auth | Mô tả |
|---|---|---|---|---|
| 8 | GET | `/projects/{id}/members` | MEMBER | List thành viên |
| 9 | POST | `/projects/{id}/members` | ADMIN+ | Thêm thành viên (body: user_id, role) |
| 10 | PATCH | `/projects/{id}/members/{userId}/role` | ADMIN+ | Đổi role (Owner đổi role Owner) |
| 11 | DELETE | `/projects/{id}/members/{userId}` | ADMIN+ | Xoá thành viên (không xoá Owner) |

### 2.3 Boards & Lists

| # | Method | URL | Auth | Mô tả |
|---|---|---|---|---|
| 12 | POST | `/projects/{id}/boards` | EDITOR+ | Tạo board |
| 13 | GET | `/projects/{id}/boards` | MEMBER | List board |
| 14 | GET | `/boards/{id}` | MEMBER | Chi tiết board kèm danh sách list |
| 15 | PUT | `/boards/{id}` | EDITOR+ | Sửa |
| 16 | DELETE | `/boards/{id}` | ADMIN+ | Xoá |
| 17 | GET | `/projects/{id}/boards/search?q=...` | MEMBER | Search board trong project |
| 18 | POST | `/boards/{id}/lists` | EDITOR+ | Tạo list (cột Kanban) |
| 19 | PUT | `/lists/{id}` | EDITOR+ | Sửa |
| 20 | DELETE | `/lists/{id}` | EDITOR+ | Xoá |
| 21 | PATCH | `/boards/{id}/lists/reorder` | EDITOR+ | Reorder list (body: `[{id, position}]`) |

### 2.4 Sprints & Reports

| # | Method | URL | Auth | Mô tả |
|---|---|---|---|---|
| 22 | POST | `/projects/{id}/sprints` | ADMIN+ | Tạo sprint |
| 23 | GET | `/projects/{id}/sprints` | MEMBER | List sprint |
| 24 | PATCH | `/sprints/{id}` | ADMIN+ | Cập nhật / đóng sprint |
| 25 | GET | `/projects/{id}/reports/burndown?sprint_id=...` | MEMBER | Burndown chart data |
| 26 | GET | `/projects/{id}/reports/summary` | MEMBER | Số task theo status / assignee / priority |

### 2.5 Internal endpoint

| # | Method | URL | Mô tả |
|---|---|---|---|
| 27 | GET | `/internal/projects/{id}/members/{userId}/role` | Trả role của user trong project — dùng cho service khác check quyền |
| 28 | GET | `/internal/projects/{id}/exists` | Verify project tồn tại |
| 29 | GET | `/internal/lists/{id}/board` | Trả `{board_id, project_id}` — dùng khi Task Service cần verify list_id |

---

## 3. Task Service (`/api/v1/tasks`, `/api/v1/labels`)

### 3.1 Task CRUD

| # | Method | URL | Auth | Mô tả |
|---|---|---|---|---|
| 1 | POST | `/tasks` | EDITOR+ | Tạo task (body: list_id, title, …). **Yêu cầu Idempotency-Key** |
| 2 | GET | `/tasks/{id}` | MEMBER | Chi tiết task |
| 3 | PUT | `/tasks/{id}` | EDITOR+ | Cập nhật (title, description, due_date, priority, assignee, labels) |
| 4 | DELETE | `/tasks/{id}` | EDITOR+ | Xoá (soft) |
| 5 | POST | `/tasks/{id}/restore` | EDITOR+ | Khôi phục từ Recycle Bin (≤30 ngày) |
| 6 | POST | `/tasks/{id}/move` | EDITOR+ | Di chuyển sang list khác (body: `{to_list_id, position}`) |
| 7 | GET | `/tasks?board_id=...&list_id=...&assignee_id=...&q=...&page=0&size=20` | MEMBER | Filter + search task scoped trong board / project |

**Create task example**

`POST /tasks`  
Header: `Idempotency-Key: 6c7c5e44-1b03-4d2c-91b5-e5ff79f3a812`
```json
{
  "list_id": 12,
  "title": "Implement login flow",
  "description": "JWT + refresh token",
  "assignee_id": 42,
  "due_date": 1764892800000,
  "priority": "HIGH",
  "label_ids": [3, 7]
}
```

**Move task**

`POST /tasks/100/move`
```json
{
  "to_list_id": 13,
  "position": 0
}
```

### 3.2 Checklist

| # | Method | URL | Auth | Mô tả |
|---|---|---|---|---|
| 8 | POST | `/tasks/{id}/checklists` | EDITOR+ | Tạo checklist |
| 9 | DELETE | `/checklists/{id}` | EDITOR+ | Xoá checklist |
| 10 | POST | `/checklists/{id}/items` | EDITOR+ | Thêm item |
| 11 | PATCH | `/checklist-items/{id}` | EDITOR+ | Sửa / toggle completed |
| 12 | DELETE | `/checklist-items/{id}` | EDITOR+ | Xoá item |

### 3.3 Dependencies

| # | Method | URL | Auth | Mô tả |
|---|---|---|---|---|
| 13 | POST | `/tasks/{id}/dependencies` | EDITOR+ | Thêm dependency (body: `{depends_on_task_id, type}`) — server check cycle |
| 14 | DELETE | `/tasks/{id}/dependencies/{depId}` | EDITOR+ | Xoá |
| 15 | GET | `/tasks/{id}/dependencies` | MEMBER | Liệt kê |

### 3.4 Watchers

| # | Method | URL | Auth | Mô tả |
|---|---|---|---|---|
| 16 | POST | `/tasks/{id}/watch` | MEMBER | Theo dõi task |
| 17 | DELETE | `/tasks/{id}/watch` | MEMBER | Bỏ theo dõi |

### 3.5 Labels

| # | Method | URL | Auth | Mô tả |
|---|---|---|---|---|
| 18 | GET | `/projects/{id}/labels` | MEMBER | List label trong project |
| 19 | POST | `/projects/{id}/labels` | ADMIN+ | Tạo label |
| 20 | PUT | `/labels/{id}` | ADMIN+ | Sửa |
| 21 | DELETE | `/labels/{id}` | ADMIN+ | Xoá |

### 3.6 Dashboard

| # | Method | URL | Auth | Mô tả |
|---|---|---|---|---|
| 22 | GET | `/tasks/me?type=assigned` | AUTH | "Tasks Assigned to Me" |
| 23 | GET | `/tasks/me?type=due_soon&days=3` | AUTH | "Tasks Due Soon" |
| 24 | GET | `/tasks/me?type=overdue` | AUTH | "Overdue Tasks" |

---

## 4. Collaboration Service (`/api/v1/tasks/{taskId}/...`, `/api/v1/comments`, ...)

### 4.1 Comments

| # | Method | URL | Auth | Mô tả |
|---|---|---|---|---|
| 1 | POST | `/tasks/{taskId}/comments` | COMMENTER+ | Bình luận. Yêu cầu `Idempotency-Key`. Body: `{content, parent_id?}` |
| 2 | GET | `/tasks/{taskId}/comments?page=...` | MEMBER | List comment |
| 3 | PUT | `/comments/{id}` | author hoặc ADMIN+ | Sửa nội dung |
| 4 | DELETE | `/comments/{id}` | author hoặc ADMIN+ | Xoá |

> "COMMENTER+" = COMMENTER / EDITOR / ADMIN / OWNER.

### 4.2 Attachments

| # | Method | URL | Auth | Mô tả |
|---|---|---|---|---|
| 5 | POST | `/tasks/{taskId}/attachments` | EDITOR+ | Upload (multipart, max 25MB) |
| 6 | GET | `/tasks/{taskId}/attachments` | MEMBER | List metadata |
| 7 | GET | `/attachments/{id}/download` | MEMBER | Tải về (server proxy MinIO hoặc trả presigned URL) |
| 8 | DELETE | `/attachments/{id}` | uploader hoặc ADMIN+ | Xoá |

### 4.3 Activity Log

| # | Method | URL | Auth | Mô tả |
|---|---|---|---|---|
| 9 | GET | `/projects/{projectId}/activities?page=...&from=...&to=...` | MEMBER | Activity của project |
| 10 | GET | `/tasks/{taskId}/activities` | MEMBER | Activity của task |

---

## 5. Notification Service (`/api/v1/notifications`)

### 5.1 REST

| # | Method | URL | Auth | Mô tả |
|---|---|---|---|---|
| 1 | GET | `/notifications?unread_only=true&page=...` | AUTH | List của chính user |
| 2 | PATCH | `/notifications/{id}/read` | AUTH | Đánh dấu đã đọc |
| 3 | PATCH | `/notifications/read-all` | AUTH | Đánh dấu đã đọc toàn bộ |
| 4 | DELETE | `/notifications/{id}` | AUTH | Xoá |
| 5 | GET | `/notifications/preferences` | AUTH | Lấy preference |
| 6 | PUT | `/notifications/preferences` | AUTH | Cập nhật preference |
| 7 | GET | `/notifications/unread-count` | AUTH | Badge count |

### 5.2 WebSocket

| Endpoint | Mô tả |
|---|---|
| `/ws/notifications` | STOMP. Handshake bằng JWT (`Authorization` header hoặc query `?token=...`). Topic `/user/queue/notifications` (private mỗi user). |
| `/ws/board/{boardId}` | Realtime sync khi 1 user kéo task — broadcast `/topic/board/{boardId}`. Chỉ user là MEMBER của project mới subscribe được. |

### 5.3 Internal (RabbitMQ consumer)

Notification Service không expose endpoint internal — đầu vào chính là **event RabbitMQ** từ các service khác (xem `EVENT_SCHEMA.md`).

---

## 6. API Gateway (`/api/v1/...`)

Gateway forward routes:

| Prefix | Service đích |
|---|---|
| `/api/v1/auth/**` | User Service |
| `/api/v1/users/**` | User Service |
| `/api/v1/projects/**` | Project Service |
| `/api/v1/boards/**`, `/api/v1/lists/**`, `/api/v1/sprints/**` | Project Service |
| `/api/v1/tasks/**`, `/api/v1/checklists/**`, `/api/v1/checklist-items/**`, `/api/v1/labels/**` | Task Service |
| `/api/v1/comments/**`, `/api/v1/attachments/**` | Collaboration Service |
| `/api/v1/notifications/**` | Notification Service |
| `/ws/**` | Notification Service (WebSocket) |
| `/internal/**` | **CHẶN từ ngoài** — chỉ chấp nhận trong Docker network |

Gateway filter:
1. **JWT verify** → extract userId/username/email → gắn header `X-User-Id`, `X-Username`, `X-User-Email`.
2. **Rate limit** Redis (vd 60 req/phút/user, 5 req/giây cho `/auth/login`).
3. **Trace ID**: nếu request không có `X-Trace-Id`, sinh UUID, gắn header.
4. **Logging**: log method + path + userId + status.

---

## 7. Error Code Catalog (cốt lõi)

| Code | HTTP | Mô tả |
|---|---|---|
| `validation_error` | 400 | Lỗi validate body |
| `unauthorized` | 401 | Chưa auth / token sai |
| `forbidden` | 403 | Không đủ quyền |
| `not_found` | 404 | Resource không tồn tại |
| `conflict` | 409 | Vd duplicate username, key project |
| `idempotency_conflict` | 409 | Idempotency-Key đã dùng với payload khác |
| `cycle_detected` | 400 | Task dependency tạo vòng tròn |
| `file_too_large` | 413 | Attachment > 25MB |
| `unsupported_media_type` | 415 | Mime type không cho phép |
| `rate_limit_exceeded` | 429 | Vượt rate limit ở Gateway |
| `circuit_open` | 503 | Resilience4j fallback — service đích down |
| `internal_server_error` | 500 | Fallback |
