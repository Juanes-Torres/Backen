package com.kairos.Kairos_backend.domain.model;

import com.kairos.Kairos_backend.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Reglas de la foto de un producto (SCRUM-103): formato real y tamaño máximo. */
class ImagenProductoTest {

    static final byte[] PNG = conCola(new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'});
    static final byte[] JPG = conCola(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0});
    static final byte[] WEBP = conCola("RIFF\0\0\0\0WEBPVP8 ".getBytes(StandardCharsets.ISO_8859_1));

    private static byte[] conCola(byte[] firma) {
        byte[] contenido = Arrays.copyOf(firma, firma.length + 20);
        Arrays.fill(contenido, firma.length, contenido.length, (byte) 7);
        return contenido;
    }

    @Test
    void reconoceElFormatoPorElContenido() {
        assertEquals(TipoImagen.PNG, Imagen.desde(PNG).tipo());
        assertEquals(TipoImagen.JPG, Imagen.desde(JPG).tipo());
        assertEquals(TipoImagen.WEBP, Imagen.desde(WEBP).tipo());
        assertEquals("image/webp", TipoImagen.WEBP.getTipoMime());
        assertEquals("jpg", TipoImagen.JPG.getExtension());
    }

    @Test
    void unArchivoQueNoEsFoto_noPasaAunqueSeLlamePng() {
        byte[] texto = "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8);
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class, () -> Imagen.desde(texto));
        assertEquals("La imagen debe ser JPG, PNG o WEBP", error.getMessage());
        // "RIFF" sin "WEBP" (por ejemplo un audio .wav) tampoco
        assertTrue(TipoImagen.detectar("RIFF\0\0\0\0WAVEfmt ".getBytes(StandardCharsets.ISO_8859_1)).isEmpty());
        assertTrue(TipoImagen.detectar(new byte[]{(byte) 0xFF}).isEmpty());
        assertTrue(TipoImagen.detectar(null).isEmpty());
    }

    @Test
    void vaciaOMasDe2MB_noPasa() {
        assertEquals("La imagen está vacía",
                assertThrows(ReglaNegocioException.class, () -> Imagen.desde(new byte[0])).getMessage());
        assertThrows(ReglaNegocioException.class, () -> Imagen.desde(null));

        byte[] grande = Arrays.copyOf(PNG, Imagen.TAMANO_MAXIMO_BYTES + 1);
        assertEquals("La imagen no puede pesar más de 2 MB",
                assertThrows(ReglaNegocioException.class, () -> Imagen.desde(grande)).getMessage());
        // Justo 2 MB sí pasa
        Imagen.desde(Arrays.copyOf(PNG, Imagen.TAMANO_MAXIMO_BYTES));
    }

    @Test
    void noSePuedeDecirQueEsPngLoQueEsJpg() {
        assertThrows(ReglaNegocioException.class, () -> new Imagen(JPG, TipoImagen.PNG));
        assertThrows(ReglaNegocioException.class, () -> new Imagen(JPG, null));
    }

    @Test
    void dosImagenesConLosMismosBytesSonIguales_yElTextoNoImprimeLosBytes() {
        Imagen una = Imagen.desde(PNG);
        Imagen otra = Imagen.desde(PNG.clone());
        assertEquals(una, otra);
        assertEquals(una.hashCode(), otra.hashCode());
        assertNotEquals(una, Imagen.desde(JPG));
        assertNotEquals(una, "otra cosa");
        assertEquals("Imagen[PNG, 28 bytes]", una.toString());
    }

    @Test
    void elProductoCambiaYQuitaSuFoto_devolviendoLaAnteriorParaBorrarla() {
        Producto producto = Producto.nuevo("iPhone 15", null, new BigDecimal("1"), null, 1L, 1L, null);
        assertFalse(producto.tieneImagen());

        assertNull(producto.cambiarImagen("productos/1-a.png"));
        assertEquals("productos/1-a.png", producto.cambiarImagen("productos/1-b.jpg"));
        assertEquals("productos/1-b.jpg", producto.getImagen());

        assertEquals("productos/1-b.jpg", producto.quitarImagen());
        assertFalse(producto.tieneImagen());
        assertNull(producto.quitarImagen());
        assertThrows(ReglaNegocioException.class, () -> producto.cambiarImagen(" "));
    }
}
