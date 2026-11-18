-- =========================================================
-- KAIRÓS - V3: plataforma multiempresa (SCRUM-51)
-- Paso 1 de 2 ("expandir"): se crea la empresa y se agrega id_empresa
-- como OPCIONAL, para que el código actual siga funcionando.
-- La V4 lo volverá obligatorio cuando el código ya envíe la empresa (SCRUM-55).
-- =========================================================

-- 1. EMPRESA
CREATE TABLE empresa (
    id_empresa      BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(150) NOT NULL,
    nit             VARCHAR(20)  NOT NULL UNIQUE,
    email           VARCHAR(100) NOT NULL,
    telefono        VARCHAR(20),
    estado          VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE'
                    CHECK (estado IN ('PENDIENTE', 'ACTIVA', 'RECHAZADA', 'SUSPENDIDA')),
    fecha_registro  DATE NOT NULL DEFAULT CURRENT_DATE
);

-- 2. Los datos que ya existen pasan a una "Empresa demo" ya aprobada
INSERT INTO empresa (nombre, nit, email, estado)
VALUES ('Empresa demo', '900000000-1', 'demo@kairos.com', 'ACTIVA');

-- 3. Cada sede, producto y venta pertenece a una empresa
ALTER TABLE almacen  ADD COLUMN id_empresa BIGINT REFERENCES empresa (id_empresa);
ALTER TABLE producto ADD COLUMN id_empresa BIGINT REFERENCES empresa (id_empresa);
ALTER TABLE venta    ADD COLUMN id_empresa BIGINT REFERENCES empresa (id_empresa);

-- 4. Los empleados (VENDEDOR y ADMINISTRADOR) pertenecen a una empresa.
--    Los CLIENTES tienen cuenta global y el SUPERADMIN es de la plataforma: quedan sin empresa.
ALTER TABLE usuario ADD COLUMN id_empresa BIGINT REFERENCES empresa (id_empresa);

UPDATE almacen  SET id_empresa = (SELECT id_empresa FROM empresa WHERE nit = '900000000-1');
UPDATE producto SET id_empresa = (SELECT id_empresa FROM empresa WHERE nit = '900000000-1');
UPDATE venta    SET id_empresa = (SELECT id_empresa FROM empresa WHERE nit = '900000000-1');
UPDATE usuario  SET id_empresa = (SELECT id_empresa FROM empresa WHERE nit = '900000000-1')
WHERE rol IN ('VENDEDOR', 'ADMINISTRADOR');

-- 5. Nuevo rol de la plataforma
ALTER TABLE usuario DROP CONSTRAINT usuario_rol_check;
ALTER TABLE usuario ADD CONSTRAINT usuario_rol_check
    CHECK (rol IN ('CLIENTE', 'VENDEDOR', 'ADMINISTRADOR', 'SUPERADMIN'));

-- 6. El número de factura es único dentro de cada empresa (no en toda la plataforma)
ALTER TABLE venta DROP CONSTRAINT venta_numero_factura_key;
ALTER TABLE venta ADD CONSTRAINT uk_venta_empresa_factura UNIQUE (id_empresa, numero_factura);

-- ÍNDICES: casi todas las consultas filtran por empresa
CREATE INDEX idx_almacen_empresa  ON almacen (id_empresa);
CREATE INDEX idx_producto_empresa ON producto (id_empresa);
CREATE INDEX idx_usuario_empresa  ON usuario (id_empresa);
CREATE INDEX idx_venta_empresa    ON venta (id_empresa, fecha);
