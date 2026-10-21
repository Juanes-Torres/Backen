package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;

import java.math.BigDecimal;

/**
 * Producto del catálogo de UNA empresa. El stock NO vive aquí: vive en Inventario (por almacén).
 */
public class Producto {

    private final Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String marca;
    private Long idCategoria;
    private final Long idEmpresa;

    public Producto(Long id, String nombre, String descripcion, BigDecimal precio,
                    String marca, Long idCategoria, Long idEmpresa) {
        this.id = id;
        this.idEmpresa = idEmpresa;
        if (idEmpresa == null) {
            throw new ReglaNegocioException("El producto debe pertenecer a una empresa");
        }
        asignarDatos(nombre, descripcion, precio, marca, idCategoria);
    }

    public static Producto nuevo(String nombre, String descripcion, BigDecimal precio,
                                 String marca, Long idCategoria, Long idEmpresa) {
        return new Producto(null, nombre, descripcion, precio, marca, idCategoria, idEmpresa);
    }

    public void actualizar(String nombre, String descripcion, BigDecimal precio,
                           String marca, Long idCategoria) {
        asignarDatos(nombre, descripcion, precio, marca, idCategoria);
    }

    private void asignarDatos(String nombre, String descripcion, BigDecimal precio,
                              String marca, Long idCategoria) {
        this.nombre = nombre == null ? null : nombre.trim();
        this.descripcion = descripcion;
        this.precio = precio;
        this.marca = marca;
        this.idCategoria = idCategoria;
        validar();
    }

    private void validar() {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("El nombre del producto es obligatorio");
        }
        if (precio == null) {
            throw new ReglaNegocioException("El precio es obligatorio");
        }
        if (precio.signum() < 0) {
            throw new ReglaNegocioException("El precio no puede ser negativo");
        }
        if (idCategoria == null) {
            throw new ReglaNegocioException("El producto debe pertenecer a una categoría");
        }
    }

    public boolean perteneceA(Long idEmpresa) { return this.idEmpresa.equals(idEmpresa); }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public String getMarca() { return marca; }
    public Long getIdCategoria() { return idCategoria; }
    public Long getIdEmpresa() { return idEmpresa; }
}
