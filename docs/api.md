# API

The service exposes **two interfaces** with distinct purposes:

-  **REST API** — management operations (history, moderation) and inter-service communication.
-  **WebSocket (STOMP)** — real-time client↔server channel, with *topics* per conversation.

### Common response codes (REST)

| Code | Meaning |
|--------|-------------|
| `200 OK` | Request processed successfully. |
| `201 Created` | Resource created. |
| `204 No Content` | Successful operation with no body. |
| `400 Bad Request` | Validation error. |
| `401 Unauthorized` | Unauthenticated user (validation with the Identity Service). |
| `403 Forbidden` | No permission (e.g. not a member of the team). |
| `404 Not Found` | Resource not found. |
| `500 Internal Server Error` | Unexpected error. |

---
