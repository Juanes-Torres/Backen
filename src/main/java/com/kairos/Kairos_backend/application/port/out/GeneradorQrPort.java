package com.kairos.Kairos_backend.application.port.out;

/**
 * PUERTO DE SALIDA: convertir un texto (por ejemplo, un enlace) en una imagen de código QR.
 * El caso de uso no sabe qué servicio hay detrás (hoy la API goQR.me).
 */
public interface GeneradorQrPort {

    /**
     * Devuelve la imagen PNG del QR.
     * Lanza ServicioExternoNoDisponibleException si el servicio está caído o lento.
     */
    byte[] generarPng(String contenido);
}
