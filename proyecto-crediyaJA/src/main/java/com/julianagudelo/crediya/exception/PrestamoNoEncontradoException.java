package com.julianagudelo.crediya.exception;

/** No existe un préstamo con el id indicado. */
public class PrestamoNoEncontradoException extends CrediYaException {

    public PrestamoNoEncontradoException(int id) {
        super("No existe un préstamo con id " + id + ".");
    }
}
