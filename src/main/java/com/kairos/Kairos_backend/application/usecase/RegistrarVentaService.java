package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.RegistrarVentaUseCase;
import com.kairos.Kairos_backend.application.port.in.Solicitante;
import com.kairos.Kairos_backend.application.port.out.AlmacenRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.InventarioRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.ProductoRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.UsuarioRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.VentaRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import com.kairos.Kairos_backend.domain.model.Almacen;
import com.kairos.Kairos_backend.domain.model.DetalleVenta;
import com.kairos.Kairos_backend.domain.model.Inventario;
import com.kairos.Kairos_backend.domain.model.Producto;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import com.kairos.Kairos_backend.domain.model.Venta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RegistrarVentaService implements RegistrarVentaUseCase {

    private final VentaRepositoryPort ventaRepository;
    private final InventarioRepositoryPort inventarioRepository;
    private final ProductoRepositoryPort productoRepository;
    private final AlmacenRepositoryPort almacenRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    public RegistrarVentaService(VentaRepositoryPort ventaRepository,
                                 InventarioRepositoryPort inventarioRepository,
                                 ProductoRepositoryPort productoRepository,
                                 AlmacenRepositoryPort almacenRepository,
                                 UsuarioRepositoryPort usuarioRepository) {
        this.ventaRepository = ventaRepository;
        this.inventarioRepository = inventarioRepository;
        this.productoRepository = productoRepository;
        this.almacenRepository = almacenRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** Todo o nada: si un producto falla, no se guarda la venta ni se descuenta ningún stock. */
    @Override
    @Transactional
    public Venta registrar(Solicitante solicitante, RegistrarVentaCommand command) {
        Long idEmpresa = solicitante.idEmpresa();
        Almacen almacen = almacenDeLaVenta(solicitante, command.idAlmacen());
        Long idCliente = clienteDeLaVenta(command.emailCliente());

        if (command.items() == null || command.items().isEmpty()) {
            throw new ReglaNegocioException("La venta debe tener al menos un producto");
        }

        // 1. Se arma TODO en memoria: precios, líneas y descuento de stock.
        //    Si algo falla aquí, todavía no se ha guardado nada.
        List<DetalleVenta> detalles = new ArrayList<>();
        List<Inventario> inventarios = new ArrayList<>();
        for (Item item : command.items()) {
            Producto producto = productoDeLaEmpresa(idEmpresa, item.idProducto());
            DetalleVenta detalle = DetalleVenta.de(producto, item.cantidad());
            Inventario inventario = inventarioRepository.buscarParaDescontar(producto.getId(), almacen.getId())
                    .orElseThrow(() -> new ReglaNegocioException(
                            producto.getNombre() + ": no hay inventario en " + almacen.getNombre()));
            try {
                inventario.registrarSalida(detalle.getCantidad());   // el dominio impide el stock negativo
            } catch (ReglaNegocioException e) {
                throw new ReglaNegocioException(producto.getNombre() + ": " + e.getMessage());
            }
            detalles.add(detalle);
            inventarios.add(inventario);
        }

        // 2. El dominio valida la venta (sin productos repetidos) y calcula el total
        String numero = Venta.numeroFactura(ventaRepository.contarPorEmpresa(idEmpresa) + 1);
        Venta venta = Venta.nueva(numero, idEmpresa, almacen.getId(), solicitante.idUsuario(), idCliente, detalles);

        // 3. Recién ahora se guarda
        inventarios.forEach(inventarioRepository::guardar);
        return ventaRepository.guardar(venta);
    }

    /** El vendedor vende siempre en SU sede; el administrador elige una sede de su empresa. */
    private Almacen almacenDeLaVenta(Solicitante solicitante, Long idAlmacenPedido) {
        Long idAlmacen = idAlmacenPedido;
        if (solicitante.rol() == Rol.VENDEDOR) {
            Long suAlmacen = usuarioRepository.buscarPorId(solicitante.idUsuario())
                    .map(Usuario::getIdAlmacen)
                    .orElseThrow(() -> new ReglaNegocioException("El vendedor no tiene almacén asignado"));
            if (idAlmacenPedido != null && !idAlmacenPedido.equals(suAlmacen)) {
                throw new ReglaNegocioException("Un vendedor solo puede vender en su propio almacén");
            }
            idAlmacen = suAlmacen;
        } else if (idAlmacen == null) {
            throw new ReglaNegocioException("Indique el almacén (idAlmacen) donde se hace la venta");
        }

        Long id = idAlmacen;
        Almacen almacen = almacenRepository.buscarPorId(id)
                .filter(a -> a.perteneceA(solicitante.idEmpresa()))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un almacén con id " + id));
        if (!almacen.estaActivo()) {
            throw new ReglaNegocioException("No se puede vender en un almacén inactivo");
        }
        return almacen;
    }

    /** El cliente es opcional; si se indica, debe ser un CLIENTE activo. */
    private Long clienteDeLaVenta(String emailCliente) {
        if (emailCliente == null || emailCliente.isBlank()) {
            return null;
        }
        return usuarioRepository.buscarPorEmail(emailCliente)
                .filter(u -> u.getRol() == Rol.CLIENTE && u.isActivo())
                .map(Usuario::getId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un cliente con email " + emailCliente));
    }

    private Producto productoDeLaEmpresa(Long idEmpresa, Long idProducto) {
        if (idProducto == null) {
            throw new ReglaNegocioException("Cada producto debe tener idProducto");
        }
        return productoRepository.buscarPorId(idProducto)
                .filter(p -> p.perteneceA(idEmpresa))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un producto con id " + idProducto));
    }
}
