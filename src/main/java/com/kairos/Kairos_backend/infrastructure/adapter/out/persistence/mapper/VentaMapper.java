package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.mapper;

import com.kairos.Kairos_backend.domain.model.DetalleVenta;
import com.kairos.Kairos_backend.domain.model.EstadoVenta;
import com.kairos.Kairos_backend.domain.model.Venta;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity.DetalleVentaEntity;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity.VentaEntity;

public final class VentaMapper {

    private VentaMapper() {
    }

    public static VentaEntity toEntity(Venta v) {
        var detalles = v.getDetalles().stream()
                .map(d -> new DetalleVentaEntity(d.getIdProducto(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal()))
                .toList();
        return new VentaEntity(v.getId(), v.getNumeroFactura(), v.getIdEmpresa(), v.getIdAlmacen(), v.getIdVendedor(),
                v.getIdCliente(), v.getFecha(), v.getTotal(), v.getEstado().name(), detalles);
    }

    public static Venta toDomain(VentaEntity e) {
        var detalles = e.getDetalles().stream()
                .map(d -> new DetalleVenta(d.getIdProducto(), d.getCantidad(), d.getPrecioUnitario()))
                .toList();
        return new Venta(e.getId(), e.getNumeroFactura(), e.getIdEmpresa(), e.getIdAlmacen(), e.getIdVendedor(),
                e.getIdCliente(), e.getFecha(), EstadoVenta.valueOf(e.getEstado()), detalles);
    }
}
