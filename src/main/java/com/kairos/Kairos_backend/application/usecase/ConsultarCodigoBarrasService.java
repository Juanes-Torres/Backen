package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.ConsultarCodigoBarrasUseCase;
import com.kairos.Kairos_backend.application.port.out.CatalogoExternoPort;
import com.kairos.Kairos_backend.application.port.out.CatalogoExternoPort.DatosProductoExterno;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.model.CodigoBarras;
import org.springframework.stereotype.Service;

@Service
public class ConsultarCodigoBarrasService implements ConsultarCodigoBarrasUseCase {

    private final CatalogoExternoPort catalogoExterno;

    public ConsultarCodigoBarrasService(CatalogoExternoPort catalogoExterno) {
        this.catalogoExterno = catalogoExterno;
    }

    @Override
    public DatosProductoExterno consultar(String codigoBarras) {
        CodigoBarras codigo = new CodigoBarras(codigoBarras);   // el dominio valida el formato
        return catalogoExterno.buscarPorCodigoBarras(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró un producto con el código " + codigo.valor() + "; llene los datos a mano"));
    }
}
