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
| `USER_SERVICE_URL` | `http://localhost:5621/api/v1` | Base URL of `cc-users-players-service` (owns user data; `cc-identity-service` only handles auth). |
| `TEAM_SERVICE_URL` | `http://localhost:5622` | Base URL of `cc-teams-service`. |
| `NOTIFICATION_SERVICE_URL` | `http://localhost:8083` | Base URL of `am-notification-service`. |
| `INTERNAL_API_KEY` | *(empty)* | Shared secret `am-notification-service` requires (`X-Internal-Api-Key` header) on its service-to-service webhooks. Unset means notifications fail with `401`, logged and swallowed like any other notification failure. |
| `USER_EXISTENCE_CHECK` | `true` | Whether to verify a user's existence via `cc-users-players-service` before creating a chat. |
| `TEAM_EXISTENCE_CHECK` | `true` | Whether to verify a team's existence via `cc-teams-service` before creating a group chat. |

See [Service Integration](integracion-servicios.md) for the exact endpoints called and the
removed audit-service integration (no such service exists in the organization).

### Chatbot (Groq)

| Variable | Default | Purpose |
|----------|---------|---------|
| `GROQ_API_KEY` | *(required, no default)* | API key for the Groq-hosted LLM used by the support chatbot. The app fails to start without it. |
| `GROQ_BASE_URL` | `https://api.groq.com/openai/v1` | Groq API base URL. |
| `GROQ_MODEL` | `llama-3.3-70b-versatile` | Chat completion model. |
| `GROQ_SYSTEM_PROMPT` | *(empty → built-in default)* | Overrides the chatbot's system prompt. |

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
