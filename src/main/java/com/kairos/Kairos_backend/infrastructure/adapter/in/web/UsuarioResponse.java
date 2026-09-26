package com.kairos.Kairos_backend.infrastructure.adapter.in.web;

import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;

public class UsuarioResponse {

    private final Long id;
    private final String nombre;
    private final String email;
    private final Rol rol;
    private final String telefono;
    private final Long idAlmacen;
    private final boolean activo;

    public UsuarioResponse(Long id, String nombre, String email, Rol rol,
                           String telefono, Long idAlmacen, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.telefono = telefono;
        this.idAlmacen = idAlmacen;
        this.activo = activo;
    }

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getTelefono(),
                usuario.getIdAlmacen(),
                usuario.isActivo()
        );
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

    public Rol getRol() {
        return rol;
    }

    public String getTelefono() {
        return telefono;
    }

    public Long getIdAlmacen() {
        return idAlmacen;
    }

    public boolean isActivo() {
        return activo;
    }
}