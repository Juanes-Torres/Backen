package com.kairos.Kairos_backend.application.port.in;

/**
 * CASO DE USO: el código QR de una factura. Al escanearlo se abre la consulta
 * de garantía de esa compra en la página web.
 * Lo pueden pedir los empleados de la empresa de la venta o el cliente que compró.
 */
public interface GenerarQrFacturaUseCase {

    /** Imagen PNG del QR. */
    byte[] generarQr(Solicitante solicitante, Long idVenta);
}
