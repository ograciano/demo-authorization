-- Seed H2 para historias 3 y 4 (SCRUM-18 / SCRUM-19)
-- Script idempotente: puede ejecutarse varias veces sin duplicar registros.

-- 1) Permisos base
MERGE INTO permission (code) KEY(code) VALUES ('ADMIN');
MERGE INTO permission (code) KEY(code) VALUES ('REPORT:DOWNLOAD');
MERGE INTO permission (code) KEY(code) VALUES ('REPORT:READ');
MERGE INTO permission (code) KEY(code) VALUES ('INTERNAL:PERMISSIONS_READ');

-- 2) Roles/perfiles
MERGE INTO role (name, active) KEY(name) VALUES ('ROLE_ADMIN', TRUE);
MERGE INTO role (name, active) KEY(name) VALUES ('ROLE_AUTH_SERVICE', TRUE);
MERGE INTO role (name, active) KEY(name) VALUES ('ROLE_REPORT_VIEWER', TRUE);
MERGE INTO role (name, active) KEY(name) VALUES ('ROLE_REPORT_OPERATOR', TRUE);
MERGE INTO role (name, active) KEY(name) VALUES ('ROLE_LEGACY_DISABLED', FALSE);

-- 3) Usuarios de prueba
MERGE INTO app_user (username, active) KEY(username) VALUES ('auth-admin-active', TRUE);
MERGE INTO app_user (username, active) KEY(username) VALUES ('auth-internal-active', TRUE);
MERGE INTO app_user (username, active) KEY(username) VALUES ('auth-viewer-active', TRUE);
MERGE INTO app_user (username, active) KEY(username) VALUES ('auth-operator-active', TRUE);
MERGE INTO app_user (username, active) KEY(username) VALUES ('auth-empty-active', TRUE);
MERGE INTO app_user (username, active) KEY(username) VALUES ('auth-inactive-admin', FALSE);
MERGE INTO app_user (username, active) KEY(username) VALUES ('auth-mixed-active', TRUE);

-- 4) role_permission
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r
JOIN permission p ON p.code = 'ADMIN'
WHERE r.name = 'ROLE_ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r
JOIN permission p ON p.code = 'INTERNAL:PERMISSIONS_READ'
WHERE r.name = 'ROLE_ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r
JOIN permission p ON p.code = 'REPORT:DOWNLOAD'
WHERE r.name = 'ROLE_ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r
JOIN permission p ON p.code = 'REPORT:READ'
WHERE r.name = 'ROLE_ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r
JOIN permission p ON p.code = 'INTERNAL:PERMISSIONS_READ'
WHERE r.name = 'ROLE_AUTH_SERVICE'
AND NOT EXISTS (
    SELECT 1 FROM role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r
JOIN permission p ON p.code = 'REPORT:READ'
WHERE r.name = 'ROLE_REPORT_VIEWER'
AND NOT EXISTS (
    SELECT 1 FROM role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r
JOIN permission p ON p.code = 'REPORT:DOWNLOAD'
WHERE r.name = 'ROLE_REPORT_OPERATOR'
AND NOT EXISTS (
    SELECT 1 FROM role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- 5) user_role
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM app_user u
JOIN role r ON r.name = 'ROLE_ADMIN'
WHERE u.username = 'auth-admin-active'
AND NOT EXISTS (
    SELECT 1 FROM user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
);

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM app_user u
JOIN role r ON r.name = 'ROLE_AUTH_SERVICE'
WHERE u.username = 'auth-internal-active'
AND NOT EXISTS (
    SELECT 1 FROM user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
);

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM app_user u
JOIN role r ON r.name = 'ROLE_REPORT_VIEWER'
WHERE u.username = 'auth-viewer-active'
AND NOT EXISTS (
    SELECT 1 FROM user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
);

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM app_user u
JOIN role r ON r.name = 'ROLE_REPORT_OPERATOR'
WHERE u.username = 'auth-operator-active'
AND NOT EXISTS (
    SELECT 1 FROM user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
);

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM app_user u
JOIN role r ON r.name = 'ROLE_ADMIN'
WHERE u.username = 'auth-inactive-admin'
AND NOT EXISTS (
    SELECT 1 FROM user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
);

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM app_user u
JOIN role r ON r.name = 'ROLE_REPORT_VIEWER'
WHERE u.username = 'auth-mixed-active'
AND NOT EXISTS (
    SELECT 1 FROM user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
);

-- 6) user_permission (para validar mezcla rol + permiso directo y idempotencia SCRUM-19)
INSERT INTO user_permission (user_id, permission_id)
SELECT u.id, p.id
FROM app_user u
JOIN permission p ON p.code = 'INTERNAL:PERMISSIONS_READ'
WHERE u.username = 'auth-mixed-active'
AND NOT EXISTS (
    SELECT 1 FROM user_permission up WHERE up.user_id = u.id AND up.permission_id = p.id
);

-- 7) Consulta rapida opcional de validacion
-- SELECT u.id, u.username, u.active, p.code
-- FROM app_user u
-- LEFT JOIN user_role ur ON ur.user_id = u.id
-- LEFT JOIN role r ON r.id = ur.role_id
-- LEFT JOIN role_permission rp ON rp.role_id = r.id
-- LEFT JOIN permission p ON p.id = rp.permission_id
-- ORDER BY u.id, p.code;
