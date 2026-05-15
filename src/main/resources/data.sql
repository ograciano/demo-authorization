INSERT INTO permissions (id, code) VALUES (1, 'REPORT:DOWNLOAD');
INSERT INTO permissions (id, code) VALUES (2, 'REPORT:READ');
INSERT INTO permissions (id, code) VALUES (3, 'ADMIN:MANAGE_PERMISSIONS');

INSERT INTO roles (id, name) VALUES (1, 'REPORT_DOWNLOADER');
INSERT INTO roles (id, name) VALUES (2, 'REPORT_READER');
INSERT INTO roles (id, name) VALUES (3, 'ADMIN_PERMISSION_MANAGER');

INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 1);
INSERT INTO role_permissions (role_id, permission_id) VALUES (2, 2);
INSERT INTO role_permissions (role_id, permission_id) VALUES (3, 3);

INSERT INTO users (id, active) VALUES (1, TRUE);
INSERT INTO users (id, active) VALUES (2, TRUE);
INSERT INTO users (id, active) VALUES (3, FALSE);

INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2);
INSERT INTO user_roles (user_id, role_id) VALUES (3, 1);

ALTER TABLE permissions ALTER COLUMN id RESTART WITH 100;
ALTER TABLE roles ALTER COLUMN id RESTART WITH 100;
ALTER TABLE users ALTER COLUMN id RESTART WITH 100;
