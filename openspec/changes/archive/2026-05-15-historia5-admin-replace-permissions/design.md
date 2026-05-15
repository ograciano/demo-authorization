## Context

La plataforma ya tiene endpoints internos para consultar y asignar permisos individuales, ademas de seguridad JWT para
endpoints protegidos. La historia 5 requiere administracion centralizada para reemplazar en una sola operacion el set
completo de permisos de un usuario.

El cambio es sensible por seguridad (solo ADMIN), consistencia (transaccional) y auditoria (antes/despues).

## Goals / Non-Goals

**Goals:**
- Habilitar `PUT /api/admin/users/{userId}/permissions` protegido para administradores.
- Validar request (`permissions` no nulo, catalogo permitido, deduplicacion).
- Soportar `permissions=[]` para revocacion total.
- Aplicar reemplazo atomico de permisos.
- Retornar estado final aplicado y registrar auditoria minima requerida.

**Non-Goals:**
- Gestion de jerarquias complejas de roles fuera del alcance administrativo basico.
- Automatizaciones de aprobacion o workflows humanos.
- Cambios en emision de JWT o politica de autenticacion del proveedor externo.

## Decisions

1. **Endpoint administrativo separado**
   - Decision: crear controlador administrativo bajo `/api/admin/users/{userId}/permissions`.
   - Rationale: separa operaciones criticas de endpoints internos de uso automatizado.
   - Alternativa: reusar endpoint de asignacion individual; descartada por semantica distinta (replace vs add).

2. **Control de acceso explicito por permiso administrativo**
   - Decision: exigir autenticacion + autoridad ADMIN (o permiso equivalente) en capa de seguridad.
   - Rationale: evita ejecucion por usuarios autenticados sin privilegio suficiente.
   - Alternativa: validacion solo en servicio; descartada por riesgo de bypass parcial.

3. **Reemplazo transaccional completo**
   - Decision: computar set objetivo deduplicado y reemplazar en una sola transaccion.
   - Rationale: garantiza consistencia ante fallos intermedios.
   - Alternativa: remove/add incremental sin transaccion; descartada por riesgo de estado parcial.

4. **Catalogo centralizado y normalizacion**
   - Decision: validar cada permiso contra lista blanca central y normalizar formato.
   - Rationale: previene insercion de permisos no soportados y facilita interoperabilidad.
   - Alternativa: aceptar entrada libre; descartada por riesgo operativo y de seguridad.

5. **Auditoria con delta de permisos**
   - Decision: registrar admin actor, usuario objetivo y permisos antes/despues.
   - Rationale: soporte a trazabilidad y post-mortem.
   - Alternativa: log solo de resultado final; descartada por observabilidad insuficiente.

## Risks / Trade-offs

- **[Riesgo] Escalamiento indebido por configuracion de reglas ADMIN** → **Mitigacion:** pruebas de seguridad 401/403
  y revisiones de configuracion.
- **[Riesgo] Contencion en actualizaciones concurrentes del mismo usuario** → **Mitigacion:** transaccion y estrategia
  de locking acorde al repositorio.
- **[Riesgo] Auditoria incompleta por fallos de logging** → **Mitigacion:** logs estructurados y pruebas de presencia
  de campos clave.

## Migration Plan

1. Crear DTO request/response administrativo.
2. Implementar servicio de reemplazo transaccional y validacion de catalogo.
3. Configurar seguridad del endpoint admin (401/403).
4. Integrar auditoria de cambios.
5. Crear pruebas unitarias/integracion/seguridad y actualizar OpenAPI.

Rollback: revertir change; no requiere migraciones destructivas de esquema.

## Open Questions

- Confirmar nombre exacto de autoridad administrativa (`ADMIN` o permiso `PERM_ADMIN:*`).
- Confirmar si politica de auto-escalamiento debe bloquear que un admin modifique sus propios permisos.
