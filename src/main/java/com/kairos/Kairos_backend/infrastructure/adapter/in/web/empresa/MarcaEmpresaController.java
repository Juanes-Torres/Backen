package com.kairos.Kairos_backend.infrastructure.adapter.in.web.empresa;

import com.kairos.Kairos_backend.application.port.in.MarcaEmpresaUseCase;
import com.kairos.Kairos_backend.domain.model.Empresa;
import com.kairos.Kairos_backend.domain.model.Imagen;
import com.kairos.Kairos_backend.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;

/** Marca de la empresa: perfil y logo (SCRUM-105). */
@RestController
@RequestMapping("/api/empresas")
public class MarcaEmpresaController {

    private final MarcaEmpresaUseCase marca;

    public MarcaEmpresaController(MarcaEmpresaUseCase marca) {
        this.marca = marca;
    }

    /** La empresa del empleado: con esto el frontend muestra su nombre y su logo en la barra. */
    @GetMapping("/mi-empresa")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public EmpresaResponse miEmpresa(@AuthenticationPrincipal UsuarioAutenticado yo) {
        return conLogo(marca.miEmpresa(yo.idEmpresa()));
    }

    /** Solo ADMINISTRADOR: cambia nombre, correo y teléfono de su empresa (el NIT no cambia). */
    @PutMapping("/mi-empresa")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public EmpresaResponse actualizar(@Valid @RequestBody PerfilEmpresaRequest request,
                                      @AuthenticationPrincipal UsuarioAutenticado yo) {
        return conLogo(marca.actualizarPerfil(yo.idEmpresa(), request.nombre(), request.email(), request.telefono()));
    }

    /** Solo ADMINISTRADOR: sube o reemplaza el logo. Va como form-data en el campo "archivo". */
    @PostMapping(path = "/mi-empresa/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public EmpresaResponse subirLogo(@RequestParam("archivo") MultipartFile archivo,
                                     @AuthenticationPrincipal UsuarioAutenticado yo) {
        marca.subirLogo(yo.idEmpresa(), bytesDe(archivo));
        return conLogo(marca.miEmpresa(yo.idEmpresa()));
    }

    @DeleteMapping("/mi-empresa/logo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> quitarLogo(@AuthenticationPrincipal UsuarioAutenticado yo) {
        marca.quitarLogo(yo.idEmpresa());
        return ResponseEntity.noContent().build();
    }

    /**
     * Público: el logo de una empresa activa. La URL no cambia al cambiar el logo, así que el
     * navegador pregunta cada vez con la "huella" (ETag) y, si no cambió, recibe 304 sin descargarlo.
     */
    @GetMapping("/{id}/logo")
    public ResponseEntity<byte[]> logo(@PathVariable Long id, WebRequest peticion) {
        Imagen logo = marca.logo(id);
        String huella = "\"" + DigestUtils.md5DigestAsHex(logo.contenido()) + "\"";
        if (peticion.checkNotModified(huella)) {
            return null;   // Spring responde 304 Not Modified
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(logo.tipo().getTipoMime()))
                .cacheControl(CacheControl.noCache().cachePublic())
                .eTag(huella)
                .body(logo.contenido());
    }

    private EmpresaResponse conLogo(Empresa empresa) {
        return EmpresaResponse.desde(empresa, marca.tieneLogo(empresa.getId()));
    }

    private static byte[] bytesDe(MultipartFile archivo) {
        try {
            return archivo.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el archivo subido", e);
        }
    }

    public record PerfilEmpresaRequest(
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 150) String nombre,
            @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no es válido") @Size(max = 100) String email,
            @Size(max = 20) String telefono) {
    }
}
