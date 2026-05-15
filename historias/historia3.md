## Historia de Usuario Enriquecida

### Contexto y Objetivo de Negocio
`authentication-service` necesita obtener permisos actualizados durante login para construir JWT sin hardcodear reglas en autenticación.

### Descripción Funcional Detallada
Se expone `GET /api/permissions/users/{userId}` para retornar permisos vigentes de un usuario. Si el usuario existe, devuelve HTTP 200 con `permissions` (vacío si no tiene permisos). Si no existe, devuelve HTTP 404. El endpoint es de uso interno entre servicios.

### Criterios de Aceptación
- [ ] Endpoint disponible y documentado para uso interno.
- [ ] Retorna HTTP 200 con `userId`, `permissions[]`, `timestamp` cuando el usuario existe.
- [ ] Retorna HTTP 200 con `permissions=[]` cuando no hay permisos asignados.
- [ ] Retorna HTTP 404 cuando `userId` no existe.
- [ ] Rechaza `userId <= 0` con HTTP 400.

### Casos Límite y Validaciones
- `userId` negativo o cero: 400.
- `userId` muy grande sin registro: 404.
- Usuario con permisos duplicados en persistencia: respuesta normalizada sin duplicados.
- Alta concurrencia sobre mismo usuario: respuesta consistente y estable.

### Requisitos No Funcionales Relevantes
- Seguridad: acceso restringido a canales internos.
- Rendimiento: respuesta rápida para no degradar login.
- Observabilidad: trazas por `userId`, resultado y latencia.
- Resiliencia: contrato estable para fallback controlado en consumidor.

### Consideraciones Técnicas (opcional)
- Backend: `PermissionController`, `PermissionService`, `PermissionRepository`.
- DTO de salida: `userId`, `permissions`, `timestamp`.
- OpenAPI: marcar explícitamente como endpoint interno.

### Requisitos de Testing (alto nivel)
- Pruebas unitarias: validación de `userId` y mapeo de respuesta.
- Pruebas de integración: consulta de usuarios con/sin permisos y no existentes.
- Pruebas de contrato: estructura JSON y códigos 200/400/404.

### Requisitos de Documentación
- Documentar consumo interno desde `authentication-service`.
- Publicar ejemplos oficiales de respuesta exitosa y error.
