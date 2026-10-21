package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.GestionarAlmacenesUseCase;
import com.kairos.Kairos_backend.application.port.out.AlmacenRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.model.Almacen;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlmacenService implements GestionarAlmacenesUseCase {

    private final AlmacenRepositoryPort almacenRepository;

    public AlmacenService(AlmacenRepositoryPort almacenRepository) {
        this.almacenRepository = almacenRepository;
    }

    @Override
    public List<Almacen> listar(Long idEmpresa) {
        return almacenRepository.listarPorEmpresa(idEmpresa);
    }

    /** Una sede de otra empresa se trata igual que una que no existe: así no se revela nada. */
    @Override
    public Almacen obtener(Long idEmpresa, Long id) {
        return almacenRepository.buscarPorId(id)
                .filter(almacen -> almacen.perteneceA(idEmpresa))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un almacén con id " + id));
    }

    @Override
    public Almacen crear(Long idEmpresa, String nombre, String ciudad, String direccion) {
        return almacenRepository.guardar(Almacen.nuevo(nombre, ciudad, direccion, idEmpresa));
    }

    @Override
    public Almacen actualizar(Long idEmpresa, Long id, String nombre, String ciudad, String direccion) {
        Almacen almacen = obtener(idEmpresa, id);
        almacen.actualizar(nombre, ciudad, direccion);
        return almacenRepository.guardar(almacen);
    }

    @Override
    public Almacen cambiarEstado(Long idEmpresa, Long id, boolean activo) {
        Almacen almacen = obtener(idEmpresa, id);
        if (activo) {
            almacen.activar();
        } else {
            almacen.desactivar();
        }
        return almacenRepository.guardar(almacen);
    }
}
