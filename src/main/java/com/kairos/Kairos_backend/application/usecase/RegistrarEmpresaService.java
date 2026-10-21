package com.kairos.Kairos_backend.application.usecase;

import com.kairos.Kairos_backend.application.port.in.RegistrarEmpresaUseCase;
import com.kairos.Kairos_backend.application.port.in.RegistrarUsuarioUseCase;
import com.kairos.Kairos_backend.application.port.in.RegistrarUsuarioUseCase.RegistrarEmpleadoCommand;
import com.kairos.Kairos_backend.application.port.out.EmpresaRepositoryPort;
import com.kairos.Kairos_backend.application.port.out.UsuarioRepositoryPort;
import com.kairos.Kairos_backend.domain.exception.RecursoDuplicadoException;
import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.Rol;
import com.kairos.Kairos_backend.domain.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrarEmpresaService implements RegistrarEmpresaUseCase {

    private final EmpresaRepositoryPort empresaRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final RegistrarUsuarioUseCase registrarUsuario;

    public RegistrarEmpresaService(EmpresaRepositoryPort empresaRepository,
                                   UsuarioRepositoryPort usuarioRepository,
                                   RegistrarUsuarioUseCase registrarUsuario) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.registrarUsuario = registrarUsuario;
    }

    /** Todo o nada: si el administrador no se puede crear, la empresa tampoco se guarda. */
    @Override
    @Transactional
    public ResultadoRegistroEmpresa registrar(RegistrarEmpresaCommand command) {
        // 1. El NIT identifica a la empresa: no puede repetirse
        if (command.nit() != null && empresaRepository.existePorNit(command.nit())) {
            throw new RecursoDuplicadoException("Ya existe una empresa con el NIT " + command.nit());
        }

        // 2. Se revisa el email del administrador ANTES de guardar la empresa
        if (command.emailAdmin() != null && usuarioRepository.existePorEmail(command.emailAdmin())) {
            throw new RecursoDuplicadoException("Ya existe un usuario con el email " + command.emailAdmin());
        }

        // 3. El dominio valida la empresa y la deja PENDIENTE
        Empresa empresa = empresaRepository.guardar(
                Empresa.nueva(command.nombre(), command.nit(), command.email(), command.telefono()));

        // 4. Su primer administrador (se reutilizan las reglas del registro de usuarios)
        Usuario administrador = registrarUsuario.registrarEmpleado(new RegistrarEmpleadoCommand(
                command.nombreAdmin(), command.emailAdmin(), command.passwordAdmin(),
                Rol.ADMINISTRADOR, command.telefonoAdmin(), null, empresa.getId()));

        return new ResultadoRegistroEmpresa(empresa, administrador);
    }
}
