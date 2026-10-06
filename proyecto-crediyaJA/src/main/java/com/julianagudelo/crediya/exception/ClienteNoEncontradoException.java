package com.julianagudelo.crediya.exception;

/** No existe un cliente con el id indicado. */
public class ClienteNoEncontradoException extends CrediYaException {

    public ClienteNoEncontradoException(int id) {
        super("No existe un cliente con id " + id + ".");
    }
}
