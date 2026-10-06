package com.julianagudelo.crediya.exception;

/** Ya existe un cliente o empleado con ese documento (no se permiten duplicados). */
public class DocumentoDuplicadoException extends CrediYaException {

    /**
     * @param tipo      "cliente" o "empleado"
     * @param documento documento repetido
     */
    public DocumentoDuplicadoException(String tipo, String documento) {
        super("Ya existe un " + tipo + " con el documento " + documento + ".");
    }
}
