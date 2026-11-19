package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.ConsultarCatalogoUseCase;
import com.kairos.Kairos_backend.application.port.in.GestionarProductosUseCase;
import com.kairos.Kairos_backend.application.port.in.MarcaEmpresaUseCase;
import com.kairos.Kairos_backend.application.port.out.DisponibilidadPort;
import com.kairos.Kairos_backend.application.port.out.EmpresaRepositoryPort;
import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.EstadoEmpresa;
import com.kairos.Kairos_backend.domain.model.Producto;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pocas consultas sin importar cuántos productos haya: una para los productos, una para los nombres
 * de las empresas y una para la disponibilidad (y el logo se revisa una vez por empresa).
 */
@Service
public class CatalogoService implements ConsultarCatalogoUseCase {

    private final GestionarProductosUseCase productos;
    private final EmpresaRepositoryPort empresas;
    private final DisponibilidadPort disponibilidad;
    private final MarcaEmpresaUseCase marca;

    public CatalogoService(GestionarProductosUseCase productos, EmpresaRepositoryPort empresas,
                           DisponibilidadPort disponibilidad, MarcaEmpresaUseCase marca) {
        this.productos = productos;
        this.empresas = empresas;
        this.disponibilidad = disponibilidad;
        this.marca = marca;
    }

    @Override
    public List<ProductoEnCatalogo> buscar(Long idEmpresa, Long idCategoria, String nombre) {
        return completar(productos.buscar(idEmpresa, idCategoria, nombre));   // solo empresas activas
    }

    @Override
    public ProductoEnCatalogo obtener(Long idProducto) {
        return completar(List.of(productos.obtener(idProducto))).getFirst();
    }

    private List<ProductoEnCatalogo> completar(List<Producto> lista) {
        if (lista.isEmpty()) {
            return List.of();
        }
        Map<Long, String> nombres = empresas.listarPorEstado(EstadoEmpresa.ACTIVA).stream()
                .collect(Collectors.toMap(Empresa::getId, Empresa::getNombre));
        Set<Long> conStock = disponibilidad.disponibles(lista.stream().map(Producto::getId).toList());
        Map<Long, Boolean> conLogo = new HashMap<>();
        return lista.stream()
                .map(p -> new ProductoEnCatalogo(p, nombres.get(p.getIdEmpresa()),
                        conLogo.computeIfAbsent(p.getIdEmpresa(), marca::tieneLogo),
                        conStock.contains(p.getId())))
                .toList();
    }
}
