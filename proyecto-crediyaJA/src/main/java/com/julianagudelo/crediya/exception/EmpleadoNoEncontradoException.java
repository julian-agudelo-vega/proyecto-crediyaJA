package com.julianagudelo.crediya.exception;

/** No existe un empleado con el id indicado. */
public class EmpleadoNoEncontradoException extends CrediYaException {

    public EmpleadoNoEncontradoException(int id) {
        super("No existe un empleado con id " + id + ".");
    }
}
