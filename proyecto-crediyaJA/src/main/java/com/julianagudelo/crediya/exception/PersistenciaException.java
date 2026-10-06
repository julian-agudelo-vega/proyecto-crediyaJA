package com.julianagudelo.crediya.exception;

/**
 * Error al leer o escribir datos: archivo de texto ilegible, formato de línea inválido,
 * fallo de conexión con MySQL o error en una consulta SQL.
 * Guarda la causa original (IOException, SQLException...) para poder depurar.
 */
public class PersistenciaException extends CrediYaException {

    public PersistenciaException(String mensaje) {
        super(mensaje);
    }

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
