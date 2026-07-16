# Team

Members responsible for the development and maintenance of the Astro Merge
Communications Service.

| Name | Role | Responsibilities |
|--------|-----|-------------------|
| *To be completed* | Tech lead | Architecture, code review, technical decisions. |
| *To be completed* | Backend developer | Services, REST controllers, and persistence (JPA/PostgreSQL). |
| *To be completed* | Backend developer | WebSocket/STOMP channel and integration with other microservices. |
| *To be completed* | QA / Tester | Test design and execution. |
| *To be completed* | DevOps | Containerization, CI/CD, and deployment. |

## Organization

- **GitHub organization:** [TECH-CUP-2026-INT](https://github.com/TECH-CUP-2026-INT)
- **Repository:** [am-communication-service](https://github.com/TECH-CUP-2026-INT/am-communication-service)

This service is one of several microservices that make up the **Astro Merge / TECH-CUP 2026**
platform. The organization currently hosts:

| Repository | Stack | Role |
|------------|-------|------|
| `am-communication-service` | Java | Messaging, chat, moderation *(this repository)* |
| `am-notification-service` | Java | Notifications |
| `am-matches-service` | Java | Match management |
| `am-logistic-service` | Java | Logistics |
| `cc-identity-service` | Java | Identity / authentication |
| `cc-teams-service` | Java | Teams |
| `cc-users-players-service` | Java | Users / players |
| `mk-tournament-service` | Java | Tournaments |
| `mk-payment-service` | Java | Payments |
| `ga-statistics-service` | HTML | Statistics |
| `TECH-CUP-2026-Docs` | — | Official platform-wide documentation |
| `TECH-CUP-FRONT` | — | Frontend application |

!!! note "Service naming"
    Repository names share prefixes (`am-`, `cc-`, `mk-`, `ga-`) that appear to group them by
    functional area or owning sub-team; this repository uses the `am-` prefix.
    **To be completed:** confirm the meaning of each prefix with the other teams. See the
    [organization on GitHub](https://github.com/orgs/TECH-CUP-2026-INT/repositories) for the
    current, authoritative list of repositories.
