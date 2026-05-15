## 1. API y contratos

- [x] 1.1 Crear/ajustar DTO de request para `check-permission` con validaciones Jakarta (`userId`, `resource`,
      `action`)
- [x] 1.2 Crear/ajustar DTO de response con campos `allowed`, `userId`, `resource`, `action`, `reason`
- [x] 1.3 Implementar endpoint `POST /api/authorization/check-permission` con anotaciones OpenAPI y respuestas 200/400

## 2. Regla de autorizacion

- [x] 2.1 Implementar en servicio la evaluacion por modelo Usuario->Roles->Permisos con regla "al menos un rol con
      permiso concede acceso"
- [x] 2.2 Aplicar validaciones funcionales de usuario inactivo, usuario sin roles y permiso ausente devolviendo
      `allowed=false`
- [x] 2.3 Normalizar `resource/action` al formato canonico `RESOURCE:ACTION` antes de comparar permisos

## 3. Errores y auditoria

- [x] 3.1 Asegurar manejo global de errores para request invalido (`400`) con formato consistente
- [x] 3.2 Registrar auditoria de denegaciones con `userId`, `resource`, `action` y motivo, sin exponer datos sensibles

## 4. Testing

- [x] 4.1 Crear pruebas unitarias del servicio para casos: permitido, denegado por falta de permiso, usuario inactivo
- [x] 4.2 Crear pruebas unitarias/controlador para validaciones HTTP 400 en entradas invalidas
- [x] 4.3 Crear pruebas de integracion para contrato de respuesta y evaluacion de permisos desde repositorio

## 5. Documentacion y validacion final

- [x] 5.1 Actualizar documentacion OpenAPI con ejemplos de respuesta permitida/denegada/error
- [x] 5.2 Ejecutar suite de pruebas relevante y ajustar hasta pasar sin regresiones
