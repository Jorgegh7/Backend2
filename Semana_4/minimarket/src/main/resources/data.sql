-- Roles iniciales del sistema
-- Se insertan automaticamente al iniciar la aplicacion
-- Requiere spring.jpa.defer-datasource-initialization=true en application.properties
INSERT INTO rol (nombre) VALUES ('ADMIN');
INSERT INTO rol (nombre) VALUES ('EMPLEADO');
INSERT INTO rol (nombre) VALUES ('CLIENTE');
