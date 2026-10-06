package com.julianagudelo.crediya.exception;

/** El número de cuotas no es 3, 6 o 12. */
public class CuotasInvalidasException extends CrediYaException {

    public CuotasInvalidasException(String mensaje) {
        super(mensaje);
    }
}
