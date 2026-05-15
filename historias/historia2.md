## Historia de Usuario Enriquecida

### Contexto y Objetivo de Negocio
Los endpoints sensibles deben ejecutarse solo para usuarios autenticados y autorizados. El objetivo es aplicar control de acceso homogéneo sobre operaciones críticas.

### Descripción Funcional Detallada
En endpoints protegidos (ej. `GET /api/reports/{reportId}/download`), el servicio valida JWT del header `Authorization`, identifica al usuario autenticado y verifica el permiso requerido (ej. `REPORT:DOWNLOAD`) antes de ejecutar lógica de negocio. Si falta token o es inválido/expirado, responde 401. Si el token es válido pero no tiene permiso, responde 403.

### Criterios de Aceptación
- [ ] Con token válido y permiso requerido, el endpoint protegido responde HTTP 200.
- [ ] Sin token, responde HTTP 401.
- [ ] Con token inválido o expirado, responde HTTP 401.
- [ ] Con token válido sin permiso requerido, responde HTTP 403.
- [ ] La validación de autorización se ejecuta antes de la lógica funcional del endpoint.
- [ ] La documentación OpenAPI expone claramente seguridad y respuestas 200/401/403.

### Casos Límite y Validaciones
- Header `Authorization` malformado (sin prefijo `Bearer `): HTTP 401.
- Token válido sin claim de identidad requerido: HTTP 401.
- Permiso requerido no configurado para endpoint: bloquear despliegue o fallar en validación de seguridad.
- Recursos inexistentes: tras pasar autorización, responder con código funcional (ej. 404) según dominio.

### Requisitos No Funcionales Relevantes
- Seguridad: control centralizado, sin lógica duplicada de permisos en controladores.
- Rendimiento: validación de token y permiso con impacto mínimo por request.
- Observabilidad: métricas de 401/403 y logs sin filtrar datos sensibles del token.
- Operabilidad: handlers consistentes para autenticación y autorización fallida.

### Consideraciones Técnicas (opcional)
- Seguridad: configuración con filtro JWT y reglas de autorización por permiso.
- API: anotar en OpenAPI `@SecurityRequirement(name="bearerAuth")`.
- Componentes sugeridos: `SecurityConfig`, `JwtAuthenticationFilter`, `PermissionEvaluator`, handlers 401/403.

### Requisitos de Testing (alto nivel)
- Pruebas unitarias: evaluación de permiso por endpoint.
- Pruebas de integración: flujo completo 200/401/403.
- Pruebas de seguridad: verificación de orden (seguridad antes de lógica de negocio).

### Requisitos de Documentación
- Actualizar matriz de permisos por endpoint.
- Incluir ejemplos de respuesta para 401 y 403 en OpenAPI.
