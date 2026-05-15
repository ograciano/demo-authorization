# Contratos API para `authentication-service` (HU3 y HU4)

## Datos de conexión

- Servicio: `authorization`
- Puerto local: `8081`
- Base URL local: `http://localhost:8081`
- Content-Type: `application/json`

## Esquema común de error

```json
{
  "error": "Bad Request | Not Found",
  "message": "descripcion funcional",
  "timestamp": "2026-05-15T00:00:00Z",
  "details": {}
}
```

Notas:
- `details` puede venir vacío (`{}`) o con campos de validación.
- En validaciones de bean/path puede incluir mensajes técnicos del validador.

---

## HU3 - Consultar permisos vigentes de usuario

### Endpoint

- Método: `GET`
- Ruta: `/api/permissions/users/{userId}`
- Propósito: entregar permisos dinámicos para construcción de JWT en `authentication-service`.

### Request

- Path param obligatorio:
  - `userId` (`Long`, `> 0`)
- Body: no aplica.

### Response 200 (usuario existente)

```json
{
  "userId": 10,
  "permissions": ["REPORT:DOWNLOAD", "REPORT:READ"],
  "timestamp": "2026-05-15T00:00:00Z"
}
```

Reglas de salida:
- `permissions` se entrega normalizado a mayúsculas.
- `permissions` se entrega sin duplicados.
- `permissions` se entrega ordenado ascendente.
- Si no tiene permisos: `permissions: []`.

### Casos de uso del servicio HU3

1. Usuario existe y tiene permisos -> `200`.
2. Usuario existe y no tiene permisos -> `200` con `permissions=[]`.
3. `userId <= 0` -> `400 Bad Request`.
4. Usuario no existe -> `404 Not Found` (`message`: `Usuario no encontrado`).

---

## HU4 - Asignar permiso inicial (idempotente)

### Endpoint

- Método: `POST`
- Ruta: `/api/permissions/users/{userId}`
- Propósito: asignar un permiso a usuario para onboarding/registro desde `authentication-service`.

### Request

- Path param obligatorio:
  - `userId` (`Long`, `> 0`)
- Body:

```json
{
  "permission": "REPORT:DOWNLOAD"
}
```

Reglas de entrada:
- `permission` obligatorio, no nulo, no vacío.
- El servicio normaliza a mayúsculas y trim.
- Debe existir en catálogo permitido.

Catálogo permitido actual (`application.properties`):
- `REPORT:READ`
- `REPORT:DOWNLOAD`
- `ADMIN:MANAGE_PERMISSIONS`

### Response 200 - Asignado

```json
{
  "userId": 10,
  "permission": "REPORT:DOWNLOAD",
  "status": "ASSIGNED",
  "timestamp": "2026-05-15T00:00:00Z"
}
```

### Response 200 - Idempotente (ya existía)

```json
{
  "userId": 10,
  "permission": "REPORT:DOWNLOAD",
  "status": "ALREADY_ASSIGNED",
  "timestamp": "2026-05-15T00:00:01Z"
}
```

### Casos de uso del servicio HU4

1. Usuario existe y permiso válido no asignado -> `200` con `status=ASSIGNED`.
2. Usuario existe y permiso válido ya asignado -> `200` con `status=ALREADY_ASSIGNED`.
3. `permission` fuera de catálogo -> `400 Bad Request` (`message`: `Permiso invalido o fuera de catalogo`).
4. `permission` nulo o vacío -> `400 Bad Request`.
5. `userId <= 0` -> `400 Bad Request`.
6. Usuario no existe -> `404 Not Found` (`message`: `Usuario no encontrado`).

