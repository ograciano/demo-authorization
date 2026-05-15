## Why

Los endpoints sensibles deben aplicar autenticacion y autorizacion centralizadas para evitar ejecucion de logica de
negocio por usuarios no autorizados. Este cambio habilita control de acceso homogeno con respuestas HTTP 401/403
consistentes para operaciones criticas.

## What Changes

- Proteger endpoint de descarga `GET /api/reports/{reportId}/download` con validacion JWT y permiso
  `REPORT:DOWNLOAD`.
- Implementar filtro de seguridad para validar header `Authorization: Bearer <token>`, token invalido/expirado y
  claims requeridos de identidad.
- Incorporar evaluacion de permisos del usuario autenticado antes de ejecutar la logica funcional del endpoint.
- Configurar handlers de seguridad para responder `401` (autenticacion fallida) y `403` (sin permiso suficiente).
- Documentar OpenAPI del endpoint protegido con `bearerAuth` y respuestas `200/401/403`.
- Agregar pruebas unitarias e integracion para flujo completo de seguridad y orden de ejecucion.

## Capabilities

### New Capabilities
- `authorization-protected-endpoints`: Aplica autenticacion JWT y autorizacion por permisos a endpoints protegidos
  con respuestas estandarizadas 401/403/200.

### Modified Capabilities
- Ninguna.

## Impact

- API afectada: nuevo endpoint protegido `GET /api/reports/{reportId}/download`.
- Seguridad afectada: `SecurityConfig`, filtro JWT, evaluador de permisos y handlers de acceso.
- Codigo afectado: capas `security`, `controller`, `service`, `config` y `exception`.
- Testing afectado: nuevos tests de seguridad para escenarios con y sin token, token invalido y falta de permiso.
