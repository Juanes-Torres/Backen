-- =========================================================
-- KAIRÓS - V1: Núcleo del sistema
-- Tablas principales del sistema
-- =========================================================

-- 1. ALMACÉN
CREATE TABLE almacen (
    id_almacen  BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    ciudad      VARCHAR(100) NOT NULL,
    direccion   VARCHAR(150),
    estado      VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
                CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- 2. USUARIO
CREATE TABLE usuario (
    id_usuario      BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    rol             VARCHAR(20) NOT NULL
                    CHECK (rol IN ('CLIENTE', 'VENDEDOR', 'ADMINISTRADOR')),
    telefono        VARCHAR(20),
    id_almacen      BIGINT REFERENCES almacen (id_almacen),
    fecha_registro  DATE NOT NULL DEFAULT CURRENT_DATE,
    activo          BOOLEAN NOT NULL DEFAULT TRUE
);

-- 3. CATEGORÍA
CREATE TABLE categoria (
    id_categoria  BIGSERIAL PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL UNIQUE
);

-- 4. PRODUCTO
CREATE TABLE producto (
    id_producto   BIGSERIAL PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    descripcion   VARCHAR(255),
    precio        NUMERIC(12,2) NOT NULL CHECK (precio >= 0),
    marca         VARCHAR(100),
    id_categoria  BIGINT NOT NULL REFERENCES categoria (id_categoria)
);

-- 5. INVENTARIO
CREATE TABLE inventario (
    id_inventario        BIGSERIAL PRIMARY KEY,
    id_producto          BIGINT NOT NULL REFERENCES producto (id_producto),
    id_almacen           BIGINT NOT NULL REFERENCES almacen (id_almacen),
    cantidad_disponible  INT NOT NULL DEFAULT 0
                         CHECK (cantidad_disponible >= 0),
    stock_minimo         INT NOT NULL DEFAULT 5
                         CHECK (stock_minimo >= 0),
    UNIQUE (id_producto, id_almacen)
);

-- 6. VENTA
CREATE TABLE venta (
    id_venta        BIGSERIAL PRIMARY KEY,
    numero_factura  VARCHAR(30) NOT NULL UNIQUE,
    id_cliente      BIGINT REFERENCES usuario (id_usuario),
    id_usuario      BIGINT NOT NULL REFERENCES usuario (id_usuario),
    id_almacen      BIGINT NOT NULL REFERENCES almacen (id_almacen),
    fecha           TIMESTAMP NOT NULL DEFAULT NOW(),
    total           NUMERIC(12,2) NOT NULL CHECK (total >= 0),
    estado          VARCHAR(20) NOT NULL DEFAULT 'COMPLETADA'
                    CHECK (estado IN ('COMPLETADA', 'ANULADA'))
);

-- 7. DETALLE DE VENTA
CREATE TABLE detalle_venta (
    id_detalle       BIGSERIAL PRIMARY KEY,
    id_venta         BIGINT NOT NULL REFERENCES venta (id_venta),
    id_producto      BIGINT NOT NULL REFERENCES producto (id_producto),
    cantidad         INT NOT NULL CHECK (cantidad > 0),
    precio_unitario  NUMERIC(12,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal         NUMERIC(12,2) NOT NULL CHECK (subtotal >= 0)
);

-- ÍNDICES
CREATE INDEX idx_producto_categoria
    ON producto (id_categoria);

CREATE INDEX idx_venta_fecha
    ON venta (fecha);

CREATE INDEX idx_venta_almacen
    ON venta (id_almacen, fecha);

CREATE INDEX idx_detalle_venta
    ON detalle_venta (id_venta);