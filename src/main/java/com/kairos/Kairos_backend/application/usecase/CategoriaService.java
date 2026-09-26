package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.GestionarCategoriasUseCase;
import com.kairos.Kairos_backend.application.port.out.CategoriaRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoDuplicadoException;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.model.Categoria;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService implements GestionarCategoriasUseCase {

    private final CategoriaRepositoryPort categoriaRepository;

    public CategoriaService(CategoriaRepositoryPort categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public List<Categoria> listar() {
        return categoriaRepository.listarTodas();
    }

    @Override
    public Categoria crear(String nombre) {
        Categoria nueva = Categoria.nueva(nombre);
        if (categoriaRepository.existePorNombre(nueva.getNombre())) {
            throw new RecursoDuplicadoException("Ya existe la categoría " + nueva.getNombre());
        }
        return categoriaRepository.guardar(nueva);
    }

    @Override
    public Categoria renombrar(Long id, String nombre) {
        Categoria categoria = categoriaRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una categoría con id " + id));
        categoria.renombrar(nombre);
        return categoriaRepository.guardar(categoria);
    }
}
