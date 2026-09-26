package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;

public interface RegistrarUsuarioUseCase {

    Usuario registrar(String nombre,
                      String email,
                      String password,
                      Rol rol,
                      String telefono,
                      Long idAlmacen);
}