-- =========================================================
-- KAIRÓS - V6: cuántas preguntas le hace cada usuario al asistente por día (SCRUM-77)
-- Sirve para el límite diario. Va en la base de datos y no en memoria (12 factores: VI).
-- Solo AGREGA una tabla: no cambia datos existentes.
-- =========================================================

CREATE TABLE uso_asistente (
    id_usuario  BIGINT  NOT NULL REFERENCES usuario (id_usuario),
    fecha       DATE    NOT NULL,
    preguntas   INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (id_usuario, fecha)
);
