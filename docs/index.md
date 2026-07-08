# Astro Merge — Communications Service

**Messaging** microservice of the **Astro Merge** system, a sports tournament
management platform built with a microservices architecture. This service concentrates
all communication between users: chatbot-assisted support chat, escalation to the
organizer, direct messaging between players, and moderation of reported messages.

---

## Description

`service-communications` (repository `am-comunication-service`) is the microservice
responsible for **communications** within Astro Merge. It is developed in **Java 21**
and **Spring Boot 3.x**, exposes real-time communication through **WebSocket (STOMP)** for
the client↔server channel, and persists information in **PostgreSQL**. Communication with the
rest of the microservices is done via **REST API / events**, separately from the WebSocket
channel.

!!! info "Project status"
    The repository is in an early phase: the base package structure
    (`controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `config`, `exception`)
    is defined, but the business logic is not yet implemented. Sections marked as
    **To be completed** must be updated as development progresses.

!!! warning "Stack ↔ code discrepancy"
    The chosen architecture uses **PostgreSQL**, but the current `pom.xml` still declares
    `spring-boot-starter-data-mongodb`. Before implementing persistence it must be replaced
    by `spring-boot-starter-data-jpa` + the PostgreSQL driver. See [Configuration](configuracion.md).

---

## Goal

Centralize the platform's messaging in a decoupled, scalable service that offers
low-latency real-time communication, guarantees the integrity of conversations and
their messages, and integrates with the rest of the microservices (teams, identity,
notifications, and auditing) via API/events.

---

## Technologies used

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Programming language |
| Spring Boot | 3.x | Application framework |
| Spring Web | — | REST API (client and inter-service communication) |
| Spring WebSocket + STOMP | — | Real-time client↔server messaging channel |
| Spring Data JPA | *(pending in `pom.xml`)* | Relational persistence |
| PostgreSQL | — | Relational database |
| Spring Validation | — | Input data validation |
| Lombok | — | Boilerplate code reduction |
| Maven | Wrapper (`mvnw`) | Dependency management and build |

---

## Main features

- **Chatbot-assisted support chat** for user help and assistance.
- **Escalation of support conversations** to the tournament organizer.
- **Direct messaging** between players.
- **Moderation** of reported messages.
- **Real time** via WebSocket (STOMP), with one *topic* per conversation.
- **Integration with other microservices** (teams, identity, notifications, auditing) via API/events.
- **Relational persistence** in PostgreSQL with referential integrity.

---

## Repository link

[:material-github: TECH-CUP-2026-INT/am-comunication-service](https://github.com/TECH-CUP-2026-INT/am-comunication-service){ .md-button .md-button--primary }

---

## Where to start?

- New to the project? Start with the [Introduction](introduccion.md).
- Looking to understand the design and technical decisions? See the [Architecture](arquitectura.md).
