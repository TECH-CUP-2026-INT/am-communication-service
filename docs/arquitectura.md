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

Communication with sibling microservices — `cc-users-players-service`, `cc-teams-service`, and
`am-notification-service` — happens exclusively over **REST via Spring Cloud OpenFeign**
declarative clients, never over the WebSocket channel. Each client has its own base URL
(`integrations.*.base-url`, see [Configuration](configuracion.md)) and its own Feign timeout
profile (`feign.client.config`), since a chat-completion call to the Groq-backed chatbot takes
noticeably longer than a plain existence check against users/teams.

Existence checks (user/team) can be toggled independently per environment
(`USER_EXISTENCE_CHECK` / `TEAM_EXISTENCE_CHECK`), so this service is never hard-blocked by a
sibling service that hasn't shipped its endpoint yet — both currently default to enabled, since
both real endpoints exist. There is no standalone audit service in the organization; support-ticket
transitions are logged locally instead of pushed to one. The full contract — JWT claim
compatibility and what each team exposes — is tracked in
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
- **Builder** (`SupportPromptBuilder`, `domain/service/support/`): assembles the prompt sent to
  the chatbot with a small fluent API — `.withSubject(...)`, `.withHistory(...)`, `.build()` — so
  `ChatbotSupportHandler` doesn't hand-concatenate strings. `build()` starts from `"Ticket
  subject: <subject>"` and, if there's prior conversation, appends a `"Conversation so far:"`
  block with one line per message, labeled `Assistant`/`User` by comparing the sender id against
  `SupportBotIdentity.BOT_USER_ID`. The last 10 messages of the ticket (`HISTORY_LIMIT`) are fed
  in, and the resulting string is what actually gets sent to Groq via `ChatbotClient.generateReply`.
  Keeping this in a builder means the prompt format can change in one place without touching the
  handler's control flow.
- **Chain of Responsibility** (support ticket escalation, `domain/service/support/`): each support
  level is a handler that either resolves the ticket or passes it to the next level. The chain,
  wired in `infrastructure/config/SupportChainConfig`, follows the order defined by `SupportLevel`
  — `FAQ → CHATBOT → MODERATOR → ORGANIZER → PENDING`:
    1. **`FaqSupportHandler`** (FAQ) — looks up a matching `Faq` by keyword and posts its answer;
       stays open at this level so the user can escalate manually if it didn't help.
    2. **`ChatbotSupportHandler`** (CHATBOT) — builds a prompt with `SupportPromptBuilder`, calls
       the Groq-backed chatbot, and always escalates to MODERATOR afterwards (with a fallback
       message if the AI call fails).
    3. **`ModeratorSupportHandler`** (MODERATOR) — human tier; escalates straight to ORGANIZER.
    4. **`OrganizerSupportHandler`** (ORGANIZER) — last human level; marks the ticket as pending
       once handled.

    `AbstractSupportHandler` implements the shared chain-walking logic (`canHandle` / delegate to
    `next`), so each concrete handler only implements `level()` and `doHandle()`.
    `SupportChainOrchestrator` holds the chain's head bean (`supportChainHead`) and is the only
    entry point application services use to run or resume the chain — extending it later is, per
    the config class's own javadoc, "a matter of inserting another handler in this single place."

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
6. Moderation follows the same pattern for reported messages: a **Report** is created, reviewed
   by a moderator, and its resolution is persisted.

`am-notification-service` is only called from the [support chain](#design-patterns) today — when a
support ticket changes level, not for every regular chat message.

## UML and architecture diagrams

### Class diagram

Domain entities of the service (chat, messaging, moderation, support tickets and the FAQ
knowledge base) with their invariant-enforcing methods — mutation happens through these
methods, never through setters.

![Class diagram](assets/diagrams/DiagramaDeClases.png)

### Controller class diagram

The eight adapters in `infrastructure/in/rest/controller` are the service's inbound edge. Six are
REST controllers (`@RestController`, each implementing its `*ControllerSwagger` contract) and two
are STOMP/WebSocket controllers (`@Controller`). None of them touch repositories or domain logic
directly: they depend only on **inbound use-case ports** (`domain/service/ports/in`) and on
MapStruct mappers to translate between DTOs and domain models, honouring the layered architecture
described above.

```mermaid
classDiagram
    direction LR

    class ChatController {
        +create(CreateChatRequest) ChatResponse
        +getById(UUID) ChatResponse
        +getMessages(UUID, page, size) PageResponse~MessageResponse~
        +close(UUID) ChatResponse
    }
    class MessageController {
        +send(SendMessageRequest) MessageResponse
        +report(UUID, ReportMessageRequest) ReportedMessageResponse
    }
    class ReportController {
        +resolve(UUID, ResolveReportRequest) ReportedMessageResponse
    }
    class SupportController {
        +create(CreateSupportTicketRequest) SupportTicketResponse
        +reply(UUID, ReplySupportTicketRequest) MessageResponse
        +escalate(UUID) SupportTicketResponse
    }
    class FaqController {
        +create(FaqRequest) FaqResponse
        +list() List~FaqResponse~
        +getById(UUID) FaqResponse
        +update(UUID, FaqRequest) FaqResponse
        +delete(UUID) void
    }
    class UserController {
        +getUserChats(UUID) List~ChatResponse~
    }
    class ChatWsController {
        +send(SendMessageRequest, AuthenticatedUser) void
    }
    class SupportWsController {
        +send(SupportSendRequest, AuthenticatedUser) void
    }

    class CreateChatUseCase {
        <<interface>>
    }
    class GetChatUseCase {
        <<interface>>
    }
    class GetChatMessagesUseCase {
        <<interface>>
    }
    class CloseChatUseCase {
        <<interface>>
    }
    class SendMessageUseCase {
        <<interface>>
    }
    class ReportMessageUseCase {
        <<interface>>
    }
    class ResolveReportUseCase {
        <<interface>>
    }
    class CreateSupportTicketUseCase {
        <<interface>>
    }
    class ReplySupportTicketUseCase {
        <<interface>>
    }
    class EscalateConversationUseCase {
        <<interface>>
    }
    class CreateFaqUseCase {
        <<interface>>
    }
    class UpdateFaqUseCase {
        <<interface>>
    }
    class DeleteFaqUseCase {
        <<interface>>
    }
    class GetFaqUseCase {
        <<interface>>
    }
    class ListFaqsUseCase {
        <<interface>>
    }
    class GetUserChatsUseCase {
        <<interface>>
    }

    ChatController ..> CreateChatUseCase
    ChatController ..> GetChatUseCase
    ChatController ..> GetChatMessagesUseCase
    ChatController ..> CloseChatUseCase
    MessageController ..> SendMessageUseCase
    MessageController ..> ReportMessageUseCase
    ReportController ..> ResolveReportUseCase
    SupportController ..> CreateSupportTicketUseCase
    SupportController ..> ReplySupportTicketUseCase
    SupportController ..> EscalateConversationUseCase
    FaqController ..> CreateFaqUseCase
    FaqController ..> UpdateFaqUseCase
    FaqController ..> DeleteFaqUseCase
    FaqController ..> GetFaqUseCase
    FaqController ..> ListFaqsUseCase
    UserController ..> GetUserChatsUseCase
    ChatWsController ..> SendMessageUseCase
    SupportWsController ..> ReplySupportTicketUseCase
```

### Component diagram

Runtime view of how the inbound adapters, the application/domain core, the outbound ports and the
sibling services fit together. Everything in the core talks to the outside world only through
ports: repositories (Spring Data JPA), the `MessagePublisher` that pushes to STOMP topics, and the
Feign clients that reach the sibling microservices and the Groq-backed chatbot.

```mermaid
flowchart LR
    Client["Client / Frontend"]

    subgraph CS["am-communication-service"]
        direction TB
        WS["WebSocket / STOMP controllers<br/>ChatWsController · SupportWsController"]
        REST["REST controllers<br/>Chat · Message · Report · Support · Faq · User"]
        UC["Application use cases<br/>(application/usecase)"]
        DOM["Domain model + support chain<br/>(Chain of Responsibility)"]
        REPO["Repositories (Spring Data JPA)"]
        PUB["MessagePublisher<br/>(WebSocketMessagePublisher)"]
        FEIGN["Feign clients<br/>(infrastructure/out/feign)"]
    end

    DB[("PostgreSQL")]
    USERS["cc-users-players-service"]
    TEAMS["cc-teams-service"]
    NOTIF["am-notification-service"]
    GROQ["Groq Chatbot API"]

    Client -->|"STOMP over WebSocket (JWT)"| WS
    Client -->|"HTTPS REST (JWT)"| REST
    WS --> UC
    REST --> UC
    UC --> DOM
    UC --> REPO
    UC --> PUB
    UC --> FEIGN
    REPO --> DB
    PUB -->|"/topic/**"| Client
    FEIGN --> USERS
    FEIGN --> TEAMS
    FEIGN --> NOTIF
    FEIGN --> GROQ
```

### Controller sequence diagrams

The following diagrams trace the end-to-end flow of each controller endpoint, from the inbound
adapter through the use-case port down to the domain, persistence and (where relevant) the sibling
services. `JwtAuthenticationFilter` / `WsAuthChannelInterceptor` have already resolved the caller
into an `AuthenticatedUser` before any handler below runs.

#### ChatController — create a chat (`POST /chats`)

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant CC as ChatController
    participant UC as CreateChatService
    participant U as UserServiceClient (Feign)
    participant T as TeamServiceClient (Feign)
    participant D as Chat (domain)
    participant R as ChatRepository

    Client->>CC: POST /chats (CreateChatRequest)
    CC->>UC: create(CreateChatCommand)
    loop each participant
        UC->>U: exists(userId)
        U-->>UC: true / false
    end
    alt teamId present
        UC->>T: exists(teamId)
        T-->>UC: true / false
    end
    UC->>D: Chat.create(type, teamId) + addParticipant(...)
    UC->>R: save(chat)
    R-->>UC: persisted Chat
    UC-->>CC: Chat
    CC-->>Client: 201 Created + ChatResponse (Location header)
```

#### ChatController — read chat, history and close

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant CC as ChatController
    participant UC as Get/CloseChat services
    participant R as ChatRepository / MessageRepository

    Client->>CC: GET /chats/{id}
    CC->>UC: getById(id, caller.userId)
    UC->>R: findById(id) + access check
    R-->>UC: Chat
    UC-->>CC: Chat
    CC-->>Client: 200 OK + ChatResponse

    Client->>CC: GET /chats/{id}/messages?page&size
    CC->>UC: getByChat(id, pageable, caller.userId)
    UC->>R: findByChatId(id, pageable)
    R-->>UC: Page<Message>
    UC-->>CC: Page<Message>
    CC-->>Client: 200 OK + PageResponse<MessageResponse>

    Client->>CC: POST /chats/{id}/close
    CC->>UC: close(id, caller.userId)
    UC->>R: findById + chat.close() + save
    UC-->>CC: Chat
    CC-->>Client: 200 OK + ChatResponse
```

#### MessageController / ChatWsController — send a message

Both the REST endpoint and the STOMP endpoint funnel into the same `SendMessageUseCase`. The
WebSocket path additionally records a metric and a trace span; the domain enforces that the chat is
open and the sender participates, and the message is **persisted before** it is published to the
topic.

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant WS as ChatWsController
    participant MC as MessageController
    participant M as WebSocketMetrics / Tracer
    participant UC as SendMessageService
    participant CR as ChatRepository
    participant D as Chat (domain)
    participant MR as MessageRepository
    participant P as MessagePublisher

    alt WebSocket (STOMP)
        Client->>WS: SEND /app/chat.send (SendMessageRequest)
        WS->>M: recordChatMessageReceived() + start span
        WS->>UC: send(SendMessageCommand)
    else REST
        Client->>MC: POST /messages (SendMessageRequest)
        MC->>UC: send(SendMessageCommand)
    end
    UC->>CR: findById(chatId)
    CR-->>UC: Chat
    UC->>D: chat.postMessage(senderId, content)
    D-->>UC: Message
    UC->>MR: save(message)
    MR-->>UC: saved Message
    UC->>P: publishChatMessage(saved)
    P-->>Client: /topic/... (real-time broadcast)
    UC-->>MC: Message
    MC-->>Client: 201 Created + MessageResponse
```

#### MessageController — report a message (`POST /messages/{id}/report`)

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant MC as MessageController
    participant UC as ReportMessageService
    participant MR as MessageRepository
    participant RR as ReportedMessageRepository
    participant D as Message (domain)

    Client->>MC: POST /messages/{id}/report (ReportMessageRequest)
    MC->>UC: report(ReportMessageCommand)
    UC->>MR: findById(messageId)
    MR-->>UC: Message
    alt message DELETED
        UC-->>MC: InvalidChatOperationException
    else already reported by caller
        UC->>RR: existsByMessageIdAndReporterId(...)
        RR-->>UC: true
        UC-->>MC: MessageAlreadyReportedException
    else new report
        UC->>RR: save(ReportedMessage.create(...))
        UC->>D: message.markReported() (if SENT)
        UC->>MR: save(message)
        UC-->>MC: ReportedMessage
        MC-->>Client: 201 Created + ReportedMessageResponse
    end
```

#### ReportController — resolve a report (`POST /reports/{id}/resolve`)

```mermaid
sequenceDiagram
    autonumber
    actor Moderator
    participant RC as ReportController
    participant UC as ResolveReportService
    participant RR as ReportedMessageRepository
    participant MA as ModeratorActionRepository
    participant MR as MessageRepository
    participant D as ReportedMessage / Message (domain)

    Moderator->>RC: POST /reports/{id}/resolve (ResolveReportRequest)
    RC->>UC: resolve(ResolveReportCommand)
    UC->>RR: findById(reportId)
    RR-->>UC: ReportedMessage
    Note over UC,D: domain: PENDING -> terminal
    UC->>D: report.resolve(status, note)
    UC->>MA: save(ModeratorAction.of(...))
    alt actionType == DELETE_MESSAGE
        UC->>D: report.getMessage().markDeleted()
        UC->>MR: save(message)
    end
    UC->>RR: save(report)
    UC-->>RC: ReportedMessage
    RC-->>Moderator: 200 OK + ReportedMessageResponse
```

#### SupportController — create a ticket & run the automated chain (`POST /support/tickets`)

Creating a ticket opens a `SUPPORT` chat (requester + support bot), persists the ticket and runs
the automated stage of the **Chain of Responsibility**. Each level transition is logged locally and
best-effort notified through `am-notification-service`; a notification failure never rolls back an
applied transition.

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant SC as SupportController
    participant UC as CreateSupportTicketService
    participant CR as ChatRepository
    participant TR as SupportTicketRepository
    participant O as SupportChainOrchestrator
    participant H as Support chain (FAQ→CHATBOT→...)
    participant G as Groq Chatbot (Feign)
    participant N as NotificationServiceClient (Feign)

    Client->>SC: POST /support/tickets (CreateSupportTicketRequest)
    SC->>UC: create(CreateSupportTicketCommand)
    UC->>CR: save(Chat.create(SUPPORT) + participants)
    UC->>TR: save(SupportTicket.open(...))
    UC->>O: runAutomatedStage(ticket)
    O->>H: supportChainHead.handle(ticket)
    H->>G: generateReply(prompt) [ChatbotSupportHandler]
    G-->>H: AI answer (or fallback)
    H-->>O: SupportResult
    O->>N: notify(chatId, requesterId, detail)
    N-->>O: ack [failure logged, not fatal]
    O-->>UC: SupportResult
    UC->>TR: save(ticket)
    UC-->>SC: SupportTicket
    SC-->>Client: 201 Created + SupportTicketResponse
```

#### SupportController / SupportWsController — reply and escalate

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant WS as SupportWsController
    participant SC as SupportController
    participant RU as ReplySupportTicketService
    participant EU as EscalateConversationService
    participant O as SupportChainOrchestrator

    alt reply over WebSocket
        Client->>WS: SEND /app/support.send (SupportSendRequest)
        WS->>RU: reply(ReplySupportTicketCommand)
    else reply over REST
        Client->>SC: POST /support/tickets/{id}/reply
        SC->>RU: reply(ReplySupportTicketCommand)
    end
    RU-->>SC: Message
    SC-->>Client: 201 Created + MessageResponse

    Client->>SC: POST /support/tickets/{id}/escalate
    SC->>EU: escalate(id, caller.userId)
    Note over EU,O: FAQ -> CHATBOT, then automated stage
    EU->>O: escalate(ticket)
    O-->>EU: SupportResult
    EU-->>SC: SupportTicket
    SC-->>Client: 200 OK + SupportTicketResponse
```

#### FaqController — FAQ knowledge-base CRUD

```mermaid
sequenceDiagram
    autonumber
    actor Admin
    participant FC as FaqController
    participant UC as Faq use cases
    participant R as FaqRepository

    Admin->>FC: POST /faqs (FaqRequest)
    FC->>UC: create(CreateFaqCommand)
    UC->>R: save(Faq)
    FC-->>Admin: 201 Created + FaqResponse (Location)

    Admin->>FC: GET /faqs
    FC->>UC: listAll()
    UC->>R: findAll()
    FC-->>Admin: 200 OK + List<FaqResponse>

    Admin->>FC: GET /faqs/{id}
    FC->>UC: getById(id)
    FC-->>Admin: 200 OK + FaqResponse

    Admin->>FC: PUT /faqs/{id} (FaqRequest)
    FC->>UC: update(UpdateFaqCommand)
    UC->>R: save(updated Faq)
    FC-->>Admin: 200 OK + FaqResponse

    Admin->>FC: DELETE /faqs/{id}
    FC->>UC: delete(id)
    UC->>R: deleteById(id)
    FC-->>Admin: 204 No Content
```

#### UserController — list a user's chats (`GET /users/{id}/chats`)

The caller may list their own chats; `MODERATOR`, `ORGANIZER` and `ADMIN` may look up anyone's,
which the controller enforces before delegating.

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant UC as UserController
    participant S as GetUserChatsService
    participant R as ChatRepository

    Client->>UC: GET /users/{id}/chats
    alt id != caller and caller lacks MODERATOR/ORGANIZER/ADMIN
        UC-->>Client: 403 UserAccessNotAllowedException
    else authorized
        UC->>S: getByUser(id)
        S->>R: findByParticipantUserId(id)
        R-->>S: List<Chat>
        S-->>UC: List<Chat>
        UC-->>Client: 200 OK + List<ChatResponse>
    end
```
