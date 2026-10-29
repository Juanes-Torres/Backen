package com.kairos.Kairos_backend.infrastructure.adapter.in.web.catalogo;

import com.kairos.Kairos_backend.application.port.out.CatalogoExternoPort.DatosProductoExterno;

/** Datos sugeridos para llenar el formulario de producto (el admin los revisa antes de guardar). */
public record DatosCodigoBarrasResponse(
        String codigoBarras,
        String nombre,
        String marca,
        String descripcion,
        String categoriaSugerida,
        String fuente
) {
    public static DatosCodigoBarrasResponse desde(String codigo, DatosProductoExterno d) {
        return new DatosCodigoBarrasResponse(codigo.trim(), d.nombre(), d.marca(), d.descripcion(),
                d.categoria(), "UPCitemdb");
    }
}
