## Why

El equipo administrador necesita actualizar permisos de usuarios en tiempo real sin manipular base de datos y con
trazabilidad. Se requiere una operacion segura y atomica para reemplazar el conjunto completo de permisos objetivo.

## What Changes

- Exponer `PUT /api/admin/users/{userId}/permissions` para reemplazo total de permisos por usuario.
- Proteger endpoint para uso exclusivo de actores autenticados con privilegio administrativo.
- Validar `userId`, `permissions` y pertenencia de permisos a catalogo permitido.
- Aceptar `permissions=[]` como revocacion total valida.
- Deduplicar permisos del request antes de persistir.
- Retornar `200` con estado final de permisos aplicados cuando la operacion es valida.
- Retornar `400` en request invalido o permisos fuera de catalogo.
- Retornar `401` sin autenticacion valida y `403` sin privilegio ADMIN.
- Retornar `404` cuando el usuario objetivo no existe.
- Ejecutar reemplazo de forma transaccional (todo o rollback completo) con auditoria antes/despues.

## Capabilities

### New Capabilities
- `authorization-admin-replace-permissions`: Permite a administradores reemplazar de manera atomica y auditada el
  conjunto de permisos de un usuario.

### Modified Capabilities
- Ninguna.

## Impact

- API afectada: nuevo endpoint `PUT /api/admin/users/{userId}/permissions`.
- Seguridad afectada: reglas de autorizacion administrativa y respuestas 401/403.
- Persistencia afectada: actualizacion transaccional del set de permisos del usuario.
- Observabilidad afectada: registro de auditoria con admin ejecutor, usuario objetivo y delta de permisos.
