package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.GestionarProductosUseCase;
import com.kairos.Kairos_backend.application.port.out.CategoriaRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.InventarioRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.ProductoRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import com.kairos.Kairos_backend.domain.model.Producto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService implements GestionarProductosUseCase {

    private final ProductoRepositoryPort productoRepository;
    private final CategoriaRepositoryPort categoriaRepository;
    private final InventarioRepositoryPort inventarioRepository;

    public ProductoService(ProductoRepositoryPort productoRepository,
                           CategoriaRepositoryPort categoriaRepository,
                           InventarioRepositoryPort inventarioRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.inventarioRepository = inventarioRepository;
    }

    @Override
    public List<Producto> buscar(Long idCategoria, String nombre) {
        return productoRepository.buscar(idCategoria, nombre);
    }

    @Override
    public Producto obtener(Long id) {
        return productoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un producto con id " + id));
    }

    @Override
    public Producto crear(ProductoCommand command) {
        validarCategoria(command.idCategoria());
        Producto nuevo = Producto.nuevo(command.nombre(), command.descripcion(),
                command.precio(), command.marca(), command.idCategoria());
        return productoRepository.guardar(nuevo);
    }

    @Override
    public Producto actualizar(Long id, ProductoCommand command) {
        Producto producto = obtener(id);
        validarCategoria(command.idCategoria());
        producto.actualizar(command.nombre(), command.descripcion(),
                command.precio(), command.marca(), command.idCategoria());
        return productoRepository.guardar(producto);
    }

    @Override
    public void eliminar(Long id) {
        if (!productoRepository.existePorId(id)) {
            throw new RecursoNoEncontradoException("No existe un producto con id " + id);
        }
        // Regla: no se borra un producto que ya tiene stock registrado en algún almacén
        if (inventarioRepository.existePorProducto(id)) {
            throw new ReglaNegocioException(
                    "No se puede eliminar: el producto tiene inventario registrado en algún almacén");
        }
        productoRepository.eliminar(id);
    }

    private void validarCategoria(Long idCategoria) {
        if (idCategoria != null && !categoriaRepository.existePorId(idCategoria)) {
            throw new RecursoNoEncontradoException("No existe una categoría con id " + idCategoria);
        }
    }
}
