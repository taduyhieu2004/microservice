# TaskFlow — Claude Project Guide

Đồ án môn microservices: **TaskFlow** — Trello/Jira hybrid, 5 services Spring Boot + FE React (chưa code).

## Tài liệu cần đọc khi cần biết chi tiết

| File | Khi nào đọc |
|---|---|
| `TÀI LIỆU ĐẶC TẢ KIẾN TRÚC HỆ THỐNG.md` | Kiến trúc tổng, Mermaid diagram, motivation |
| `CONVENTIONS.md` | **Đọc đầu tiên trước khi code** — quy ước bắt buộc cho mọi service |
| `ERD.md` | Schema DB từng service, jsonb fields |
| `API_SPEC.md` | Endpoint, request/response shape, header chuẩn, error codes |
| `EVENT_SCHEMA.md` | RabbitMQ topology, envelope, payload từng event |
| `AUTHORIZATION_MATRIX.md` | Role × endpoint table |
| `IMPLEMENTATION_PLAN.md` | Roadmap, DoD từng phase |
| `SETUP.md` | Build/run/test toàn stack |

## Stack

- Java 17, Spring Boot 3.3.5, Maven 3.6+
- Spring Cloud 2023.0.3 (Eureka, Config, Gateway)
- PostgreSQL 15 (database-per-service), Redis 7, RabbitMQ 3.12, MinIO
- JJWT 0.11.5 (HS256, 3 token types: ACCESS / REFRESH / RESET)
- MapStruct 1.5.5, Liquibase XML, Hypersistence Utils (jsonb)
- FE (chưa code): React + Vite + TS + shadcn/ui + Tailwind + TanStack Query + Zustand

## Cấu trúc folder

```
/home/hieu/Documents/ms/
├── code/                                # tất cả Maven projects
│   ├── taskflow-events-contract/       (jar — shared event DTO)
│   ├── taskflow-common/                (jar — BaseEntity, ApiResponse, exceptions, ...)
│   ├── taskflow-eureka/                :8761
│   ├── taskflow-config/                :8888 (config-repo/ là filesystem backend)
│   ├── taskflow-gateway/               :8080 (JWT verify + route)
│   ├── taskflow-user/                  :8081
│   ├── taskflow-project/               :8082
│   ├── taskflow-task/                  :8083
│   ├── taskflow-collab/                :8084
│   ├── taskflow-notification/          :8085 (REST + WebSocket STOMP)
│   └── taskflow-fe-mockup/             (HTML mockup, chưa code React)
└── *.md                                 (các tài liệu)
```

## Convention BẮT BUỘC khi code service mới hoặc sửa code cũ

Đọc `CONVENTIONS.md` để chi tiết. Tóm tắt rule quan trọng nhất:

1. **Shared lib**: mọi service phụ thuộc `taskflow-common` + `taskflow-events-contract`. Khi sửa shared lib phải `mvn install` rồi rebuild service dùng nó.
2. **Entity**: extends `AuditEntity` từ shared common. `createdAt/lastUpdatedAt` là **`Long` epoch millis** (KHÔNG dùng `LocalDateTime`). `createdBy/lastUpdatedBy` là **username string** (không phải userId).
3. **Liquibase**: XML format. Master ở `db/master.xml`, changesets ở `db/changelog/YYYYMMDD-HHMM-<feature>.xml`. ID: `create-<table>-table`. `ddl-auto=validate`, không bao giờ để Hibernate sinh DDL.
4. **JSON snake_case**: thêm `@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)` ở DTO. Khi nhận query param snake_case: `@RequestParam("project_id") Long projectId` (Spring KHÔNG tự bind).
5. **API wrapper**: trả `ApiResponse<T>`. Pagination: `PageResponse<T>`. Lỗi: `BaseException` + subclass (`NotFoundException.of("Task", id)`, `BadRequestException(code)`...). `@RestControllerAdvice` ở `advice/ExceptionHandlerAdvice.java` extends `GlobalExceptionHandler` của shared lib.
6. **JWT auth**: Gateway verify JWT, gắn header `X-User-Id` / `X-Username` / `X-User-Email` / `X-Trace-Id`. Service downstream KHÔNG verify lại JWT — đọc header qua `SecurityHeaderUtils.currentUserId()`. SecurityConfig của service downstream chỉ `permitAll()` + STATELESS.
7. **Cross-service auth**: gọi Project Service `/internal/projects/{id}/members/{userId}/role` (qua Eureka name `lb://taskflow-project` + WebClient + `@CircuitBreaker(name="project-service")`). Cache role ở Redis 5 phút (key `role:<projectId>:<userId>`, marker `__NULL__` cho non-member).
8. **RabbitMQ**:
   - Exchange: `taskflow.events` (topic). Constants ở `taskflow-events-contract`.
   - Publisher: bọc `try-catch` `AmqpException` để service vẫn chạy nếu broker down.
   - Consumer signature: `@RabbitListener(queues=...)` + tham số `EventEnvelope<Map<String,Object>>` (KHÔNG dùng `JsonNode` — bị mismatch type id `__TypeId__`).
   - Queue bindings phải dùng **`Declarables`** (List<Binding> KHÔNG tự register).
   - Consumer phải **idempotent** — dedup theo `event_id` qua DB hoặc Redis.
9. **Gateway routing trick**: route `/api/v1/tasks/*/comments/**` + `/attachments/**` đến Collab Service phải đặt **TRƯỚC** route `/api/v1/tasks/**` đi tới Task Service (matching theo declaration order).
10. **Postgres + null param**: tránh `LIKE LOWER(CONCAT('%', :q, '%'))` với `:q=null` (sẽ fail với "function lower(bytea) does not exist"). Convert null → empty string trước khi pass vào query, dùng `:q = '' OR ...`.
11. **MinIO config**: KHÔNG gọi self `@Bean` từ `@PostConstruct` (circular ref). Dùng `@EventListener(ApplicationReadyEvent.class)` cho init bucket.
12. **Optimistic lock** trên Task: cột `version` + `@Version`.
13. **Cycle detection** (Task dependencies): DFS trong service trước khi insert.

## Hạ tầng đang chạy (dev local)

| Service | Container | Port (host) | Credentials |
|---|---|---|---|
| Postgres | `cms-postgres-uat` | 5432 | postgres / postgres |
| Redis | `cms-redis` | **6397** (KHÔNG phải 6379) | — |
| RabbitMQ | `taskflow-rabbitmq` | 5672, 15672 | guest / guest |
| MinIO | `cms-minio` | 9000, 9001 | minioadmin / minioadmin |

5 database: `taskflow_user`, `taskflow_project`, `taskflow_task`, `taskflow_collab`, `taskflow_notif`. Bucket MinIO: `taskflow-attachments`.

Khi chạy service phải set: `export REDIS_PORT=6397 REDIS_HOST=localhost`.

## Build & run

```bash
# Khi sửa shared lib
cd /home/hieu/Documents/ms/code/taskflow-events-contract && mvn -q clean install
cd /home/hieu/Documents/ms/code/taskflow-common && mvn -q clean install -DskipTests

# Khi sửa 1 service
cd /home/hieu/Documents/ms/code/taskflow-<service> && mvn -q clean package -DskipTests

# Run order: Eureka → Config → User → Project → Task → Collab → Notification → Gateway
# Đợi ~30s sau khi tất cả UP để Eureka heartbeat sync trước khi gọi qua Gateway
```

## Smoke test pattern (dùng nhiều khi dev)

Login Alice (dữ liệu seed có sẵn alice/bob/charlie với password `secret123`):

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"secret123"}' \
  | python3 -c "import sys,json;print(json.load(sys.stdin)['data']['access_token'])")
```

Gọi qua Gateway: `Authorization: Bearer $TOKEN`.

## Nguyên tắc khi làm việc

- **KHÔNG note tác giả Claude/AI vào commit hay code**. Cụ thể:
  - Không thêm trailer `Co-Authored-By: Claude …` vào commit message.
  - Không thêm comment kiểu "generated by Claude/AI" trong source code.
  - Tác giả commit là user, không có attribution AI dưới bất kỳ hình thức nào.
- **Đừng tự sinh DDL từ Hibernate**. Mọi schema thay đổi đi qua Liquibase changeset mới với ID + author.
- **Đừng skip pkill khi restart**: `pkill -f "taskflow-<service>.*\.jar"` trước khi `java -jar` lại để tránh port conflict.
- **Khi rebuild shared lib**, phải build LẠI cả service phụ thuộc. Maven local cache không tự update jar.
- **Tránh `sleep` dài** trong shell — dùng `until <check>; do sleep 1; done` hoặc Bash `run_in_background`.
- **PostgreSQL `cms-postgres-uat`** đang chứa cả các project khác (`cms`, `quiz`...). Chỉ tạo/sửa DB có prefix `taskflow_`.
- **Convention vs SOAR**: tham chiếu chính là `/home/hieu/Downloads/SOAR_BE/SOAR/`. Đọc convention từ project đó nhưng có một số khác biệt liệt kê ở `CONVENTIONS.md` mục 18.

## Trạng thái phase (cập nhật khi xong phase)

- [x] Phase 0 — Foundation (events lib, common lib, Eureka, Config, Gateway skeleton)
- [x] Phase 1 — User Service + JWT filter Gateway
- [x] Phase 2 — Project Service (Project + Board + List + Sprint + Member + saga default board)
- [x] Phase 3 — Task Service (CRUD + move + filter + dependency cycle + label + checklist + watcher + cleanup consumer)
- [x] Phase 4 — Collaboration Service (Comment + Attachment MinIO + Activity Log + cleanup consumer)
- [x] Phase 5 — Notification Service (REST + WebSocket STOMP + event consumer + board broadcast)
- [ ] Phase 6 — Polish (Zipkin tracing, rate limit, README per service, Postman)
- [ ] Phase 7 — Frontend React (mockup HTML đã có ở `code/taskflow-fe-mockup/`, chưa port React)

## FE — đang ở giai đoạn mockup

Đã có 6 file HTML tĩnh ở `code/taskflow-fe-mockup/` (login, dashboard, board, task detail, notifications, members) làm reference design trước khi code React. Mở `index.html` để xem overview.

Stack chốt cho khi code thật: **Vite + React 18 + TypeScript + shadcn/ui + Tailwind + TanStack Query + Zustand + dnd-kit + @stomp/stompjs**. Lưu JWT ở localStorage. Cần bật CORS ở Gateway trước khi bắt đầu (hiện đang disable).

## Khi user yêu cầu code mới

1. Đọc `CONVENTIONS.md` mục liên quan trước.
2. Match pattern của service đã có (User → Project → Task được code đồng nhất, là template tốt).
3. Liquibase changeset phải đúng naming convention.
4. Test bằng cURL qua Gateway, không chỉ test direct port của service.
5. Cập nhật `SETUP.md` mục cURL examples khi thêm endpoint mới.
6. Cập nhật bảng "Trạng thái phase" ở file này khi hoàn thành phase.
