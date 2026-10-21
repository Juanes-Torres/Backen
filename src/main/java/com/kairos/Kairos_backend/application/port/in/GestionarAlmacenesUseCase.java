package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.Almacen;

import java.util.List;

/**
 * CASO DE USO: administrar las sedes/almacenes de UNA empresa.
 * idEmpresa sale del token: una sede de otra empresa responde 404.
 */
public interface GestionarAlmacenesUseCase {

    List<Almacen> listar(Long idEmpresa);

    Almacen obtener(Long idEmpresa, Long id);

    Almacen crear(Long idEmpresa, String nombre, String ciudad, String direccion);

    Almacen actualizar(Long idEmpresa, Long id, String nombre, String ciudad, String direccion);

    Almacen cambiarEstado(Long idEmpresa, Long id, boolean activo);
}
