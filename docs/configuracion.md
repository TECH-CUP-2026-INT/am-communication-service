# Configuration

The service is configured entirely through environment variables, with development-friendly
defaults defined in `src/main/resources/application.yaml`. This page documents those variables,
how to run the service locally, and the observability endpoints it exposes.

## Environment variables

### Database

| Variable | Default | Purpose |
|----------|---------|---------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/communications` | JDBC connection URL. |
| `DB_USERNAME` | `communications` | Database user. |
| `DB_PASSWORD` | `communications` | Database password. |
| `JPA_DDL_AUTO` | `update` | Hibernate `ddl-auto` strategy. |

### Server

| Variable | Default | Purpose |
|----------|---------|---------|
| `SERVER_PORT` | `8080` | HTTP port for both the REST API and the WebSocket (STOMP) endpoint. |

### Security (JWT)

| Variable | Default | Purpose |
|----------|---------|---------|
| `JWT_SECRET` | *(dev-only fallback)* | HS256 signing secret. **Must** match the value used by `cc-identity-service` in every real deployment; only defaulted locally for convenience. |
| `JWT_ISSUER` | *(empty)* | Expected `iss` claim, if enforced. |
| `JWT_AUDIENCE` | *(empty)* | Expected `aud` claim, if enforced. |
| `JWT_CLOCK_SKEW_SECONDS` | `30` | Tolerance applied when validating token expiration. |

### WebSocket

| Variable | Default | Purpose |
|----------|---------|---------|
| `WS_ALLOWED_ORIGINS` | `http://localhost:*` | Allowed origins for the STOMP endpoint (CORS). |

### Inter-service integration

| Variable | Default | Purpose |
|----------|---------|---------|
| `USER_SERVICE_URL` | `http://localhost:11711` | Base URL of `cc-identity-service`. |
| `TEAM_SERVICE_URL` | `http://localhost:8082` | Base URL of `cc-teams-service`. |
| `NOTIFICATION_SERVICE_URL` | `http://localhost:8083` | Base URL of `am-notification-service`. |
| `AUDIT_SERVICE_URL` | `http://localhost:8084` | Base URL of the audit service. |
| `USER_EXISTENCE_CHECK` | `true` | Whether to verify a user's existence via `cc-identity-service` before creating a chat. |
| `TEAM_EXISTENCE_CHECK` | `true` | Whether to verify a team's existence via `cc-teams-service` before creating a group chat. |

!!! warning "Existence checks in local Docker Compose"
    `docker-compose.yml` defaults both `USER_EXISTENCE_CHECK` and `TEAM_EXISTENCE_CHECK` to
    `false`, because neither `cc-identity-service` nor `cc-teams-service` currently exposes the
    lookup endpoint this service expects (see [Service Integration](integracion-servicios.md)).
    Set them to `true` once those endpoints exist.

### Chatbot (Groq)

| Variable | Default | Purpose |
|----------|---------|---------|
| `GROQ_API_KEY` | *(required, no default)* | API key for the Groq-hosted LLM used by the support chatbot. The app fails to start without it. |
| `GROQ_BASE_URL` | `https://api.groq.com/openai/v1` | Groq API base URL. |
| `GROQ_MODEL` | `llama-3.3-70b-versatile` | Chat completion model. |
| `GROQ_SYSTEM_PROMPT` | *(empty → built-in default)* | Overrides the chatbot's system prompt. |

## Running locally

The project ships a `docker-compose.yml` with a PostgreSQL 16 instance and the service itself.
Two variables have **no default** and must be provided explicitly:

```bash
export JWT_SECRET="<same secret as cc-identity-service>"
export GROQ_API_KEY="<your Groq API key>"

docker compose up
```

The app becomes available on `http://localhost:8080` (REST API, Swagger UI at `/swagger-ui.html`,
and the STOMP endpoint) once the `postgres` health check passes.

## Observability

Spring Boot Actuator is exposed with the following endpoints (`management.endpoints.web.exposure.include`):

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health` | Liveness/readiness check. |
| `/actuator/info` | Build/application info. |
| `/actuator/prometheus` | Metrics in Prometheus exposition format. |
| `/actuator/metrics` | Micrometer metrics browser. |

Distributed tracing is sent to Zipkin (`management.zipkin.tracing.endpoint`, default
`http://localhost:9411/api/v2/spans`, sampling controlled by `TRACING_SAMPLING`, default `1.0`).
The repository's `observability/` folder provides a ready-to-use `prometheus.yml` (scrape config)
and `grafana-datasource.yml` (Prometheus datasource for Grafana) for local monitoring stacks.
