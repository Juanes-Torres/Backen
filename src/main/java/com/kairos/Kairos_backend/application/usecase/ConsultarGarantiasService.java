package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.ConsultarGarantiasUseCase;
import com.kairos.Kairos_backend.application.port.in.Solicitante;
import com.kairos.Kairos_backend.application.port.out.CompraConGarantia;
import com.kairos.Kairos_backend.application.port.out.ConsultaGarantiasPort;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import com.kairos.Kairos_backend.domain.model.Garantia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ConsultarGarantiasService implements ConsultarGarantiasUseCase {

    private final ConsultaGarantiasPort consultaGarantias;
    private final Clock reloj;   // "hoy" viene de afuera: así las pruebas pueden fijar la fecha

    public ConsultarGarantiasService(ConsultaGarantiasPort consultaGarantias, Clock reloj) {
        this.consultaGarantias = consultaGarantias;
        this.reloj = reloj;
    }

    @Override
    public List<GarantiaDeProducto> consultar(Solicitante solicitante, String cedula, String factura) {
        String cedulaLimpia = limpio(cedula);
        String facturaLimpia = limpio(factura);
        if ((cedulaLimpia == null) == (facturaLimpia == null)) {
            throw new ReglaNegocioException("Envíe la cédula o el número de factura (solo uno de los dos)");
        }
        if (solicitante.idEmpresa() == null) {
            throw new ReglaNegocioException("Solo los empleados de una empresa consultan garantías");
        }

        List<CompraConGarantia> compras = cedulaLimpia != null
                ? consultaGarantias.comprasPorCedula(solicitante.idEmpresa(), cedulaLimpia)
                : consultaGarantias.comprasPorFactura(solicitante.idEmpresa(), String.valueOf(facturaLimpia).toUpperCase());

        LocalDate hoy = LocalDate.now(reloj);
        return compras.stream().map(compra -> {
            // La regla de vigencia vive en el dominio: fecha de compra + meses de garantía
            Garantia garantia = new Garantia(compra.fechaCompra().toLocalDate(), compra.mesesGarantia());
            return new GarantiaDeProducto(compra, garantia.fechaFin(), garantia.vigenteEl(hoy));
        }).toList();
    }

    /** Texto sin espacios a los lados; vacío o nulo cuenta como "no enviado". */
    private static String limpio(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
