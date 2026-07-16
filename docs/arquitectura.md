# Architecture

## System architecture

The Communications Service is an independent microservice within the **Astro Merge**
platform. Internally it follows a **layered architecture** (controller → service →
repository) and exposes **two clearly differentiated entry channels**:

- **WebSocket (STOMP):** real-time **client↔server** channel for exchanging messages.
- **REST API:** management operations (history, moderation) and **communication with other microservices**.


## Architecture decisions

### Why WebSocket?

- **Real time without *polling*.** Chat requires immediate message delivery. With REST, the
  client would have to *poll* the server repeatedly, increasing latency and
  load. WebSocket keeps a persistent bidirectional connection and **pushes** messages
  to subscribed clients as soon as they occur.
- **STOMP over WebSocket** provides a **publish/subscribe** model with destinations and
  *topics*, which allows naturally modeling **one *topic* per conversation** (group chat,
  support conversation, direct messaging) and delivering each message only to the subscribers
  of that conversation.
- **Bounded scope:** WebSocket is **exclusively** the client↔server channel. Communication
  between microservices does **not** use WebSocket (see below).

### Why PostgreSQL and not NoSQL?

- **Referential integrity.** The domain has clear relationships: a **Message** belongs to
  a **Conversation**, and a **Report** references a **Message**. PostgreSQL's foreign keys
  and constraints guarantee this integrity natively.
- **Volume that does not justify NoSQL.** The system's messaging write volume does not
  reach the scale at which a NoSQL database would provide decisive advantages; the simplicity
  and transactional guarantees of a relational engine weigh more.
- **Relational queries.** Paginating a conversation's history, filtering reports by
  status, or joining messages with their reports is expressed directly in SQL.

### Inter-service communication: API / events

Communication with sibling microservices — `cc-identity-service`, `cc-teams-service`,
`am-notification-service`, and the audit service — happens exclusively over **REST via
Spring Cloud OpenFeign** declarative clients, never over the WebSocket channel. Each client has
its own base URL (`integrations.*.base-url`, see [Configuration](configuracion.md)) and its own
Feign timeout profile (`feign.client.config`), since a chat-completion call to the Groq-backed
chatbot takes noticeably longer than a plain existence check against identity/teams.

Existence checks (user/team) can be toggled independently per environment
(`USER_EXISTENCE_CHECK` / `TEAM_EXISTENCE_CHECK`), so this service is never hard-blocked by a
sibling service that hasn't shipped its endpoint yet. The full contract — current gaps, JWT
claim compatibility, and what each team still needs to expose — is tracked in
[Service Integration](integracion-servicios.md).

## Design patterns

- **Layered architecture** (`controller` → `service` → `repository`): each layer only talks to
  the one directly below it; controllers never touch repositories directly.
- **DTO + Mapper** (MapStruct): entities never cross the API boundary directly. Mappers in
  `mapper/` translate between JPA entities and the DTOs exposed by REST/WebSocket controllers,
  keeping persistence details out of the public contract.
- **Repository pattern** (Spring Data JPA): data access is expressed through repository
  interfaces in `repository/`, with query derivation/JPQL instead of hand-written SQL.
- **Centralized exception handling**: cross-cutting error handling lives in `exception/`
  (`@RestControllerAdvice`-style translation of domain/validation errors into consistent HTTP
  responses), instead of try/catch blocks scattered across controllers.
- **Rich domain entities**: entities enforce their own invariants through behavior methods
  (see the [class diagram](#class-diagram)) rather than exposing plain setters, so state changes
  stay consistent with the domain rules.

## Components

```text
communications/
├── config/       # WebSocket/STOMP configuration, CORS, beans
├── controller/   # REST and WebSocket (STOMP) controllers
├── dto/          # Data transfer objects
├── entity/       # JPA entities (Conversacion, Mensaje, Reporte)
├── exception/    # Exceptions and centralized error handling
├── mapper/       # Entity <-> DTO conversion
├── repository/   # Data access (Spring Data JPA)
└── service/      # Business logic and integration with other services
```

## General flow

End-to-end path of a direct/group message:

1. The client opens a **STOMP over WebSocket** connection carrying its JWT.
2. A JWT filter validates the token and resolves the caller's identity (see
   [Service Integration](integracion-servicios.md#jwt-compatibility) for the exact claim
   resolution rules).
3. The client subscribes to the conversation's *topic* (`/topic/conversacion/{id}`).
4. The corresponding **service** validates the request (e.g. team membership via
   `cc-teams-service` when `TEAM_EXISTENCE_CHECK` is enabled) and persists the message through
   the **repository** layer.
5. The message is broadcast in real time to every client subscribed to that *topic*.
6. The service asynchronously calls `am-notification-service` (Feign) to trigger a notification,
   and reports the event to the audit service.
7. Moderation follows the same pattern for reported messages: a **Report** is created, reviewed
   by a moderator, and its resolution is persisted and reported to auditing.

## UML and architecture diagrams

### Class diagram

Domain entities of the service (chat, messaging, moderation, support tickets and the FAQ
knowledge base) with their invariant-enforcing methods — mutation happens through these
methods, never through setters.

![Class diagram](assets/diagrams/DiagramaDeClases.png)
