package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;

import java.time.LocalDate;

public class Usuario {

    private final Long id;
    private final String nombre;
    private final String email;
    private final String passwordHash;
    private final Rol rol;
    private final String telefono;
    private final Long idAlmacen;
    private final LocalDate fechaRegistro;
    private boolean activo;

    public Usuario(Long id, String nombre, String email, String passwordHash,
                   Rol rol, String telefono, Long idAlmacen,
                   LocalDate fechaRegistro, boolean activo) {

        this.id = id;
        this.nombre = nombre == null ? null : nombre.trim();
        this.email = email == null ? null : email.trim().toLowerCase();
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.telefono = telefono;
        this.idAlmacen = idAlmacen;
        this.fechaRegistro = fechaRegistro;
        this.activo = activo;

        validar();
    }

    public static Usuario nuevo(String nombre, String email, String passwordHash,
                                Rol rol, String telefono, Long idAlmacen) {

        return new Usuario(
                null,
                nombre,
                email,
                passwordHash,
                rol,
                telefono,
                idAlmacen,
                LocalDate.now(),
                true
        );
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
            throw new ReglaNegocioException(
                    "Un vendedor debe estar asignado a un almacén"
            );
        }
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

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public String getTelefono() {
        return telefono;
    }

    public Long getIdAlmacen() {
        return idAlmacen;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public boolean isActivo() {
        return activo;
    }
}