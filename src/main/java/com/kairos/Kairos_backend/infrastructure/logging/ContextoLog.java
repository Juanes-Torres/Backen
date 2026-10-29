package com.kairos.Kairos_backend.infrastructure.logging;

/**
 * Nombres de los datos que acompañan cada línea de log (MDC de SLF4J).
 * Un solo lugar para no escribirlos distinto en cada clase.
 */
public final class ContextoLog {

    public static final String ID_PETICION = "idPeticion";
    public static final String ID_USUARIO = "idUsuario";
    public static final String ID_EMPRESA = "idEmpresa";

    private ContextoLog() {
    }
}
