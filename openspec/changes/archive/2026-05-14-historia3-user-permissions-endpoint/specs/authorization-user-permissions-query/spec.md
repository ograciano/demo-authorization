## ADDED Requirements

### Requirement: Consulta interna de permisos por usuario
El sistema SHALL exponer una operacion interna para consultar permisos vigentes de un usuario por `userId` para
integracion con `authentication-service`.

#### Scenario: Usuario existente con permisos
- **WHEN** se consulta `GET /api/permissions/users/{userId}` para un usuario existente con permisos asignados
- **THEN** el sistema responde `200` con `userId`, `permissions[]` y `timestamp`

#### Scenario: Usuario existente sin permisos
- **WHEN** se consulta `GET /api/permissions/users/{userId}` para un usuario existente sin permisos efectivos
- **THEN** el sistema responde `200` con `permissions=[]`

### Requirement: Validacion de parametro de entrada
El sistema MUST validar que `userId` sea mayor que cero antes de ejecutar la consulta de permisos.

#### Scenario: userId invalido
- **WHEN** el cliente envia `userId` igual o menor que cero
- **THEN** el sistema responde `400`

### Requirement: Respuesta para usuario inexistente
El sistema MUST responder recurso inexistente cuando el `userId` consultado no corresponde a un usuario registrado.

#### Scenario: userId sin registro
- **WHEN** se consulta el endpoint con un `userId` no existente
- **THEN** el sistema responde `404`

### Requirement: Normalizacion de permisos en salida
El sistema SHALL retornar permisos deduplicados y en formato canonico para garantizar estabilidad de contrato.

#### Scenario: Permisos duplicados en persistencia
- **WHEN** el usuario tiene permisos repetidos por multiples roles
- **THEN** el sistema responde `permissions[]` sin duplicados
