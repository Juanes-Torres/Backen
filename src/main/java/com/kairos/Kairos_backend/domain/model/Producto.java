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
    /** Dónde está guardada la foto (por ejemplo "productos/15-a1b2.jpg"); nulo = sin foto. */
    private String imagen;

    public Producto(Long id, String nombre, String descripcion, BigDecimal precio,
                    String marca, Long idCategoria, Long idEmpresa, Integer mesesGarantia) {
        this(id, nombre, descripcion, precio, marca, idCategoria, idEmpresa, mesesGarantia, null);
    }

    public Producto(Long id, String nombre, String descripcion, BigDecimal precio,
                    String marca, Long idCategoria, Long idEmpresa, Integer mesesGarantia, String imagen) {
        this.id = id;
        this.idEmpresa = idEmpresa;
        this.imagen = imagen;
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

    /** mesesGarantia nulo = se conserva el plazo que ya tenía (un cliente viejo de la API no lo borra). */
    public void actualizar(String nombre, String descripcion, BigDecimal precio,
                           String marca, Long idCategoria, Integer mesesGarantia) {
        asignarDatos(nombre, descripcion, precio, marca, idCategoria,
                mesesGarantia == null ? this.mesesGarantia : mesesGarantia);
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

    /** Pone o cambia la foto. Devuelve la anterior (nula si no tenía) para poder borrarla. */
    public String cambiarImagen(String nuevaImagen) {
        if (nuevaImagen == null || nuevaImagen.isBlank()) {
            throw new ReglaNegocioException("La ubicación de la imagen es obligatoria");
        }
        String anterior = this.imagen;
        this.imagen = nuevaImagen;
        return anterior;
    }

    /** Quita la foto. Devuelve la que tenía (nula si no tenía). */
    public String quitarImagen() {
        String anterior = this.imagen;
        this.imagen = null;
        return anterior;
    }

    public boolean tieneImagen() { return imagen != null; }

    public boolean perteneceA(Long idEmpresa) { return this.idEmpresa.equals(idEmpresa); }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public String getMarca() { return marca; }
    public Long getIdCategoria() { return idCategoria; }
    public int getMesesGarantia() { return mesesGarantia; }
    public Long getIdEmpresa() { return idEmpresa; }
    public String getImagen() { return imagen; }
}
