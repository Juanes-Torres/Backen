package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;

/**
 * Sede o tienda de una empresa.
 */
public class Almacen {

    private final Long id;
    private String nombre;
    private String ciudad;
    private String direccion;
    private EstadoAlmacen estado;
    private final Long idEmpresa;

    public Almacen(Long id, String nombre, String ciudad, String direccion, EstadoAlmacen estado, Long idEmpresa) {
        this.id = id;
        this.estado = estado;
        this.idEmpresa = idEmpresa;
        if (idEmpresa == null) {
            throw new ReglaNegocioException("El almacén debe pertenecer a una empresa");
        }
        asignarDatos(nombre, ciudad, direccion);
    }

    public static Almacen nuevo(String nombre, String ciudad, String direccion, Long idEmpresa) {
        return new Almacen(null, nombre, ciudad, direccion, EstadoAlmacen.ACTIVO, idEmpresa);
    }

    public void actualizar(String nombre, String ciudad, String direccion) {
        asignarDatos(nombre, ciudad, direccion);
    }

    private void asignarDatos(String nombre, String ciudad, String direccion) {
        this.nombre = nombre == null ? null : nombre.trim();
        this.ciudad = ciudad == null ? null : ciudad.trim();
        this.direccion = direccion;
        if (this.nombre == null || this.nombre.isBlank()) {
            throw new ReglaNegocioException("El nombre del almacén es obligatorio");
        }
        if (this.ciudad == null || this.ciudad.isBlank()) {
            throw new ReglaNegocioException("La ciudad del almacén es obligatoria");
        }
        if (this.estado == null) {
            throw new ReglaNegocioException("El estado del almacén es obligatorio");
        }
    }

    public void activar() { this.estado = EstadoAlmacen.ACTIVO; }
    public void desactivar() { this.estado = EstadoAlmacen.INACTIVO; }
    public boolean estaActivo() { return estado == EstadoAlmacen.ACTIVO; }
    public boolean perteneceA(Long idEmpresa) { return this.idEmpresa.equals(idEmpresa); }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCiudad() { return ciudad; }
    public String getDireccion() { return direccion; }
    public EstadoAlmacen getEstado() { return estado; }
    public Long getIdEmpresa() { return idEmpresa; }
}
