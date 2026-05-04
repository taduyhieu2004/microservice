# SETUP — TaskFlow

Hướng dẫn cài đặt và chạy các service đã hoàn thành (cập nhật theo phase).

## 1. Yêu cầu hệ thống

| Tool | Version tối thiểu | Kiểm tra |
|---|---|---|
| Java | 17 | `java -version` |
| Maven | 3.6.3+ | `mvn -version` |
| Docker | 20+ | `docker --version` |

## 2. Cấu trúc thư mục code

```
/home/hieu/Documents/ms/code/
├── taskflow-events-contract/   (jar 0.1.0 — shared event DTOs)
├── taskflow-common/            (jar 0.1.0 — shared base classes)
├── taskflow-eureka/            (port 8761)
├── taskflow-config/            (port 8888)
│   └── config-repo/            (config chung cho mọi service)
├── taskflow-gateway/           (port 8080)
├── taskflow-user/              (port 8081)
├── taskflow-project/           (port 8082)
├── taskflow-task/              (port 8083)
├── taskflow-collab/            (port 8084)
└── taskflow-notification/      (port 8085, có WebSocket)
```

## 3. Hạ tầng (Postgres / Redis / RabbitMQ / MinIO)

### 3.1 Postgres

Mặc định kết nối `localhost:5432`, user `postgres`, pass `postgres`.

Nếu chưa có Postgres:

```bash
docker run -d --name taskflow-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15
```

Tạo databases (tới Phase 5 cần đủ 5 DB):

```bash
docker exec -i <POSTGRES_CONTAINER_NAME> psql -U postgres -c "CREATE DATABASE taskflow_user;"
docker exec -i <POSTGRES_CONTAINER_NAME> psql -U postgres -c "CREATE DATABASE taskflow_project;"
docker exec -i <POSTGRES_CONTAINER_NAME> psql -U postgres -c "CREATE DATABASE taskflow_task;"
docker exec -i <POSTGRES_CONTAINER_NAME> psql -U postgres -c "CREATE DATABASE taskflow_collab;"
docker exec -i <POSTGRES_CONTAINER_NAME> psql -U postgres -c "CREATE DATABASE taskflow_notif;"
```

### 3.2 Redis

Mặc định kết nối `localhost:6379`. Nếu chưa có:

```bash
docker run -d --name taskflow-redis -p 6379:6379 redis:7-alpine
```

> Nếu Redis chạy trên port khác (vd 6397), set env `REDIS_PORT=6397` khi chạy service.

### 3.3 RabbitMQ

Phase 2 trở đi yêu cầu RabbitMQ (Project Service publish event `project.*`, `board.*`, `list.*`):

```bash
docker run -d --name taskflow-rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  rabbitmq:3.12-management
```

Management UI: `http://localhost:15672` (guest/guest).

### 3.4 MinIO

Phase 4 (Collaboration Service) cần MinIO cho attachment storage. Nếu chưa có:

```bash
docker run -d --name taskflow-minio \
  -e MINIO_ROOT_USER=minioadmin \
  -e MINIO_ROOT_PASSWORD=minioadmin \
  -p 9000:9000 -p 9001:9001 \
  minio/minio server /data --console-address ":9001"
```

Console UI: `http://localhost:9001` (minioadmin/minioadmin).
Bucket `taskflow-attachments` được Collab Service auto-tạo lúc startup.

## 4. Build toàn bộ project

Lần đầu (theo thứ tự — shared lib build trước):

```bash
cd /home/hieu/Documents/ms/code

cd taskflow-events-contract && mvn clean install -DskipTests && cd ..
cd taskflow-common         && mvn clean install -DskipTests && cd ..
cd taskflow-eureka         && mvn clean package -DskipTests && cd ..
cd taskflow-config         && mvn clean package -DskipTests && cd ..
cd taskflow-gateway        && mvn clean package -DskipTests && cd ..
cd taskflow-user           && mvn clean package -DskipTests && cd ..
cd taskflow-project        && mvn clean package -DskipTests && cd ..
cd taskflow-task           && mvn clean package -DskipTests && cd ..
cd taskflow-collab         && mvn clean package -DskipTests && cd ..
cd taskflow-notification   && mvn clean package -DskipTests && cd ..
```

Khi đã build sạch một lần, các lần sau chỉ cần build module nào sửa:

```bash
cd taskflow-user && mvn clean package -DskipTests
```

## 5. Chạy hệ thống

### 5.1 Thứ tự khởi động bắt buộc

```
Eureka  →  Config Server  →  User  →  Project  →  Task  →  Collab  →  Notification  →  Gateway
```

(Mỗi bước đợi service trước UP rồi mới start tiếp. Phải đợi ~30s cho Eureka heartbeat đồng bộ trước khi gọi qua Gateway.)

### 5.2 Env vars

Đặt trước khi run nếu hạ tầng dùng port khác mặc định:

```bash
# Postgres
export POSTGRES_URL=jdbc:postgresql://localhost:5432/taskflow_user
export POSTGRES_USERNAME=postgres
export POSTGRES_PASSWORD=postgres

# Redis (default 6379)
export REDIS_HOST=localhost
export REDIS_PORT=6379

# Eureka & Config (mặc định localhost)
export EUREKA_URL=http://localhost:8761/eureka/
export CONFIG_URI=http://localhost:8888
```

### 5.3 Chạy thủ công 4 tab terminal

**Tab 1 — Eureka**
```bash
cd /home/hieu/Documents/ms/code/taskflow-eureka
java -jar target/taskflow-eureka-0.1.0.jar
```
Đợi log `Started EurekaApplication` rồi truy cập http://localhost:8761 (Eureka dashboard).

**Tab 2 — Config Server**
```bash
cd /home/hieu/Documents/ms/code/taskflow-config
java -jar target/taskflow-config-0.1.0.jar
```
Test: `curl http://localhost:8888/application/default` (trả config chung).

**Tab 3 — User Service**
```bash
cd /home/hieu/Documents/ms/code/taskflow-user
java -jar target/taskflow-user-0.1.0.jar
```

**Tab 4 — Project Service**
```bash
cd /home/hieu/Documents/ms/code/taskflow-project
java -jar target/taskflow-project-0.1.0.jar
```

**Tab 5 — Task Service**
```bash
cd /home/hieu/Documents/ms/code/taskflow-task
java -jar target/taskflow-task-0.1.0.jar
```

**Tab 6 — Collaboration Service**
```bash
cd /home/hieu/Documents/ms/code/taskflow-collab
java -jar target/taskflow-collab-0.1.0.jar
```

**Tab 7 — Notification Service**
```bash
cd /home/hieu/Documents/ms/code/taskflow-notification
java -jar target/taskflow-notification-0.1.0.jar
```

**Tab 8 — Gateway**
```bash
cd /home/hieu/Documents/ms/code/taskflow-gateway
java -jar target/taskflow-gateway-0.1.0.jar
```

### 5.4 Verify

Sau ~30s, mọi service đã register Eureka:

```bash
curl -s http://localhost:8761/eureka/apps -H "Accept: application/json" \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print([a['name'] for a in d['applications']['application']])"
```

Mong đợi kết quả: `['TASKFLOW-COLLAB', 'TASKFLOW-CONFIG', 'TASKFLOW-GATEWAY', 'TASKFLOW-NOTIFICATION', 'TASKFLOW-PROJECT', 'TASKFLOW-TASK', 'TASKFLOW-USER']`.

## 6. Test API qua Gateway (port 8080)

### 6.1 Register

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@example.com","password":"secret123","full_name":"Alice"}'
```

### 6.2 Login (lấy access token)

```bash
LOGIN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"secret123"}')

TOKEN=$(echo "$LOGIN" | python3 -c "import sys,json;print(json.load(sys.stdin)['data']['access_token'])")
echo $TOKEN
```

### 6.3 Get profile

```bash
curl http://localhost:8080/api/v1/users/me -H "Authorization: Bearer $TOKEN"
```

### 6.4 Update profile

```bash
curl -X PUT http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"full_name":"Alice Updated","bio":"hello microservice"}'
```

### 6.5 Refresh token

```bash
REFRESH=$(echo "$LOGIN" | python3 -c "import sys,json;print(json.load(sys.stdin)['data']['refresh_token'])")
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refresh_token\":\"$REFRESH\"}"
```

### 6.6 Search

```bash
curl "http://localhost:8080/api/v1/users?q=alice&page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

### 6.7 Logout

```bash
curl -X POST http://localhost:8080/api/v1/auth/logout -H "Authorization: Bearer $TOKEN"
# Token cũ giờ trả 401 nếu dùng tiếp
```

### 6.8 Forgot / Reset password

DEV mode trả token thẳng trong response (PROD sẽ qua email):

```bash
TOKEN_RESET=$(curl -s -X POST http://localhost:8080/api/v1/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com"}' \
  | python3 -c "import sys,json;print(json.load(sys.stdin)['data']['dev_reset_token'])")
echo $TOKEN_RESET

curl -X POST http://localhost:8080/api/v1/auth/reset-password \
  -H "Content-Type: application/json" \
  -d "{\"token\":\"$TOKEN_RESET\",\"new_password\":\"newpass456\"}"
```

### 6.9 Block /internal

```bash
curl -i http://localhost:8080/api/v1/internal/users/1/contact -H "Authorization: Bearer $TOKEN"
# HTTP 404, body: {"data":{"code":"endpoint_not_exposed",...}}
```

### 6.10 Tạo project (saga: tự động kèm 1 board + 3 lists)

```bash
PROJECT=$(curl -s -X POST http://localhost:8080/api/v1/projects \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"TaskFlow Mobile","key":"TFM","type":"SOFTWARE"}')
echo "$PROJECT" | python3 -m json.tool
PROJECT_ID=$(echo "$PROJECT" | python3 -c "import sys,json;print(json.load(sys.stdin)['data']['id'])")

# Verify default board + 3 lists
curl -s http://localhost:8080/api/v1/projects/$PROJECT_ID/boards \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
curl -s http://localhost:8080/api/v1/boards/1 \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

### 6.11 Add member khác vào project

```bash
curl -X POST http://localhost:8080/api/v1/projects/$PROJECT_ID/members \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"user_id": 2, "role": "EDITOR"}'

curl http://localhost:8080/api/v1/projects/$PROJECT_ID/members \
  -H "Authorization: Bearer $TOKEN"
```

### 6.12 Đổi role / xoá member

```bash
curl -X PATCH http://localhost:8080/api/v1/projects/$PROJECT_ID/members/2/role \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"role": "VIEWER"}'

curl -X DELETE http://localhost:8080/api/v1/projects/$PROJECT_ID/members/2 \
  -H "Authorization: Bearer $TOKEN"
```

### 6.13 Tạo board / list / reorder

```bash
# Tạo board mới
curl -X POST http://localhost:8080/api/v1/projects/$PROJECT_ID/boards \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Bugs","color":"#FF6B6B"}'

# Reorder lists
curl -X PATCH http://localhost:8080/api/v1/boards/1/lists/reorder \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"items":[{"id":1,"position":2},{"id":2,"position":0},{"id":3,"position":1}]}'
```

### 6.14 Tạo sprint

```bash
curl -X POST http://localhost:8080/api/v1/projects/$PROJECT_ID/sprints \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Sprint 1","goal":"MVP","start_date":1777870000000,"end_date":1779080000000}'

curl http://localhost:8080/api/v1/projects/$PROJECT_ID/sprints \
  -H "Authorization: Bearer $TOKEN"
```

### 6.15 Verify event publish (RabbitMQ)

```bash
docker exec taskflow-rabbitmq rabbitmqctl list_exchanges | grep taskflow
# taskflow.events  topic
```

Mở UI http://localhost:15672 (guest/guest) → **Exchanges → taskflow.events** sẽ thấy stats publish event mỗi khi tạo project / board / list / member.

### 6.16 Tạo task

```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"list_id":1,"title":"Setup login","priority":"HIGH","assignee_id":1}'
```

### 6.17 Move task (drag & drop giữa list)

```bash
curl -X POST http://localhost:8080/api/v1/tasks/1/move \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"to_list_id":2}'
```

### 6.18 Filter / search task

```bash
# Filter trong project
curl "http://localhost:8080/api/v1/tasks?project_id=1&page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"

# Search keyword
curl "http://localhost:8080/api/v1/tasks?project_id=1&q=login" \
  -H "Authorization: Bearer $TOKEN"

# Filter theo assignee + priority
curl "http://localhost:8080/api/v1/tasks?project_id=1&assignee_id=1&priority=HIGH" \
  -H "Authorization: Bearer $TOKEN"
```

### 6.19 Task dependency + cycle detection

```bash
# Task 2 phụ thuộc task 1
curl -X POST http://localhost:8080/api/v1/tasks/2/dependencies \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"depends_on_task_id":1,"type":"BLOCKS"}'

# Cố tạo cycle: task 1 phụ thuộc task 2 (server từ chối với code cycle_detected)
curl -X POST http://localhost:8080/api/v1/tasks/1/dependencies \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"depends_on_task_id":2}'
```

### 6.20 Label

```bash
# Tạo label
curl -X POST "http://localhost:8080/api/v1/labels?project_id=1" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Backend","color":"#5BA4CF"}'

# Gán label vào task
curl -X PUT http://localhost:8080/api/v1/tasks/1 \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"label_ids":[1]}'

# List label trong project
curl "http://localhost:8080/api/v1/labels?project_id=1" -H "Authorization: Bearer $TOKEN"
```

### 6.21 Checklist

```bash
# Tạo checklist
curl -X POST http://localhost:8080/api/v1/tasks/1/checklists \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"title":"Sub-tasks"}'

# Thêm item
curl -X POST http://localhost:8080/api/v1/checklists/1/items \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"content":"Step 1","position":0}'

# Toggle completed
curl -X PATCH http://localhost:8080/api/v1/checklist-items/1 \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"completed":true}'
```

### 6.22 Watch task

```bash
curl -X POST http://localhost:8080/api/v1/tasks/1/watch -H "Authorization: Bearer $TOKEN"
curl -X DELETE http://localhost:8080/api/v1/tasks/1/watch -H "Authorization: Bearer $TOKEN"
```

### 6.23 Cascade delete (event-driven)

```bash
# Xoá list → Task Service consume event → soft delete tất cả task trong list
curl -X DELETE http://localhost:8080/api/v1/lists/1 -H "Authorization: Bearer $TOKEN"
sleep 2
# Verify tasks gone
curl "http://localhost:8080/api/v1/tasks?project_id=1&list_id=1" -H "Authorization: Bearer $TOKEN"
# total_elements: 0
```

### 6.24 Comment trên task

```bash
# Tạo comment (hỗ trợ @userN mention)
curl -X POST http://localhost:8080/api/v1/tasks/1/comments \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"content":"Looks good @user2 please review"}'

# List
curl "http://localhost:8080/api/v1/tasks/1/comments?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"

# Update / delete (chỉ author hoặc admin)
curl -X PUT http://localhost:8080/api/v1/comments/1 \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"content":"updated"}'
curl -X DELETE http://localhost:8080/api/v1/comments/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 6.25 Attachment upload / download

```bash
# Upload (multipart, max 25MB)
echo "hello" > /tmp/sample.txt
curl -X POST http://localhost:8080/api/v1/tasks/1/attachments \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/tmp/sample.txt;type=text/plain"

# List
curl http://localhost:8080/api/v1/tasks/1/attachments \
  -H "Authorization: Bearer $TOKEN"

# Download (binary stream)
curl -O -J http://localhost:8080/api/v1/attachments/1/download \
  -H "Authorization: Bearer $TOKEN"

# Delete (chỉ uploader hoặc admin) — xoá luôn file MinIO
curl -X DELETE http://localhost:8080/api/v1/attachments/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 6.26 Activity Log (event-driven)

Collab Service consume mọi event từ tất cả service và ghi vào `activity_logs`. Xem:

```bash
# Activity của project
curl "http://localhost:8080/api/v1/activities/projects/1?size=20" \
  -H "Authorization: Bearer $TOKEN"

# Activity của task (gồm cả comment, attachment liên quan)
curl http://localhost:8080/api/v1/activities/tasks/1 \
  -H "Authorization: Bearer $TOKEN"
```

Mỗi activity có `payload` jsonb chứa snapshot event gốc (tiện cho FE render diff).

### 6.27 Cleanup khi xoá task

```bash
# Xoá task → Collab Service consume task.deleted → soft delete comments + attachments + xoá file MinIO
curl -X DELETE http://localhost:8080/api/v1/tasks/1 -H "Authorization: Bearer $TOKEN"
sleep 2
# Comments + attachments của task 1 đều có deleted=true
```

### 6.28 Notification (Phase 5)

Notification Service consume event từ tất cả service và tạo notification cho user phù hợp:

| Event | → Notification cho |
|---|---|
| `task.created` (có assignee) | assignee mới |
| `task.assigned` | assignee mới |
| `task.due_soon` / `task.overdue` | assignee |
| `comment.added` (có @mention) | user được mention |
| `project.member.added` | user mới được mời |

```bash
# Lấy notification của mình
curl http://localhost:8080/api/v1/notifications -H "Authorization: Bearer $TOKEN"

# Chỉ unread
curl "http://localhost:8080/api/v1/notifications?unread_only=true" \
  -H "Authorization: Bearer $TOKEN"

# Badge count
curl http://localhost:8080/api/v1/notifications/unread-count \
  -H "Authorization: Bearer $TOKEN"

# Mark đã đọc
curl -X PATCH http://localhost:8080/api/v1/notifications/1/read \
  -H "Authorization: Bearer $TOKEN"
curl -X PATCH http://localhost:8080/api/v1/notifications/read-all \
  -H "Authorization: Bearer $TOKEN"

# Preference (in-app + email + per-type)
curl http://localhost:8080/api/v1/notifications/preferences \
  -H "Authorization: Bearer $TOKEN"
curl -X PUT http://localhost:8080/api/v1/notifications/preferences \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"in_app_enabled":true, "email_enabled":false, "per_type_settings":{"TASK_DUE_SOON":true}}'
```

### 6.29 WebSocket realtime

Notification Service expose 2 STOMP destination:

| Destination | Loại | Mục đích |
|---|---|---|
| `/user/queue/notifications` | private mỗi user | Push notification mới ngay khi tạo |
| `/topic/board/{boardId}` | broadcast | Sync board: `task.created`, `task.moved`, `task.deleted` |

Connect URL: `ws://localhost:8080/ws/notifications?token=<JWT>` (qua Gateway).

Test bằng wscat (hoặc trình STOMP client như `Dark/STOMP-WebSocket Tester`):

```bash
# Cài wscat
npm install -g wscat

# Connect raw WS (không SockJS)
wscat -c "ws://localhost:8080/ws/notifications?token=$TOKEN"

# Sau khi connect, gửi STOMP CONNECT frame:
# CONNECT\naccept-version:1.2\nhost:localhost\n\n 

# Subscribe topic board:
# SUBSCRIBE\nid:sub-0\ndestination:/topic/board/6\n\n 

# Subscribe user queue:
# SUBSCRIBE\nid:sub-1\ndestination:/user/queue/notifications\n\n 
```

Trong test thực tế dùng FE (Phase 7) hoặc HTML page nhỏ với SockJS + Stomp.js client.

### 6.30 End-to-end demo (Notification + WebSocket)

```bash
# Alice login + tạo task gán Bob
ALICE=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" -d '{"username":"alice","password":"secret123"}' \
  | python3 -c "import sys,json;print(json.load(sys.stdin)['data']['access_token'])")

# Sau khi tạo task với assignee_id=2, Bob nhận TASK_ASSIGNED notification
curl http://localhost:8080/api/v1/notifications \
  -H "Authorization: Bearer $(curl -s -X POST http://localhost:8080/api/v1/auth/login \
      -H 'Content-Type: application/json' -d '{\"username\":\"bob\",\"password\":\"secret123\"}' \
      | python3 -c 'import sys,json;print(json.load(sys.stdin)[\"data\"][\"access_token\"])')"
```

## 7. Swagger UI

Truy cập trực tiếp từng service (không qua Gateway):

- User Service: `http://localhost:8081/swagger-ui.html`
- Project Service: `http://localhost:8082/swagger-ui.html`
- Task Service: `http://localhost:8083/swagger-ui.html`
- Collaboration Service: `http://localhost:8084/swagger-ui.html`
- Notification Service: `http://localhost:8085/swagger-ui.html`

## 8. Cleanup

```bash
# Tắt mọi process Java taskflow-
pkill -f "taskflow-.*\.jar"

# Tắt container hạ tầng (giữ Postgres dùng chung)
docker stop taskflow-redis taskflow-rabbitmq 2>/dev/null
```

## 9. Troubleshooting

| Triệu chứng | Nguyên nhân | Cách xử lý |
|---|---|---|
| `Connection refused` Postgres | Container chưa up / port khác | `docker ps`, sửa env `POSTGRES_URL` |
| `BindException port already in use` | Port bị chiếm bởi process khác | `lsof -i :8081`, kill hoặc đổi `SERVER_PORT` |
| Service không xuất hiện trong Eureka | Eureka chưa kịp đồng bộ (heartbeat 30s) | Đợi 30s, hoặc restart service |
| Login OK nhưng `/users/me` qua Gateway 401 | Token cũ bị invalidate bởi login mới | Single-session policy — luôn dùng token mới nhất |
| Gateway 503 khi gọi `/api/v1/users/...` | User Service chưa register Eureka | Đợi 30s, kiểm tra Eureka dashboard |
| Liquibase lỗi `validation failed` | Đổi schema Liquibase sau khi đã chạy | Drop database rồi tạo lại, hoặc thêm changeset mới |
| `JWT signature does not match` | Secret key Gateway và User khác nhau | Đảm bảo `JWT_ACCESS_SECRET` cùng giá trị 2 nơi |

## 10. Phase đã hoàn thành

| Phase | Hạng mục | Trạng thái |
|---|---|---|
| 0.1 | taskflow-events-contract | ✅ |
| 0.2 | taskflow-common | ✅ |
| 0.3a | Eureka Server | ✅ |
| 0.3b | Config Server | ✅ |
| 0.4 | API Gateway skeleton | ✅ |
| 1.1–1.7 | User Service + JWT filter ở Gateway | ✅ |
| 2.1–2.10 | Project Service: Project/Board/List/Sprint/Member + saga + event publisher | ✅ |
| 3.1–3.8 | Task Service: Task CRUD + move + filter + dependency cycle + label + checklist + watcher + cleanup consumer | ✅ |
| 4.1–4.8 | Collaboration: Comment + Attachment (MinIO) + Activity Log (jsonb) + cleanup consumer | ✅ |
| 5.1–5.6 | Notification: REST + WebSocket STOMP + event consumer (notify + board broadcast) | ✅ |
