## Context

El proyecto `demo-authorization` expone una API REST por capas en Spring Boot 3.5.14 con Java 17 y base H2. La
historia 1 requiere una evaluacion de autorizacion desacoplada de autenticacion, basada en permisos derivados de
roles (`RESOURCE:ACTION`), con respuesta funcional consistente y trazabilidad de denegaciones.

La implementacion debe respetar:
- Validacion Jakarta en entrada y respuestas HTTP consistentes (`400` para request invalido).
- Separacion por capas (`controller`, `service`, `repository`, `dto`, `exception`, `config`).
- No exponer detalles internos sensibles en errores o logs.

## Goals / Non-Goals

**Goals:**
- Habilitar `POST /api/authorization/check-permission` con contrato estable (`allowed`, `userId`, `resource`,
  `action`, `reason`).
- Evaluar permisos considerando usuario activo, roles asociados y presencia de al menos un permiso coincidente.
- Incorporar auditoria de denegaciones con metadatos minimos (`userId`, `resource`, `action`, motivo).
- Documentar endpoint en OpenAPI con respuestas principales (`200`, `400`).

**Non-Goals:**
- Implementar autenticacion/login o emision de JWT en este cambio.
- Introducir nuevos frameworks o cambiar arquitectura existente.
- Redefinir el modelo de roles/permisos fuera de lo necesario para evaluar la historia 1.

## Decisions

1. **Entrada y validacion en DTO de request**
   - Decision: usar `CheckPermissionRequestDTO` con `@NotNull`, `@Positive`, `@NotBlank`.
   - Rationale: centraliza validacion declarativa y garantiza `400` uniforme.
   - Alternativa considerada: validacion manual en controller; descartada por duplicacion y menor consistencia.

2. **Normalizacion de `resource/action` a uppercase en capa de servicio**
   - Decision: normalizar para comparar permisos en formato canonico `RESOURCE:ACTION`.
   - Rationale: evita falsos negativos por diferencias de mayusculas/minusculas sin cambiar contrato externo.
   - Alternativa considerada: rechazar diferencias de case; descartada por peor UX y mayor friccion integradora.

3. **Evaluacion de autorizacion en `AuthorizationService` usando repositorios existentes**
   - Decision: resolver usuario, estado activo, roles y permisos desde capa `repository` y aplicar regla "al menos un
     rol con permiso concede acceso".
   - Rationale: mantiene controller liviano y logica de negocio centralizada.
   - Alternativa considerada: logica distribuida en controller/filtros; descartada por acoplamiento y baja
     reutilizacion.

4. **Respuesta funcional siempre 200 para decision de negocio, 400 para request invalido**
   - Decision: para solicitudes validas, devolver `allowed=true|false` con razon; reservar errores HTTP para
     validaciones de entrada.
   - Rationale: se alinea a la historia que modela la autorizacion como consulta funcional.
   - Alternativa considerada: devolver 403 cuando permiso no existe; descartada en este endpoint porque no protege
     recurso propio, solo informa decision.

5. **Auditoria de denegaciones via logging estructurado**
   - Decision: registrar denegaciones con `log.warn` y sin incluir secretos ni detalle interno de tablas/roles.
   - Rationale: cumple trazabilidad operativa y restricciones de seguridad.
   - Alternativa considerada: no registrar denegaciones; descartada por incumplimiento de observabilidad.

## Risks / Trade-offs

- **[Riesgo] Normalizacion de case puede ocultar errores de catalogo** → **Mitigacion:** mantener catalogo canonico y
  pruebas de contrato con ejemplos de entrada mixtos.
- **[Riesgo] Costo de consultas de roles/permisos en llamadas frecuentes** → **Mitigacion:** consultas eficientes con
  fetch adecuado y pruebas de integracion de latencia basica.
- **[Riesgo] Mensajes de razon demasiado genericos para soporte** → **Mitigacion:** usar razones funcionales estables
  para cliente y detalle tecnico solo en logs internos.

## Migration Plan

1. Agregar DTOs, servicio y endpoint de verificacion.
2. Ajustar manejo de errores para validaciones de request.
3. Publicar actualizacion OpenAPI.
4. Ejecutar pruebas unitarias/integracion y validacion de contrato.
5. Desplegar; no requiere migraciones de base de datos para H2 actual.

Rollback: revertir el change completo si se detectan regresiones, al no existir cambios destructivos de esquema.

## Open Questions

- Confirmar si la normalizacion de `resource/action` debe ser obligatoria en todos los endpoints de permisos.
- Confirmar catalogo formal de permisos validos para validar semanticamente `RESOURCE:ACTION` en futuras historias.
