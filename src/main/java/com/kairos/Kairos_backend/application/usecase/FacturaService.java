package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.ConsultarFacturaUseCase;
import com.kairos.Kairos_backend.application.port.in.ConsultarVentasUseCase;
import com.kairos.Kairos_backend.application.port.in.ConsultarVentasUseCase.VentaConDetalle;
import com.kairos.Kairos_backend.application.port.in.Solicitante;
import com.kairos.Kairos_backend.application.port.out.AlmacenRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.EmpresaRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.LineaVentaDetalle;
import com.kairos.Kairos_backend.application.port.out.ProductoRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.UsuarioRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.VentaResumen;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.model.Garantia;
import com.kairos.Kairos_backend.domain.model.Producto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class FacturaService implements ConsultarFacturaUseCase {

    private final ConsultarVentasUseCase ventas;
    private final EmpresaRepositoryPort empresas;
    private final AlmacenRepositoryPort almacenes;
    private final UsuarioRepositoryPort usuarios;
    private final ProductoRepositoryPort productos;

    public FacturaService(ConsultarVentasUseCase ventas, EmpresaRepositoryPort empresas,
                          AlmacenRepositoryPort almacenes, UsuarioRepositoryPort usuarios,
                          ProductoRepositoryPort productos) {
        this.ventas = ventas;
        this.empresas = empresas;
        this.almacenes = almacenes;
        this.usuarios = usuarios;
        this.productos = productos;
    }

    @Override
    public Factura factura(Solicitante solicitante, Long idVenta) {
        // Los permisos son los mismos del detalle de la venta: se reutiliza esa consulta (404 si no la puede ver)
        VentaConDetalle detalle = ventas.buscar(solicitante, idVenta);
        VentaResumen venta = detalle.venta();
        LocalDate diaCompra = venta.fecha().toLocalDate();

        Emisor emisor = empresas.buscarPorId(venta.idEmpresa())
                .map(e -> new Emisor(e.getNombre(), e.getNit(), e.getEmail(), e.getTelefono()))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la empresa de la venta " + idVenta));
        Sede sede = almacenes.buscarPorId(venta.idAlmacen())
                .map(a -> new Sede(a.getNombre(), a.getCiudad(), a.getDireccion()))
                .orElse(new Sede(venta.almacen(), null, null));
        // Del cliente solo va lo que lleva una factura: nombre y cédula (nunca su correo ni su teléfono)
        Cliente cliente = venta.idCliente() == null ? null : usuarios.buscarPorId(venta.idCliente())
                .map(c -> new Cliente(c.getNombre(), c.getCedula()))
                .orElse(new Cliente(venta.cliente(), null));

        return new Factura(venta.numeroFactura(), venta.fecha(), venta.estado(), emisor, sede, venta.vendedor(),
                cliente, detalle.lineas().stream().map(l -> linea(l, diaCompra)).toList(), venta.total());
    }

    private Linea linea(LineaVentaDetalle l, LocalDate diaCompra) {
        int meses = productos.buscarPorId(l.idProducto()).map(Producto::getMesesGarantia).orElse(0);
        LocalDate hasta = meses > 0 ? new Garantia(diaCompra, meses).fechaFin() : null;
        return new Linea(l.producto(), l.cantidad(), l.precioUnitario(), l.subtotal(), meses, hasta);
    }
}
