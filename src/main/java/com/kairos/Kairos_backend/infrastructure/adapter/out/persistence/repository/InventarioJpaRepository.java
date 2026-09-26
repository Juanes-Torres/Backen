package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.repository;

import com.kairos.Kairos_backend.application.port.out.InventarioDetalle;
import com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity.InventarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventarioJpaRepository extends JpaRepository<InventarioEntity, Long> {

    boolean existsByIdProductoAndIdAlmacen(Long idProducto, Long idAlmacen);

    boolean existsByIdProducto(Long idProducto);

    // Consultas JPQL: une inventario + producto + almacén para traer también los nombres.
    // "new ...InventarioDetalle(...)" crea directamente el record con el resultado.

    @Query("""
            SELECT new com.kairos.Kairos_backend.application.port.out.InventarioDetalle(
                i.id, p.id, p.nombre, a.id, a.nombre, i.cantidadDisponible, i.stockMinimo)
            FROM InventarioEntity i
            JOIN ProductoEntity p ON p.id = i.idProducto
            JOIN AlmacenEntity a ON a.id = i.idAlmacen
            ORDER BY a.nombre, p.nombre
            """)
    List<InventarioDetalle> listarDetalle();

    @Query("""
            SELECT new com.kairos.Kairos_backend.application.port.out.InventarioDetalle(
                i.id, p.id, p.nombre, a.id, a.nombre, i.cantidadDisponible, i.stockMinimo)
            FROM InventarioEntity i
            JOIN ProductoEntity p ON p.id = i.idProducto
            JOIN AlmacenEntity a ON a.id = i.idAlmacen
            WHERE i.idAlmacen = :idAlmacen
            ORDER BY p.nombre
            """)
    List<InventarioDetalle> listarDetallePorAlmacen(@Param("idAlmacen") Long idAlmacen);

    @Query("""
            SELECT new com.kairos.Kairos_backend.application.port.out.InventarioDetalle(
                i.id, p.id, p.nombre, a.id, a.nombre, i.cantidadDisponible, i.stockMinimo)
            FROM InventarioEntity i
            JOIN ProductoEntity p ON p.id = i.idProducto
            JOIN AlmacenEntity a ON a.id = i.idAlmacen
            WHERE i.cantidadDisponible < i.stockMinimo
            ORDER BY a.nombre, p.nombre
            """)
    List<InventarioDetalle> listarBajoStock();

    @Query("""
            SELECT new com.kairos.Kairos_backend.application.port.out.InventarioDetalle(
                i.id, p.id, p.nombre, a.id, a.nombre, i.cantidadDisponible, i.stockMinimo)
            FROM InventarioEntity i
            JOIN ProductoEntity p ON p.id = i.idProducto
            JOIN AlmacenEntity a ON a.id = i.idAlmacen
            WHERE i.id = :id
            """)
    Optional<InventarioDetalle> buscarDetallePorId(@Param("id") Long id);
}
