package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.repository;

import com.kairos.Kairos_backend.application.port.out.LineaVentaDetalle;
import com.kairos.Kairos_backend.application.port.out.VentaResumen;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity.VentaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VentaJpaRepository extends JpaRepository<VentaEntity, Long> {

    long countByIdEmpresa(Long idEmpresa);

    /**
     * Ventas con los nombres de la sede, el vendedor y el cliente (LEFT JOIN: la venta de
     * mostrador no tiene cliente). Un filtro nulo no se aplica.
     */
    @Query("""
            SELECT new com.kairos.Kairos_backend.application.port.out.VentaResumen(
                v.id, v.numeroFactura, v.fecha, v.idEmpresa, a.id, a.nombre, u.id, u.nombre,
                c.id, c.nombre, v.estado, v.total)
            FROM VentaEntity v
            JOIN AlmacenEntity a ON a.id = v.idAlmacen
            JOIN UsuarioEntity u ON u.id = v.idVendedor
            LEFT JOIN UsuarioEntity c ON c.id = v.idCliente
            WHERE (:idEmpresa IS NULL OR v.idEmpresa = :idEmpresa)
              AND (:idAlmacen IS NULL OR v.idAlmacen = :idAlmacen)
              AND (:idVendedor IS NULL OR v.idVendedor = :idVendedor)
              AND (:idCliente IS NULL OR v.idCliente = :idCliente)
              AND v.fecha >= :desde AND v.fecha < :hasta
            ORDER BY v.fecha DESC, v.id DESC
            """)
    List<VentaResumen> listarResumen(@Param("idEmpresa") Long idEmpresa,
                                     @Param("idAlmacen") Long idAlmacen,
                                     @Param("idVendedor") Long idVendedor,
                                     @Param("idCliente") Long idCliente,
                                     @Param("desde") LocalDateTime desde,
                                     @Param("hasta") LocalDateTime hasta);

    @Query("""
            SELECT new com.kairos.Kairos_backend.application.port.out.VentaResumen(
                v.id, v.numeroFactura, v.fecha, v.idEmpresa, a.id, a.nombre, u.id, u.nombre,
                c.id, c.nombre, v.estado, v.total)
            FROM VentaEntity v
            JOIN AlmacenEntity a ON a.id = v.idAlmacen
            JOIN UsuarioEntity u ON u.id = v.idVendedor
            LEFT JOIN UsuarioEntity c ON c.id = v.idCliente
            WHERE v.id = :idVenta
            """)
    Optional<VentaResumen> buscarResumen(@Param("idVenta") Long idVenta);

    /** Líneas de la factura con el nombre del producto. */
    @Query("""
            SELECT new com.kairos.Kairos_backend.application.port.out.LineaVentaDetalle(
                d.idProducto, p.nombre, d.cantidad, d.precioUnitario, d.subtotal)
            FROM VentaEntity v
            JOIN v.detalles d
            JOIN ProductoEntity p ON p.id = d.idProducto
            WHERE v.id = :idVenta
            ORDER BY p.nombre
            """)
    List<LineaVentaDetalle> lineasDe(@Param("idVenta") Long idVenta);
}
