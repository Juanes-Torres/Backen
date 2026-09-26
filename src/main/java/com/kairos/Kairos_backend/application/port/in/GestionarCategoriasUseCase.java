package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.Categoria;

import java.util.List;

/**
 * CASO DE USO: administrar las categorías del catálogo.
 */
public interface GestionarCategoriasUseCase {

    List<Categoria> listar();

    Categoria crear(String nombre);

    Categoria renombrar(Long id, String nombre);
}
