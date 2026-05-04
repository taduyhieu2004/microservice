# CONVENTIONS — TaskFlow Microservices

Tài liệu này quy định convention **bắt buộc** cho cả 5 service (User, Project, Task, Collaboration, Notification). Phần lớn rút từ project SOAR; các điểm khác biệt được liệt kê ở mục 18.

## 1. Tech Stack & Version

| Mục | Giá trị |
|---|---|
| Java | 17 |
| Spring Boot | 3.3.x |
| Maven | 3.9+ |
| PostgreSQL | 15+ |
| Redis | 7+ |
| RabbitMQ | 3.12+ |
| MinIO | mới nhất ổn định |

Mỗi service là **single-module Maven**, repo/folder riêng. Không monolith multi-module.

## 2. Project Structure

Package gốc: `com.taskflow.<service>`. Ví dụ `com.taskflow.project`, `com.taskflow.task`.

Folder layout (theo SOAR):

```
src/main/java/com/taskflow/<service>/
├── advice/                  ExceptionHandlerAdvice (@RestControllerAdvice)
├── annotation/              Custom validators
├── config/                  @Configuration beans
│   └── auditor/
├── constant/                Constants + enums (đúng chính tả, không "constanst")
│   └── enums/
├── controller/              @RestController
├── dto/
│   ├── request/             <Entity>Request, Create<Entity>Request, ...
│   ├── response/            <Entity>Response, ApiResponse, PageResponse, Error
│   └── event/               Event payload DTO (RabbitMQ)
├── entity/
│   ├── base/                BaseEntity, AuditEntity
│   └── enums/
├── exception/
│   ├── base/                BaseException, NotFoundException, BadRequestException, ...
│   └── <feature>/
├── facade/                  Business orchestration
│   └── impl/
├── filter/                  JwtAuthenticationFilter (riêng Gateway)
├── messaging/
│   ├── publisher/
│   └── consumer/
├── repository/              Spring Data JPA
├── security/                SecurityConfiguration, SecurityUtils
├── service/                 Business logic
│   └── impl/
├── util/
└── <Service>Application.java
```

Layer flow bắt buộc: **Controller → Facade → Service → Repository**. Controller chỉ I/O. Facade orchestrate nhiều Service. Service chứa business logic. Repository chỉ data access.

## 3. Application Config

Format: **YAML**. Profiles: `dev`, `dev-local`, `prod`. Tất cả secret/host phải override được qua environment variable.

Skeleton `application.yml`:

```yaml
spring:
  application:
    name: ${SPRING_APP_NAME:project-service}
  profiles:
    active: ${PROFILE_ACTIVE:dev}
  datasource:
    url: ${POSTGRES_URL:jdbc:postgresql://localhost:5432/taskflow_project}
    username: ${POSTGRES_USERNAME:postgres}
    password: ${POSTGRES_PASSWORD:postgres}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate          # bắt buộc validate, schema do Liquibase quản
  liquibase:
    change-log: classpath:db/master.xml
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}

server:
  port: ${SERVER_PORT:8082}

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URL:http://localhost:8761/eureka}

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  endpoint:
    health:
      probes:
        enabled: true

logging:
  level:
    root: INFO
    com.taskflow: DEBUG
  pattern:
    level: "%5p [${spring.application.name:-},%X{traceId:-},%X{spanId:-}]"
```

## 4. Database & Liquibase

- Format: **XML** (đồng bộ SOAR).
- Master file: `src/main/resources/db/master.xml`.
- Changeset folder: `src/main/resources/db/changelog/`.
- Naming changeset: `YYYYMMDD-HHMM-<feature>.xml` (vd `20260504-0900-projects.xml`).
- ID changeset: `create-<table>-table` hoặc `add-<column>-to-<table>`.
- Author: github username.
- Bắt buộc `spring.jpa.hibernate.ddl-auto=validate` — Hibernate KHÔNG sinh DDL.

Master.xml ví dụ:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   https://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">
    <!-- Bảng độc lập -->
    <include file="db/changelog/20260504-0900-projects.xml"/>
    <!-- Phụ thuộc mức 1 -->
    <include file="db/changelog/20260504-0905-project_members.xml"/>
    <include file="db/changelog/20260504-0910-boards.xml"/>
    <!-- Phụ thuộc mức 2 -->
    <include file="db/changelog/20260504-0915-lists.xml"/>
</databaseChangeLog>
```

## 5. Entity Base

```java
@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
```

```java
@EqualsAndHashCode(callSuper = true)
@Data
@MappedSuperclass
public abstract class AuditEntity extends BaseEntity {
    @CreatedBy   private String createdBy;        // username (String)
    @CreatedDate private Long   createdAt;        // epoch millis
    @LastModifiedBy   private String lastUpdatedBy;
    @LastModifiedDate private Long   lastUpdatedAt;
    @Column(name = "deleted") private Boolean deleted = false;
}
```

- ID strategy: `BIGSERIAL` (auto-increment Long).
- `createdBy/lastUpdatedBy` lưu **username string**, không lưu user id.
- Thời gian lưu **epoch millis (Long)**, không dùng `LocalDateTime`.
- Soft delete bằng cột `deleted`. Mọi query mặc định `WHERE deleted = false`.

`AuditorAware` lấy username từ SecurityContext, default `SYSTEM` nếu chưa auth.

## 6. JWT Specification

Library: **JJWT v0.11.5+**. Algorithm: **HS256**.

3 loại token (theo SOAR):

| Token Type | TTL | Mục đích |
|---|---|---|
| ACCESS_TOKEN | 1 giờ | Gọi API |
| REFRESH_TOKEN | 30 ngày | Đổi access token mới |
| RESET_PASSWORD_TOKEN | 5 phút | Reset password (1 lần) |

Mỗi loại có secret key Base64 riêng:

```yaml
jwt:
  access-token:
    secret-key: ${JWT_ACCESS_SECRET:<base64>}
    ttl: 3600000
  refresh-token:
    secret-key: ${JWT_REFRESH_SECRET:<base64>}
    ttl: 2592000000
  reset-password-token:
    secret-key: ${JWT_RESET_SECRET:<base64>}
    ttl: 300000
```

**Claims** (mở rộng so với SOAR — SOAR chỉ có `sub`):

| Claim | Giá trị | Lý do |
|---|---|---|
| `sub` | userId (String) | Chuẩn JWT |
| `username` | username | Gateway forward sang `X-Username` không cần gọi User Service |
| `email` | email | Notification Service nhận từ header để gửi mail |
| `iat` | issued at (epoch s) | Chuẩn |
| `exp` | expiration (epoch s) | Chuẩn |

**KHÔNG** đưa role per-project vào JWT vì role gắn với từng project (không global). Role được kiểm tra runtime tại Service đích bằng cách gọi Project Service (cache Redis 5 phút).

Sau khi verify JWT, **API Gateway** forward các header:

```
X-User-Id: 42
X-Username: alice
X-User-Email: alice@example.com
X-Trace-Id: <generated/forward>
```

Service downstream tin tưởng các header này (mạng Docker nội bộ, các service khác không expose port).

Token revocation: mỗi token được lưu vào Redis key `jwt:<userId>:<tokenType>` với TTL = TTL của token. Logout xoá key. Filter check key tồn tại trước khi cho qua (theo SOAR).

## 7. REST API Convention

URL: `/api/v1/<resource>`. JSON body: **snake_case** (qua `@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)` ở DTO).

Response wrapper:

```java
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ApiResponse<T> {
    private int    status;
    private String message;
    private T      data;
    private String timestamp;        // ISO-8601

    public static <T> ApiResponse<T> ok(String message, T data) {
        return of(200, message, data, Instant.now().toString());
    }
    public static <T> ApiResponse<T> created(String message, T data) {
        return of(201, message, data, Instant.now().toString());
    }
}
```

Pagination (mở rộng so SOAR — thêm page/size để FE phân trang được):

```java
@Data
@AllArgsConstructor(staticName = "of")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PageResponse<T> {
    private List<T> content;
    private long    totalElements;
    private int     page;
    private int     size;
}
```

Error payload (theo SOAR):

```java
@Data
@AllArgsConstructor(staticName = "of")
public class Error {
    private String code;
    private Object detail;
}
```

DTO naming:
- Input: `<Entity>Request`, `Create<Entity>Request`, `Update<Entity>Request`, `<Entity>FilterRequest`
- Output: `<Entity>Response`, `<Entity>SummaryResponse` (rút gọn)

Mapper: **MapStruct** (khác SOAR — chuẩn hoá để giảm boilerplate).

Validation: `@Valid` + Jakarta Validation + custom `@Constraint`.

Header chuẩn:

| Header | Mục đích |
|---|---|
| `Authorization: Bearer <jwt>` | Auth (chỉ client → Gateway) |
| `Accept-Language` | i18n (default `vi`) |
| `Idempotency-Key` | Tạo task / comment / attachment |
| `X-User-Id` | Service-to-service (Gateway gắn) |
| `X-Username` | Service-to-service |
| `X-User-Email` | Service-to-service |
| `X-Trace-Id` | Distributed tracing |

## 8. Exception Handling

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseException extends RuntimeException {
    private final String              code;
    private final int                 status;
    private final Map<String, String> params;
}
```

Subclass: `NotFoundException`, `BadRequestException`, `UnauthorizedException`, `ForbiddenException`, `ConflictException`, `InternalServerError`.

Global handler `@RestControllerAdvice` ở `advice/ExceptionHandlerAdvice.java` bắt: `BaseException`, `MethodArgumentNotValidException`, `Exception` (fallback).

Mỗi `code` PHẢI có entry trong `i18n/messages.properties` và `messages_vi.properties`.

## 9. Logging & Tracing

- **SLF4J + Lombok @Slf4j**.
- Log level: prod=INFO, dev=DEBUG.
- **Distributed tracing bắt buộc** qua **Micrometer + Zipkin** (khác SOAR — TaskFlow là microservices nên cần). MDC tự động chèn `traceId`, `spanId`.
- Log pattern có `[%X{traceId:-},%X{spanId:-}]`.

## 10. Docker

Mỗi service Dockerfile multi-stage riêng (theo SOAR):

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /opt/app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre AS layer
WORKDIR /opt/app
COPY --from=build /opt/app/target/*.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:17-jre
ENV TZ=Asia/Ho_Chi_Minh
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
WORKDIR /opt/app
RUN groupadd app && useradd -g app -m app
USER app
COPY --from=layer /opt/app/dependencies/ ./
COPY --from=layer /opt/app/spring-boot-loader/ ./
COPY --from=layer /opt/app/snapshot-dependencies/ ./
COPY --from=layer /opt/app/application/ ./
ENTRYPOINT ["java","-XX:+UseG1GC","org.springframework.boot.loader.launch.JarLauncher"]
```

**Healthcheck bắt buộc** trong `docker-compose.yml`:

```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:${SERVER_PORT}/actuator/health"]
  interval: 15s
  timeout: 3s
  retries: 5
  start_period: 30s
```

Chỉ Gateway map port ra host. Service khác chỉ trong network `taskflow-network`.

## 11. Validation - Custom Annotation Pattern

Theo SOAR — annotation + inner Validator class:

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HexColor.HexColorValidator.class)
public @interface HexColor {
    String message() default "invalid_hex_color";
    Class<?>[] groups()  default {};
    Class<? extends Payload>[] payload() default {};

    class HexColorValidator implements ConstraintValidator<HexColor, String> {
        @Override public boolean isValid(String v, ConstraintValidatorContext c) {
            return v == null || v.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
        }
    }
}
```

## 12. i18n

Tệp: `src/main/resources/i18n/messages.properties` (en), `messages_vi.properties` (vi).

Key format: `<service>.<domain>.<error>` — ví dụ `project.member.already_exists`.

Lookup qua `MessageSource`. Header request: `Accept-Language` (default `vi`).

## 13. Constants

Một file gốc `<Service>Constants.java`, chia inner class theo nhóm:

```java
public final class TaskFlowConstants {
    public static final class Headers {
        public static final String USER_ID         = "X-User-Id";
        public static final String USERNAME        = "X-Username";
        public static final String USER_EMAIL      = "X-User-Email";
        public static final String TRACE_ID        = "X-Trace-Id";
        public static final String IDEMPOTENCY_KEY = "Idempotency-Key";
        public static final String LANGUAGE        = "Accept-Language";
        public static final String DEFAULT_LANGUAGE = "vi";
    }
    public static final class Cache {
        public static final long PROJECT_ROLE_TTL = 300;    // 5 phút
        public static final long IDEMPOTENCY_TTL  = 86400;  // 24h
    }
    public static final class Limits {
        public static final long MAX_FILE_SIZE      = 25L * 1024 * 1024; // 25MB
        public static final int  MAX_TASKS_PER_LIST = 1000;
    }
    public static final class TokenPrefix {
        public static final String JWT = "jwt:";              // jwt:<userId>:<type>
        public static final String IDEM = "idem:";            // idem:<service>:<key>
    }
}
```

## 14. Cross-Service Communication

WebClient + Eureka + Resilience4j:

```java
@Bean
@LoadBalanced
public WebClient.Builder webClientBuilder() {
    return WebClient.builder();
}
```

Quy tắc:
- URL dạng `http://<service-name>/api/v1/...` (qua Eureka name).
- Forward header `X-User-Id`, `X-Username`, `X-User-Email`, `X-Trace-Id`.
- Bọc `@CircuitBreaker(name = "<service>", fallbackMethod = "...")` + `@TimeLimiter(name = "<service>")` (timeout 2s) + `@Retry(name = "<service>")` (1-2 lần, chỉ retry lỗi mạng).
- Cache Redis cho query lặp lại (vd lookup user email, project role).

## 15. Idempotency

Tạo `task` / `comment` / `attachment`: client gửi header `Idempotency-Key` (UUID v4).

Server lưu Redis key `idem:<service>:<endpoint>:<userId>:<key>` TTL 24h, value = response JSON đã lưu. Request trùng key → trả response cũ HTTP 200.

## 16. Repository Pattern

```java
@NoRepositoryBean
public interface BaseRepository<T> extends JpaRepository<T, Long> {}
```

Naming: `<Entity>Repository extends BaseRepository<<Entity>>`.

JPQL DTO projection (theo SOAR):

```java
@Query("""
    SELECT new com.taskflow.project.dto.response.BoardSummaryResponse(b.id, b.name, b.color)
    FROM Board b
    WHERE b.projectId = :projectId AND b.deleted = false
""")
List<BoardSummaryResponse> listSummary(@Param("projectId") Long projectId);
```

## 17. Multi-tenancy

**KHÔNG có** trong TaskFlow (SOAR có `tenancyId` vì là SaaS — TaskFlow đồ án bỏ qua để đơn giản).

## 18. Khác biệt với SOAR — Tóm tắt

| Mục | SOAR | TaskFlow |
|---|---|---|
| Module | Single-module monolith | 5 service riêng |
| Messaging | Kafka | RabbitMQ |
| JWT claims | Chỉ `sub` | `sub` + `username` + `email` |
| Mapper | Manual | MapStruct |
| Distributed tracing | Không | Có (Zipkin) |
| Multi-tenancy | Có (`tenancyId`) | Không |
| Pagination | `content + amount` | `content + totalElements + page + size` |
| Healthcheck | Không cấu hình | Bắt buộc |
| Service discovery | Không | Eureka |
| API Gateway | Không | Spring Cloud Gateway |
| Centralized config | Không | Spring Cloud Config |
| Idempotency | Không | Có (Redis) |
