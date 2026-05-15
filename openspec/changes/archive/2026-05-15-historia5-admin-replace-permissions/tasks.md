## 1. Contrato API y seguridad administrativa

- [x] 1.1 Crear DTO request para reemplazo con `permissions[]` y validaciones (`null`, formato, duplicados)
- [x] 1.2 Crear DTO response con usuario objetivo y permisos finales aplicados
- [x] 1.3 Implementar endpoint `PUT /api/admin/users/{userId}/permissions` documentado con respuestas
      `200/400/401/403/404`
- [x] 1.4 Configurar autorizacion para permitir acceso solo a actores ADMIN autenticados

## 2. Logica de negocio transaccional

- [x] 2.1 Implementar servicio administrativo para validar usuario objetivo y catalogo de permisos permitidos
- [x] 2.2 Implementar deduplicacion de permisos solicitados antes de persistencia
- [x] 2.3 Implementar reemplazo atomico del conjunto de permisos (all-or-nothing)
- [x] 2.4 Implementar soporte de revocacion total cuando `permissions=[]`

## 3. Auditoria y observabilidad

- [x] 3.1 Registrar auditoria con admin ejecutor, usuario objetivo y permisos antes/despues
- [x] 3.2 Registrar errores de autorizacion y validacion sin exponer datos sensibles

## 4. Testing

- [x] 4.1 Crear pruebas unitarias para reglas de validacion y reemplazo transaccional
- [x] 4.2 Crear pruebas de seguridad para escenarios `401`, `403` y `200`
- [x] 4.3 Crear pruebas de integracion para `200`, `400`, `404` y consistencia post-actualizacion
- [x] 4.4 Crear prueba de rollback ante falla intermedia para validar atomicidad

## 5. Cierre funcional

- [x] 5.1 Actualizar OpenAPI y documentacion interna de politica ADMIN y auditoria
- [x] 5.2 Ejecutar suite de pruebas relevante y corregir regresiones antes de cerrar el cambio
