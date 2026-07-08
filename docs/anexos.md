# Anexos

## Referencias

- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/index.html)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/)
- [Spring for WebSocket (STOMP)](https://docs.spring.io/spring-framework/reference/web/websocket/stomp.html)
- [Jakarta Bean Validation](https://beanvalidation.org/)
- [Project Lombok](https://projectlombok.org/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [STOMP Protocol Specification](https://stomp.github.io/)
- [Testcontainers for Java](https://java.testcontainers.org/)

## Bibliografía


## Recursos adicionales

### Herramientas de documentación

| Recurso | Descripción |
|---------|-------------|
| [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/) | Tema usado para esta documentación. |
| [MkDocs](https://www.mkdocs.org/) | Generador de sitios de documentación. |

### Servir esta documentación localmente

Requiere Python 3:

```bash
python -m venv .venv-docs
.venv-docs/bin/pip install mkdocs-material
.venv-docs/bin/mkdocs serve   # http://localhost:8000
```

Generar el sitio estático:

```bash
.venv-docs/bin/mkdocs build
```

!!! tip "Publicación en GitHub Pages"
    ```bash
    .venv-docs/bin/mkdocs gh-deploy
    ```
    **Pendiente de completar:** confirmar la rama y el flujo de publicación oficiales del equipo.

## Glosario

| Término | Definición |
|---------|------------|
| **STOMP** | *Simple Text Oriented Messaging Protocol*; protocolo de mensajería pub/sub usado sobre WebSocket. |
| **WebSocket** | Protocolo de comunicación bidireccional en tiempo real sobre una única conexión TCP. |
| **Topic** | Destino de suscripción STOMP; aquí, uno por conversación (`/topic/conversacion/{id}`). |
| **DTO** | *Data Transfer Object*; objeto para transportar datos entre capas. |
| **JPA** | *Jakarta Persistence API*; estándar de mapeo objeto-relacional usado con PostgreSQL. |
| **Escalamiento** | Transferir una conversación de soporte del chatbot a un organizador humano. |
| **Moderación** | Revisión de mensajes reportados y registro de su resolución. |
| **CI/CD** | *Continuous Integration / Continuous Delivery*. |
