# IMPLEMENTATION PLAN — TaskFlow

Roadmap chia thành 7 phase. Mỗi phase có **Definition of Done (DoD)** cụ thể. Khi xong 1 task, đánh dấu `[x]`.

## Tổng quan thứ tự

```
Phase 0: Foundation              (chung, làm trước)
   ↓
Phase 1: User Service            (cấp JWT — các service khác cần)
   ↓
Phase 2: Project Service         (cấp role — các service khác cần check quyền)
   ↓
Phase 3: Task Service            (core business)
   ↓
Phase 4: Collaboration Service   (depend Task)
   ↓
Phase 5: Notification Service    (consume events từ tất cả)
   ↓
Phase 6: Polish (tracing, resilience, healthcheck, docs)
   ↓
Phase 7: Frontend (sau cùng)
```

**Có thể chạy song song** nếu nhóm ≥ 2 người (xem mục cuối file).

---

## Phase 0 — Foundation (1–2 ngày)

Mục tiêu: dựng hạ tầng dùng chung, viết shared library trước để các service kế thừa.

### 0.1 Shared library `taskflow-events-contract`
- [ ] Tạo Maven project (jar)
- [ ] Class `EventEnvelope<T>` (event_id, event_type, schema_version, occurred_at, actor_id, trace_id, data)
- [ ] DTO cho **mọi event** liệt kê ở `EVENT_SCHEMA.md` (`UserRegisteredEvent`, `ProjectCreatedEvent`, `TaskCreatedEvent`, …)
- [ ] Constants: `Exchanges.TASKFLOW_EVENTS`, `RoutingKeys.TASK_CREATED`, … (có hết)
- [ ] `mvn install` ra local repo

### 0.2 Shared library `taskflow-common` (tuỳ chọn — hoặc copy code vào mỗi service)
- [ ] `BaseEntity`, `AuditEntity`
- [ ] `ApiResponse<T>`, `PageResponse<T>`, `Error`
- [ ] `BaseException` + `NotFoundException`, `BadRequestException`, `UnauthorizedException`, `ForbiddenException`, `ConflictException`, `InternalServerError`
- [ ] `ExceptionHandlerAdvice` template
- [ ] `AuditorAwareImpl`
- [ ] `TaskFlowConstants` (Headers, Cache, Limits, TokenPrefix)
- [ ] `WebClientConfig` + Resilience4j default

> Quyết định: dùng shared lib hay copy-paste code vào từng service? Khuyến nghị shared lib cho `taskflow-common` để tránh đồng bộ thủ công.

### 0.3 Infrastructure services
- [ ] **Eureka Server** (`taskflow-eureka`, port 8761)
- [ ] **Config Server** (`taskflow-config`, port 8888) — đọc config từ filesystem `config-repo/` (đơn giản hơn Git cho đồ án)
- [ ] **Zipkin** — chạy bằng Docker image có sẵn `openzipkin/zipkin`

### 0.4 API Gateway (skeleton, chưa JWT)
- [ ] `taskflow-gateway` (port 8080)
- [ ] Route forward theo prefix (xem mục 6 của `API_SPEC.md`)
- [ ] Đăng ký Eureka

**DoD Phase 0**: Khởi động được Eureka + Config + Gateway + Zipkin. Gateway forward request giả tới `httpbin.org` qua route test.

---

## Phase 1 — User Service (3–5 ngày)

Mục tiêu: cấp JWT, để các service khác có thể auth.

### 1.1 Project setup
- [ ] Maven project `taskflow-user` (port 8081)
- [ ] `pom.xml` đủ deps: web, security, data-jpa, postgres, liquibase, validation, lombok, mapstruct, amqp, redis, jjwt, springdoc, taskflow-common, taskflow-events-contract
- [ ] `application.yml` (datasource, eureka, rabbit, redis, jwt secret keys)
- [ ] Folder structure theo `CONVENTIONS.md` mục 2

### 1.2 Database
- [ ] Liquibase master + changeset `20XXXXXX-users.xml`, `20XXXXXX-password_resets.xml`
- [ ] Entity `User`, `PasswordReset`
- [ ] `UserRepository`, `PasswordResetRepository`

### 1.3 JWT
- [ ] `TokenServiceImpl` — tạo / verify 3 loại token
- [ ] `PropertiesConfiguration` đọc 3 secret key + TTL
- [ ] Lưu token vào Redis `jwt:<userId>:<type>` để revoke được

### 1.4 Auth endpoints
- [ ] `POST /auth/register` (validate email/username unique, hash BCrypt, publish `user.registered`)
- [ ] `POST /auth/login` (verify password, sinh access + refresh, lưu Redis)
- [ ] `POST /auth/refresh` (verify refresh, sinh access mới)
- [ ] `POST /auth/logout` (xoá Redis key)
- [ ] `POST /auth/forgot-password` (sinh reset token, lưu hash vào DB, gửi event để Notification gửi mail)
- [ ] `POST /auth/reset-password` (verify reset token, đổi password, đánh dấu used)
- [ ] `POST /auth/change-password` (verify old password)

### 1.5 Profile endpoints
- [ ] `GET /users/me`, `PUT /users/me`
- [ ] `GET /users/{id}` (public profile)
- [ ] `GET /users?q=...` (search username/email)
- [ ] `POST /users/me/avatar` (upload MinIO)

### 1.6 Internal endpoints
- [ ] `GET /internal/users/{id}/contact` → `{email, full_name}`
- [ ] `POST /internal/users/exists` (batch check)

### 1.7 Event publisher — ~~loại bỏ~~

User Service KHÔNG publish event. Welcome mail không cần thiết, thông tin user được service khác lookup qua REST `/internal/users/{id}/contact`. RabbitMQ sẽ xuất hiện lần đầu ở Phase 2 (Project Service).

### 1.8 Security
- [ ] User Service KHÔNG cần JWT filter (chỉ public endpoint + endpoint sau khi login)
- [ ] Hoặc dùng JWT filter cho `/users/me`, `/users/me/avatar`, `/auth/logout`, `/auth/change-password`

### 1.9 Hoàn thiện Gateway
- [ ] Thêm `JwtAuthFilter` ở Gateway (verify access token, gắn `X-User-Id`, `X-Username`, `X-User-Email`)
- [ ] Route `/api/v1/auth/**` (PUBLIC), `/api/v1/users/**` (AUTH)
- [ ] Block `/api/v1/internal/**` từ ngoài

### 1.10 Test thủ công (Postman)
- [ ] Register → Login → Lấy access token → Gọi `/users/me` qua Gateway → 200 OK
- [ ] Logout → Gọi lại → 401

**DoD Phase 1**: User register/login qua Gateway, JWT verify ở Gateway, các header forward đúng tới User Service. Email reset password gửi được (có thể mock SMTP bằng MailHog).

---

## Phase 2 — Project Service (5–7 ngày)

Mục tiêu: quản lý project, board, list, member, sprint. Cấp endpoint `/internal/role` cho service khác.

### 2.1 Project setup
- [ ] `taskflow-project` (port 8082)
- [ ] `pom.xml`, `application.yml`, structure

### 2.2 Database
- [ ] Liquibase: `projects`, `project_members`, `boards`, `lists`, `sprints`
- [ ] Entities + Repositories

### 2.3 Project CRUD
- [ ] `POST /projects` (caller thành OWNER, publish `project.created`)
- [ ] `GET /projects` (list của tôi — dựa `project_members`)
- [ ] `GET /projects/{id}`, `PUT`, `DELETE` (soft + publish `project.deleted`)
- [ ] `GET /projects/search?q=`
- [ ] `POST /projects/{id}/transfer-ownership`

### 2.4 Members
- [ ] `GET / POST / PATCH / DELETE /projects/{id}/members[/{userId}[/role]]`
- [ ] Validate user tồn tại bằng cách gọi User Service `/internal/users/exists` (WebClient + Resilience4j)
- [ ] Publish `project.member.added`, `project.member.removed`, `project.member.role_changed`

### 2.5 Board / List
- [ ] CRUD board, CRUD list
- [ ] `PATCH /boards/{id}/lists/reorder`
- [ ] Publish `board.created`, `board.deleted`, `list.created`, `list.deleted`

### 2.6 Sprint & Reports
- [ ] CRUD sprint
- [ ] `GET /projects/{id}/reports/burndown`, `GET /projects/{id}/reports/summary`

### 2.7 Internal endpoints
- [ ] `GET /internal/projects/{id}/members/{userId}/role` → `{role}` hoặc 404
- [ ] `GET /internal/projects/{id}/exists`
- [ ] `GET /internal/lists/{id}/board` → `{board_id, project_id}`

### 2.8 Authorization helper
- [ ] `AuthorizationService.requireRole(projectId, userId, minRole)` — Project Service tự biết role của member, không cần gọi đâu

### 2.9 Saga: tạo project mới
- [ ] Sau khi `project.created` publish, **Project Service tự consume** để tạo board mặc định + add owner làm member (nếu chưa làm trong cùng transaction tạo project)

**DoD Phase 2**: Tạo project → tự động có 1 board mặc định + 3 list ("To Do", "Doing", "Done") + Owner đã được add. CRUD member work, có event publish (xem ở RabbitMQ Management UI).

---

## Phase 3 — Task Service (5–7 ngày)

Mục tiêu: core business — task, dependency, label, watcher.

### 3.1 Project setup
- [ ] `taskflow-task` (port 8083)

### 3.2 Database
- [ ] Liquibase: `tasks` (kèm cột `version` cho optimistic lock), `labels`, `task_label_mappings`, `checklists`, `checklist_items`, `task_dependencies`, `task_watchers`
- [ ] Entities + Repositories

### 3.3 Cross-service client
- [ ] `ProjectServiceClient` (WebClient): `getRole`, `verifyList`, `verifyProjectExists`
- [ ] `UserServiceClient`: `verifyUser`, `getContact`
- [ ] Cache Redis 5 phút cho role
- [ ] Resilience4j Circuit Breaker + Retry + TimeLimiter

### 3.4 Authorization
- [ ] `AuthorizationService.requireRole(projectId, userId, minRole)` — gọi Project Service `/internal/role`

### 3.5 Task CRUD
- [ ] `POST /tasks` (idempotency key + verify list_id qua Project Service)
- [ ] `GET /tasks/{id}`
- [ ] `PUT /tasks/{id}` (optimistic lock)
- [ ] `DELETE /tasks/{id}` (soft, publish `task.deleted`)
- [ ] `POST /tasks/{id}/restore`
- [ ] `POST /tasks/{id}/move` (verify to_list_id, update position, publish `task.moved`)
- [ ] `GET /tasks?board_id=&list_id=&assignee_id=&q=` (filter + Postgres ILIKE search)
- [ ] Publish `task.created`, `task.updated` (kèm changes diff), `task.assigned`

### 3.6 Checklist
- [ ] CRUD checklist + items, toggle completed

### 3.7 Dependencies
- [ ] `POST /tasks/{id}/dependencies` — DFS check cycle trước khi save
- [ ] `DELETE`, `GET`
- [ ] Publish `task.dependency.changed`

### 3.8 Watchers
- [ ] `POST/DELETE /tasks/{id}/watch`

### 3.9 Labels
- [ ] CRUD label scoped per project

### 3.10 Dashboard
- [ ] `GET /tasks/me?type=assigned|due_soon|overdue`
- [ ] Cache Redis 60s

### 3.11 Scheduler
- [ ] Cron mỗi 15 phút quét task `due_date` sắp tới → publish `task.due_soon` (24h trước hạn) và `task.overdue`

### 3.12 Consumer
- [ ] `project.deleted` → soft delete tất cả task của project
- [ ] `list.deleted` → move task sang list "Backlog" hoặc soft delete (chọn 1)
- [ ] `board.deleted` → cascade tương tự

**DoD Phase 3**: Tạo / sửa / move / xoá task qua Gateway. Dependency cycle bị từ chối. Search task trong board hoạt động. Scheduler phát event `task.due_soon` đúng thời điểm.

---

## Phase 4 — Collaboration Service (3–5 ngày)

Mục tiêu: comment, attachment, activity log.

### 4.1 Project setup
- [ ] `taskflow-collab` (port 8084)

### 4.2 Database
- [ ] Liquibase: `comments`, `attachments`, `activity_logs` (jsonb payload)
- [ ] Entities + Repositories
- [ ] Index `(project_id, occurred_at DESC)` cho activity_logs

### 4.3 Cross-service client
- [ ] `TaskServiceClient.verifyTask(taskId)`
- [ ] `ProjectServiceClient.getRole`
- [ ] `UserServiceClient.getContact`

### 4.4 MinIO
- [ ] `MinioConfig` (bucket `taskflow-attachments`)
- [ ] `MinioStorageService.upload(file, key)` + `presignedDownloadUrl(key)` + `delete(key)`

### 4.5 Comments
- [ ] CRUD theo authorization matrix
- [ ] Parse `@username` → mention list (regex)
- [ ] Publish `comment.added` (kèm `mentioned_user_ids`)

### 4.6 Attachments
- [ ] `POST /tasks/{taskId}/attachments` (multipart, max 25MB, mime whitelist)
- [ ] Upload MinIO, lưu metadata
- [ ] `GET /attachments/{id}/download` (proxy hoặc presigned URL)
- [ ] `DELETE` (xoá MinIO + DB)
- [ ] Publish `attachment.uploaded`

### 4.7 Activity Log
- [ ] Consumer queue `collab.activity.q` — bind tất cả `task.*`, `project.*`, `board.*`, `list.*`, `comment.*`, `attachment.*`
- [ ] Mỗi event → 1 row activity_logs với payload jsonb
- [ ] `GET /projects/{id}/activities?from=&to=&page=`
- [ ] `GET /tasks/{id}/activities`

### 4.8 Cleanup consumer
- [ ] `task.deleted` → xoá comment + attachment (cả file MinIO)
- [ ] `project.deleted` → cascade

**DoD Phase 4**: Tạo task → comment → upload file → kéo task qua list khác → vào activity log thấy đủ 4 dòng.

---

## Phase 5 — Notification Service (3–5 ngày)

Mục tiêu: thông báo realtime + email.

### 5.1 Project setup
- [ ] `taskflow-notification` (port 8085)
- [ ] Deps: web, websocket, mail, thymeleaf, amqp, redis, jpa, postgres, liquibase

### 5.2 Database
- [ ] Liquibase: `notifications`, `notification_preferences`
- [ ] Entities + Repositories

### 5.3 REST endpoints
- [ ] `GET /notifications`, `PATCH /{id}/read`, `PATCH /read-all`, `DELETE /{id}`, `GET /unread-count`
- [ ] `GET / PUT /notifications/preferences`

### 5.4 WebSocket
- [ ] `WebSocketConfig` (STOMP + SockJS)
- [ ] `WebSocketAuthInterceptor` — verify JWT trong handshake
- [ ] Topic `/user/queue/notifications` (private)
- [ ] Topic `/topic/board/{boardId}` (broadcast — check role MEMBER trước khi subscribe)

### 5.5 Email
- [ ] `MailService` (Spring Mail + Thymeleaf)
- [ ] Template: welcome, reset-password, member-invited, task-due-soon
- [ ] Local dev dùng MailHog

### 5.6 Consumer
- [ ] Queue `notification.q` bind: `task.*`, `project.member.*`, `comment.*`, `user.registered`
- [ ] Switch theo `event_type` → tạo notification cho recipient phù hợp:
  - `task.assigned` → assignee mới
  - `task.due_soon`, `task.overdue` → assignee
  - `task.moved`, `task.updated` → watchers
  - `comment.added` → assignee + watchers + mentioned
  - `project.member.added` → user mới
  - `user.registered` → welcome mail
- [ ] Lưu DB + push WebSocket + (nếu prefer email) gửi email

### 5.7 Realtime board sync
- [ ] Consumer `task.moved`, `task.updated`, `list.created/deleted` → broadcast `/topic/board/{boardId}`

**DoD Phase 5**: Login bằng 2 tab → tab A kéo task → tab B thấy update realtime + 1 notification badge.

---

## Phase 6 — Polish (2–3 ngày)

### 6.1 Tracing
- [ ] Thêm `micrometer-tracing-bridge-brave` + `zipkin-reporter-brave` cho mọi service
- [ ] Verify trace xuyên 3 service trên Zipkin UI

### 6.2 Resilience
- [ ] Tune timeout / retry / circuit breaker theo profile
- [ ] Test scenario: tắt User Service → tạo task → 503 nhanh, không hang

### 6.3 Rate limit
- [ ] Gateway dùng Redis Rate Limiter (60 req/phút/user, 5 req/giây cho `/auth/login`)

### 6.4 Healthcheck
- [ ] `/actuator/health` cho mọi service trả status đầy đủ
- [ ] Liveness / Readiness probe (dùng nếu deploy K8s sau này)

### 6.5 Documentation
- [ ] SpringDoc OpenAPI ở mỗi service → `/swagger-ui.html`
- [ ] Postman collection export, cam kết với repo
- [ ] README mỗi service: setup, env vars, run

### 6.6 Error catalog
- [ ] Tất cả `code` trong `messages.properties` + `messages_vi.properties`

**DoD Phase 6**: Bật Zipkin UI → trace 1 request `POST /tasks` thấy đi qua Gateway → Task → Project → User. Tắt 1 service không làm sập service khác.

---

## Phase 7 — Frontend (sau cùng, tách riêng)

(Chỉ liệt kê khung — chốt stack sau khi BE ổn)

- [ ] Chọn stack (React + Vite khả năng cao nhất)
- [ ] Auth flow + lưu JWT (cookie httpOnly hay localStorage — quyết định cuối)
- [ ] Trang: login, dashboard, project list, board kanban (drag-drop), task detail modal, profile
- [ ] WebSocket client cho realtime
- [ ] Bật CORS ở Gateway
- [ ] Build production → serve qua Nginx hoặc tích hợp vào Gateway

---

## Phương án song song hoá (nếu nhóm ≥ 2 người)

| Nhóm | Phase 0 | Sau Phase 0 |
|---|---|---|
| Người 1 | Foundation + Gateway | Phase 1 (User) → Phase 5 (Notification) |
| Người 2 | (giúp Phase 0) | Phase 2 (Project) → Phase 4 (Collaboration) |
| Người 3 (nếu có) | Shared lib events | Phase 3 (Task) — phase nặng nhất |

Quy tắc:
- Tất cả deps cross-service đều **mock** ban đầu (return fake data) khi service kia chưa xong, chỉ tích hợp thật khi cả 2 sẵn sàng.
- Schema event chốt **TRƯỚC** khi viết publisher/consumer (đã có ở `EVENT_SCHEMA.md`).

---

## Mốc nghiệm thu tổng thể

| Mốc | Tiêu chí |
|---|---|
| **MVP** (sau Phase 3) | Tạo project → tạo board → tạo task → kéo task → ổn định |
| **Beta** (sau Phase 5) | Có comment, attachment, notification realtime |
| **Demo bảo vệ** (sau Phase 6) | Có Zipkin trace, Swagger, README đầy đủ, Postman collection |
| **Final** (sau Phase 7) | Có giao diện chạy được trên trình duyệt |

---

## Ghi chú thực dụng

- **Bắt đầu mỗi service** = clone từ User Service skeleton sau khi User Service đã chạy → tiết kiệm setup.
- **Test mỗi endpoint** ngay khi viết xong, đừng để gom dồn cuối phase.
- **Commit nhỏ, message rõ** — `feat(task): add move endpoint`, `fix(project): cycle in role check`.
- **RabbitMQ Management UI** (`http://localhost:15672`) là bạn — kiểm tra event publish/consume từng bước.
- **MailHog** (`http://localhost:8025`) cho dev gửi email.
- **Postman environment**: 1 cho local, lưu access token vào biến.
