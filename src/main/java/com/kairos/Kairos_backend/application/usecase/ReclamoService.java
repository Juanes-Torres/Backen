package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.GestionarReclamosUseCase;
import com.kairos.Kairos_backend.application.port.in.Solicitante;
import com.kairos.Kairos_backend.application.port.out.ProductoRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.ReclamoRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.VentaRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import com.kairos.Kairos_backend.domain.model.EstadoReclamo;
import com.kairos.Kairos_backend.domain.model.Garantia;
import com.kairos.Kairos_backend.domain.model.Producto;
import com.kairos.Kairos_backend.domain.model.ReclamoGarantia;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Venta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReclamoService implements GestionarReclamosUseCase {

    private final ReclamoRepositoryPort reclamoRepository;
    private final VentaRepositoryPort ventaRepository;
    private final ProductoRepositoryPort productoRepository;
    private final Clock reloj;

    public ReclamoService(ReclamoRepositoryPort reclamoRepository, VentaRepositoryPort ventaRepository,
                          ProductoRepositoryPort productoRepository, Clock reloj) {
        this.reclamoRepository = reclamoRepository;
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.reloj = reloj;
    }

    @Override
    public ReclamoGarantia registrar(Solicitante solicitante, RegistrarReclamoCommand command) {
        exigirEmpleado(solicitante);

        // 1. La venta debe ser de la empresa del empleado (si no, es como si no existiera)
        Venta venta = ventaRepository.buscarPorId(command.idVenta())
                .filter(v -> v.getIdEmpresa().equals(solicitante.idEmpresa()))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una venta con id " + command.idVenta()));

        // 2. El producto debe estar en esa factura
        boolean estaEnLaFactura = venta.getDetalles().stream()
                .anyMatch(d -> d.getIdProducto().equals(command.idProducto()));
        if (!estaEnLaFactura) {
            throw new ReglaNegocioException("El producto " + command.idProducto() + " no está en la factura "
                    + venta.getNumeroFactura());
        }

        // 3. La garantía debe seguir vigente (regla del dominio: fecha de compra + meses)
        Producto producto = productoRepository.buscarPorId(command.idProducto())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un producto con id " + command.idProducto()));
        Garantia garantia = producto.garantiaDesde(venta.getFecha().toLocalDate());
        if (!garantia.vigenteEl(LocalDate.now(reloj))) {
            throw new ReglaNegocioException("La garantía de " + producto.getNombre() + " venció el " + garantia.fechaFin());
        }

        ReclamoGarantia reclamo = ReclamoGarantia.nuevo(venta.getIdEmpresa(), venta.getId(), producto.getId(),
                venta.getIdCliente(), command.motivo(), solicitante.idUsuario(), LocalDateTime.now(reloj));
        return reclamoRepository.guardar(reclamo);
    }

    @Override
    public ReclamoGarantia cambiarEstado(Solicitante solicitante, Long idReclamo, EstadoReclamo nuevo, String nota) {
        exigirEmpleado(solicitante);
        ReclamoGarantia reclamo = reclamoRepository.buscarPorId(idReclamo)
                .filter(r -> r.perteneceA(solicitante.idEmpresa()))
                .orElseThrow(() -> noExiste(idReclamo));
        reclamo.cambiarEstado(nuevo, nota, solicitante.idUsuario(), LocalDateTime.now(reloj));
        return reclamoRepository.guardar(reclamo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamoGarantia> listar(Solicitante solicitante) {
        if (solicitante.rol() == Rol.CLIENTE) {
            return reclamoRepository.listarPorCliente(solicitante.idUsuario());
        }
        exigirEmpleado(solicitante);
        return reclamoRepository.listarPorEmpresa(solicitante.idEmpresa());
    }

    @Override
    @Transactional(readOnly = true)
    public ReclamoGarantia buscar(Solicitante solicitante, Long idReclamo) {
        return reclamoRepository.buscarPorId(idReclamo)
                .filter(r -> PermisosVenta.puedeVer(solicitante, r.getIdEmpresa(), r.getIdCliente()))
                .orElseThrow(() -> noExiste(idReclamo));
    }

    private static void exigirEmpleado(Solicitante solicitante) {
        boolean empleado = (solicitante.rol() == Rol.ADMINISTRADOR || solicitante.rol() == Rol.VENDEDOR)
                && solicitante.idEmpresa() != null;
        if (!empleado) {
            throw new ReglaNegocioException("Solo los empleados de una empresa gestionan reclamos");
        }
    }

    private static RecursoNoEncontradoException noExiste(Long idReclamo) {
        return new RecursoNoEncontradoException("No existe un reclamo con id " + idReclamo);
    }
}
