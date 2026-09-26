package com.kairos.Kairos_backend.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventario")
public class InventarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inventario")
    private Long id;

    @Column(name = "id_producto", nullable = false)
    private Long idProducto;

    @Column(name = "id_almacen", nullable = false)
    private Long idAlmacen;

    @Column(name = "cantidad_disponible", nullable = false)
    private Integer cantidadDisponible;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    protected InventarioEntity() {
    }

    public InventarioEntity(Long id, Long idProducto, Long idAlmacen,
                            Integer cantidadDisponible, Integer stockMinimo) {
        this.id = id;
        this.idProducto = idProducto;
        this.idAlmacen = idAlmacen;
        this.cantidadDisponible = cantidadDisponible;
        this.stockMinimo = stockMinimo;
    }

    public Long getId() { return id; }
    public Long getIdProducto() { return idProducto; }
    public Long getIdAlmacen() { return idAlmacen; }
    public Integer getCantidadDisponible() { return cantidadDisponible; }
    public Integer getStockMinimo() { return stockMinimo; }
}
