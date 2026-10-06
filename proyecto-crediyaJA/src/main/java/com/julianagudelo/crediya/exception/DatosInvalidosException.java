package com.julianagudelo.crediya.exception;

/** Un dato de texto, fecha u objeto obligatorio es nulo, vacío o tiene un formato inválido. */
public class DatosInvalidosException extends CrediYaException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
