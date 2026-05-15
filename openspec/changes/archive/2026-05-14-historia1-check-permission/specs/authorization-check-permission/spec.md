## ADDED Requirements

### Requirement: Evaluacion de permiso por usuario
El sistema SHALL exponer una operacion de autorizacion que evalua si un usuario puede ejecutar una accion sobre un
recurso usando permisos derivados de sus roles.

#### Scenario: Usuario activo con permiso requerido
- **WHEN** se solicita evaluacion para un `userId` activo con al menos un rol que contiene el permiso
  `RESOURCE:ACTION` requerido
- **THEN** el sistema retorna resultado con `allowed=true`

#### Scenario: Usuario activo sin permiso requerido
- **WHEN** se solicita evaluacion para un `userId` activo sin ningun rol que contenga el permiso requerido
- **THEN** el sistema retorna resultado con `allowed=false`

#### Scenario: Usuario inactivo
- **WHEN** se solicita evaluacion para un `userId` inactivo
- **THEN** el sistema retorna resultado con `allowed=false`

### Requirement: Contrato del endpoint de autorizacion
El sistema SHALL publicar `POST /api/authorization/check-permission` con request y response contractuales estables
para consumo sincrono.

#### Scenario: Request valido
- **WHEN** el consumidor envia un body valido con `userId`, `resource` y `action`
- **THEN** el sistema responde `200` con `allowed`, `userId`, `resource`, `action` y `reason`

#### Scenario: Usuario sin roles
- **WHEN** el consumidor envia un body valido para un usuario existente sin roles o sin permisos efectivos
- **THEN** el sistema responde `200` con `allowed=false` y razon funcional de denegacion

### Requirement: Validacion de entrada y errores HTTP
El sistema MUST validar campos de entrada antes de ejecutar evaluacion de permisos y devolver errores HTTP
estandarizados para entrada invalida.

#### Scenario: userId invalido
- **WHEN** el consumidor envia `userId` nulo o `<= 0`
- **THEN** el sistema responde `400`

#### Scenario: resource o action invalidos
- **WHEN** el consumidor envia `resource` o `action` nulos o vacios
- **THEN** el sistema responde `400`

### Requirement: Auditoria de denegaciones
El sistema MUST registrar eventos de denegacion de autorizacion con metadatos minimos de trazabilidad sin exponer
detalles internos sensibles.

#### Scenario: Registro de denegacion
- **WHEN** la evaluacion resulte en `allowed=false`
- **THEN** el sistema registra un evento de auditoria con `userId`, `resource`, `action` y motivo de denegacion
