# authorization-assign-user-permission Specification

## Purpose
TBD - created by archiving change historia4-assign-user-permission. Update Purpose after archive.
## Requirements
### Requirement: Endpoint interno de asignacion de permiso
El sistema SHALL exponer `POST /api/permissions/users/{userId}` para asignar un permiso inicial a un usuario por
consumo interno entre servicios.

#### Scenario: Request valido con campo permission
- **WHEN** el cliente interno envia un body JSON valido con `permission`
- **THEN** el sistema procesa la asignacion y responde `200`

### Requirement: Validacion de entrada y catalogo
El sistema MUST validar `userId`, presencia de `permission` y pertenencia del permiso a lista blanca vigente.

#### Scenario: userId invalido
- **WHEN** `userId` es nulo, cero o negativo
- **THEN** el sistema responde `400`

#### Scenario: permission invalido o fuera de catalogo
- **WHEN** `permission` es nulo, vacio, malformado o no permitido por lista blanca
- **THEN** el sistema responde `400`

### Requirement: Idempotencia de asignacion
El sistema SHALL garantizar semantica idempotente para reintentos de la misma asignacion usuario-permiso.

#### Scenario: Nueva asignacion
- **WHEN** el usuario no tenia asignado el permiso solicitado
- **THEN** el sistema responde `200` con `status=ASSIGNED`

#### Scenario: Asignacion repetida
- **WHEN** el usuario ya tenia asignado el permiso solicitado
- **THEN** el sistema responde `200` con `status=ALREADY_ASSIGNED`

### Requirement: Contrato de respuesta y usuario inexistente
El sistema MUST devolver respuesta estructurada con estado funcional y error semantico para usuario inexistente.

#### Scenario: Contrato exitoso
- **WHEN** la operacion retorna `ASSIGNED` o `ALREADY_ASSIGNED`
- **THEN** la respuesta incluye `userId`, `permission`, `status` y `timestamp`

#### Scenario: Usuario no encontrado
- **WHEN** el `userId` no existe en el sistema
- **THEN** el sistema responde `404`

