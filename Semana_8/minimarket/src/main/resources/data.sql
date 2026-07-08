INSERT INTO rol (id, nombre) VALUES (1, 'ADMIN');
INSERT INTO categoria (id, nombre) VALUES (1, 'Lácteos');
INSERT INTO usuario (id, username, password) VALUES (1, 'admin', '$2a$10$DPQL2g59EBhi9srYz1dfNuuDRmYc92/Y8eufvVLU4cEq0r2Pkho9S');
INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (1, 1);
ALTER TABLE usuario ALTER COLUMN id RESTART WITH 2;