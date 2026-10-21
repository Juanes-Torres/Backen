package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;

import java.time.LocalDate;

/**
 * Usuario del DOMINIO: Java puro, sin Spring ni JPA.
 * Contiene los datos del usuario y las reglas del negocio sobre él.
 */
public class Usuario {

    private final Long id;
    private String nombre;
    private final String email;
    private final String passwordHash;
    private final Rol rol;
    private String telefono;
    private Long idAlmacen;
    private final Long idEmpresa;
    private final LocalDate fechaRegistro;
    private boolean activo;

    /** Reconstruye un usuario que YA existe en la base de datos. */
    public Usuario(Long id, String nombre, String email, String passwordHash, Rol rol,
                   String telefono, Long idAlmacen, Long idEmpresa, LocalDate fechaRegistro, boolean activo) {
        this.id = id;
        this.nombre = nombre == null ? null : nombre.trim();
        this.email = email == null ? null : email.trim().toLowerCase();
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.telefono = telefono;
        this.idAlmacen = idAlmacen;
        this.idEmpresa = idEmpresa;
        this.fechaRegistro = fechaRegistro;
        this.activo = activo;
        validar();
    }

    /** Crea un usuario NUEVO sin empresa (el id lo asigna la base de datos al guardar). */
    public static Usuario nuevo(String nombre, String email, String passwordHash,
                                Rol rol, String telefono, Long idAlmacen) {
        return nuevo(nombre, email, passwordHash, rol, telefono, idAlmacen, null);
    }

    /** Crea un usuario NUEVO que pertenece a una empresa (empleados). */
    public static Usuario nuevo(String nombre, String email, String passwordHash,
                                Rol rol, String telefono, Long idAlmacen, Long idEmpresa) {
        return new Usuario(null, nombre, email, passwordHash, rol,
                telefono, idAlmacen, idEmpresa, LocalDate.now(), true);
    }

    private void validar() {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("El nombre es obligatorio");
        }
        if (email == null || !email.contains("@")) {
            throw new ReglaNegocioException("El email no es válido");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new ReglaNegocioException("La contraseña es obligatoria");
        }
        if (rol == null) {
            throw new ReglaNegocioException("El rol es obligatorio");
        }
        if (rol == Rol.VENDEDOR && idAlmacen == null) {
            throw new ReglaNegocioException("Un vendedor debe estar asignado a un almacén");
        }
        // Vendedores y administradores trabajan para una empresa
        if ((rol == Rol.VENDEDOR || rol == Rol.ADMINISTRADOR) && idEmpresa == null) {
            throw new ReglaNegocioException("Un empleado debe pertenecer a una empresa");
        }
        // El cliente tiene una cuenta global: compra en cualquier empresa
        if (rol == Rol.CLIENTE && idEmpresa != null) {
            throw new ReglaNegocioException("Un cliente no pertenece a ninguna empresa");
        }
        // El superadministrador es de la plataforma, no de una empresa
        if (rol == Rol.SUPERADMIN && idEmpresa != null) {
            throw new ReglaNegocioException("El superadministrador no pertenece a ninguna empresa");
        }
    }

    // ----- Comportamiento del negocio -----

    /** Actualiza los datos personales que el usuario puede cambiar. */
    public void actualizarDatos(String nuevoNombre, String nuevoTelefono) {
        this.nombre = nuevoNombre == null ? null : nuevoNombre.trim();
        this.telefono = nuevoTelefono;
        validar();
    }

    /** Solo un administrador cambia el almacén de un vendedor. */
    public void asignarAlmacen(Long nuevoIdAlmacen) {
        this.idAlmacen = nuevoIdAlmacen;
        validar();
    }

    public void desactivar() {
        this.activo = false;
    }

    public void activar() {
        this.activo = true;
    }

    public boolean esAdministrador() {
        return rol == Rol.ADMINISTRADOR;
    }

    public boolean perteneceA(Long otraEmpresa) {
        return idEmpresa != null && idEmpresa.equals(otraEmpresa);
    }

    // ----- Getters -----

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Rol getRol() { return rol; }
    public String getTelefono() { return telefono; }
    public Long getIdAlmacen() { return idAlmacen; }
    public Long getIdEmpresa() { return idEmpresa; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public boolean isActivo() { return activo; }
}
