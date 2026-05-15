## 1. Contrato y validaciones de entrada

- [x] 1.1 Crear DTO request con campo obligatorio `permission` y validaciones de formato
- [x] 1.2 Crear DTO response con `userId`, `permission`, `status`, `timestamp`
- [x] 1.3 Implementar endpoint `POST /api/permissions/users/{userId}` con validacion `userId > 0` y OpenAPI
      `200/400/404`

## 2. Logica de asignacion idempotente

- [x] 2.1 Implementar servicio para validar usuario existente y resolver permiso solicitado
- [x] 2.2 Implementar validacion de lista blanca de permisos permitidos
- [x] 2.3 Implementar asignacion con estado `ASSIGNED` cuando crea relacion
- [x] 2.4 Implementar flujo idempotente con estado `ALREADY_ASSIGNED` cuando relacion ya existe

## 3. Persistencia y consistencia

- [x] 3.1 Ajustar repositorios/consultas para detectar existencia de asignacion usuario-permiso sin duplicar datos
- [x] 3.2 Asegurar comportamiento consistente ante reintentos concurrentes de la misma asignacion

## 4. Testing

- [x] 4.1 Crear pruebas unitarias del servicio para catalogo valido/invalido y estados `ASSIGNED`/`ALREADY_ASSIGNED`
- [x] 4.2 Crear pruebas de integracion para `200 ASSIGNED`, `200 ALREADY_ASSIGNED`, `400` y `404`
- [x] 4.3 Verificar por prueba contrato de respuesta con `userId`, `permission`, `status`, `timestamp`

## 5. Documentacion y verificacion final

- [x] 5.1 Actualizar OpenAPI y ejemplos para semantica idempotente del endpoint interno
- [x] 5.2 Ejecutar suite de pruebas relevante y corregir regresiones antes de cerrar el cambio
