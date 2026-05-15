## Context

El servicio `authorization` ya expone consulta y validacion de permisos. La historia 4 agrega una operacion de
asignacion inicial para `authentication-service` durante registro, con requisitos de idempotencia, validacion contra
catalogo permitido y errores consistentes `400/404`.

La implementacion requiere controlar duplicados por reintentos concurrentes y responder estado funcional
`ASSIGNED|ALREADY_ASSIGNED` sin romper contrato interno.

## Goals / Non-Goals

**Goals:**
- Exponer `POST /api/permissions/users/{userId}` para asignar un permiso a usuario.
- Validar `userId > 0`, campo `permission` obligatorio y pertenencia a lista blanca.
- Garantizar idempotencia: repetir misma asignacion retorna `ALREADY_ASSIGNED`.
- Retornar contrato con `userId`, `permission`, `status`, `timestamp`.
- Responder `404` cuando usuario no existe.

**Non-Goals:**
- Reemplazo masivo de permisos o administracion completa (HU posterior).
- Cambios al proceso de emision de JWT en `authentication-service`.
- Introducir colas/eventos asinc para esta operacion.

## Decisions

1. **Catalogo centralizado de permisos validos**
   - Decision: definir componente interno con lista blanca de permisos permitidos.
   - Rationale: valida negocio y evita asignaciones arbitrarias.
   - Alternativa: aceptar cualquier string `RESOURCE:ACTION`; descartada por riesgo operativo.

2. **Asignacion idempotente en capa de servicio**
   - Decision: verificar primero si el usuario ya posee el permiso; si existe devolver `ALREADY_ASSIGNED`.
   - Rationale: semantica clara para reintentos de `authentication-service`.
   - Alternativa: confiar en excepcion de constraint unica; descartada por peor UX y acoplamiento a BD.

3. **Persistencia sobre modelo Usuario->Roles->Permisos existente**
   - Decision: reutilizar relaciones actuales y agregar permiso en rol operativo del usuario.
   - Rationale: minimiza impacto estructural y mantiene consistencia con consultas actuales.
   - Alternativa: crear tabla usuario-permiso separada en este cambio; descartada por mayor esfuerzo/migracion.

4. **Respuesta funcional 200 para casos exitosos idempotentes**
   - Decision: usar `200` tanto para `ASSIGNED` como `ALREADY_ASSIGNED`.
   - Rationale: contrato simple y robusto para cliente interno.
   - Alternativa: `409` para duplicado; descartada porque historia exige idempotencia exitosa.

## Risks / Trade-offs

- **[Riesgo] Condiciones de carrera en asignacion concurrente** → **Mitigacion:** transaccion + verificacion previa +
  constraint en relacion.
- **[Riesgo] Catalogo desactualizado respecto a politicas reales** → **Mitigacion:** externalizar catalogo en
  configuracion versionada.
- **[Riesgo] Ambiguedad del rol destino para asignacion** → **Mitigacion:** definir regla explicita en implementacion
  (rol tecnico por defecto o rol existente del usuario).

## Migration Plan

1. Crear DTO request/response de asignacion.
2. Implementar validacion de catalogo y logica idempotente en servicio.
3. Exponer endpoint `POST /api/permissions/users/{userId}` con `400/404`.
4. Actualizar OpenAPI y ejemplos de respuesta.
5. Ejecutar pruebas unitarias/integracion y ajuste de concurrencia basica.

Rollback: revertir el change si se detectan regresiones; sin migraciones destructivas.

## Open Questions

- Confirmar estrategia exacta de asignacion cuando usuario tiene multiples roles.
- Confirmar lista blanca oficial inicial de permisos para este entorno demo.
