package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Producto del catálogo de UNA empresa. El stock NO vive aquí: vive en Inventario (por almacén).
 */
public class Producto {

    /** Si el administrador no indica el plazo de garantía, se usan 12 meses (SCRUM-73). */
    public static final int MESES_GARANTIA_POR_DEFECTO = 12;

    private final Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String marca;
    private Long idCategoria;
    private int mesesGarantia;
    private final Long idEmpresa;

    public Producto(Long id, String nombre, String descripcion, BigDecimal precio,
                    String marca, Long idCategoria, Long idEmpresa, Integer mesesGarantia) {
        this.id = id;
        this.idEmpresa = idEmpresa;
        if (idEmpresa == null) {
            throw new ReglaNegocioException("El producto debe pertenecer a una empresa");
        }
        asignarDatos(nombre, descripcion, precio, marca, idCategoria, mesesGarantia);
    }

    /** mesesGarantia nulo = 12 meses. */
    public static Producto nuevo(String nombre, String descripcion, BigDecimal precio,
                                 String marca, Long idCategoria, Long idEmpresa, Integer mesesGarantia) {
        return new Producto(null, nombre, descripcion, precio, marca, idCategoria, idEmpresa, mesesGarantia);
    }

    public void actualizar(String nombre, String descripcion, BigDecimal precio,
                           String marca, Long idCategoria, Integer mesesGarantia) {
        asignarDatos(nombre, descripcion, precio, marca, idCategoria, mesesGarantia);
    }

    /** La garantía de este producto para una compra hecha en esa fecha. */
    public Garantia garantiaDesde(LocalDate fechaCompra) {
        return new Garantia(fechaCompra, mesesGarantia);
    }

    private void asignarDatos(String nombre, String descripcion, BigDecimal precio,
                              String marca, Long idCategoria, Integer mesesGarantia) {
        this.nombre = nombre == null ? null : nombre.trim();
        this.descripcion = descripcion;
        this.precio = precio;
        this.marca = marca;
        this.idCategoria = idCategoria;
        this.mesesGarantia = mesesGarantia == null ? MESES_GARANTIA_POR_DEFECTO : mesesGarantia;
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
        if (mesesGarantia < 0) {
            throw new ReglaNegocioException("Los meses de garantía no pueden ser negativos");
        }
    }

    public boolean perteneceA(Long idEmpresa) { return this.idEmpresa.equals(idEmpresa); }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public String getMarca() { return marca; }
    public Long getIdCategoria() { return idCategoria; }
    public int getMesesGarantia() { return mesesGarantia; }
    public Long getIdEmpresa() { return idEmpresa; }
}
