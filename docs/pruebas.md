# Testing

The project includes `spring-boot-starter-test`, which bundles **JUnit 5**, **Mockito**,
**AssertJ**, and **Spring Test**. There are currently **48 test classes** (~200 `@Test` methods)
under `src/test/java`, covering domain models, use cases, REST/WebSocket controllers, Feign
adapters, and persistence.

## Tools used

| Tool | Use |
|------------|-----|
| JUnit 5 | Testing framework. |
| Mockito | Mocks and stubs. |
| AssertJ | Fluent assertions. |
| Spring Boot Test | Context and integration tests (`@SpringBootTest`, `@WebMvcTest`, `@AutoConfigureMockMvc`). |
| SonarCloud | Static analysis, run in CI. |

---

## Static analysis

Static analysis runs on **SonarCloud**, triggered as a Maven goal in CI rather than as a build
plugin declared in `pom.xml`:

![SonarCloud dashboard for service-communications: quality gate passed, 0 open issues on
security/reliability/maintainability, 100% hotspots reviewed, 92.2% coverage, 0% duplications](assets/img/sonnar.png)

*Latest analysis (7/16/2026) — Quality Gate **Passed**, 4.8k lines of code (Java, XML).*

## Coverage
As of the dashboard above, SonarCloud reports **92.2%** coverage with **0%** duplications. 
## Relevant tests

A representative sample of what's actually covered, beyond a plain CRUD smoke test:

| Test class | What it verifies |
|---|---|
| `domain/service/support/SupportChainOrchestratorTest` | The support [chain of responsibility](arquitectura.md#design-patterns)'s orchestration, using hand-rolled fake handlers to check escalation and notification side effects. |
| `infrastructure/out/feign/LiveWireContractVerificationTest` | Boots the real `Feign*Client` adapters through Spring Cloud OpenFeign's actual wiring against plain-JDK `HttpServer` stubs mirroring cc-users-players-service/cc-teams-service/am-notification-service's real contracts — proves the request path, headers (including the notification service's `X-Internal-Api-Key`), and JSON body actually match what those services expect, not just that this service's own code is internally consistent. |
| `domain/service/support/ChatbotSupportHandlerTest` | The Groq-backed chatbot handler, including the fallback message path when the AI call raises `IntegrationException` and the always-escalate-to-MODERATOR behavior. |
| `domain/service/support/SupportPromptBuilderTest` | The [prompt builder](arquitectura.md#design-patterns)'s output format — subject-only vs. with conversation history, and correct `Assistant`/`User` role labeling. |
| `domain/model/ChatTest` | The `Chat` aggregate's own invariants (creation, participants, closing) — the largest domain-model test class. |
| `infrastructure/in/rest/controller/ChatControllerTest` | `MockMvc`-based controller test with mocked use cases but real MapStruct-generated mappers, exercising the actual JSON contract. |
| `infrastructure/out/persistence/PersistenceAdapterIntegrationTest` | A genuine `@SpringBootTest` + `@Transactional` integration test against a real PostgreSQL instance (`docker compose up postgres`) — no mocks, deliberately, to catch mapping/persistence issues mock-based service tests can't. |
| `infrastructure/out/feign/FeignGroqChatbotClientTest` | The Groq Feign adapter, including empty-response handling and `FeignException` → `IntegrationException` translation. |
| `infrastructure/config/JwtAuthenticationFilterTest` | JWT authentication filter behavior for valid, invalid, and missing tokens. |

Every REST controller and every application-layer use case has its own test class following the
same conventions as the examples above — mocked collaborators for unit-level use-case tests, and
`MockMvc` for controller-level tests.
