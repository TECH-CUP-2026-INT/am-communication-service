# Astro Merge — Servicio de Comunicaciones

Microservicio de **mensajería** del sistema **Astro Merge**, una plataforma de gestión de
torneos deportivos construida con arquitectura de microservicios. Este servicio concentra
toda la comunicación entre usuarios: chat de soporte con chatbot, escalamiento al
organizador, mensajería directa entre jugadores y moderación de mensajes reportados.

---

## Descripción

`service-communications` (repositorio `am-comunication-service`) es el microservicio
responsable de las **comunicaciones** dentro de Astro Merge. Está desarrollado en **Java 21**
y **Spring Boot 3.x**, expone comunicación en tiempo real mediante **WebSocket (STOMP)** para
el canal cliente↔servidor y persiste la información en **PostgreSQL**. La comunicación con el
resto de microservicios se realiza vía **API REST / eventos**, de forma separada al canal
WebSocket.

!!! info "Estado del proyecto"
    El repositorio se encuentra en fase inicial: la estructura base de paquetes
    (`controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `config`, `exception`)
    está definida, pero la lógica de negocio aún no está implementada. Las secciones marcadas
    como **Pendiente de completar** deben actualizarse conforme avance el desarrollo.

!!! warning "Discrepancia stack ↔ código"
    La arquitectura decidida usa **PostgreSQL**, pero el `pom.xml` actual todavía declara
    `spring-boot-starter-data-mongodb`. Antes de implementar la persistencia debe sustituirse
    por `spring-boot-starter-data-jpa` + el driver de PostgreSQL. Ver [Configuración](configuracion.md).

---

## Objetivo

Centralizar la mensajería de la plataforma en un servicio desacoplado y escalable que ofrezca
comunicación en tiempo real de baja latencia, garantice la integridad de las conversaciones y
sus mensajes, y se integre con el resto de microservicios (equipos, identidad, notificaciones
y auditoría) mediante API/eventos.

---

## Tecnologías utilizadas

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 21 | Lenguaje de programación |
| Spring Boot | 3.x | Framework de aplicación |
| Spring Web | — | API REST (cliente y comunicación entre servicios) |
| Spring WebSocket + STOMP | — | Canal de mensajería en tiempo real cliente↔servidor |
| Spring Data JPA | *(pendiente en `pom.xml`)* | Persistencia relacional |
| PostgreSQL | — | Base de datos relacional |
| Spring Validation | — | Validación de datos de entrada |
| Lombok | — | Reducción de código repetitivo (boilerplate) |
| Maven | Wrapper (`mvnw`) | Gestión de dependencias y construcción |

---

## Características principales

- **Chat de soporte con chatbot** para ayuda y atención al usuario.
- **Escalamiento de conversaciones** de soporte al organizador del torneo.
- **Mensajería directa** entre jugadores.
- **Moderación** de mensajes reportados.
- **Tiempo real** mediante WebSocket (STOMP), con un *topic* por conversación.
- **Integración con otros microservicios** (equipos, identidad, notificaciones, auditoría) vía API/eventos.
- **Persistencia relacional** en PostgreSQL con integridad referencial.

---

## Enlace al repositorio

[:material-github: TECH-CUP-2026-INT/am-comunication-service](https://github.com/TECH-CUP-2026-INT/am-comunication-service){ .md-button .md-button--primary }

---

## ¿Por dónde empezar?

- ¿Nuevo en el proyecto? Comienza por la [Introducción](introduccion.md).
- ¿Buscas entender el diseño y las decisiones técnicas? Consulta la [Arquitectura](arquitectura.md).
