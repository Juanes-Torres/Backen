package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.GestionarProductosUseCase;
import com.kairos.Kairos_backend.application.port.out.CategoriaRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.EmpresaRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.InventarioRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.ProductoRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.Producto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService implements GestionarProductosUseCase {

    private final ProductoRepositoryPort productoRepository;
    private final CategoriaRepositoryPort categoriaRepository;
    private final InventarioRepositoryPort inventarioRepository;
    private final EmpresaRepositoryPort empresaRepository;

    public ProductoService(ProductoRepositoryPort productoRepository,
                           CategoriaRepositoryPort categoriaRepository,
                           InventarioRepositoryPort inventarioRepository,
                           EmpresaRepositoryPort empresaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.inventarioRepository = inventarioRepository;
        this.empresaRepository = empresaRepository;
    }

    // ----- Catálogo público: solo se ven las empresas ACTIVAS -----

    @Override
    public List<Producto> buscar(Long idEmpresa, Long idCategoria, String nombre) {
        if (idEmpresa != null && !empresaActiva(idEmpresa)) {
            throw new RecursoNoEncontradoException("No existe una empresa con id " + idEmpresa);
        }
        return productoRepository.buscarEnCatalogo(idEmpresa, idCategoria, nombre);
    }

    /** Un producto de una empresa pendiente, rechazada o suspendida no se muestra. */
    @Override
    public Producto obtener(Long id) {
        return productoRepository.buscarPorId(id)
                .filter(producto -> empresaActiva(producto.getIdEmpresa()))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un producto con id " + id));
    }

    private boolean empresaActiva(Long idEmpresa) {
        return empresaRepository.buscarPorId(idEmpresa).map(Empresa::estaActiva).orElse(false);
    }

    // ----- Administración: solo dentro de la empresa del token -----

    @Override
    public Producto crear(Long idEmpresa, ProductoCommand command) {
        validarCategoria(command.idCategoria());
        Producto nuevo = Producto.nuevo(command.nombre(), command.descripcion(),
                command.precio(), command.marca(), command.idCategoria(), idEmpresa);
        return productoRepository.guardar(nuevo);
    }

    @Override
    public Producto actualizar(Long idEmpresa, Long id, ProductoCommand command) {
        Producto producto = obtenerDeEmpresa(idEmpresa, id);
        validarCategoria(command.idCategoria());
        producto.actualizar(command.nombre(), command.descripcion(),
                command.precio(), command.marca(), command.idCategoria());
        return productoRepository.guardar(producto);
    }

    @Override
    public void eliminar(Long idEmpresa, Long id) {
        obtenerDeEmpresa(idEmpresa, id);
        // Regla: no se borra un producto que ya tiene stock registrado en algún almacén
        if (inventarioRepository.existePorProducto(id)) {
            throw new ReglaNegocioException(
                    "No se puede eliminar: el producto tiene inventario registrado en algún almacén");
        }
        productoRepository.eliminar(id);
    }

    /** Un producto de otra empresa se trata igual que uno que no existe. */
    private Producto obtenerDeEmpresa(Long idEmpresa, Long id) {
        return productoRepository.buscarPorId(id)
                .filter(producto -> producto.perteneceA(idEmpresa))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un producto con id " + id));
    }

    private void validarCategoria(Long idCategoria) {
        if (idCategoria != null && !categoriaRepository.existePorId(idCategoria)) {
            throw new RecursoNoEncontradoException("No existe una categoría con id " + idCategoria);
        }
    }
}
