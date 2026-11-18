package com.kairos.Kairos_backend.domain.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * Formatos de foto que acepta KAIRÓS. El formato se reconoce por los primeros bytes del archivo
 * ("firma"), no por su nombre: un archivo malicioso renombrado a .jpg no pasa.
 */
public enum TipoImagen {

    JPG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    WEBP("image/webp", "webp");

    private static final byte[] FIRMA_JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] FIRMA_PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final byte[] RIFF = {'R', 'I', 'F', 'F'};
    private static final byte[] WEBP_MARCA = {'W', 'E', 'B', 'P'};

    private final String tipoMime;
    private final String extension;

    TipoImagen(String tipoMime, String extension) {
        this.tipoMime = tipoMime;
        this.extension = extension;
    }

    /** Vacío si el contenido no es una foto JPG, PNG ni WEBP. */
    public static Optional<TipoImagen> detectar(byte[] contenido) {
        if (empiezaCon(contenido, 0, FIRMA_JPG)) {
            return Optional.of(JPG);
        }
        if (empiezaCon(contenido, 0, FIRMA_PNG)) {
            return Optional.of(PNG);
        }
        // WEBP: "RIFF" + 4 bytes de tamaño + "WEBP"
        if (empiezaCon(contenido, 0, RIFF) && empiezaCon(contenido, 8, WEBP_MARCA)) {
            return Optional.of(WEBP);
        }
        return Optional.empty();
    }

    private static boolean empiezaCon(byte[] contenido, int desde, byte[] firma) {
        return contenido != null && contenido.length >= desde + firma.length
                && Arrays.equals(contenido, desde, desde + firma.length, firma, 0, firma.length);
    }

    public String getTipoMime() { return tipoMime; }
    public String getExtension() { return extension; }
}
