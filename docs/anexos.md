# Appendices

## References

- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/index.html)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/)
- [Spring for WebSocket (STOMP)](https://docs.spring.io/spring-framework/reference/web/websocket/stomp.html)
- [Jakarta Bean Validation](https://beanvalidation.org/)
- [Project Lombok](https://projectlombok.org/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [STOMP Protocol Specification](https://stomp.github.io/)
- [Testcontainers for Java](https://java.testcontainers.org/)

## Bibliography


## Additional resources

### Documentation tools

| Resource | Description |
|---------|-------------|
| [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/) | Theme used for this documentation. |
| [MkDocs](https://www.mkdocs.org/) | Documentation site generator. |

### Serving this documentation locally

Requires Python 3:

```bash
python -m venv .venv-docs
.venv-docs/bin/pip install mkdocs-material
.venv-docs/bin/mkdocs serve   # http://localhost:8000
```

Build the static site:

```bash
.venv-docs/bin/mkdocs build
```

!!! tip "Publishing to GitHub Pages"
    ```bash
    .venv-docs/bin/mkdocs gh-deploy
    ```
    **To be completed:** confirm the team's official publishing branch and workflow.

## Glossary

| Term | Definition |
|---------|------------|
| **STOMP** | *Simple Text Oriented Messaging Protocol*; pub/sub messaging protocol used over WebSocket. |
| **WebSocket** | Bidirectional real-time communication protocol over a single TCP connection. |
| **Topic** | STOMP subscription destination; here, one per conversation (`/topic/conversacion/{id}`). |
| **DTO** | *Data Transfer Object*; object for transporting data between layers. |
| **JPA** | *Jakarta Persistence API*; object-relational mapping standard used with PostgreSQL. |
| **Escalation** | Transferring a support conversation from the chatbot to a human organizer. |
| **Moderation** | Review of reported messages and recording of their resolution. |
| **CI/CD** | *Continuous Integration / Continuous Delivery*. |
