package com.kairos.Kairos_backend.application.port.out;

import com.kairos.Kairos_backend.domain.model.Almacen;

import java.util.List;
import java.util.Optional;

public interface AlmacenRepositoryPort {

    Almacen guardar(Almacen almacen);

    Optional<Almacen> buscarPorId(Long id);

    List<Almacen> listarTodos();

    boolean existePorId(Long id);
}
