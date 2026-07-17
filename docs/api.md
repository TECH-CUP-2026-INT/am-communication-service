# API

The service exposes **two interfaces** with distinct purposes:

- **REST API** — management operations (chats, messages, support tickets, moderation, FAQs) and
  inter-service communication.
- **WebSocket (STOMP)** — real-time client↔server channel, with one *topic* per conversation.

Both are documented with **springdoc-openapi**: once the app is running, the interactive Swagger UI
is available at `/swagger-ui.html` (raw spec at `/v3/api-docs`). Every endpoint below is described
exactly as it appears there — same summaries, same request/response examples — just grouped by
resource and written out in plain language.

Every REST endpoint except the FAQ ones requires a `Bearer` JWT (`Authorization: Bearer <token>`),
declared via the `Bearer Authentication` security scheme in the OpenAPI spec. FAQs are public reads
because they're meant to be answerable by the chatbot before a user is necessarily authenticated in
the support flow.

---

## Chats

Base path `/chats`. Covers creating a chat, reading it back, paging through its messages, and
closing it once it's done.

| Method & path | What it does |
|---|---|
| `POST /chats` | Creates a chat between the given participants (`DIRECT` chat between two users, or a team chat via `teamId`). Returns the new chat with a `Location` header pointing at it. |
| `GET /chats/{id}` | Returns a chat's details, but only if the caller is one of its participants. |
| `GET /chats/{id}/messages` | Returns the chat's messages, paginated (`page`, `size`, default `0`/`20`) and sorted oldest-first by `sentAt`. |
| `POST /chats/{id}/close` | Marks the chat as closed — no further messages can be sent to it afterward. |

## Messages

Base path `/messages`. Sending a message to an existing chat, and flagging one for moderation.

| Method & path | What it does |
|---|---|
| `POST /messages` | Sends a message to a chat the caller belongs to. |
| `POST /messages/{id}/report` | Reports a message with a reason, opening a moderation case for it. |

## Support tickets

Base path `/support/tickets`. This is the entry point into the [support chain of
responsibility](arquitectura.md#design-patterns): a ticket starts at the FAQ level and moves up
through the chatbot, a moderator, and finally an organizer as it gets escalated.

| Method & path | What it does |
|---|---|
| `POST /support/tickets` | Opens a new support ticket for the authenticated user with a subject; runs the chain's automated stage immediately. |
| `POST /support/tickets/{id}/reply` | Adds a message to an existing ticket's conversation. |
| `POST /support/tickets/{id}/escalate` | Manually escalates the ticket to the next level in the chain. |

Creating a ticket only needs a subject — everything else (FAQ matching, chatbot reply, escalation)
happens server-side through the handler chain:

## Reports (moderation)

Base path `/reports`. Where a `MODERATOR`, `ORGANIZER`, or `ADMIN` resolves a reported message.

| Method & path | What it does |
|---|---|
| `POST /reports/{id}/resolve` | Resolves a report by setting a resolution status, a moderator note, and an action to take on the message (e.g. delete it). |


## FAQs

Base path `/faqs`. Full CRUD over the frequently-asked-question entries the `FaqSupportHandler`
matches against — the only resource in this API that isn't behind authentication, since the
chatbot's FAQ step needs to be reachable before/without a session.

| Method & path | What it does |
|---|---|
| `POST /faqs` | Creates a FAQ with a list of matching keywords and an answer. |
| `GET /faqs` | Lists every FAQ in the system. |
| `GET /faqs/{id}` | Returns one FAQ by id. |
| `PUT /faqs/{id}` | Updates a FAQ's keywords and/or answer. |
| `DELETE /faqs/{id}` | Deletes a FAQ. |


## Users

Base path `/users`.

| Method & path | What it does |
|---|---|
| `GET /users/{id}/chats` | Lists all chats a user is part of. |

---

## WebSocket (STOMP)

The real-time channel is documented in the same Swagger UI, under the `WebSocket Chat` and
`Support WebSocket` tags, even though it isn't plain REST. The client opens a STOMP connection
carrying its JWT, subscribes to a conversation's topic, and sends messages by publishing to an
`/app/...` destination:

| STOMP destination | What it does |
|---|---|
| `/app/chat.send` | Sends a chat message; validated, traced, and forwarded to the chat service, then broadcast to `/topic/conversacion/{id}`. |
| `/app/support.send` | Sends a message on an existing support ticket, forwarded to the corresponding ticket. |
---

## Common response codes (REST)

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
