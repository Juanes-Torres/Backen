-- =========================================================
-- KAIRÓS - V4: cada dato pertenece a una empresa (SCRUM-55)
-- Paso 2 de 2 ("contraer"): la V3 agregó id_empresa como OPCIONAL;
-- ahora que el código siempre lo envía, se vuelve OBLIGATORIO.
-- =========================================================

-- 1. Por seguridad: lo que se creó entre la V3 y esta versión sin empresa
--    pasa a la "Empresa demo" (en una base al día no cambia nada).
UPDATE almacen  SET id_empresa = (SELECT id_empresa FROM empresa WHERE nit = '900000000-1')
WHERE id_empresa IS NULL;
UPDATE producto SET id_empresa = (SELECT id_empresa FROM empresa WHERE nit = '900000000-1')
WHERE id_empresa IS NULL;
-- La venta es de la empresa dueña de la sede donde se hizo
UPDATE venta v SET id_empresa = a.id_empresa
FROM almacen a
WHERE a.id_almacen = v.id_almacen AND v.id_empresa IS NULL;
UPDATE usuario  SET id_empresa = (SELECT id_empresa FROM empresa WHERE nit = '900000000-1')
WHERE rol IN ('VENDEDOR', 'ADMINISTRADOR') AND id_empresa IS NULL;

-- 2. Sedes, productos y ventas siempre tienen empresa
ALTER TABLE almacen  ALTER COLUMN id_empresa SET NOT NULL;
ALTER TABLE producto ALTER COLUMN id_empresa SET NOT NULL;
ALTER TABLE venta    ALTER COLUMN id_empresa SET NOT NULL;

-- 3. Empleados (VENDEDOR, ADMINISTRADOR) CON empresa; CLIENTE y SUPERADMIN SIN empresa
ALTER TABLE usuario ADD CONSTRAINT ck_usuario_empresa_segun_rol CHECK (
    (rol IN ('VENDEDOR', 'ADMINISTRADOR') AND id_empresa IS NOT NULL)
    OR (rol IN ('CLIENTE', 'SUPERADMIN') AND id_empresa IS NULL)
);
