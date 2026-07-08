# Introducción

## Contexto

**Astro Merge** es una plataforma de gestión de **torneos deportivos** construida con una
arquitectura de microservicios. Dentro de esa plataforma, el **Servicio de Comunicaciones**
(`am-comunication-service`) concentra toda la mensajería entre los actores del sistema:
jugadores, organizadores y el sistema de soporte.

El servicio combina un canal **WebSocket (STOMP)** para la interacción en tiempo real
cliente↔servidor con una **API REST** para operaciones de gestión (historial, moderación) y
para la comunicación con otros microservicios. La persistencia se realiza en **PostgreSQL**.

## Problema

En una plataforma de torneos, la comunicación ocurre en múltiples contextos —soporte,
mensajería directa entre jugadores, chats grupales de equipo— y debe ser inmediata y confiable.
Resolver esto dentro de cada servicio generaría acoplamiento y duplicación. Se necesita un
microservicio **especializado** que:

- Ofrezca mensajería en **tiempo real** sin recurrir a *polling*.
- Gestione distintos **tipos de conversación** (soporte, directa, grupal) de forma uniforme.
- Permita **escalar** conversaciones de soporte al organizador.
- Provea **moderación** de contenido reportado.
- Mantenga la **integridad** entre conversaciones, mensajes y reportes.
- Se integre con el resto de la plataforma (equipos, identidad, notificaciones, auditoría).