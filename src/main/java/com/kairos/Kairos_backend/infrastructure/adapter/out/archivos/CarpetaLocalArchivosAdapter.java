package com.kairos.Kairos_backend.infrastructure.adapter.out.archivos;

import com.kairos.Kairos_backend.application.port.out.AlmacenamientoArchivosPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * ADAPTADOR de AlmacenamientoArchivosPort que guarda los archivos en una carpeta del servidor.
 * La carpeta sale de ARCHIVOS_CARPETA (12 factores: III). En Docker es un volumen, para que las
 * fotos no se pierdan al recrear el contenedor.
 *
 * Nota: con varias copias del backend, todas deben ver la misma carpeta (o cambiar este adaptador
 * por uno de un servicio en la nube, como Amazon S3, sin tocar el caso de uso).
 */
@Component
public class CarpetaLocalArchivosAdapter implements AlmacenamientoArchivosPort {

    private static final Logger log = LoggerFactory.getLogger(CarpetaLocalArchivosAdapter.class);

    private final Path raiz;

    public CarpetaLocalArchivosAdapter(@Value("${kairos.archivos.carpeta}") String carpeta) {
        this.raiz = Path.of(carpeta).toAbsolutePath().normalize();
        try {
            Files.createDirectories(raiz);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear la carpeta de archivos " + raiz, e);
        }
        log.info("Archivos guardados en {}", raiz);
    }

    @Override
    public void guardar(String ruta, byte[] contenido) {
        Path destino = resolver(ruta);
        try {
            Files.createDirectories(destino.getParent());
            // Se escribe en un temporal y luego se mueve: nadie lee nunca un archivo a medio escribir
            Path temporal = Files.createTempFile(destino.getParent(), ".subiendo-", ".tmp");
            Files.write(temporal, contenido);
            Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el archivo " + ruta, e);
        }
    }

    @Override
    public Optional<byte[]> leer(String ruta) {
        try {
            return Optional.of(Files.readAllBytes(resolver(ruta)));
        } catch (NoSuchFileException e) {
            log.warn("El archivo {} no existe en {}", ruta, raiz);
            return Optional.empty();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el archivo " + ruta, e);
        }
    }

    @Override
    public void borrar(String ruta) {
        try {
            Files.deleteIfExists(resolver(ruta));
        } catch (IOException e) {
            // No es grave: queda un archivo de más, pero nada se rompe
            log.warn("No se pudo borrar el archivo {}: {}", ruta, e.getMessage());
        }
    }

    /** Una ruta como "../../etc/passwd" no puede salirse de la carpeta de archivos. */
    private Path resolver(String ruta) {
        Path destino = raiz.resolve(ruta).normalize();
        if (ruta.isBlank() || !destino.startsWith(raiz) || destino.equals(raiz)) {
            throw new IllegalArgumentException("Ruta de archivo no permitida: " + ruta);
        }
        return destino;
    }
}
