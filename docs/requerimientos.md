# Requirements

The functional requirements come from the project backlog (Astro Merge). The non-functional
requirements are derived from the integrations between microservices and from the
architecture decisions already made.

    The functional requirements listed come from the **Jira backlog** shared by the
    team. **No** backlog file, *issues*, or user stories were found
    within the repository (`git log` with a single commit and no backlog files).
    **To be completed:** link the Jira board or export the user stories
    to the repository so they can be expanded and traced here.

## Functional requirements

| ID | Requirement | Description | Status |
|----|-------------|-------------|--------|
| RF-01 | Chatbot-assisted help and support chat | A user can start a support conversation handled by a chatbot. | Planned |
| RF-02 | Escalation to the organizer | A support conversation can be escalated to a human organizer when the chatbot does not resolve it. | Planned |
| RF-03 | Direct messaging between players | Two players can exchange direct messages in real time. | Planned |
| RF-04 | Moderation of reported messages | A message can be reported and reviewed by a moderator, who records a resolution. | Planned |
| RF-05 | Team group chat | Group chat associated with a team (`equipo_id`); requires validating membership. | Derived from the model/integrations |

!!! info "RF-05 — inferred"
    The **group** conversation type with `equipo_id` (data model) and the integration with
    the **Teams Service** ("validate membership before allowing group chat") imply a
    team group chat use case, even though it does not appear explicitly in the shared
    backlog list. **To be completed:** confirm this story with the product team.

## Non-functional requirements

| ID | Category | Requirement |
|----|----------|-------------|
| RNF-01 | Performance | Real-time messaging must have low latency via WebSocket (STOMP), avoiding *polling*. |
| RNF-02 | Integrity | Persistence must guarantee referential integrity between conversations, messages, and reports (relational PostgreSQL). |
| RNF-03 | Scalability | Connection state is kept in memory; the business logic must be able to scale horizontally. |
| RNF-04 | Integration — Identity | Validate that the user is authenticated (Identity Service) before participating in a chat. |
| RNF-05 | Integration — Teams | Validate team membership (Teams Service) before allowing group chat. |
| RNF-06 | Integration — Notifications | Trigger a notification (Notifications Service) when a new message arrives. |
| RNF-07 | Integration — Auditing | Report chat creation and moderation reports to the Auditing Service. |
| RNF-08 | Portability | The service must be able to run in Docker containers. |
| RNF-09 | Observability | *To be completed: define required metrics, logs, and traceability.* |
| RNF-10 | Security | *To be completed: define TLS, CORS, and the WebSocket channel's authorization policy.* |

## Main use cases

!!! note "Use case diagram"
    Add the UML use case diagram here at `docs/assets/diagrams/casos-de-uso.png`.
    <!-- ![Use case diagram](assets/diagrams/casos-de-uso.png) -->

### CU-01 — Support chat with escalation

| Field | Description |
|-------|-------------|
| **Actor** | User (player) · Chatbot · Organizer |
| **Precondition** | The user is authenticated (validated with the Identity Service). |
| **Main flow** | 1. The user opens a support conversation. 2. The chatbot responds. 3. If it does not resolve, the conversation is **escalated** to the organizer. 4. The organizer continues the conversation in real time. |
| **Postcondition** | The conversation and its messages are persisted; participants are notified. |
| **Exceptions** | Unauthenticated user → rejection; no organizer available → *policy to be defined*. |

### CU-02 — Direct messaging between players

| Field | Description |
|-------|-------------|
| **Actor** | Sending player · Receiving player |
| **Precondition** | Both users authenticated. |
| **Main flow** | 1. The sender sends a message. 2. It is validated and persisted. 3. It is delivered in real time to the receiver subscribed to the *topic*. 4. A notification is triggered. |
| **Postcondition** | Message stored and delivered. |

### CU-03 — Moderation of a reported message

| Field | Description |
|-------|-------------|
| **Actor** | Reporting user · Moderator |
| **Precondition** | The message to be reported exists. |
| **Main flow** | 1. A user reports a message stating the reason. 2. A **Report** is created. 3. A moderator reviews it and records the resolution. 4. The event is reported to the Auditing Service. |
| **Postcondition** | The report is recorded with its resolution. |

### CU-04 — Team group chat *(inferred)*

| Field | Description |
|-------|-------------|
| **Actor** | Players who are members of a team |
| **Precondition** | The user is a member of the team (validated with the Teams Service). |
| **Main flow** | 1. Membership is validated. 2. The player subscribes to the group chat *topic*. 3. Real-time message exchange. |
| **Postcondition** | Messages persisted and delivered to connected members. |

    Document the acceptance criteria of each use case based on the Jira
    stories.
