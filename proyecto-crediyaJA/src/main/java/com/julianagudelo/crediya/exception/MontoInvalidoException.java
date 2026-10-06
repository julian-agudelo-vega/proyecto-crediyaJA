package com.julianagudelo.crediya.exception;

/** Un monto es nulo, no es positivo, tiene más de 2 decimales o supera el máximo permitido. */
public class MontoInvalidoException extends CrediYaException {

    public MontoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
