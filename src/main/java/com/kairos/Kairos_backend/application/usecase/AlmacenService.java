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
    public List<Almacen> listar() {
        return almacenRepository.listarTodos();
    }

    @Override
    public Almacen obtener(Long id) {
        return almacenRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un almacén con id " + id));
    }

    @Override
    public Almacen crear(String nombre, String ciudad, String direccion) {
        return almacenRepository.guardar(Almacen.nuevo(nombre, ciudad, direccion));
    }

    @Override
    public Almacen actualizar(Long id, String nombre, String ciudad, String direccion) {
        Almacen almacen = obtener(id);
        almacen.actualizar(nombre, ciudad, direccion);
        return almacenRepository.guardar(almacen);
    }

    @Override
    public Almacen cambiarEstado(Long id, boolean activo) {
        Almacen almacen = obtener(id);
        if (activo) {
            almacen.activar();
        } else {
            almacen.desactivar();
        }
        return almacenRepository.guardar(almacen);
    }
}
