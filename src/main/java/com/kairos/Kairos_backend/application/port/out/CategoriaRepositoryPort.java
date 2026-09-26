package com.kairos.Kairos_backend.application.port.out;

import com.kairos.Kairos_backend.domain.model.Categoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepositoryPort {

    Categoria guardar(Categoria categoria);

    Optional<Categoria> buscarPorId(Long id);

    List<Categoria> listarTodas();

    boolean existePorId(Long id);

    boolean existePorNombre(String nombre);
}
