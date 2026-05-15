## Why

`authentication-service` necesita consultar permisos actualizados durante login sin hardcodear reglas de autorizacion.
Se requiere un endpoint interno estable para obtener permisos vigentes por usuario con respuestas 200/400/404 claras.

## What Changes

- Exponer `GET /api/permissions/users/{userId}` para consumo interno entre servicios.
- Retornar `200` con `userId`, `permissions[]` y `timestamp` cuando el usuario existe.
- Retornar `200` con `permissions=[]` cuando el usuario existe sin permisos efectivos.
- Retornar `404` cuando `userId` no existe.
- Validar `userId > 0` y responder `400` para valores invalidos.
- Normalizar respuesta de permisos sin duplicados.
- Documentar OpenAPI del endpoint con ejemplos de `200`, `400` y `404`.
- Incorporar pruebas unitarias, integracion y contrato para casos con/sin permisos y usuario inexistente.

## Capabilities

### New Capabilities
- `authorization-user-permissions-query`: Consulta interna de permisos vigentes por usuario para integracion con
  autenticacion durante construccion de JWT.

### Modified Capabilities
- Ninguna.

## Impact

- API afectada: nuevo endpoint interno `GET /api/permissions/users/{userId}`.
- Codigo afectado: capas `controller`, `service`, `repository`, `dto`, `exception` y documentacion OpenAPI.
- Integraciones afectadas: consumidor interno `authentication-service` en flujo de login.
- Testing afectado: nuevas pruebas para `200/400/404`, deduplicacion y estabilidad de contrato JSON.
