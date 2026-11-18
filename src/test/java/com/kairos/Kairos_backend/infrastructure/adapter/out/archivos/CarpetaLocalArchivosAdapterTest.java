package com.kairos.Kairos_backend.infrastructure.adapter.out.archivos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** La carpeta de archivos con una carpeta temporal que JUnit borra al terminar. */
class CarpetaLocalArchivosAdapterTest {

    @TempDir
    Path carpeta;

    @Test
    void guardaLeeYBorra_creandoLasSubcarpetas() {
        var archivos = new CarpetaLocalArchivosAdapter(carpeta.resolve("fotos").toString());

        archivos.guardar("productos/1-a.png", new byte[]{1, 2, 3});

        assertTrue(Files.exists(carpeta.resolve("fotos/productos/1-a.png")));
        assertArrayEquals(new byte[]{1, 2, 3}, archivos.leer("productos/1-a.png").orElseThrow());

        archivos.guardar("productos/1-a.png", new byte[]{9});   // reemplazar
        assertArrayEquals(new byte[]{9}, archivos.leer("productos/1-a.png").orElseThrow());

        archivos.borrar("productos/1-a.png");
        assertTrue(archivos.leer("productos/1-a.png").isEmpty());
        assertDoesNotThrow(() -> archivos.borrar("productos/1-a.png"));   // ya no existe: no falla
    }

    @Test
    void ningunaRutaPuedeSalirseDeLaCarpeta() {
        var archivos = new CarpetaLocalArchivosAdapter(carpeta.resolve("fotos").toString());

        assertThrows(IllegalArgumentException.class, () -> archivos.guardar("../afuera.txt", new byte[]{1}));
        assertThrows(IllegalArgumentException.class, () -> archivos.leer("productos/../../afuera.txt"));
        assertThrows(IllegalArgumentException.class, () -> archivos.borrar(""));
        assertTrue(Files.notExists(carpeta.resolve("afuera.txt")));
    }

    @Test
    void siNoPuedeCrearLaCarpeta_noArranca() throws Exception {
        Path archivo = Files.writeString(carpeta.resolve("soy-un-archivo"), "x");
        assertThrows(IllegalStateException.class,
                () -> new CarpetaLocalArchivosAdapter(archivo.resolve("fotos").toString()));
    }
}
