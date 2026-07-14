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


## Design patterns


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

## UML and architecture diagrams

### Class diagram

Domain entities of the service (chat, messaging, moderation, support tickets and the FAQ
knowledge base) with their invariant-enforcing methods — mutation happens through these
methods, never through setters.

![Class diagram](assets/diagrams/DiagramaDeClases.png)
