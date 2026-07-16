# Service Integration

This page documents the actual contract between `am-communication-service` and two sibling
microservices — `cc-identity-service` and `cc-teams-service` — as verified against their
source on 2026-07-12, and the gaps that still block full interoperability.

## Status snapshot

| Service | State | Blocking gap |
|---|---|---|
| `cc-identity-service` | Implemented (branch `develop`; `main` is an empty skeleton) | JWT lacks `userId`/`username`/`roles` claims; no user-lookup endpoint |
| `cc-teams-service` | Empty skeleton (no controllers, entities, or config on any branch) | No endpoints at all yet |

Until both gaps close, this service runs with mitigations described below so it isn't blocked
on dependencies that don't exist yet.

## JWT compatibility

Both services sign HS256 tokens with `jjwt`, and both read the signing secret from the `JWT_SECRET`
environment variable (`security.jwt.secret` here, `app.jwt.secret` there) — **the values must
match in every deployment**, but the algorithm and the env var name already line up.

`cc-identity-service` currently only puts `sub` (the user's **email**, not a UUID), `iat` and
`exp` in the token — no `username`, no `roles`, no `iss`/`aud`. This service's domain keys
everything (chat participants, message senders) by UUID, so `JwtService` resolves the caller id
with a fallback chain instead of requiring `sub` to already be a UUID:

1. `sub` is a UUID → used as-is (unchanged behavior for well-formed tokens).
2. Otherwise, a `userId` claim (UUID string) → used if present.
3. Otherwise, a UUID derived deterministically from the (non-blank) `sub` via
   `UUID.nameUUIDFromBytes` — the same subject always maps to the same id.
4. A blank/absent `sub` is always rejected.

**This is a shim, not a real fix.** It lets `cc-identity-service` tokens authenticate today, but
the synthetic id has no relationship to any id `cc-identity-service` itself knows about. Once
that service issues a real `userId` claim, step 2 takes over automatically and this shim should
be retired — existing chats created under a synthetic id will not automatically merge with the
"real" id for the same user.

`roles`: absent claims already yield an empty role set (no change needed). `cc-identity-service`'s
roles (`USER`, `ADMIN`, `REFEREE`, `ORGANIZER`) live in its database, not in the token, so no role
reaches this service today regardless. `iss`/`aud` stay unconfigured (`JWT_ISSUER`/`JWT_AUDIENCE`
unset) since `cc-identity-service` doesn't emit them.

### Role mapping

This service's moderation endpoints (`POST /reports/{id}/resolve`, and looking up another user's
chats via `GET /users/{id}/chats`) accept `MODERATOR` and `ORGANIZER` — this service's own
participant roles — plus `ADMIN`, since that's `cc-identity-service`'s administrative role and it
has no `MODERATOR` equivalent.

## Outbound calls: what this service expects

`UserServiceClient` / `TeamServiceClient` only need an existence check, used when creating a chat
(`CreateChatService`):

| Call | Method + path | Success | Not found | Auth header sent |
|---|---|---|---|---|
| User exists | `GET /users/{id}` | any 2xx (body ignored) | `404` | none |
| Team exists | `GET /teams/{id}` | any 2xx (body ignored) | `404` | none |

Neither call currently sends `Authorization`. If either service starts requiring it, that's a
change on this side to make (propagate the caller's bearer token) — flag it when the contract is
agreed.

**Neither endpoint exists today**: `cc-identity-service` has no user-lookup route, and
`cc-teams-service` has no routes at all. Each check can be disabled independently so
`POST /chats` isn't blocked on a dependency that doesn't exist yet:

```yaml
integrations:
  user-service:
    existence-check-enabled: ${USER_EXISTENCE_CHECK:true}
  team-service:
    existence-check-enabled: ${TEAM_EXISTENCE_CHECK:true}
```

`docker-compose.yml` in this repo sets both to `false` by default (override with
`USER_EXISTENCE_CHECK=true` / `TEAM_EXISTENCE_CHECK=true` once the endpoints exist). When
disabled, every id is treated as existing and a warning is logged once at startup.

## Ports

- `cc-identity-service` dev default: **`11711`** (its own `application.yml` literally flags this
  as unconfirmed with other teams). This service's default `USER_SERVICE_URL` now points there.
- `cc-teams-service` has no `server.port` configured at all (Spring Boot default `8080` would
  apply as-is). This service's default `TEAM_SERVICE_URL` still assumes `8082` — confirm once
  `cc-teams-service` picks a port.

## What to ask the other teams for

**cc-identity-service:**
1. A `userId` (UUID) claim in the JWT — this is the one that actually unblocks removing the
   synthetic-id shim above.
2. `username` and `roles` claims, if this service is meant to authorize by role from the token
   alone rather than treating every caller as roleless.
3. A `GET /users/{id}` (or equivalent) lookup endpoint.
4. Confirmation that `11711` is the real, stable port across environments.

**cc-teams-service:**
1. A `GET /teams/{id}` lookup endpoint returning 200/404 (body not required by this service).
2. Its intended port, so `TEAM_SERVICE_URL`'s default can be corrected if needed.
3. Whether it will validate the same shared-secret HS256 JWT (recommended, for consistency with
   `cc-identity-service` and this service).
