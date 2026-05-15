## Historia de Usuario Enriquecida

### Contexto y Objetivo de Negocio
Durante el registro, `authentication-service` debe asignar permisos iniciales a un usuario de manera confiable e idempotente.

### Descripción Funcional Detallada
Se expone `POST /api/permissions/users/{userId}` para asignar un permiso a un usuario con body `{ "permission": "..." }`. Si el permiso se asigna, responde estado de éxito. Si ya estaba asignado, retorna éxito idempotente. Si el permiso no es válido, responde 400. Si el usuario no existe, responde 404.

### Criterios de Aceptación
- [ ] Endpoint disponible para uso interno.
- [ ] Acepta JSON con campo obligatorio `permission`.
- [ ] Retorna HTTP 200 con estado `ASSIGNED` cuando se crea la relación.
- [ ] Retorna HTTP 200 con estado `ALREADY_ASSIGNED` si ya existía.
- [ ] Retorna HTTP 400 con permisos fuera de lista blanca.
- [ ] Retorna HTTP 404 si `userId` no existe.
- [ ] Incluye `userId`, `permission`, `status`, `timestamp` en la respuesta.

### Casos Límite y Validaciones
- `permission` nulo o vacío: HTTP 400.
- `permission` con formato inválido o no catalogado: HTTP 400.
- `userId <= 0`: HTTP 400.
- Reintentos concurrentes del mismo permiso: no duplicar registros.

### Requisitos No Funcionales Relevantes
- Seguridad: endpoint de consumo interno.
- Confiabilidad: idempotencia y consistencia transaccional.
- Rendimiento: operación de asignación de baja latencia.
- Observabilidad: logs de asignación por `userId` y permiso.

### Consideraciones Técnicas (opcional)
- Backend: `PermissionController`, `PermissionService`, repositorio de relación usuario-permiso.
- Validación: catálogo centralizado de permisos válidos.
- OpenAPI: ejemplos de 200/400/404 y propósito interno.

### Requisitos de Testing (alto nivel)
- Pruebas unitarias: validación de catálogo e idempotencia.
- Pruebas de integración: persistencia de asignación y consulta posterior.
- Pruebas transaccionales: comportamiento consistente ante fallos.

### Requisitos de Documentación
- Documentar lista blanca vigente de permisos.
- Documentar semántica idempotente del endpoint.
