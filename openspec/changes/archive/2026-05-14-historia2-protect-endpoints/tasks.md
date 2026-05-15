## 1. Seguridad JWT base

- [x] 1.1 Definir/ajustar componente JWT del proyecto para validar firma, expiracion y claims obligatorios
- [x] 1.2 Implementar `JwtAuthenticationFilter` para leer header bearer y poblar `SecurityContext`
- [x] 1.3 Implementar `AuthenticationEntryPoint` para respuestas `401` consistentes en fallas de autenticacion

## 2. Autorizacion por permiso

- [x] 2.1 Implementar evaluacion de permiso `REPORT:DOWNLOAD` para el usuario autenticado
- [x] 2.2 Configurar `SecurityConfig` para proteger `GET /api/reports/{reportId}/download` y responder `403` cuando
      falte permiso
- [x] 2.3 Asegurar que seguridad se ejecute antes de logica funcional del endpoint protegido

## 3. Endpoint protegido y contrato

- [x] 3.1 Crear endpoint `GET /api/reports/{reportId}/download` con controlador y servicio de descarga
- [x] 3.2 Definir respuestas funcionales para `200` y manejo de recurso inexistente segun dominio (ej. `404`)
- [x] 3.3 Actualizar OpenAPI con `@SecurityRequirement(name=\"bearerAuth\")` y respuestas `200/401/403`

## 4. Pruebas y validacion

- [x] 4.1 Crear pruebas unitarias para filtro JWT y evaluacion de permiso por endpoint
- [x] 4.2 Crear pruebas de integracion de seguridad para escenarios `200`, `401` (sin token/invalido) y `403`
- [x] 4.3 Verificar por prueba que la logica funcional no se ejecuta cuando seguridad bloquea la solicitud

## 5. Observabilidad y cierre

- [x] 5.1 Agregar logging de eventos 401/403 sin exponer token ni informacion sensible
- [x] 5.2 Ejecutar suite de pruebas y corregir regresiones antes de cerrar el cambio
