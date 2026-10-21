package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.GestionarInventarioUseCase;
import com.kairos.Kairos_backend.application.port.out.AlmacenRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.InventarioDetalle;
import com.kairos.Kairos_backend.application.port.out.InventarioRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.ProductoRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoDuplicadoException;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import com.kairos.Kairos_backend.domain.model.Almacen;
import com.kairos.Kairos_backend.domain.model.Inventario;
import com.kairos.Kairos_backend.domain.model.TipoMovimiento;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventarioService implements GestionarInventarioUseCase {

    private final InventarioRepositoryPort inventarioRepository;
    private final ProductoRepositoryPort productoRepository;
    private final AlmacenRepositoryPort almacenRepository;

    public InventarioService(InventarioRepositoryPort inventarioRepository,
                             ProductoRepositoryPort productoRepository,
                             AlmacenRepositoryPort almacenRepository) {
        this.inventarioRepository = inventarioRepository;
        this.productoRepository = productoRepository;
        this.almacenRepository = almacenRepository;
    }

    @Override
    public List<InventarioDetalle> listar(Long idEmpresa, Long idAlmacen) {
        return inventarioRepository.listarDetalle(idEmpresa, idAlmacen);
    }

    @Override
    public List<InventarioDetalle> listarBajoStock(Long idEmpresa) {
        return inventarioRepository.listarBajoStock(idEmpresa);
    }

    @Override
    public InventarioDetalle registrar(Long idEmpresa, Long idProducto, Long idAlmacen,
                                       int cantidadInicial, int stockMinimo) {
        // El producto y el almacén deben ser de la misma empresa del token
        boolean productoDeLaEmpresa = idProducto != null && productoRepository.buscarPorId(idProducto)
                .filter(producto -> producto.perteneceA(idEmpresa))
                .isPresent();
        if (!productoDeLaEmpresa) {
            throw new RecursoNoEncontradoException("No existe un producto con id " + idProducto);
        }
        Almacen almacen = almacenDeLaEmpresa(idEmpresa, idAlmacen);
        if (!almacen.estaActivo()) {
            throw new ReglaNegocioException("No se puede registrar inventario en un almacén inactivo");
        }
        if (inventarioRepository.existePorProductoYAlmacen(idProducto, idAlmacen)) {
            throw new RecursoDuplicadoException(
                    "Ese producto ya tiene inventario en ese almacén; use el ajuste de stock");
        }

        Inventario guardado = inventarioRepository.guardar(
                Inventario.nuevo(idProducto, idAlmacen, cantidadInicial, stockMinimo));
        return detalle(guardado.getId());
    }

    @Override
    public InventarioDetalle ajustar(Long idEmpresa, Long idInventario, TipoMovimiento tipo, int cantidad) {
        Inventario inventario = obtener(idEmpresa, idInventario);
        inventario.ajustar(tipo, cantidad);          // el dominio impide el stock negativo
        inventarioRepository.guardar(inventario);
        return detalle(idInventario);
    }

    @Override
    public InventarioDetalle cambiarStockMinimo(Long idEmpresa, Long idInventario, int stockMinimo) {
        Inventario inventario = obtener(idEmpresa, idInventario);
        inventario.cambiarStockMinimo(stockMinimo);
        inventarioRepository.guardar(inventario);
        return detalle(idInventario);
    }

    /** El inventario es de la empresa dueña de su almacén. Si es de otra, se responde 404. */
    private Inventario obtener(Long idEmpresa, Long id) {
        Inventario inventario = inventarioRepository.buscarPorId(id)
                .orElseThrow(() -> inventarioNoEncontrado(id));
        boolean deLaEmpresa = almacenRepository.buscarPorId(inventario.getIdAlmacen())
                .filter(almacen -> almacen.perteneceA(idEmpresa))
                .isPresent();
        if (!deLaEmpresa) {
            throw inventarioNoEncontrado(id);
        }
        return inventario;
    }

    private Almacen almacenDeLaEmpresa(Long idEmpresa, Long idAlmacen) {
        RecursoNoEncontradoException noExiste =
                new RecursoNoEncontradoException("No existe un almacén con id " + idAlmacen);
        if (idAlmacen == null) {
            throw noExiste;
        }
        return almacenRepository.buscarPorId(idAlmacen)
                .filter(almacen -> almacen.perteneceA(idEmpresa))
                .orElseThrow(() -> noExiste);
    }

    private InventarioDetalle detalle(Long id) {
        return inventarioRepository.buscarDetallePorId(id).orElseThrow(() -> inventarioNoEncontrado(id));
    }

    private static RecursoNoEncontradoException inventarioNoEncontrado(Long id) {
        return new RecursoNoEncontradoException("No existe un registro de inventario con id " + id);
    }
}
