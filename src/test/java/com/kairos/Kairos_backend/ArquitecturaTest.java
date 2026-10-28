package com.kairos.Kairos_backend;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Vigila las reglas de la arquitectura hexagonal (y la "D" de SOLID: las dependencias van hacia adentro).
 * Lee los "import" de cada archivo: si alguien rompe una regla, esta prueba falla y dice dónde.
 * No usa librerías extra.
 */
class ArquitecturaTest {

    private static final Path BASE = Path.of("src/main/java/com/kairos/Kairos_backend");
    private static final String PAQUETE = "com.kairos.Kairos_backend.";

    @Test
    void elDominioEsJavaPuro() {
        assertSinImports("domain",
                "org.springframework", "jakarta.", "org.hibernate",
                PAQUETE + "application", PAQUETE + "infrastructure");
    }

    @Test
    void laAplicacionNoConoceLaInfraestructura() {
        assertSinImports("application",
                PAQUETE + "infrastructure", "jakarta.persistence", "jakarta.servlet", "org.springframework.web");
    }

    @Test
    void losControllersSoloUsanLosPuertosDeEntrada() {
        assertSinImports("infrastructure/adapter/in",
                PAQUETE + "application.usecase", PAQUETE + "infrastructure.adapter.out");
    }

    @Test
    void laPersistenciaNoConoceLaWeb() {
        assertSinImports("infrastructure/adapter/out", PAQUETE + "infrastructure.adapter.in");
    }

    private static void assertSinImports(String capa, String... prohibidos) {
        List<String> violaciones = archivosJava(BASE.resolve(capa))
                .flatMap(archivo -> importsProhibidos(archivo, prohibidos))
                .toList();
        assertTrue(violaciones.isEmpty(), "La capa '" + capa + "' rompe la arquitectura:\n"
                + String.join("\n", violaciones));
    }

    private static Stream<Path> archivosJava(Path carpeta) {
        try (Stream<Path> archivos = Files.walk(carpeta)) {
            return archivos.filter(p -> p.toString().endsWith(".java")).toList().stream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Stream<String> importsProhibidos(Path archivo, String... prohibidos) {
        try {
            return Files.readAllLines(archivo).stream()
                    .map(String::trim)
                    .filter(linea -> linea.startsWith("import "))
                    .filter(linea -> Stream.of(prohibidos).anyMatch(p -> linea.substring(7).startsWith(p)))
                    .map(linea -> "  " + BASE.relativize(archivo) + " -> " + linea);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
