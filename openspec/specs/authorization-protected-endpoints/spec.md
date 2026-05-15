# authorization-protected-endpoints Specification

## Purpose
TBD - created by archiving change historia2-protect-endpoints. Update Purpose after archive.
## Requirements
### Requirement: Autenticacion JWT obligatoria para endpoint protegido
El sistema MUST exigir token JWT bearer valido para acceder al endpoint protegido
`GET /api/reports/{reportId}/download`.

#### Scenario: Request sin token
- **WHEN** el cliente invoca el endpoint sin header `Authorization`
- **THEN** el sistema responde HTTP `401`

#### Scenario: Header Authorization malformado
- **WHEN** el cliente envia header `Authorization` sin prefijo `Bearer `
- **THEN** el sistema responde HTTP `401`

#### Scenario: Token invalido o expirado
- **WHEN** el cliente envia un token no valido o expirado
- **THEN** el sistema responde HTTP `401`

### Requirement: Autorizacion por permiso para descarga de reporte
El sistema SHALL permitir la ejecucion del endpoint protegido solo cuando el usuario autenticado posea el permiso
`REPORT:DOWNLOAD`.

#### Scenario: Token valido con permiso requerido
- **WHEN** el cliente envia token valido con permiso `REPORT:DOWNLOAD`
- **THEN** el sistema responde HTTP `200`

#### Scenario: Token valido sin permiso requerido
- **WHEN** el cliente envia token valido pero sin permiso `REPORT:DOWNLOAD`
- **THEN** el sistema responde HTTP `403`

### Requirement: Orden de seguridad antes de logica funcional
El sistema MUST ejecutar validacion de autenticacion y autorizacion antes de invocar la logica de negocio del endpoint
protegido.

#### Scenario: Seguridad bloquea antes de negocio
- **WHEN** la solicitud no supera autenticacion o autorizacion
- **THEN** el sistema finaliza en `401` o `403` sin ejecutar logica funcional de descarga

### Requirement: Documentacion de seguridad en OpenAPI
El sistema MUST documentar el endpoint protegido con esquema de seguridad bearer y respuestas de seguridad esperadas.

#### Scenario: Contrato OpenAPI de endpoint protegido
- **WHEN** se publica la documentacion OpenAPI del endpoint de descarga
- **THEN** el contrato incluye `@SecurityRequirement(name=\"bearerAuth\")` y respuestas `200`, `401` y `403`

