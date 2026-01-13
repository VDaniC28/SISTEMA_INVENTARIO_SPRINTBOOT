USE sisinven;

--                                      INSERCION DE DATOS

INSERT INTO roles (nombre) VALUES ('ROLE_ADMINISTRADOR');

INSERT INTO empresas (nombre, ruc, direccion, telefono, estado, fechaRegistro) 
VALUES ('Calzado D-JHONEY', '20600336194', 'WIRACOCHA NRO. 831 SEC. RIO SECO-EL PORVENIR', '948040420', true, CURDATE());

INSERT INTO empresas (nombre, ruc, direccion, telefono, estado, fechaRegistro) 
VALUES ('Polleria-Wanka', '20600336132', 'WIRACOCHA NRO. 831 SEC. RIO SECO-EL PORVENIR', '948040420', true, CURDATE());

INSERT INTO usuarios (username, email, password, Estado, FechaRegistro) 
VALUES ('admin', 'admin@appsistema.com', '$2a$10$lA3bnZiGmIW2Y1KzZjDnZeO4s4TvlseI1wCkDq6nh4TBakwDTBKkS', true, CURDATE());

SELECT idRol FROM roles WHERE nombre = 'ROLE_ADMINISTRADOR';
SELECT idEmpresa FROM empresas WHERE nombre = 'Calzado D-JHONEY';
SELECT idUsuario FROM usuarios WHERE username = 'admin';

-- Asocia el usuario 'admin' (idUsuario=1) con el rol 'ADMINISTRADOR' (idRol=1)
INSERT INTO usuario_roles (idUsuario, idRol) VALUES (1, 1);

-- Asocia el usuario 'admin' (idUsuario=1) con la empresa principal (idEmpresa=1)
INSERT INTO usuario_empresas (idUsuario, idEmpresa) VALUES (1, 1);

SELECT
  u.idUsuario,
  u.username
FROM usuarios AS u
INNER JOIN usuario_roles AS ur
  ON u.idUsuario = ur.idUsuario
INNER JOIN roles AS r
  ON ur.idRol = r.idRol
WHERE
  r.nombre = 'ROLE_VENDEDOR';

select * from salida_inventario;
select * from salidas_inventario_detalle;
select * from inventario_almacenes;
INSERT INTO proveedores (nombreProveedor, tipoDocumento, numeroDocumento, tipoPersona, email, telefono, direccion)
VALUES 
('Proveedor Uno', 'DNI', '12345678', 'NATURAL', 'proveedoruno@example.com', '123456789', 'Calle Falsa 123'),
('Proveedor Dos', 'RUC', '12345678901', 'JURIDICA', 'proveedordos@example.com', '987654321', 'Avenida Siempre Viva 742'),
('Proveedor Tres', 'DNI', '87654321', 'NATURAL', 'proveedortres@example.com', '321654987', 'Calle Real 456');

SELECT * FROM salidas_inventario_detalle WHERE idSalida = 1;

ALTER TABLE suministros MODIFY COLUMN imagenURL VARCHAR(2048);
select * from guias_remision_detalle;
SELECT
    GD.idDetalleGuia,
    GD.numeroItem,
    GD.cantidad
FROM
    guias_remision_detalle GD
WHERE
    GD.idGuia = 2;

ALTER TABLE backups
ALTER COLUMN tamanoArchivo SET DEFAULT 0;