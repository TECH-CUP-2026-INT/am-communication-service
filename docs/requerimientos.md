# Requerimientos

Los requisitos funcionales provienen del backlog del proyecto (Astro Merge). Los requisitos
no funcionales se derivan de las integraciones entre microservicios y de las decisiones de
arquitectura ya tomadas.

!!! note "Origen y verificación"
    Los requisitos funcionales listados provienen del **backlog de Jira** compartido por el
    equipo. **No se encontró** ningún archivo de backlog, *issues* ni historias de usuario
    dentro del repositorio (`git log` con un único commit y sin ficheros de backlog).
    **Pendiente de completar:** enlazar el tablero de Jira o exportar las historias de usuario
    al repositorio para poder ampliarlas y trazarlas aquí.

## Requisitos funcionales

| ID | Requisito | Descripción | Estado |
|----|-----------|-------------|--------|
| RF-01 | Chat de ayuda y soporte con chatbot | Un usuario puede iniciar una conversación de soporte atendida por un chatbot. | Previsto |
| RF-02 | Escalamiento al organizador | Una conversación de soporte puede escalarse a un organizador humano cuando el chatbot no resuelve. | Previsto |
| RF-03 | Mensajería directa entre jugadores | Dos jugadores pueden intercambiar mensajes directos en tiempo real. | Previsto |
| RF-04 | Moderación de mensajes reportados | Un mensaje puede ser reportado y revisado por un moderador, que registra una resolución. | Previsto |
| RF-05 | Chat grupal por equipo | Chat grupal asociado a un equipo (`equipo_id`); requiere validar membresía. | Derivado del modelo/integraciones |

!!! info "RF-05 — inferido"
    El tipo de conversación **grupal** con `equipo_id` (modelo de datos) y la integración con
    el **Servicio de Equipos** ("validar membresía antes de permitir chat grupal") implican un
    caso de uso de chat grupal por equipo, aunque no aparezca explícito en la lista del backlog
    compartida. **Pendiente de completar:** confirmar esta historia con el equipo de producto.

## Requisitos no funcionales

| ID | Categoría | Requisito |
|----|-----------|-----------|
| RNF-01 | Rendimiento | La mensajería en tiempo real debe tener baja latencia mediante WebSocket (STOMP), evitando *polling*. |
| RNF-02 | Integridad | La persistencia debe garantizar integridad referencial entre conversaciones, mensajes y reportes (PostgreSQL relacional). |
| RNF-03 | Escalabilidad | El estado de conexión se mantiene en memoria; la lógica de negocio debe poder escalarse horizontalmente. |
| RNF-04 | Integración — Identidad | Validar que el usuario esté autenticado (Servicio de Identidad) antes de participar en un chat. |
| RNF-05 | Integración — Equipos | Validar la membresía del equipo (Servicio de Equipos) antes de permitir el chat grupal. |
| RNF-06 | Integración — Notificaciones | Disparar una notificación (Servicio de Notificaciones) cuando llega un mensaje nuevo. |
| RNF-07 | Integración — Auditoría | Reportar al Servicio de Auditoría la creación de chats y los reportes de moderación. |
| RNF-08 | Portabilidad | El servicio debe poder ejecutarse en contenedores Docker. |
| RNF-09 | Observabilidad | *Pendiente de completar: definir métricas, logs y trazabilidad requeridos.* |
| RNF-10 | Seguridad | *Pendiente de completar: definir TLS, CORS y política de autorización del canal WebSocket.* |

## Casos de uso principales

!!! note "Diagrama de casos de uso"
    Añade aquí el diagrama UML de casos de uso en `docs/assets/diagrams/casos-de-uso.png`.
    <!-- ![Diagrama de casos de uso](assets/diagrams/casos-de-uso.png) -->

### CU-01 — Chat de soporte con escalamiento

| Campo | Descripción |
|-------|-------------|
| **Actor** | Usuario (jugador) · Chatbot · Organizador |
| **Precondición** | El usuario está autenticado (validado con el Servicio de Identidad). |
| **Flujo principal** | 1. El usuario abre una conversación de soporte. 2. El chatbot responde. 3. Si no resuelve, la conversación se **escala** al organizador. 4. El organizador continúa la conversación en tiempo real. |
| **Postcondición** | La conversación y sus mensajes quedan persistidos; se notifica a los participantes. |
| **Excepciones** | Usuario no autenticado → rechazo; ningún organizador disponible → *pendiente de definir política*. |

### CU-02 — Mensajería directa entre jugadores

| Campo | Descripción |
|-------|-------------|
| **Actor** | Jugador emisor · Jugador receptor |
| **Precondición** | Ambos usuarios autenticados. |
| **Flujo principal** | 1. El emisor envía un mensaje. 2. Se valida y persiste. 3. Se entrega en tiempo real al receptor suscrito al *topic*. 4. Se dispara notificación. |
| **Postcondición** | Mensaje almacenado y entregado. |

### CU-03 — Moderación de un mensaje reportado

| Campo | Descripción |
|-------|-------------|
| **Actor** | Usuario que reporta · Moderador |
| **Precondición** | Existe el mensaje a reportar. |
| **Flujo principal** | 1. Un usuario reporta un mensaje indicando el motivo. 2. Se crea un **Reporte**. 3. Un moderador lo revisa y registra la resolución. 4. Se reporta el evento al Servicio de Auditoría. |
| **Postcondición** | El reporte queda registrado con su resolución. |

### CU-04 — Chat grupal de equipo *(inferido)*

| Campo | Descripción |
|-------|-------------|
| **Actor** | Jugadores miembros de un equipo |
| **Precondición** | El usuario es miembro del equipo (validado con el Servicio de Equipos). |
| **Flujo principal** | 1. Se valida la membresía. 2. El jugador se suscribe al *topic* del chat grupal. 3. Intercambio de mensajes en tiempo real. |
| **Postcondición** | Mensajes persistidos y entregados a los miembros conectados. |

!!! info "Pendiente de completar"
    Documentar los criterios de aceptación de cada caso de uso a partir de las historias de
    Jira.
