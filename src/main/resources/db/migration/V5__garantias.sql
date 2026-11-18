-- =========================================================
-- KAIRÓS - V5: garantías y reclamos (SCRUM-72)
-- Solo AGREGA columnas y tablas: no borra ni cambia datos existentes.
-- =========================================================

-- 1. Cada producto tiene su plazo de garantía en meses (SCRUM-73).
--    Los productos que ya existen quedan con 12 meses.
ALTER TABLE producto ADD COLUMN meses_garantia INTEGER NOT NULL DEFAULT 12;
ALTER TABLE producto ADD CONSTRAINT ck_producto_meses_garantia CHECK (meses_garantia >= 0);

-- 2. Cédula del usuario, para buscar sus compras y garantías (SCRUM-74).
--    Opcional: los usuarios que ya existen no la tienen. Si se llena, no se puede repetir
--    (en PostgreSQL varias filas pueden tener NULL sin violar el UNIQUE).
ALTER TABLE usuario ADD COLUMN cedula VARCHAR(15);
ALTER TABLE usuario ADD CONSTRAINT uk_usuario_cedula UNIQUE (cedula);

-- 3. Reclamo de garantía de un producto de una venta (SCRUM-75)
CREATE TABLE reclamo_garantia (
    id_reclamo        BIGSERIAL PRIMARY KEY,
    id_empresa        BIGINT       NOT NULL REFERENCES empresa (id_empresa),
    id_venta          BIGINT       NOT NULL REFERENCES venta (id_venta),
    id_producto       BIGINT       NOT NULL REFERENCES producto (id_producto),
    id_cliente        BIGINT       REFERENCES usuario (id_usuario),   -- nulo en una venta de mostrador
    motivo            VARCHAR(500) NOT NULL,
    estado            VARCHAR(20)  NOT NULL DEFAULT 'RECIBIDO',
    fecha_registro    TIMESTAMP    NOT NULL DEFAULT now(),
    id_registrado_por BIGINT       NOT NULL REFERENCES usuario (id_usuario),
    CONSTRAINT ck_reclamo_estado CHECK (estado IN
        ('RECIBIDO', 'EN_REVISION', 'REPARADO', 'CAMBIADO', 'RECHAZADO', 'ENTREGADO'))
);

CREATE INDEX ix_reclamo_empresa ON reclamo_garantia (id_empresa);
CREATE INDEX ix_reclamo_cliente ON reclamo_garantia (id_cliente);

-- 4. Historial del reclamo: cada cambio de estado guarda la fecha y quién lo hizo
CREATE TABLE seguimiento_reclamo (
    id_seguimiento BIGSERIAL PRIMARY KEY,
    id_reclamo     BIGINT       NOT NULL REFERENCES reclamo_garantia (id_reclamo),
    estado         VARCHAR(20)  NOT NULL,
    nota           VARCHAR(500),
    fecha          TIMESTAMP    NOT NULL DEFAULT now(),
    id_usuario     BIGINT       NOT NULL REFERENCES usuario (id_usuario),
    CONSTRAINT ck_seguimiento_estado CHECK (estado IN
        ('RECIBIDO', 'EN_REVISION', 'REPARADO', 'CAMBIADO', 'RECHAZADO', 'ENTREGADO'))
);

CREATE INDEX ix_seguimiento_reclamo ON seguimiento_reclamo (id_reclamo);
