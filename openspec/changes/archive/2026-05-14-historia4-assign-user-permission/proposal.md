## Why

`authentication-service` necesita asignar permisos iniciales durante registro de forma consistente e idempotente para
evitar duplicados y errores en reintentos. Se requiere un endpoint interno con validacion de catalogo y respuestas
funcionales estables.

## What Changes

- Exponer `POST /api/permissions/users/{userId}` para asignacion interna de permisos por usuario.
- Aceptar body JSON con campo obligatorio `permission`.
- Validar `userId > 0`, formato del permiso y pertenencia a lista blanca.
- Retornar `200` con `status=ASSIGNED` cuando se crea la asignacion.
- Retornar `200` con `status=ALREADY_ASSIGNED` cuando el permiso ya existia (idempotencia).
- Retornar `400` para payload invalido o permiso fuera de catalogo.
- Retornar `404` cuando `userId` no existe.
- Incluir `userId`, `permission`, `status`, `timestamp` en respuesta.
- Documentar OpenAPI con ejemplos de respuesta y semantica idempotente.

## Capabilities

### New Capabilities
- `authorization-assign-user-permission`: Permite asignar permisos iniciales a un usuario de forma validada e
  idempotente para consumo interno entre servicios.

### Modified Capabilities
- Ninguna.

## Impact

- API afectada: nuevo endpoint interno `POST /api/permissions/users/{userId}`.
- Codigo afectado: capas `controller`, `service`, `repository`, `dto`, `exception` y seguridad interna.
- Datos afectados: relacion de permisos de usuario y reglas de deduplicacion en persistencia.
- Testing afectado: pruebas unitarias de validacion/idempotencia y pruebas de integracion `200/400/404`.
