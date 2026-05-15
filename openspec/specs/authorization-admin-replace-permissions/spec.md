# authorization-admin-replace-permissions Specification

## Purpose
TBD - created by archiving change historia5-admin-replace-permissions. Update Purpose after archive.
## Requirements
### Requirement: Endpoint admin para reemplazo atomico de permisos
El sistema SHALL exponer `PUT /api/admin/users/{userId}/permissions` para reemplazar el conjunto completo de permisos
de un usuario en una sola operacion transaccional.

#### Scenario: Reemplazo valido de permisos
- **WHEN** un administrador autenticado envia un request valido con permisos permitidos
- **THEN** el sistema responde `200` con el conjunto final aplicado

#### Scenario: Revocacion total valida
- **WHEN** un administrador autenticado envia `permissions=[]`
- **THEN** el sistema responde `200` y revoca todos los permisos del usuario objetivo

### Requirement: Autorizacion administrativa obligatoria
El sistema MUST permitir acceso al endpoint solo a actores autenticados con privilegio administrativo.

#### Scenario: Sin autenticacion
- **WHEN** se invoca el endpoint sin token valido
- **THEN** el sistema responde `401`

#### Scenario: Autenticado sin privilegio ADMIN
- **WHEN** se invoca el endpoint con token valido pero sin privilegio administrativo
- **THEN** el sistema responde `403`

### Requirement: Validacion de request y catalogo
El sistema MUST validar `userId`, estructura de request y pertenencia de permisos a lista blanca.

#### Scenario: Request invalido
- **WHEN** `permissions` es nulo o contiene valores invalidos/no permitidos
- **THEN** el sistema responde `400`

#### Scenario: Duplicados en permisos
- **WHEN** el request contiene permisos repetidos
- **THEN** el sistema deduplica antes de persistir y aplica resultado unico

### Requirement: Usuario objetivo inexistente y auditoria
El sistema MUST reportar `404` para usuario inexistente y registrar auditoria del cambio administrativo.

#### Scenario: Usuario no existe
- **WHEN** el `userId` objetivo no existe
- **THEN** el sistema responde `404`

#### Scenario: Auditoria de actualizacion
- **WHEN** la operacion de reemplazo se ejecuta
- **THEN** el sistema registra admin ejecutor, usuario afectado y permisos antes/despues

