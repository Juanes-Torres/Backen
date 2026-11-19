package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.MarcaEmpresaUseCase;
import com.kairos.Kairos_backend.application.port.out.AlmacenamientoArchivosPort;
import com.kairos.Kairos_backend.application.port.out.EmpresaRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoNoEncontradoException;
import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.Imagen;
import org.springframework.stereotype.Service;

/**
 * El logo se guarda en la carpeta de archivos con un nombre fijo por empresa ("empresas/15/logo"):
 * así no hace falta una columna nueva en la base de datos. El formato se reconoce por el contenido.
 */
@Service
public class MarcaEmpresaService implements MarcaEmpresaUseCase {

    private final EmpresaRepositoryPort empresas;
    private final AlmacenamientoArchivosPort archivos;

    public MarcaEmpresaService(EmpresaRepositoryPort empresas, AlmacenamientoArchivosPort archivos) {
        this.empresas = empresas;
        this.archivos = archivos;
    }

    static String rutaLogo(Long idEmpresa) {
        return "empresas/" + idEmpresa + "/logo";
    }

    @Override
    public Empresa miEmpresa(Long idEmpresa) {
        if (idEmpresa == null) {
            throw new ReglaNegocioException("El usuario no pertenece a ninguna empresa");
        }
        return empresas.buscarPorId(idEmpresa)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una empresa con id " + idEmpresa));
    }

    @Override
    public Empresa actualizarPerfil(Long idEmpresa, String nombre, String email, String telefono) {
        Empresa empresa = miEmpresa(idEmpresa);
        empresa.actualizarDatos(nombre, email, telefono);   // el dominio valida los datos
        return empresas.guardar(empresa);
    }

    @Override
    public void subirLogo(Long idEmpresa, byte[] contenido) {
        miEmpresa(idEmpresa);
        Imagen logo = Imagen.desde(contenido);   // misma regla que las fotos de productos
        archivos.guardar(rutaLogo(idEmpresa), logo.contenido());
    }

    @Override
    public void quitarLogo(Long idEmpresa) {
        miEmpresa(idEmpresa);
        archivos.borrar(rutaLogo(idEmpresa));
    }

    @Override
    public Imagen logo(Long idEmpresa) {
        boolean activa = empresas.buscarPorId(idEmpresa).map(Empresa::estaActiva).orElse(false);
        return (activa ? archivos.leer(rutaLogo(idEmpresa)) : java.util.Optional.<byte[]>empty())
                .map(Imagen::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("La empresa " + idEmpresa + " no tiene logo"));
    }

    @Override
    public boolean tieneLogo(Long idEmpresa) {
        return idEmpresa != null && archivos.existe(rutaLogo(idEmpresa));
    }
}
