-- =========================================================
-- KAIRÓS - V7: foto de cada producto (SCRUM-103)
-- La foto NO se guarda en la base de datos: va en la carpeta de archivos (ARCHIVOS_CARPETA).
-- Aquí solo queda dónde está, por ejemplo "productos/15-a1b2c3.jpg".
-- Solo AGREGA una columna opcional: los productos que ya existen quedan sin foto.
-- =========================================================

ALTER TABLE producto ADD COLUMN imagen VARCHAR(255);
