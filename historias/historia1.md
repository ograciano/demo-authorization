## Historia de Usuario Enriquecida

### Contexto y Objetivo de Negocio
El sistema consumidor necesita validar autorización por permisos para decidir si ejecuta o bloquea operaciones protegidas. El objetivo es separar autenticación de autorización y responder de forma consistente si una acción está permitida.

### Descripción Funcional Detallada
Se implementa `POST /api/authorization/check-permission` para recibir `userId`, `resource` y `action`, resolver permisos del usuario a partir de sus roles y devolver `allowed=true|false` con una razón funcional. Si el usuario está inactivo, o el recurso/acción no corresponde a permisos válidos, la respuesta es denegada. El endpoint no autentica usuarios; solo evalúa autorización.

### Criterios de Aceptación
- [ ] Dado un usuario activo con al menos un rol que contiene el permiso requerido, retorna `allowed=true`.
- [ ] Dado un usuario activo sin el permiso requerido, retorna `allowed=false`.
- [ ] Dado un usuario inactivo, retorna `allowed=false`.
- [ ] Dado request inválido (campos faltantes o formato inválido), responde HTTP 400.
- [ ] La respuesta incluye `allowed`, `userId`, `resource`, `action` y `reason`.
- [ ] Se registran eventos de autorización denegada para auditoría.

### Casos Límite y Validaciones
- `userId` nulo, `<= 0` o no numérico: HTTP 400.
- `resource` o `action` vacíos: HTTP 400.
- Variaciones de mayúsculas/minúsculas en `resource/action`: normalizar o rechazar según catálogo definido.
- Usuario sin roles asignados: `allowed=false`.
- Usuario con múltiples roles y permisos repetidos: no duplicar ni alterar evaluación.

### Requisitos No Funcionales Relevantes
- Seguridad: no exponer estructura interna de roles/permisos en mensajes de error.
- Rendimiento: evaluación en baja latencia para uso sincrónico en flujos protegidos.
- Observabilidad: trazabilidad de denegaciones con `userId`, recurso, acción y motivo.
- Confiabilidad: formato estándar de errores para 4xx/5xx.

### Consideraciones Técnicas (opcional)
- Backend / API: endpoint `POST /api/authorization/check-permission`.
- Dominio: Usuario -> Roles -> Permisos (`RESOURCE:ACTION`).
- Documentación: OpenAPI con respuestas 200 y 400; `@SecurityRequirements()` vacío si endpoint público.

### Requisitos de Testing (alto nivel)
- Pruebas unitarias: regla “al menos un rol con permiso concede acceso”, usuario inactivo, usuario sin roles.
- Pruebas de integración: resolución de permisos desde repositorios y formato de respuesta.
- Pruebas de contrato: request/response y códigos HTTP documentados.

### Requisitos de Documentación
- Documentar contrato del endpoint y ejemplos de respuestas permitida/denegada/error.
- Actualizar guía de permisos con nomenclatura `RESOURCE:ACTION`.
