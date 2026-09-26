package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;

/**
 * Tipo de producto: Celulares, Portátiles, Accesorios...
 */
public class Categoria {

    private final Long id;
    private String nombre;

    public Categoria(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre == null ? null : nombre.trim();
        validar();
    }

    public static Categoria nueva(String nombre) {
        return new Categoria(null, nombre);
    }

    private void validar() {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("El nombre de la categoría es obligatorio");
        }
        if (nombre.length() > 100) {
            throw new ReglaNegocioException("El nombre de la categoría no puede superar 100 caracteres");
        }
    }

    public void renombrar(String nuevoNombre) {
        this.nombre = nuevoNombre == null ? null : nuevoNombre.trim();
        validar();
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
}
