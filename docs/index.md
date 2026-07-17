<div class="am-hero" markdown>
<p class="am-hero__subtitle">Astro Merge Platform</p>

# Communications Service

**Messaging** microservice of the **Astro Merge** system, a sports tournament
management platform built with a microservices architecture. This service concentrates
all communication between users: chatbot-assisted support chat, escalation to the
organizer, direct messaging between players, and moderation of reported messages.
</div>

## Description

`service-communications` (repository `am-communication-service`) is the microservice
responsible for **communications** within Astro Merge. It is developed in **Java 21**
and **Spring Boot 3.x**, exposes real-time communication through **WebSocket (STOMP)** for
the client↔server channel, and persists information in **PostgreSQL**. Communication with the
rest of the microservices is done via **REST API / events** (Feign clients), separately from
the WebSocket channel.

!!! info "Project status"
    The repository is in an early phase: the base package structure
    (`controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `config`, `exception`)
    is defined, but the business logic is not yet fully implemented. Sections marked as
    **To be completed** must be updated as development progresses.

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
| Spring Boot | 3.5.x | Application framework |
| Spring Web | — | REST API (client and inter-service communication) |
| Spring WebSocket + STOMP | — | Real-time client↔server messaging channel |
| Spring Data JPA | — | Relational persistence |
| PostgreSQL | — | Relational database |
| Spring Validation | — | Input data validation |
| Spring Security | — | JWT-based authentication and endpoint authorization |
| Spring Cloud OpenFeign | — | Declarative REST clients to sibling microservices |
| Spring Boot Actuator | — | Health, info, and metrics endpoints |
| Micrometer + Prometheus + Zipkin | — | Metrics and distributed tracing (observability) |
| MapStruct | 1.6.x | Entity ↔ DTO mapping |
| springdoc-openapi | 2.8.x | OpenAPI/Swagger UI for the REST API |
| jjwt | 0.12.x | JWT signing/validation (HS256) |
| Lombok | — | Boilerplate code reduction |
| Maven | Wrapper (`mvnw`) | Dependency management and build |
| Docker + Docker Compose | — | Containerization and local orchestration (app + PostgreSQL) |

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

[:material-github: TECH-CUP-2026-INT/am-communication-service](https://github.com/TECH-CUP-2026-INT/am-communication-service){ .md-button .md-button--primary }

[![CI (Push)](https://github.com/TECH-CUP-2026-INT/am-communication-service/actions/workflows/ci-push.yml/badge.svg)](https://github.com/TECH-CUP-2026-INT/am-communication-service/actions/workflows/ci-push.yml)
[![Deploy](https://github.com/TECH-CUP-2026-INT/am-communication-service/actions/workflows/deploy.yml/badge.svg)](https://github.com/TECH-CUP-2026-INT/am-communication-service/actions/workflows/deploy.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=TECH-CUP-2026-INT_am-communication-service&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=TECH-CUP-2026-INT_am-communication-service)

---

## Documentation map

| Page | What you'll find there |
|---|---|
| [Introduction](introduccion.md) | Context, scope and problem this service solves within Astro Merge. |
| [Requirements](requerimientos.md) | Functional and non-functional requirements. |
| [Configuration](configuracion.md) | Environment variables, profiles, and how to run the service locally. |
| [Architecture](arquitectura.md) | Layered design, design decisions (WebSocket, PostgreSQL), design patterns (Builder, Chain of Responsibility, DTO/Mapper, Repository), request flow, and the class diagram. |
| [API](api.md) | REST and WebSocket endpoints, grouped by resource, in plain language guided by the Swagger annotations. |
| [Service Integration](integracion-servicios.md) | How this service talks to `cc-identity-service`, `cc-teams-service`, `am-notification-service`, the audit service, and Groq — plus current integration gaps. |
| [Testing](pruebas.md) | Testing stack, static analysis (SonarCloud), coverage status, and a tour of the most relevant test classes. |
| [Team](equipo.md) | Who works on this service and the organization it belongs to. |
| [Appendices](anexos.md) | References, glossary, and documentation tooling. |

## Where to start?

- New to the project? Start with the [Introduction](introduccion.md).
- Looking to understand the design and technical decisions? See the [Architecture](arquitectura.md).
- Want to call the API? Jump straight to the [API reference](api.md).
- Working on a sibling microservice? See [Service Integration](integracion-servicios.md) for the
  current contract and open gaps.
