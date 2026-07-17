# Service Integration

This page documents the actual contract between `am-communication-service` and the external
services it talks to, verified against their live source on 2026-07-17
(`cc-identity-service`, `cc-teams-service`, `cc-users-players-service`, `am-notification-service`)
and against this service's own Feign clients (notification/Groq), plus the gaps that still block
full interoperability with the sibling microservices.

## Overview

All outbound calls go through a **Feign client** (`infrastructure/out/feign/`) implementing a
domain-level port, so the rest of the codebase depends on an interface, not on HTTP details. Each
target has its own `integrations.*.base-url` property and its own Feign timeout profile.

| Service | Direction | How it's called | What it's used for |
|---|---|---|---|
| `cc-identity-service` | Inbound (JWT) | Every authenticated REST/WebSocket request carries a JWT signed with a shared HS256 secret; this service validates it locally, it doesn't call identity per request. | Authenticating callers and resolving who they are. |
| `cc-users-players-service` | Outbound (optional) | `GET /internal/players/{id}/exists` via `UserServiceFeignClient`, toggle `USER_EXISTENCE_CHECK`. | Confirming a user exists before adding them to a chat. |
| `cc-teams-service` | Outbound (optional) | `GET /teams/{id}` via `TeamServiceFeignClient`, toggle `TEAM_EXISTENCE_CHECK`. | Confirming a team exists before creating a team chat. |
| `am-notification-service` | Outbound | `POST /api/notificaciones/mensajes` via `NotificationServiceFeignClient`, `X-Internal-Api-Key` header required. | Notifying a user when their support ticket changes level. |
| Groq (LLM API) | Outbound | `POST /chat/completions` via `GroqChatFeignClient`, bearer-token authenticated. | Generating the chatbot's replies in the support chain. |

The two existence checks (user/team) are the only calls that can be disabled per environment —
notifications and the chatbot are always active, since neither blocks a request the way an
existence check would (both are wrapped in best-effort error handling instead).

There is **no standalone audit service** anywhere in the `TECH-CUP-2026-INT` organization, and per
the team's own confirmation there never will be — `SUPPORT_TRANSITION` events are recorded with a
local `log.info` in `SupportChainOrchestrator` instead of a remote call. An earlier version of this
service called a phantom `audit-service` here; that integration has been removed entirely (see
[Removed: audit service](#removed-audit-service) below).

## Status snapshot

| Service | State | Notes |
|---|---|---|
| `cc-identity-service` | Implemented (`main`) | Owns auth/credentials only. JWT `sub` is the real user UUID; `role` (singular) claim present. No user-lookup endpoint — that's `cc-users-players-service`'s job. |
| `cc-users-players-service` | Implemented (`main`) | Source of truth for `userId` and profile data. Exposes `InternalPlayerController` specifically for service-to-service calls (existence, public profile, captaincy). |
| `cc-teams-service` | Implemented (`main`) | Exposes `TeamInfoController` at `/teams/{teamId}` (unauthenticated, service-to-service), built for `mk-tournament-service` and `cc-users-players-service` but usable by us the same way. |

## JWT compatibility

Both `cc-identity-service` and this service sign HS256 tokens with `jjwt`, and both read the
signing secret from the `JWT_SECRET` environment variable (`security.jwt.secret` here, `jwt.secret`
there) — **the values must match in every deployment**.

`cc-identity-service`'s `JwtUtil.generateToken` puts the real user UUID in `sub`, plus `email` and
a **singular** `role` claim (e.g. `"ORGANIZER"`, not a `roles` array). `JwtService.parse` here
handles both:

1. Caller id: `sub` is used directly when it's a UUID (the normal case with real
   `cc-identity-service` tokens today). A `userId` claim, then a deterministic UUID derived from a
   non-UUID `sub`, remain as fallbacks for tokens issued elsewhere that don't already put a UUID in
   `sub`.
2. Roles: a `roles` array claim is read if present; otherwise a singular `role` string claim (what
   `cc-identity-service` actually emits) is used as a one-element set. Absent both, the caller has
   no roles.

`iss`/`aud` stay unconfigured (`JWT_ISSUER`/`JWT_AUDIENCE` unset) since `cc-identity-service`
doesn't emit them.

### Role mapping

This service's moderation endpoints (`POST /reports/{id}/resolve`, and looking up another user's
chats via `GET /users/{id}/chats`) accept `MODERATOR` and `ORGANIZER` — this service's own
participant roles — plus `ADMIN`, since that's `cc-identity-service`'s administrative role
(`cc-identity-service`'s `UserRole` enum: `PLAYER`, `CAPTAIN`, `REFEREE`, `ORGANIZER`, `ADMIN`) and
it has no `MODERATOR` equivalent.

## Outbound calls: what this service expects

`UserServiceClient` / `TeamServiceClient` only need an existence check, used when creating a chat
(`CreateChatService`):

| Call | Method + path | Response shape |
|---|---|---|
| User exists | `GET /internal/players/{id}/exists` (cc-users-players-service) | Always `200` with `{"exists": boolean}` — never `404` |
| Team exists | `GET /teams/{id}` (cc-teams-service) | `200` (body ignored) if it exists, `404` if not |

Note the two checks have genuinely different response shapes, not just different paths: the player
check always answers `200` and puts the boolean in the body, while the team check uses the HTTP
status itself. `FeignUserServiceClient`/`FeignTeamServiceClient` each match their real endpoint's
convention rather than forcing both into one shape.

Neither call currently sends `Authorization` — both are unauthenticated, internal,
service-to-service endpoints on the other side. If either service starts requiring it, that's a
change on this side to make (propagate the caller's bearer token).

Both checks are enabled by default now that both real endpoints exist:

```yaml
integrations:
  user-service:
    existence-check-enabled: ${USER_EXISTENCE_CHECK:true}
  team-service:
    existence-check-enabled: ${TEAM_EXISTENCE_CHECK:true}
```

When disabled, every id is treated as existing and a warning is logged once at startup — useful if
either dependency becomes unreachable in some environment.

## Ports

- `cc-identity-service` dev default: **`8081`**.
- `cc-users-players-service` dev default: **`8084`**, with `server.servlet.context-path: /api/v1`
  — so its full base URL is `http://host:8084/api/v1`, and `USER_SERVICE_URL` here is set
  accordingly.
- `cc-teams-service` dev default: **`5622`**.

## Notifications

`SupportChainOrchestrator` notifies the ticket's requester whenever their ticket moves through the
[chain of responsibility](arquitectura.md#design-patterns) — a one-way, fire-and-forget call that
never blocks the transition on a rich response, and is wrapped so a failure here doesn't undo the
transition (which is already applied and logged by that point).

`am-notification-service` has no generic `POST /notifications`. Its actual code exposes
`POST /api/notificaciones/mensajes` (`ChatEventController`), accepting a `ChatMessageEvent{chatId,
senderId, senderName, recipientId, messagePreview, sentAt}` — their own comment marks this as a
*"contrato propuesto, pendiente de confirmar"* aimed at this service, with two open questions:
whether `recipientId` fans out per chat member for group chats, and whether the full message text
or a truncated preview is expected.

`FeignNotificationServiceClient` adapts to this today for our one current use (support-ticket
transitions, always a single recipient):

- `chatId` = the ticket's underlying chat id.
- `senderId` = `SupportBotIdentity.BOT_USER_ID`.
- `senderName` = a fixed `"TechCup Support"` — this service has no display-name concept anywhere in
  its domain (everything is UUID-keyed), so there's no real name to send. If this port is ever used
  for an actual named human's message, this needs revisiting.
- `messagePreview` = the transition detail string (e.g. `"ESCALATED: FAQ -> CHATBOT"`).

This sidesteps both open questions for *this* use case (never a fan-out, always a short string) but
doesn't resolve them for a general-purpose use — flag it to the notification team if this port ever
needs to carry a real chat message.

**Authentication**: this and every other service-to-service webhook on `am-notification-service`
(`SecurityConfig.SERVICE_TO_SERVICE_PATHS`) is guarded by `InternalApiKeyFilter`, requiring a
matching `X-Internal-Api-Key` header (role `SERVICIO_INTERNO`) — a request without it is rejected
with `401` before it reaches the controller, regardless of path. `NotificationFeignClientConfig`
adds this header from `integrations.notification-service.api-key` (env `INTERNAL_API_KEY`), the
same shared-secret env var name `am-notification-service` itself reads and that
`cc-identity-service`'s internal email-lookup endpoint also expects — a real org-wide convention,
not something specific to this pair of services. No default is set; an unconfigured key just means
notifications fail with `401` instead of succeeding, caught the same way any other notification
failure is.

## Removed: audit service

An earlier version of this integration called `POST /audit-events` on an `AUDIT_SERVICE_URL`
(default `http://localhost:8084`) that never corresponded to any real service — no `audit-service`
repository exists in the `TECH-CUP-2026-INT` organization, and the team has confirmed none is
planned. Two call sites (`CreateSupportTicketService.create()` and
`SupportChainOrchestrator`) called it; the one in `CreateSupportTicketService` had **no error
handling at all**, so every support-ticket creation failed outright until this was removed.

`AuditServiceClient`, `AuditServiceFeignClient`, `FeignAuditServiceClient`, and `AuditPayload` have
been deleted, along with the `integrations.audit-service` config block. `SupportChainOrchestrator`
now records each `SUPPORT_TRANSITION` with a local `log.info` instead.

(`cc-identity-service` and `cc-teams-service` each have their own internal `AuditController` for
querying *their own* security/audit trail — logins, role changes, etc. Those are admin-only query
endpoints on each service's own data, not an ingestion endpoint this service could push events
into, so they don't substitute for what was removed here.)

## Chatbot (Groq)

The chatbot step of the support chain (`ChatbotSupportHandler`) talks to the **Groq** chat
completions API through `GroqChatFeignClient` → `FeignGroqChatbotClient`, which implements the
`ChatbotClient` port. The prompt sent on each call is assembled by the
[`SupportPromptBuilder`](arquitectura.md#design-patterns) from the ticket's subject and its recent
message history.

- **Base URL**: `integrations.groq.base-url` (Groq's public OpenAI-compatible endpoint).
- **Auth**: a `RequestInterceptor`, scoped to this client only (not a global bean, precisely so it
  doesn't leak onto the identity/teams/notification clients), adds
  `Authorization: Bearer <GROQ_API_KEY>` from `GroqProperties`. The API key has no default — the
  application won't start without a real one configured.
- **Model and system prompt**: configurable via `integrations.groq.model` and
  `integrations.groq.system-prompt`, so the persona/behavior of the assistant can be tuned without
  a code change.
- **Timeout**: Groq gets its own Feign profile (`feign.client.config.groq-chatbot.read-timeout:
  30000`, 30s) instead of the 5s default used for the plain existence checks, since a chat
  completion legitimately takes longer than a lookup.
- **Failure handling**: if the call raises an `IntegrationException`, the handler doesn't fail the
  ticket — it posts a fallback message to the user and escalates straight to a human moderator, so
  an AI outage degrades gracefully into "you'll talk to a person" rather than an error.
