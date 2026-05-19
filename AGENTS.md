# Instrucciones para el Agente IA

## Contexto del Proyecto

- **Nombre**: demo-authorization
- **Tipo**: API backend
- **Propósito**: Proveer capacidades de autorización para validar, consultar, asignar y administrar permisos de usuarios.
- **Arquitectura**: Monolito modular en capas (controller, service, repository, dto, entity) sobre Spring Boot.

## Fuente funcional Jira

- **Proyecto Jira**: SCRUM
- **Issues base para este documento**:
- `SCRUM-16` Autorización - Validar permiso por rol para acción solicitada.
- `SCRUM-17` Seguridad - Proteger endpoint de descarga con permisos JWT.
- `SCRUM-18` Permisos - Consultar permisos dinámicos por usuario interno.
- `SCRUM-19` Permisos - Asignar permiso predeterminado durante registro.
- `SCRUM-20` Administración - Actualizar permisos de usuario con control ADMIN.

## Stack Tecnológico

### Core
- **Lenguaje**: Java 17
- **Framework**: Spring Boot 3.5.14
- **Módulos**: Spring Web, Spring Security, Spring Data JPA
- **Base de datos**: H2 (runtime)
- **Documentación API**: springdoc-openapi (`springdoc-openapi-starter-webmvc-ui` 2.8.16)

### Testing
- **Framework**: JUnit 5 + Spring Boot Test
- **Integración**: Testcontainers (dependencias preparadas)
- **Seguridad**: spring-security-test

### Herramientas
- **Build y dependencias**: Maven (`pom.xml`, `mvnw`)
- **Configuración app**: `src/main/resources/application.properties`
- **MCP interno**: `mcp/jira-writer` para interacción con Jira

## Estructura del Proyecto

```text
demo-authorization/
├── src/
│   ├── main/
│   │   ├── java/com/vass/authorization/
│   │   │   └── AuthorizationApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/java/com/vass/authorization/
│       ├── AuthorizationApplicationTests.java
│       ├── TestAuthorizationApplication.java
│       └── TestcontainersConfiguration.java
├── mcp/jira-writer/              # MCP server para Jira
├── jira/                         # Consultas JQL y utilidades
├── AGENTS.md
├── pom.xml
└── README.md
```

## Convenciones de Código

### Nomenclatura

| Elemento | Convención | Ejemplo |
|----------|------------|---------|
| Paquetes | lowercase por dominio | `com.vass.authorization.security` |
| Clases | PascalCase | `AuthorizationService` |
| Métodos | camelCase | `checkPermission` |
| DTOs | Sufijo `Request` / `Response` | `PermissionCheckRequest` |
| Entidades | Sufijo `Entity` | `RoleEntity` |
| Repositorios | Sufijo `Repository` | `UserPermissionRepository` |
| Constantes | UPPER_SNAKE_CASE | `REPORT_DOWNLOAD` |

### Formato de Código
- **Líneas**: Máximo 120 caracteres
- **Indentación**: 4 espacios
- **Imports**: Ordenados por IDE y sin wildcard
- **Manejo de nulos**: Validación explícita en capa de entrada

### Documentación
- Comentarios y nombres claros en español o inglés técnico consistente por archivo.
- Endpoints documentados con anotaciones OpenAPI (`@Operation`, `@ApiResponse`, `@SecurityRequirement`).
- Errores HTTP con estructura uniforme (`timestamp`, `status`, `error`, `message`, `path`).

## Patrones a Seguir

### Patrón: Autorización por Permiso
- Resolver permiso requerido como `RESOURCE:ACTION`.
- Denegar por defecto ante cualquier duda de identidad, rol o permiso.
- Para endpoints de evaluación (`check-permission`), responder `allowed=true/false` sin exponer detalles internos sensibles.

### Patrón: Seguridad HTTP
- `401 Unauthorized`: token ausente, malformado, inválido o expirado.
- `403 Forbidden`: token válido sin permisos suficientes.
- La lógica de seguridad debe ejecutarse antes de la lógica de negocio.

### Patrón: Operaciones Idempotentes
- `SCRUM-19`: asignación de permiso por usuario debe ser idempotente (`ALREADY_ASSIGNED` cuando ya exista).

### Patrón: Operaciones Atómicas
- `SCRUM-20`: reemplazo de permisos de usuario debe ser transaccional (todo o nada) y auditable.

## Restricciones de Seguridad

### PROHIBIDO (NUNCA hacer)
1. Exponer secretos (tokens Jira, JWT secretos, credenciales de BD) en código, logs o respuestas HTTP.
2. Ejecutar lógica de negocio sensible si falla autenticación/autorización.
3. Devolver detalles internos de modelo de roles/permisos en errores públicos.
4. Permitir elevación de privilegios sin control explícito de rol/permisos ADMIN.
5. Modificar issues Jira automáticamente (comentarios/transiciones/creación) sin petición explícita del usuario.

### OBLIGATORIO (SIEMPRE hacer)
1. Validar entrada (`userId > 0`, campos obligatorios, permisos en lista blanca).
2. Diferenciar claramente errores 400, 401, 403 y 404.
3. Registrar auditoría de denegaciones y cambios administrativos de permisos.
4. Mantener separación de responsabilidades por capas.
5. Cubrir reglas de seguridad con pruebas unitarias e integración.

## Estándares de Testing

### Estructura de Tests

```text
src/test/java/com/vass/authorization/
├── unit/                  # (pendiente de crear)
├── integration/           # (pendiente de crear)
└── security/              # (pendiente de crear)
```

### Nomenclatura de Tests
```java
// Patrón recomendado:
// should_<resultado>_when_<condicion>()
```

### Cobertura mínima esperada por historias
- `SCRUM-16`: allowed true/false, usuario inactivo, request inválido.
- `SCRUM-17`: respuestas 200/401/403 para endpoint protegido.
- `SCRUM-18`: 200 con permisos, 200 con vacío, 404 no encontrado, 400 inválido.
- `SCRUM-19`: 200 ASSIGNED, 200 ALREADY_ASSIGNED, 400 inválido, 404 no encontrado.
- `SCRUM-20`: 200 con ADMIN, 401 sin token, 403 sin privilegios, 400 permisos inválidos, 404 usuario no encontrado.

## Reglas de Negocio

### Dominio de autorización
- Un usuario puede tener múltiples roles.
- Un rol puede tener múltiples permisos.
- Si al menos un rol del usuario contiene el permiso requerido, se permite acceso.
- Usuario inactivo debe ser denegado.

### Dominio de permisos internos
- Consulta de permisos (`SCRUM-18`) es endpoint interno para consumo de `authentication-service`.
- Asignación de permiso (`SCRUM-19`) debe ser idempotente.
- Actualización administrativa (`SCRUM-20`) reemplaza el conjunto completo de permisos.

## Workflows del Equipo

### Jira + MCP (`jira-writer`)
1. Consultar historias con `jira_search_issues` usando JQL acotada por proyecto y keys.
2. No crear/comentar/transicionar issues salvo solicitud explícita.
3. Para tareas Ready for Dev: usar `jira_get_my_ready_tasks`.

### OpenSpec
1. Antes de implementar cambios funcionales relevantes, generar propuesta:
```bash
openspec propose <issue-key>
```
2. Preparar `proposal.md`, `tasks.md` y `design.md` cuando haya impacto arquitectónico.
3. No aplicar (`openspec apply`) sin instrucción explícita.

## Definición de Ready
- Historia Jira entendida con criterios de aceptación claros.
- Contrato API definido (request/response/códigos HTTP).
- Dependencias identificadas (security, persistencia, integraciones internas).
- Casos de prueba principales definidos.

## Definición de Done
- Implementación completa de la historia sin romper contratos existentes.
- Pruebas unitarias/integración relevantes en verde.
- Documentación OpenAPI actualizada.
- Reglas de seguridad y auditoría cubiertas.
- Sin secretos ni datos sensibles expuestos.

## Decisiones Pendientes de Confirmar

> Pendiente de confirmar: modelo de datos final para usuarios, roles y permisos (tablas/relaciones exactas).

> Pendiente de confirmar: estrategia definitiva de validación JWT (firma, expiración, revocación).

> Pendiente de confirmar: mecanismo de restricción "uso interno" para endpoints de permisos (mTLS, red privada, gateway, allowlist).

> Pendiente de confirmar: esquema de persistencia para auditoría (`audit_log`) y política de retención.

## Restricciones para Agentes

- No inventar frameworks, infraestructura cloud o brokers no presentes en Jira o en el repo.
- No borrar archivos ni revertir cambios del usuario sin autorización explícita.
- No introducir secretos en commits ni en archivos de configuración.
- No asumir que la arquitectura ya está implementada: actualmente el repo está en estado base de bootstrap.

---

**Última actualización**: 2026-05-15
**Versión**: 1.0
