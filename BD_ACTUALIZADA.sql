-- DROP DATABASE IF EXISTS sisinven;
CREATE DATABASE sisinven;
USE sisinven;

-- =====================================================
-- 				MÓDULO DE SEGURIDAD Y USUARIOS
-- =====================================================

CREATE TABLE roles (
    idRol INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(70) NOT NULL UNIQUE
);

-- Tabla: Usuarios
CREATE TABLE usuarios (
    idUsuario INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    Estado BOOLEAN DEFAULT TRUE,
    FechaRegistro date
);

-- Tabla: Relación usuario - roles
CREATE TABLE usuario_roles (
    idUsuario INT,
    idRol INT,
    PRIMARY KEY (idUsuario, idRol),
    FOREIGN KEY (idUsuario) REFERENCES usuarios(idUsuario) ON DELETE CASCADE,
    FOREIGN KEY (idRol) REFERENCES roles(idRol) ON DELETE CASCADE
);

-- =====================================================
-- MÓDULO DE EMPRESAS (Para selección al inicio de sesión)
-- =====================================================

CREATE TABLE empresas (
    idEmpresa INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    ruc VARCHAR(20) UNIQUE,
    direccion VARCHAR(150),
    telefono VARCHAR(20),
    estado BOOLEAN DEFAULT TRUE,
    fechaRegistro DATE
);

-- Tabla: Relación usuario - empresas (muchos a muchos)
CREATE TABLE usuario_empresas (
    idUsuario INT,
    idEmpresa INT,
    PRIMARY KEY (idUsuario, idEmpresa),
    FOREIGN KEY (idUsuario) REFERENCES usuarios(idUsuario) ON DELETE CASCADE,
    FOREIGN KEY (idEmpresa) REFERENCES empresas(idEmpresa) ON DELETE CASCADE
);

-- =====================================================
-- 				MÓDULO DE PROVEEDORES
-- =====================================================

create table proveedores(
	idProveedor INT AUTO_INCREMENT PRIMARY KEY,
    nombreProveedor VARCHAR(150) NOT NULL,
    tipoDocumento ENUM('DNI', 'RUC') NOT NULL,
    numeroDocumento VARCHAR(11) NOT NULL UNIQUE,
    tipoPersona ENUM('JURIDICA', 'NATURAL') NOT NULL,
    email VARCHAR(300) NOT NULL,
    telefono VARCHAR(13), 
    direccion VARCHAR(200),
    estado BOOLEAN DEFAULT TRUE,
    fechaRegistro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_doc_proveedor CHECK (
        (tipoDocumento = 'DNI' AND LENGTH(numeroDocumento) = 8) OR
        (tipoDocumento = 'RUC' AND LENGTH(numeroDocumento) = 11)
    )
);

-- =====================================================
-- 			MÓDULO DE CATÁLOGOS MAESTROS
-- =====================================================

create table marcas(
	idMarca INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL UNIQUE,
    origen VARCHAR(50),
    descripcionMarca VARCHAR(200)
);

create table categorias(
	idCategoria int auto_increment primary key,
	nombreCategoria VARCHAR(150) NOT NULL UNIQUE
);

create table material_principal(
	idMaterialPrincipal INT AUTO_INCREMENT PRIMARY KEY,
    nombreMaterial VARCHAR(150) NOT NULL UNIQUE
);

create table color(
	idColor INT AUTO_INCREMENT PRIMARY KEY,
	nombreColor VARCHAR(150) NOT NULL UNIQUE
);

create table tipo_item(
	idTipoItem INT AUTO_INCREMENT PRIMARY KEY,
	descripcion ENUM('Producto', 'Suministro') NOT NULL
);

create table tallas(
	idTalla INT AUTO_INCREMENT PRIMARY KEY,
    valor VARCHAR(50) NOT NULL UNIQUE
);

create table generos(
	idGenero INT AUTO_INCREMENT PRIMARY KEY,
    nombreGenero VARCHAR(150) NOT NULL UNIQUE
);
create table tipo_Persona(
	idTipoPersona INT AUTO_INCREMENT PRIMARY KEY,
    nombreTipoPersona VARCHAR(50) NOT NULL UNIQUE
);
create table material_suela(
	idMaterialSuela INT AUTO_INCREMENT PRIMARY KEY,
    descripcionSuela VARCHAR(150) NOT NULL UNIQUE
);
-- ciclo de vida y condición: Defectuoso	, En reparación
create table estados_producto(
	idEstadoProducto INT AUTO_INCREMENT PRIMARY KEY,
    descripcionEstado VARCHAR(150) NOT NULL,
    afectaStock BOOLEAN DEFAULT TRUE
);

-- =====================================================
-- 					MÓDULO DE PRODUCTOS
-- =====================================================

create table productos(
	idProducto INT AUTO_INCREMENT PRIMARY KEY,
    serialProducto VARCHAR(150) NOT NULL,
    nombreProducto VARCHAR(150) NOT NULL,
    descripcionProducto VARCHAR(150) NOT NULL,
    
    -- Referencias a catálogos
    idTipoItem int,
    idMarca int,
    idCategoria int,
    idProveedor int,
    idColor int,
    idGenero int,
    idMaterialSuela int,
    idTalla int,
    idMaterialPrincipal int,
    idTipoPersona int,
    idEstadoProducto int,
    
    -- Precios y control de stock
    precioVenta DECIMAL(10,4) NOT NULL,
    stockMinimo INT DEFAULT 0,
    stockMaximo INT DEFAULT 1000,
    
    -- Información adicional
    imagenURL VARCHAR(2048),
    fechaRegistro date,
    
    -- Constraints
    CONSTRAINT chk_precios_producto CHECK (precioVenta > 0),
    CONSTRAINT chk_stock_producto CHECK (stockMinimo >= 0 AND stockMaximo > stockMinimo),
    
    FOREIGN KEY (idTipoItem) REFERENCES tipo_item(idTipoItem) ON DELETE CASCADE,
    FOREIGN KEY (idMarca) REFERENCES marcas(idMarca) ON DELETE CASCADE,
    FOREIGN KEY (idCategoria) REFERENCES categorias(idCategoria) ON DELETE CASCADE,
    FOREIGN KEY (idProveedor) REFERENCES proveedores(idProveedor) ON DELETE CASCADE,
    FOREIGN KEY (idColor) REFERENCES color(idColor) ON DELETE CASCADE,
    FOREIGN KEY (idGenero) REFERENCES generos(idGenero) ON DELETE CASCADE,
    FOREIGN KEY (idMaterialSuela) REFERENCES material_suela(idMaterialSuela) ON DELETE CASCADE,
    FOREIGN KEY (idTalla) REFERENCES tallas(idTalla) ON DELETE CASCADE,
    FOREIGN KEY (idMaterialPrincipal) REFERENCES material_principal(idMaterialPrincipal) ON DELETE CASCADE,
    FOREIGN KEY (idEstadoProducto) REFERENCES estados_producto(idEstadoProducto) ON DELETE CASCADE,
    FOREIGN KEY (idTipoPersona) REFERENCES tipo_Persona(idTipoPersona) ON DELETE CASCADE
);

create table suministros(
	idSuministro INT AUTO_INCREMENT PRIMARY KEY,
    nombreSuministro VARCHAR(150) NOT NULL,
    codigoSuministro VARCHAR(150) NOT NULL,
    
    -- Referencias
    idTipoItem int,
    idProveedor int,

    
    -- Precios y control de stock
    precioCompra DECIMAL(10,4) NOT NULL,
    stockMinimo INT DEFAULT 0,
    stockMaximo INT DEFAULT 1000,
    
    -- Información adicional
    loteActual VARCHAR(50),
    imagenURL VARCHAR(2048),
    fechaRegistro date,
    
    FOREIGN KEY (idTipoItem) REFERENCES tipo_item(idTipoItem) ON DELETE CASCADE,
    FOREIGN KEY (idProveedor) REFERENCES proveedores(idProveedor) ON DELETE CASCADE
   
);
-- =====================================================
-- 				MÓDULO DE ALMACENES E INVENTARIO
-- ====================================================
create table almacenes(
	idAlmacen INT AUTO_INCREMENT PRIMARY KEY,
    nombreAlmacen VARCHAR(150) NOT NULL,
    ubicacion VARCHAR(150) NOT NULL,
    capacidadMaxima int,
    tipoAlmacen ENUM('Principal', 'Sucursal', 'Transito') DEFAULT 'Principal'
);
create table inventario_almacenes (
	idInventario INT AUTO_INCREMENT PRIMARY KEY,
    idAlmacen INT,
    idProducto INT,
    idSuministro INT,
    stock INT NOT NULL DEFAULT 0,
    stockReservado INT DEFAULT 0,
    lote VARCHAR(50),
    
    -- Constraints
    CONSTRAINT chk_item_inventario CHECK (
        (idProducto IS NOT NULL AND idSuministro IS NULL) OR 
        (idProducto IS NULL AND idSuministro IS NOT NULL)
    ),
    CONSTRAINT chk_stock_inventario CHECK (stock >= 0 AND stockReservado >= 0 AND stockReservado <= stock),
    
    FOREIGN KEY (idAlmacen) REFERENCES almacenes(idAlmacen) ON DELETE CASCADE,
    FOREIGN KEY (idProducto) REFERENCES productos(idProducto) ON DELETE CASCADE,
    FOREIGN KEY (idSuministro) REFERENCES suministros(idSuministro) ON DELETE CASCADE,
    
    -- Claves únicas
    UNIQUE KEY uk_almacen_producto_lote (idAlmacen, idProducto, lote),
    UNIQUE KEY uk_almacen_suministro_lote (idAlmacen, idSuministro, lote)
);
-- =====================================================
-- 			MÓDULO DE ÓRDENES DE COMPRA
-- =====================================================

create table ordenes_compras(
	idOrdenes INT AUTO_INCREMENT PRIMARY KEY,
    fechaOrden date,
    fechaEntregaEsperada DATE,
    fechaEntregaReal DATE,	
    idProveedor int,
    idAlmacen int,
    montoSubtotal DECIMAL(12,4) NOT NULL,
    impuestos DECIMAL(12,4) DEFAULT 0,
    descuentos DECIMAL(12,4) DEFAULT 0,
    estadoOrden ENUM('Pendiente', 'Confirmada', 'RecepcionParcial', 'Recibida', 'Cancelada') NOT NULL,
    FOREIGN KEY (idProveedor) REFERENCES proveedores(idProveedor) ON DELETE CASCADE,
    FOREIGN KEY (idAlmacen) REFERENCES almacenes(idAlmacen) ON DELETE CASCADE
);
create table ordenes_compras_detalle(
	idDetalle INT AUTO_INCREMENT PRIMARY KEY,
    idOrdenes int,
    idProducto INT,
    idSuministro INT,
    cantidadSolicitada INT NOT NULL,
    cantidadRecibida INT DEFAULT 0,
    precioUnitario DECIMAL(10,4) NOT NULL,
    descuentoUnitario DECIMAL(10,4) DEFAULT 0,
    impuestoUnitario DECIMAL(10,4) DEFAULT 0,
    
    -- Constraints
    CONSTRAINT chk_item_orden CHECK (
        (idProducto IS NOT NULL AND idSuministro IS NULL) OR 
        (idProducto IS NULL AND idSuministro IS NOT NULL)
    ),
    CONSTRAINT chk_cantidades_orden CHECK (cantidadRecibida >= 0 AND cantidadRecibida <= cantidadSolicitada),
    
    FOREIGN KEY (idOrdenes) REFERENCES ordenes_compras(idOrdenes) ON DELETE CASCADE,
    FOREIGN KEY (idProducto) REFERENCES productos(idProducto) ON DELETE CASCADE,
    FOREIGN KEY (idSuministro) REFERENCES suministros(idSuministro) ON DELETE CASCADE
);

-- =====================================================
-- 			MÓDULO DE CLIENTES Y VENTAS
-- =====================================================
create table clientes(
	idCliente INT AUTO_INCREMENT PRIMARY KEY,
    tipoCliente ENUM('Persona', 'Empresa') NOT NULL,
    tipoDocumento ENUM('DNI', 'RUC', 'Pasaporte') NOT NULL,
    numeroDocumento VARCHAR(20) NOT NULL,
    
    -- Para personas
    nombreCliente VARCHAR(150) NOT NULL,
    
    -- Para empresas
    nombreComercial VARCHAR(150),
    
    -- Datos de contacto
    telefono VARCHAR(13),
    email VARCHAR(100),
    direccion VARCHAR(200),
    
    fechaRegistro DATE,
    estadoValor VARCHAR(100),
    
    -- Constraints
    CONSTRAINT chk_cliente_documento CHECK (
        (tipoDocumento = 'DNI' AND LENGTH(numeroDocumento) = 8) OR
        (tipoDocumento = 'RUC' AND LENGTH(numeroDocumento) = 11) OR
        (tipoDocumento = 'Pasaporte' AND LENGTH(numeroDocumento) BETWEEN 6 AND 20)
    ),
    -- Restricción para validar los valores de estadoValor
    CONSTRAINT chk_estado_cliente CHECK (estadoValor IN ('BUEN PAGADOR', 'MAL PAGADOR')),
    
    -- Claves únicas
    UNIQUE KEY uk_cliente_documento (tipoDocumento, numeroDocumento)
);

-- =====================================================
-- 		TABLAS DE SALIDA E INVENTARIO MEJORADAS
-- =====================================================
create table salida_inventario(
	idSalida INT AUTO_INCREMENT PRIMARY KEY,
    idUsuario INT, -- El usuario que registra la venta
    idCliente INT, -- La empresa en la que ocurre la venta
    idAlmacen INT NOT NULL,
    numeroSalida VARCHAR(50) NOT NULL UNIQUE, -- Número de comprobante/documento
    fechaSalida date NOT NULL,
    tipoSalida ENUM('Venta', 'Transferencia','Devolucion') NOT NULL DEFAULT 'Venta',
    tipoComprobante ENUM('Boleta', 'Factura', 'Guia', 'Nota') DEFAULT 'Boleta',
    
    -- Montos
    subtotal DECIMAL(12,4) NOT NULL,
    impuestos DECIMAL(12,4) DEFAULT 0,
    descuentos DECIMAL(12,4) DEFAULT 0,
    montoTotal DECIMAL(12,4) NOT NULL,
    
    estadoSalida ENUM('Pendiente', 'Completada', 'Anulada') DEFAULT 'Pendiente',
    
    -- Constraints
    CONSTRAINT chk_montos_salida CHECK (
        subtotal >= 0 AND 
        montoTotal >= 0 AND 
        impuestos >= 0 AND 
        descuentos >= 0
    ),
    
    FOREIGN KEY (idUsuario) REFERENCES usuarios(idUsuario) ON DELETE CASCADE,
    FOREIGN KEY (idCliente) REFERENCES clientes(idCliente) ON DELETE CASCADE,
    FOREIGN KEY (idAlmacen) REFERENCES almacenes(idAlmacen) ON DELETE CASCADE
);

create table salidas_inventario_detalle(
	idDetalleSalida INT AUTO_INCREMENT PRIMARY KEY,
    idSalida INT,
    idProducto INT,
    idSuministro INT,
    idAlmacen INT NOT NULL,
    cantidad INT NOT NULL,
    precioUnitario DECIMAL(10,4) NOT NULL,
    descuentoUnitario DECIMAL(10,4) DEFAULT 0,
    impuestoUnitario DECIMAL(10,4) DEFAULT 0,
    subtotalLinea DECIMAL(12,4) NOT NULL,
    lote VARCHAR(50), -- Lote específico si aplica
    
    -- Constraints
    CONSTRAINT chk_item_salida CHECK (
        (idProducto IS NOT NULL AND idSuministro IS NULL) OR 
        (idProducto IS NULL AND idSuministro IS NOT NULL)
    ),
    CONSTRAINT chk_cantidades_salida CHECK (
        cantidad > 0 AND 
        precioUnitario >= 0 AND 
        descuentoUnitario >= 0 AND 
        impuestoUnitario >= 0 AND
        subtotalLinea >= 0
    ),
    FOREIGN KEY (idSalida) REFERENCES salida_inventario(idSalida) ON DELETE CASCADE,
    FOREIGN KEY (idProducto) REFERENCES productos(idProducto) ON DELETE CASCADE,
    FOREIGN KEY (idSuministro) REFERENCES suministros(idSuministro) ON DELETE CASCADE,
    FOREIGN KEY (idAlmacen) REFERENCES almacenes(idAlmacen) ON DELETE CASCADE
);

CREATE TABLE guias_remision (
    idGuia INT AUTO_INCREMENT PRIMARY KEY,
    numeroGuia VARCHAR(50) NOT NULL UNIQUE,
    serieGuia VARCHAR(10), -- Ej: E001
    correlativoGuia VARCHAR(20), -- Ej: 0000001
    fechaEmision DATE NOT NULL,
    fechaTraslado DATE, -- Fecha en que se realiza el movimiento
	tipoGuia ENUM('Entrada', 'Salida', 'Transferencia') NOT NULL,
    estadoGuia ENUM('Emitida', 'En_Transito', 'Recibida', 'Anulada') NOT NULL DEFAULT 'Emitida',
    
    -- Referencias
    idEmpresa INT NOT NULL, -- La empresa que emite la guía
    idOrdenes INT, -- Clave foránea a la tabla ordenes_compras para guías de entrada
    idSalida INT, -- Clave foránea a la tabla salidas_inventario para guías de salida
    idAlmacenOrigen INT, -- Para guías de salida y transferencia
    idAlmacenDestino INT, -- Para guías de entrada y transferencia
    
    -- Datos del transportista y vehículo
    transportista VARCHAR(150),
    placaVehiculo VARCHAR(20),
    licenciaConducir VARCHAR(20),
    
    -- Información del traslado
    motivoTraslado ENUM('Venta', 'Compra', 'Transferencia', 'Devolucion', 'Otros') NOT NULL,
    pesoTotal DECIMAL(8,2), -- Peso total del envío en kg
    numeroPackages INT DEFAULT 1, -- Número de bultos o paquetes
    observaciones VARCHAR(600),
    fechaRegistro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints para asegurar la lógica de la guía
    CONSTRAINT chk_referencias_guia CHECK (
        (tipoGuia = 'Entrada' AND idOrdenes IS NOT NULL AND idSalida IS NULL) OR
        (tipoGuia = 'Salida' AND idSalida IS NOT NULL AND idOrdenes IS NULL) OR
        (tipoGuia = 'Transferencia' AND idAlmacenOrigen IS NOT NULL AND idAlmacenDestino IS NOT NULL)
    ),
    FOREIGN KEY (idEmpresa) REFERENCES empresas(idEmpresa) ON DELETE CASCADE,
    FOREIGN KEY (idOrdenes) REFERENCES ordenes_compras(idOrdenes) ON DELETE CASCADE,
    FOREIGN KEY (idSalida) REFERENCES salida_inventario(idSalida) ON DELETE CASCADE,
    FOREIGN KEY (idAlmacenOrigen) REFERENCES almacenes(idAlmacen) ON DELETE CASCADE,
    FOREIGN KEY (idAlmacenDestino) REFERENCES almacenes(idAlmacen) ON DELETE CASCADE
);
CREATE TABLE guias_remision_detalle (
    idDetalleGuia INT AUTO_INCREMENT PRIMARY KEY,
    numeroItem INT NOT NULL, -- Posición del ítem en la guía (ej. 1, 2, 3)
    
    -- Referencia
    idGuia INT not null,
    idProducto INT,
    idSuministro INT,
    
    -- Cantidades y peso
    cantidad INT NOT NULL, -- Cantidad enviada
    cantidadRecibida INT DEFAULT 0, -- Cantidad que se recepciona
    unidadMedida VARCHAR(20) DEFAULT 'UND',
    pesoUnitario DECIMAL(8,3), -- Peso en kg por unidad
    
    -- Atributos de trazabilidad
    lote VARCHAR(50), -- Lote del producto/suministro
    
    -- Estado y observaciones
    estadoItem ENUM('Pendiente', 'Recibido', 'Faltante', 'Dañado') DEFAULT 'Pendiente',
    observacionesItem VARCHAR(255),
    
    -- Constraints mejorados
    CONSTRAINT chk_item_guia CHECK (
        (idProducto IS NOT NULL AND idSuministro IS NULL) OR 
        (idProducto IS NULL AND idSuministro IS NOT NULL)
    ),
    CONSTRAINT chk_cantidades_guia CHECK (
        cantidad > 0 AND 
        cantidadRecibida >= 0 AND 
        cantidadRecibida <= cantidad AND
        pesoUnitario >= 0
    ),
    FOREIGN KEY (idGuia) REFERENCES guias_remision(idGuia) ON DELETE CASCADE,
    FOREIGN KEY (idProducto) REFERENCES productos(idProducto) ON DELETE CASCADE,
    FOREIGN KEY (idSuministro) REFERENCES suministros(idSuministro) ON DELETE CASCADE,
    
    -- Clave única para el ítem
    UNIQUE KEY uk_guia_item (idGuia, numeroItem)
);

-- =====================================================
-- 		TABLA BITACORA
-- =====================================================

CREATE TABLE auditoria (
    idAuditoria INT AUTO_INCREMENT PRIMARY KEY,
    idUsuario INT NOT NULL,
    tipoAccion ENUM('INSERT', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT', 'EXPORT', 'IMPORT') NOT NULL,
    nombreTabla VARCHAR(100) NOT NULL,
    idRegistroAfectado INT NULL,
    descripcionAccion VARCHAR(255) NULL,
    valorAnterior JSON NULL,
    valorNuevo JSON NULL,
    ipAcceso VARCHAR(45) NULL,
    navegador VARCHAR(100) NULL,
    fechaAccion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (idUsuario) REFERENCES usuarios(idUsuario) ON DELETE CASCADE
);

-- Tabla para registrar los pagos de los clientes
CREATE TABLE pagos_clientes (
    idPago INT AUTO_INCREMENT PRIMARY KEY,
    idCliente INT NOT NULL,
    idSalida INT NULL, -- Opcional: para asociar el abono a una venta específica
    montoAbono DECIMAL(12,4) NOT NULL,
    fechaAbono DATETIME DEFAULT CURRENT_TIMESTAMP,
    idUsuario INT NOT NULL,
    CONSTRAINT chk_monto_abono CHECK (montoAbono > 0),
    FOREIGN KEY (idCliente) REFERENCES clientes(idCliente) ON DELETE CASCADE,
    FOREIGN KEY (idSalida) REFERENCES salida_inventario(idSalida) ON DELETE SET NULL,
    FOREIGN KEY (idUsuario) REFERENCES usuarios(idUsuario) ON DELETE CASCADE
);

CREATE INDEX idx_pagos_cliente ON pagos_clientes(idCliente);
CREATE INDEX idx_pagos_salida ON pagos_clientes(idSalida);
CREATE INDEX idx_pagos_fecha ON pagos_clientes(fechaAbono);


CREATE TABLE backups (
    idBackup INT AUTO_INCREMENT PRIMARY KEY,
    nombreArchivo VARCHAR(255) NOT NULL,
    rutaArchivo VARCHAR(500) NOT NULL,
    tamanoArchivo BIGINT NOT NULL DEFAULT 0 COMMENT 'Tamaño en bytes',
    tipoBackup ENUM('COMPLETO', 'INCREMENTAL', 'DIFERENCIAL') NOT NULL DEFAULT 'COMPLETO',
    estadoBackup ENUM('EXITOSO', 'FALLIDO', 'EN_PROCESO') NOT NULL DEFAULT 'EN_PROCESO',
    fechaBackup DATETIME NOT NULL,
    fechaRegistro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    idUsuario INT NOT NULL,
    observaciones TEXT,
    numeroTablas INT DEFAULT 0,
    duracionSegundos INT DEFAULT 0,
    FOREIGN KEY (idUsuario) REFERENCES usuarios(idUsuario) ON DELETE CASCADE
);
