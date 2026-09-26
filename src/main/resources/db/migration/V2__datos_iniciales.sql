-- =========================================================
-- KAIRÓS - V2: datos iniciales para pruebas y demo
-- (el administrador inicial NO va aquí: lo crea AdminInicial.java
--  al arrancar, para que su contraseña quede cifrada con BCrypt)
-- =========================================================

-- Almacenes / sedes
INSERT INTO almacen (nombre, ciudad, direccion) VALUES
    ('Sede Centro', 'Bogotá', 'Cra. 7 # 12-45'),
    ('Sede Norte',  'Bogotá', 'Cl. 140 # 15-20'),
    ('Sede Chía',   'Chía',   'Av. Pradilla # 5-30');

-- Categorías (ON CONFLICT: si ya existía una con ese nombre, no falla)
INSERT INTO categoria (nombre) VALUES
    ('Celulares'), ('Portátiles'), ('Accesorios'), ('Tablets')
ON CONFLICT (nombre) DO NOTHING;

-- Productos (el id de la categoría se busca por su nombre)
INSERT INTO producto (nombre, descripcion, precio, marca, id_categoria)
SELECT v.nombre, v.descripcion, v.precio, v.marca, c.id_categoria
FROM (VALUES
    ('Galaxy A55 256GB',        'Celular 5G, 8 GB RAM',          1599000.00, 'Samsung',  'Celulares'),
    ('iPhone 15 128GB',         'Celular 5G, chip A16',          3899000.00, 'Apple',    'Celulares'),
    ('Redmi Note 13',           'Celular 128 GB, 6 GB RAM',       899000.00, 'Xiaomi',   'Celulares'),
    ('IdeaPad Slim 3',          'Portátil 15.6", Ryzen 5, 16 GB', 2499000.00, 'Lenovo',  'Portátiles'),
    ('MacBook Air M3',          'Portátil 13.6", 8 GB, 256 GB',  5299000.00, 'Apple',    'Portátiles'),
    ('Audífonos Bluetooth T3',  'Inalámbricos con estuche',        129000.00, 'JBL',     'Accesorios'),
    ('Cargador USB-C 25W',      'Carga rápida',                     89000.00, 'Samsung', 'Accesorios'),
    ('Galaxy Tab A9',           'Tablet 8.7", 64 GB',              749000.00, 'Samsung', 'Tablets')
) AS v(nombre, descripcion, precio, marca, categoria)
JOIN categoria c ON c.nombre = v.categoria;

-- Inventario inicial (algunos quedan por debajo del mínimo para probar /bajo-stock)
INSERT INTO inventario (id_producto, id_almacen, cantidad_disponible, stock_minimo)
SELECT p.id_producto, a.id_almacen, v.cantidad, v.minimo
FROM (VALUES
    ('Galaxy A55 256GB',       'Sede Centro', 12, 5),
    ('Galaxy A55 256GB',       'Sede Norte',   3, 5),
    ('iPhone 15 128GB',        'Sede Centro',  6, 3),
    ('Redmi Note 13',          'Sede Chía',   20, 5),
    ('IdeaPad Slim 3',         'Sede Norte',   4, 2),
    ('MacBook Air M3',         'Sede Centro',  1, 2),
    ('Audífonos Bluetooth T3', 'Sede Centro', 30, 10),
    ('Cargador USB-C 25W',     'Sede Chía',    8, 10)
) AS v(producto, almacen, cantidad, minimo)
JOIN producto p ON p.nombre = v.producto
JOIN almacen  a ON a.nombre = v.almacen
ON CONFLICT (id_producto, id_almacen) DO NOTHING;
