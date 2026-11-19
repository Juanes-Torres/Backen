package com.kairos.Kairos_backend.infrastructure.adapter.in.web.catalogo;

import com.kairos.Kairos_backend.application.port.in.GestionarImagenProductoUseCase;
import com.kairos.Kairos_backend.domain.model.Imagen;
import com.kairos.Kairos_backend.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;

/** Foto de cada producto (SCRUM-103). */
@RestController
@RequestMapping("/api/productos/{id}/imagen")
public class ImagenProductoController {

    private final GestionarImagenProductoUseCase imagenes;

    public ImagenProductoController(GestionarImagenProductoUseCase imagenes) {
        this.imagenes = imagenes;
    }

    /** Público, como el catálogo: se puede poner directo en un <img src="...">. */
    @GetMapping
    public ResponseEntity<byte[]> ver(@PathVariable Long id) {
        Imagen imagen = imagenes.ver(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagen.tipo().getTipoMime()))
                // El navegador la guarda 1 hora; si la foto cambia, la URL del catálogo cambia (?v=...)
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                .body(imagen.contenido());
    }

    /** Solo ADMINISTRADOR: sube o reemplaza la foto. Va como form-data en el campo "archivo". */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ProductoResponse subir(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo,
                                  @AuthenticationPrincipal UsuarioAutenticado yo) {
        return ProductoResponse.desde(imagenes.subir(yo.idEmpresa(), id, bytesDe(archivo)));
    }

    /** Solo ADMINISTRADOR: usa la foto que ofrece el catálogo de códigos de barras (UPCitemdb). */
    @PostMapping("/codigo-barras")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ProductoResponse desdeCodigoBarras(@PathVariable Long id, @Valid @RequestBody CodigoBarrasRequest request,
                                              @AuthenticationPrincipal UsuarioAutenticado yo) {
        return ProductoResponse.desde(imagenes.usarImagenDelCodigoBarras(yo.idEmpresa(), id, request.codigoBarras()));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> quitar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado yo) {
        imagenes.quitar(yo.idEmpresa(), id);
        return ResponseEntity.noContent().build();
    }

    private static byte[] bytesDe(MultipartFile archivo) {
        try {
            return archivo.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el archivo subido", e);
        }
    }

    public record CodigoBarrasRequest(@NotBlank(message = "El código de barras es obligatorio") String codigoBarras) {
    }
}
