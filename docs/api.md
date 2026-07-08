# API

El servicio expone **dos interfaces** con propósitos distintos:

-  **API REST** — operaciones de gestión (historial, moderación) y comunicación entre servicios.
-  **WebSocket (STOMP)** — canal en tiempo real cliente↔servidor, con *topics* por conversación.

### Códigos de respuesta comunes (REST)

| Código | Significado |
|--------|-------------|
| `200 OK` | Petición procesada correctamente. |
| `201 Created` | Recurso creado. |
| `204 No Content` | Operación exitosa sin cuerpo. |
| `400 Bad Request` | Error de validación. |
| `401 Unauthorized` | Usuario no autenticado (validación con Servicio de Identidad). |
| `403 Forbidden` | Sin permiso (p. ej. no es miembro del equipo). |
| `404 Not Found` | Recurso no encontrado. |
| `500 Internal Server Error` | Error inesperado. |

---

