## 1. Contrato API y DTOs

- [x] 1.1 Crear DTO de respuesta para consulta de permisos con campos `userId`, `permissions` y `timestamp`
- [x] 1.2 Implementar endpoint `GET /api/permissions/users/{userId}` y documentar respuestas `200/400/404`
- [x] 1.3 Validar parametro `userId` con restriccion `> 0` para respuesta `400`

## 2. Logica de consulta de permisos

- [x] 2.1 Implementar servicio para resolver usuario por `userId` y construir permisos vigentes
- [x] 2.2 Implementar manejo de usuario inexistente con excepcion de dominio y respuesta `404`
- [x] 2.3 Deduplicar y normalizar permisos para responder lista estable

## 3. Persistencia y rendimiento

- [x] 3.1 Ajustar repositorio para cargar relaciones necesarias (usuarios, roles y permisos) en una consulta eficiente
- [x] 3.2 Verificar respuesta consistente en escenarios de usuario sin roles/permisos

## 4. Testing

- [x] 4.1 Crear pruebas unitarias del servicio para casos: usuario con permisos, sin permisos y no existente
- [x] 4.2 Crear pruebas de integracion para `200` con permisos, `200` con lista vacia, `400` por `userId` invalido y
      `404` por usuario inexistente
- [x] 4.3 Crear prueba de contrato para estructura JSON (`userId`, `permissions[]`, `timestamp`)

## 5. Documentacion y cierre

- [x] 5.1 Actualizar OpenAPI con descripcion de uso interno y ejemplos de respuestas exitosas y de error
- [x] 5.2 Ejecutar suite de pruebas relevante y corregir regresiones antes de cerrar el cambio
