package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Reclamo de garantía de UN producto de UNA venta (SCRUM-75).
 * Guarda su historial: cada cambio de estado queda con la fecha y quién lo hizo.
 */
public class ReclamoGarantia {

    private static final int MAX_TEXTO = 500;

    private final Long id;
    private final Long idEmpresa;
    private final Long idVenta;
    private final Long idProducto;
    private final Long idCliente;          // nulo si la venta fue de mostrador
    private final String motivo;
    private EstadoReclamo estado;
    private final LocalDateTime fechaRegistro;
    private final Long idRegistradoPor;
    private final List<SeguimientoReclamo> historial;

    /** Reconstruye un reclamo que YA existe en la base de datos. */
    public ReclamoGarantia(Long id, Long idEmpresa, Long idVenta, Long idProducto, Long idCliente, String motivo,
                           EstadoReclamo estado, LocalDateTime fechaRegistro, Long idRegistradoPor,
                           List<SeguimientoReclamo> historial) {
        if (idEmpresa == null || idVenta == null || idProducto == null || idRegistradoPor == null) {
            throw new ReglaNegocioException("El reclamo debe tener empresa, venta, producto y quién lo registra");
        }
        String texto = motivo == null ? "" : motivo.trim();
        if (texto.isEmpty()) {
            throw new ReglaNegocioException("El motivo del reclamo es obligatorio");
        }
        if (texto.length() > MAX_TEXTO) {
            throw new ReglaNegocioException("El motivo admite máximo " + MAX_TEXTO + " caracteres");
        }
        this.id = id;
        this.idEmpresa = idEmpresa;
        this.idVenta = idVenta;
        this.idProducto = idProducto;
        this.idCliente = idCliente;
        this.motivo = texto;
        this.estado = estado == null ? EstadoReclamo.RECIBIDO : estado;
        this.fechaRegistro = fechaRegistro;
        this.idRegistradoPor = idRegistradoPor;
        this.historial = new ArrayList<>(historial == null ? List.of() : historial);
    }

    /** Un reclamo NUEVO empieza en RECIBIDO, y ese primer paso queda en el historial. */
    public static ReclamoGarantia nuevo(Long idEmpresa, Long idVenta, Long idProducto, Long idCliente,
                                        String motivo, Long idRegistradoPor, LocalDateTime ahora) {
        return new ReclamoGarantia(null, idEmpresa, idVenta, idProducto, idCliente, motivo, EstadoReclamo.RECIBIDO,
                ahora, idRegistradoPor, List.of(new SeguimientoReclamo(EstadoReclamo.RECIBIDO, null, ahora, idRegistradoPor)));
    }

    /** Cambia el estado solo si el paso está permitido, y lo anota en el historial. */
    public void cambiarEstado(EstadoReclamo nuevo, String nota, Long idUsuario, LocalDateTime ahora) {
        if (nuevo == null) {
            throw new ReglaNegocioException("El nuevo estado es obligatorio");
        }
        if (!estado.puedePasarA(nuevo)) {
            throw new ReglaNegocioException("Un reclamo " + estado + " no puede pasar a " + nuevo);
        }
        String textoNota = nota == null || nota.isBlank() ? null : nota.trim();
        if (textoNota != null && textoNota.length() > MAX_TEXTO) {
            throw new ReglaNegocioException("La nota admite máximo " + MAX_TEXTO + " caracteres");
        }
        this.estado = nuevo;
        this.historial.add(new SeguimientoReclamo(nuevo, textoNota, ahora, idUsuario));
    }

    public boolean perteneceA(Long otraEmpresa) {
        return idEmpresa.equals(otraEmpresa);
    }

    public Long getId() { return id; }
    public Long getIdEmpresa() { return idEmpresa; }
    public Long getIdVenta() { return idVenta; }
    public Long getIdProducto() { return idProducto; }
    public Long getIdCliente() { return idCliente; }
    public String getMotivo() { return motivo; }
    public EstadoReclamo getEstado() { return estado; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public Long getIdRegistradoPor() { return idRegistradoPor; }
    public List<SeguimientoReclamo> getHistorial() { return List.copyOf(historial); }
}
