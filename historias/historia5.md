## Historia de Usuario Enriquecida

### Contexto y Objetivo de Negocio
El equipo administrador requiere gestionar permisos de usuarios en tiempo real sin cambios manuales en base de datos.

### Descripción Funcional Detallada
Se expone `PUT /api/admin/users/{userId}/permissions` para reemplazar atómicamente el conjunto de permisos de un usuario. Solo usuarios autenticados con rol/permisos administrativos pueden ejecutar la operación. El endpoint valida usuario destino y validez de permisos solicitados, y retorna el estado final aplicado.

### Criterios de Aceptación
- [ ] Endpoint protegido y operativo.
- [ ] Solo actores ADMIN autenticados pueden invocar la operación.
- [ ] Acepta body `{"permissions": ["REPORT_READ", "REPORT_DOWNLOAD"]}`.
- [ ] Retorna HTTP 200 con permisos actualizados cuando la operación es válida.
- [ ] Retorna HTTP 400 si hay permisos inválidos o request inválido.
- [ ] Retorna HTTP 401 sin autenticación.
- [ ] Retorna HTTP 403 para autenticado sin privilegio ADMIN.
- [ ] Retorna HTTP 404 si usuario objetivo no existe.
- [ ] Operación transaccional: actualización total o rollback completo.

### Casos Límite y Validaciones
- `permissions=[]`: válido, revoca todos los permisos.
- `permissions=null`: HTTP 400.
- Duplicados en array: deduplicar antes de persistir.
- Intento de auto-escalamiento de privilegios no permitido por política: bloquear con 403.

### Requisitos No Funcionales Relevantes
- Seguridad: JWT + autorización administrativa explícita.
- Auditoría: registrar admin ejecutor, usuario afectado, permisos antes/después.
- Rendimiento: actualización eficiente bajo carga moderada.
- Observabilidad: trazabilidad de operaciones y errores de autorización.

### Consideraciones Técnicas (opcional)
- Backend: `AdminPermissionController`, `AdminPermissionService`.
- Seguridad: validación por política/permiso administrativo en capa de autorización.
- OpenAPI: seguridad requerida y respuestas 200/400/401/403/404.

### Requisitos de Testing (alto nivel)
- Pruebas unitarias: validación de permisos y reglas de autorización administrativa.
- Pruebas de integración: persistencia de reemplazo total de permisos.
- Pruebas de seguridad: escenarios 401/403/200.
- Pruebas de auditoría: generación de registro con metadatos mínimos requeridos.

### Requisitos de Documentación
- Actualizar política de administración de permisos.
- Documentar eventos auditables y campos obligatorios de trazabilidad.
