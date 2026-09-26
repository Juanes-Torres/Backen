package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.Almacen;

import java.util.List;

/**
 * CASO DE USO: administrar las sedes/almacenes.
 */
public interface GestionarAlmacenesUseCase {

    List<Almacen> listar();

    Almacen obtener(Long id);

    Almacen crear(String nombre, String ciudad, String direccion);

    Almacen actualizar(Long id, String nombre, String ciudad, String direccion);

    Almacen cambiarEstado(Long id, boolean activo);
}
