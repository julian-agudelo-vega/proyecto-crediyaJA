package com.julianagudelo.crediya.exception;

/**
 * Excepción base de CrediYa. Todas las excepciones propias del proyecto la extienden,
 * así la interfaz de consola puede capturar "cualquier error controlado de CrediYa"
 * sin tener que usar catch (Exception e).
 *
 * Es una excepción no verificada (RuntimeException): los errores de validación y de
 * persistencia se propagan hasta el menú, que es quien muestra el mensaje al usuario.
 */
public class CrediYaException extends RuntimeException {

    public CrediYaException(String mensaje) {
        super(mensaje);
    }

    public CrediYaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
