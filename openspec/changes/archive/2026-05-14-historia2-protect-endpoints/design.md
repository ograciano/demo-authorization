## Context

El servicio `authorization` ya expone validacion funcional de permisos (`check-permission`) y usa Spring Security
basico. La historia 2 requiere endurecer endpoints sensibles con autenticacion JWT y autorizacion por permisos antes
de cualquier logica funcional, devolviendo `401` o `403` segun corresponda.

El cambio impacta seguridad transversal: filtro de autenticacion, configuracion de reglas HTTP, evaluacion de
permisos y manejo consistente de errores de seguridad.

## Goals / Non-Goals

**Goals:**
- Proteger `GET /api/reports/{reportId}/download` con JWT bearer y permiso `REPORT:DOWNLOAD`.
- Garantizar orden de validacion: autenticacion y autorizacion antes de ejecutar servicio de negocio.
- Estandarizar respuestas `401` y `403` con handlers de seguridad.
- Reflejar la proteccion en OpenAPI con `bearerAuth` y ejemplos de respuestas.

**Non-Goals:**
- Rediseñar el mecanismo de emision de JWT o flujo de login.
- Cambiar contratos de historia 1 (`check-permission`) o modelo principal de permisos.
- Incorporar proveedores externos OAuth2/Entra ID en este cambio.

## Decisions

1. **Filtro JWT dedicado en cadena de Spring Security**
   - Decision: agregar `JwtAuthenticationFilter` (`OncePerRequestFilter`) para extraer token bearer, validarlo con la
     utilidad JWT actual del proyecto y poblar `SecurityContext`.
   - Rationale: centraliza autenticacion y evita duplicar validacion por controlador.
   - Alternativa: validar token dentro del controller; descartada por acoplamiento y riesgo de bypass.

2. **Autorizacion por permiso en config de seguridad**
   - Decision: mapear `/api/reports/*/download` con requisito de permiso `REPORT:DOWNLOAD`, evaluado contra permisos
     del usuario autenticado.
   - Rationale: reglas declarativas en `SecurityConfig` y enforcement uniforme.
   - Alternativa: if/else en endpoint; descartada por duplicacion y dificultad de auditoria.

3. **Handlers separados para 401 y 403**
   - Decision: implementar `AuthenticationEntryPoint` (401) y `AccessDeniedHandler` (403) con payload consistente.
   - Rationale: diferencia clara entre fallo de autenticacion y falta de privilegio.
   - Alternativa: respuestas por defecto de Spring; descartada por inconsistencia de contrato.

4. **Endpoint protegido de ejemplo con servicio desacoplado**
   - Decision: crear `ReportController` y servicio stub de descarga para validar comportamiento 200/401/403.
   - Rationale: permite cubrir HU2 sin mezclar detalles de almacenamiento de reportes.
   - Alternativa: reutilizar endpoint inexistente; descartada por falta de superficie real para pruebas.

## Risks / Trade-offs

- **[Riesgo] Reglas de permisos desalineadas entre token y BD** → **Mitigacion:** definir fuente unica de permisos en
  claims JWT para autorizacion de endpoint protegido y validar formato.
- **[Riesgo] Falsos 401 por parseo estricto del header** → **Mitigacion:** pruebas de cabecera malformada/ausente y
  mensajes consistentes.
- **[Riesgo] Sobreexposicion de informacion en errores de seguridad** → **Mitigacion:** payload generico sin token ni
  stacktrace; detalles solo en logs internos.

## Migration Plan

1. Agregar componentes de seguridad (filtro JWT, handlers 401/403, evaluador de permiso).
2. Ajustar `SecurityConfig` con reglas de acceso para endpoint protegido.
3. Implementar endpoint `GET /api/reports/{reportId}/download` y su servicio base.
4. Actualizar OpenAPI con `bearerAuth` y respuestas de seguridad.
5. Ejecutar pruebas unitarias e integracion para 200/401/403.

Rollback: revertir el change si hay regresiones; no requiere migracion de esquema.

## Open Questions

- Confirmar claim de identidad obligatorio esperado en JWT (`sub`, `userId` u otro).
- Confirmar claim de permisos en token (`permissions` vs `scope`) para estandarizar evaluacion.
