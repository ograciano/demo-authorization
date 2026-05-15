## Context

El servicio `authorization` ya tiene modelo de usuarios, roles y permisos y endpoints para verificacion funcional. La
historia 3 agrega una consulta interna para que `authentication-service` obtenga permisos vigentes por `userId` en el
momento de emitir JWT, sin replicar reglas de autorizacion.

El endpoint debe ser estable, rapido y seguro para consumo interno, con respuestas consistentes `200/400/404` y
normalizacion de permisos sin duplicados.

## Goals / Non-Goals

**Goals:**
- Exponer `GET /api/permissions/users/{userId}` para consulta interna de permisos.
- Retornar contrato fijo `userId`, `permissions[]`, `timestamp`.
- Responder `200` con lista vacia cuando usuario existe sin permisos.
- Responder `404` cuando el usuario no existe.
- Validar `userId > 0` y responder `400` para request invalido.

**Non-Goals:**
- Cambiar flujo de autenticacion o emision de JWT.
- Implementar asignacion/actualizacion administrativa de permisos (historias posteriores).
- Rediseñar persistencia o introducir caches distribuidos en este cambio.

## Decisions

1. **Endpoint interno dedicado**
   - Decision: crear `GET /api/permissions/users/{userId}` en controlador de permisos.
   - Rationale: separa claramente consulta de permisos del endpoint de decision binaria de autorizacion.
   - Alternativa: reutilizar `check-permission`; descartada porque contrato y objetivo son distintos.

2. **Servicio de consulta basado en agregacion Usuario->Roles->Permisos**
   - Decision: resolver usuario con fetch detallado y construir lista deduplicada de permisos.
   - Rationale: mantiene coherencia con el modelo de autorizacion ya implementado.
   - Alternativa: consulta SQL plana de permisos; descartada por menor legibilidad y acoplamiento temprano.

3. **Normalizacion y orden estable en respuesta**
   - Decision: normalizar codigos de permiso y devolver lista deduplicada con orden deterministico.
   - Rationale: contrato predecible para consumidor interno y menor ruido en comparaciones de token.
   - Alternativa: devolver orden natural de persistencia; descartada por inestabilidad entre ejecuciones.

4. **Manejo explicito de 404**
   - Decision: si `userId` no existe, lanzar excepcion de dominio mapeada a `404`.
   - Rationale: contrato claro y semantica HTTP correcta para recurso inexistente.
   - Alternativa: devolver `200` con lista vacia; descartada porque oculta ausencia de usuario.

## Risks / Trade-offs

- **[Riesgo] Endpoint interno expuesto fuera de red esperada** → **Mitigacion:** restringir acceso por configuracion
  de seguridad/red en despliegue.
- **[Riesgo] Variaciones de performance bajo alta concurrencia de login** → **Mitigacion:** consulta con fetch
  optimizado y pruebas de integracion.
- **[Riesgo] Duplicados por datos inconsistentes en relaciones** → **Mitigacion:** deduplicacion en servicio y pruebas
  de contrato.

## Migration Plan

1. Crear DTO de salida para consulta de permisos.
2. Implementar servicio y endpoint `GET /api/permissions/users/{userId}`.
3. Integrar validacion de `userId` y manejo `400/404`.
4. Documentar OpenAPI del endpoint interno.
5. Implementar pruebas unitarias e integracion.

Rollback: revertir el change si hay regresiones; no requiere migracion de esquema.

## Open Questions

- Confirmar mecanismo final de restriccion “uso interno” (mTLS, red privada, allowlist o API gateway).
- Confirmar si `timestamp` debe ser en UTC ISO-8601 con milisegundos o formato truncado.
