package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;

import java.time.LocalDate;

/**
 * Empresa de tecnología que usa la plataforma KAIRÓS.
 * Cada empresa tiene sus propias sedes, productos, empleados y ventas.
 */
public class Empresa {

    private final Long id;
    private String nombre;
    private final String nit;
    private String email;
    private String telefono;
    private EstadoEmpresa estado;
    private final LocalDate fechaRegistro;

    /** Reconstruye una empresa que YA existe en la base de datos. */
    public Empresa(Long id, String nombre, String nit, String email, String telefono,
                   EstadoEmpresa estado, LocalDate fechaRegistro) {
        this.id = id;
        this.nit = nit == null ? null : nit.trim();
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
        asignarDatos(nombre, email, telefono);
        if (this.nit == null || this.nit.isBlank()) {
            throw new ReglaNegocioException("El NIT de la empresa es obligatorio");
        }
        if (this.estado == null) {
            throw new ReglaNegocioException("El estado de la empresa es obligatorio");
        }
    }

    /** Una empresa NUEVA siempre queda PENDIENTE hasta que el superadministrador la apruebe. */
    public static Empresa nueva(String nombre, String nit, String email, String telefono) {
        return new Empresa(null, nombre, nit, email, telefono, EstadoEmpresa.PENDIENTE, LocalDate.now());
    }

    public void actualizarDatos(String nombre, String email, String telefono) {
        asignarDatos(nombre, email, telefono);
    }

    private void asignarDatos(String nombre, String email, String telefono) {
        this.nombre = nombre == null ? null : nombre.trim();
        this.email = email == null ? null : email.trim().toLowerCase();
        this.telefono = telefono;
        if (this.nombre == null || this.nombre.isBlank()) {
            throw new ReglaNegocioException("El nombre de la empresa es obligatorio");
        }
        if (this.email == null || !this.email.contains("@")) {
            throw new ReglaNegocioException("El email de la empresa no es válido");
        }
    }

    // ----- Comportamiento del negocio (lo usa el superadministrador) -----

    public void aprobar() {
        exigirEstado(EstadoEmpresa.PENDIENTE, "Solo se puede aprobar una empresa pendiente");
        this.estado = EstadoEmpresa.ACTIVA;
    }

    public void rechazar() {
        exigirEstado(EstadoEmpresa.PENDIENTE, "Solo se puede rechazar una empresa pendiente");
        this.estado = EstadoEmpresa.RECHAZADA;
    }

    public void suspender() {
        exigirEstado(EstadoEmpresa.ACTIVA, "Solo se puede suspender una empresa activa");
        this.estado = EstadoEmpresa.SUSPENDIDA;
    }

    /** Solo una empresa ACTIVA puede operar: sus empleados inician sesión y su catálogo es visible. */
    public boolean estaActiva() {
        return estado == EstadoEmpresa.ACTIVA;
    }

    private void exigirEstado(EstadoEmpresa esperado, String mensaje) {
        if (estado != esperado) {
            throw new ReglaNegocioException(mensaje);
        }
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getNit() { return nit; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public EstadoEmpresa getEstado() { return estado; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
}
