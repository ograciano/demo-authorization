# Instrucciones para el Agente IA

## Contexto del Proyecto

- **Nombre**: `demo-authorization`
- **Tipo**: API REST
- **Propósito**: Servicio de autorización para validar y administrar permisos por usuario/rol, desacoplado del proceso de autenticación.
- **Arquitectura**: Spring Boot por capas (controller, service, repository, dto, entity, security, exception, config).

## Stack Tecnológico

### Core
- **Lenguaje**: Java 17
- **Framework**: Spring Boot `3.5.14`
- **Base de datos**: H2 (runtime actual), preparado para JPA
- **Documentación API**: Springdoc OpenAPI `2.8.16`

### Testing
- **Framework**: JUnit 5 + Spring Boot Test + Spring Security Test
- **Integración**: Testcontainers (`spring-boot-testcontainers`, `org.testcontainers:junit-jupiter`)

### Herramientas
- **Dependencias / build**: Maven (`pom.xml`)
- **Ejecución local**: `./mvnw spring-boot:run`

## Estructura del Proyecto

```text
demo-authorization/
├── src/
│   ├── main/
│   │   ├── java/com/vass/authorization/
│   │   │   ├── controller/         # Endpoints REST
│   │   │   ├── service/            # Lógica de negocio
│   │   │   ├── repository/         # Acceso a datos (JPA)
│   │   │   ├── dto/                # Contratos request/response
│   │   │   ├── entity/             # Entidades de dominio persistente
│   │   │   ├── security/           # JWT, filtros, evaluadores, handlers
│   │   │   ├── exception/          # Excepciones y manejo global
│   │   │   └── config/             # Configuración (OpenAPI/Security)
│   │   └── resources/
│   └── test/java/com/vass/authorization/
├── historias/                      # Historias funcionales base
├── AGENTS.md
└── pom.xml
```

## Convenciones de Código

### Nomenclatura

| Elemento | Convención | Ejemplo |
|----------|------------|---------|
| Variables | `camelCase` | `userId` |
| Métodos | `camelCase` | `checkPermission` |
| Clases | `PascalCase` | `AuthorizationService` |
| Constantes | `UPPER_SNAKE_CASE` | `REPORT_DOWNLOAD` |
| Archivos Java | `PascalCase.java` | `PermissionController.java` |

### Formato de Código

- **Líneas**: máximo 120 caracteres
- **Indentación**: 4 espacios
- **Strings**: comillas dobles
- **Imports**: sin wildcard, ordenados por grupos (Java, Spring, terceros, proyecto)

### Documentación

- **Comentarios**: español técnico claro
- **API**: todo endpoint debe incluir `@Operation` y `@ApiResponse` relevantes
- **README**: mantener instrucciones mínimas de ejecución y pruebas

## Patrones a Seguir

### Manejo de Errores

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ErrorResponse.of("Bad Request", ex.getMessage()));
    }
}
```

### Validación de Inputs

```java
public record CheckPermissionRequestDTO(
        @NotNull @Positive Long userId,
        @NotBlank String resource,
        @NotBlank String action) {}
```

### Logging y Auditoría

```java
if (!allowed) {
    log.warn("Authorization denied userId={} permission={}:{}", userId, resource, action);
}
```

## Restricciones de Seguridad

### PROHIBIDO (NUNCA hacer)

1. **Exponer secretos o tokens completos en logs**
2. **Ejecutar lógica de negocio antes de validar autenticación/autorización**
3. **Hardcodear permisos en authentication-service o en controladores**
4. **Retornar detalles internos de roles/tablas en mensajes de error**
5. **Duplicar lógica de autorización por endpoint cuando puede centralizarse**

### OBLIGATORIO (SIEMPRE hacer)

1. **Separar autenticación de autorización**.
2. **Responder 401 cuando falta token o es inválido**.
3. **Responder 403 cuando el usuario autenticado no tiene permiso**.
4. **Usar validación de request (`jakarta.validation`) y responder 400 en entradas inválidas**.
5. **Registrar eventos de denegación y cambios administrativos de permisos (auditoría)**.

## Estándares de Testing

### Estructura de Tests

```text
src/test/java/com/vass/authorization/
├── controller/
├── service/
├── security/
└── dto/
```

### Nomenclatura de Tests

```java
// Patrón: test<Metodo>_<Escenario>_<Resultado>
void testCheckPermission_UserInactive_ReturnsDenied() { }
```

### Estructura de Test (AAA)

```java
@Test
void testAssignPermission_AlreadyAssigned_ReturnsIdempotentStatus() {
    // Arrange
    var request = new PermissionAssignmentDTO("REPORT_READ");

    // Act
    var response = permissionService.assignPermission(1L, request.permission());

    // Assert
    assertEquals("ALREADY_ASSIGNED", response.status());
}
```

## Reglas de Negocio

### Modelo de Permisos

- Un usuario puede tener uno o varios roles.
- Un rol puede tener uno o varios permisos.
- Si al menos un rol contiene el permiso requerido, el acceso es permitido.
- Si usuario está inactivo, el acceso es denegado.
- Convención de permisos: `RESOURCE:ACTION` (ej. `REPORT:DOWNLOAD`).

### Endpoints Funcionales (Historias 1-5)

1. `POST /api/authorization/check-permission`
   - Evalúa autorización por `userId`, `resource`, `action`.
   - Respuesta funcional `allowed=true|false`.
2. `GET /api/reports/{reportId}/download` (protegido)
   - Requiere JWT válido y permiso `REPORT:DOWNLOAD`.
3. `GET /api/permissions/users/{userId}` (interno)
   - Retorna permisos dinámicos para construcción de JWT en autenticación.
4. `POST /api/permissions/users/{userId}` (interno)
   - Asigna permiso inicial; operación idempotente.
5. `PUT /api/admin/users/{userId}/permissions` (admin)
   - Reemplaza permisos de usuario de forma atómica; requiere privilegio administrativo.

### Reglas de Validación HTTP

- `400`: request inválido (`userId <= 0`, campos vacíos/nulos, permiso inválido).
- `401`: token ausente, malformado, inválido o expirado.
- `403`: autenticado sin permisos suficientes.
- `404`: usuario/recurso objetivo inexistente.

## Workflows del Equipo

### Commits

```text
<tipo>(<alcance>): <descripción>

Tipos: feat, fix, refactor, test, docs, chore
```

### Branches

- `main`: rama principal
- `feature/<descripcion-corta>`: nuevas funcionalidades
- `fix/<descripcion-corta>`: correcciones

### Pull Requests

- Debe incluir resumen funcional y técnico.
- Debe incluir evidencia de pruebas (unitarias/integración) según cambio.
- Debe actualizar OpenAPI/Swagger cuando cambian contratos.

## Ejemplos de Referencia

### Service

```java
public AuthorizationResponse checkPermission(CheckPermissionRequestDTO request) {
    boolean allowed = permissionResolver.userHasPermission(request.userId(), request.resource(), request.action());
    return new AuthorizationResponse(allowed, request.userId(), request.resource(), request.action(),
            allowed ? "Permiso concedido" : "Permiso denegado");
}
```

### Controller

```java
@PostMapping("/api/authorization/check-permission")
public ResponseEntity<AuthorizationResponse> checkPermission(@Valid @RequestBody CheckPermissionRequestDTO request) {
    return ResponseEntity.ok(authorizationService.checkPermission(request));
}
```

### Test

```java
@Test
void testCheckPermission_WithoutPermission_ReturnsDenied() {
    // Arrange
    var request = new CheckPermissionRequestDTO(2L, "REPORT", "DELETE");

    // Act
    var response = authorizationService.checkPermission(request);

    // Assert
    assertFalse(response.allowed());
}
```

---

**Última actualización**: 2026-05-14
**Versión**: 1.0
