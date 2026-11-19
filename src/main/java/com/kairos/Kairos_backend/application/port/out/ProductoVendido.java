package com.kairos.Kairos_backend.application.port.out;

import java.math.BigDecimal;

/** Vista de LECTURA: cuántas unidades se vendieron de un producto en un periodo y por cuánto. */
public record ProductoVendido(String producto, Long unidades, BigDecimal total) {
}
