package com.kairos.Kairos_backend.application.port.in;

import com.kairos.Kairos_backend.domain.model.Empresa;

import java.util.List;

/**
 * CASO DE USO PÚBLICO: las empresas donde un cliente puede comprar.
 * Va aparte de GestionarEmpresasUseCase (del superadmin): quien solo consulta
 * no depende de métodos que no usa (principio de segregación de interfaces).
 */
public interface ConsultarEmpresasActivasUseCase {

    List<Empresa> listarActivas();
}
