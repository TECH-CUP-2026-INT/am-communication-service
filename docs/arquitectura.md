# Arquitectura

## Arquitectura del sistema

El Servicio de Comunicaciones es un microservicio independiente dentro de la plataforma
**Astro Merge**. Internamente sigue una **arquitectura por capas** (controller → service →
repository) y expone **dos canales de entrada** claramente diferenciados:

- **WebSocket (STOMP):** canal en tiempo real **cliente↔servidor** para el intercambio de mensajes.
- **API REST:** operaciones de gestión (historial, moderación) y **comunicación con otros microservicios**.


## Decisiones de arquitectura

### ¿Por qué WebSocket?

- **Tiempo real sin *polling*.** El chat requiere entrega inmediata de mensajes. Con REST, el
  cliente tendría que *sondear* (polling) el servidor repetidamente, aumentando latencia y
  carga. WebSocket mantiene una conexión persistente bidireccional y **empuja** los mensajes
  a los clientes suscritos en cuanto ocurren.
- **STOMP sobre WebSocket** aporta un modelo de **publicación/suscripción** con destinos y
  *topics*, lo que permite modelar naturalmente **un *topic* por conversación** (chat grupal,
  conversación de soporte, mensajería directa) y entregar cada mensaje solo a los suscriptores
  de esa conversación.
- **Ámbito acotado:** el WebSocket es **exclusivamente** el canal cliente↔servidor. La
  comunicación entre microservicios **no** usa WebSocket (ver más abajo).

### ¿Por qué PostgreSQL y no NoSQL?

- **Integridad referencial.** El dominio tiene relaciones claras: un **Mensaje** pertenece a
  una **Conversación**, y un **Reporte** referencia a un **Mensaje**. Las claves foráneas y
  las restricciones de PostgreSQL garantizan esta integridad de forma nativa.
- **Volumen que no justifica NoSQL.** El volumen de escrituras de mensajería del sistema no
  alcanza la escala en la que una base NoSQL aportaría ventajas decisivas; la simplicidad y
  las garantías transaccionales de un motor relacional pesan más.
- **Consultas relacionales.** Paginar el historial de una conversación, filtrar reportes por
  estado o unir mensajes con sus reportes se expresa de forma directa en SQL.

### Comunicación entre microservicios: API / eventos


## Patrones de diseño


## Componentes

```text
communications/
├── config/       # Configuración WebSocket/STOMP, CORS, beans
├── controller/   # Controladores REST y WebSocket (STOMP)
├── dto/          # Objetos de transferencia de datos
├── entity/       # Entidades JPA (Conversacion, Mensaje, Reporte)
├── exception/    # Excepciones y manejo centralizado de errores
├── mapper/       # Conversión entidad <-> DTO
├── repository/   # Acceso a datos (Spring Data JPA)
└── service/      # Lógica de negocio e integración con otros servicios
```

## Flujo general

## Diagramas UML y de arquitectura

