## Why

El sistema requiere una validacion de autorizacion desacoplada de autenticacion para decidir, de forma consistente y
de baja latencia, si una operacion protegida debe permitirse o bloquearse. Esta capacidad se necesita ahora para
soportar consumidores que dependen de una respuesta funcional uniforme `allowed=true|false`.

## What Changes

- Crear el endpoint `POST /api/authorization/check-permission` en `authorization-service`.
- Validar entrada con `userId > 0`, `resource` y `action` no vacios, devolviendo `400` para request invalido.
- Evaluar permisos por modelo `Usuario -> Roles -> Permisos` con convencion `RESOURCE:ACTION`.
- Responder contrato funcional con `allowed`, `userId`, `resource`, `action` y `reason`.
- Denegar autorizacion para usuario inactivo, usuario sin roles o ausencia del permiso requerido.
- Registrar eventos de denegacion para auditoria sin exponer detalles internos sensibles.
- Documentar OpenAPI del endpoint con respuestas `200` y `400`.

## Capabilities

### New Capabilities
- `authorization-check-permission`: Evalua autorizacion por permisos para un usuario y retorna decision funcional
  consistente con motivo de resultado.

### Modified Capabilities
- Ninguna.

## Impact

- API afectada: nuevo endpoint `POST /api/authorization/check-permission`.
- Codigo afectado: capas `controller`, `service`, `repository`, `dto`, `exception`, `config` y auditoria/logging.
- Seguridad: se mantiene separacion autenticacion/autorizacion; el endpoint realiza solo evaluacion de permisos.
- Testing: nuevas pruebas unitarias, integracion y contrato para escenarios permitidos, denegados y request invalido.
