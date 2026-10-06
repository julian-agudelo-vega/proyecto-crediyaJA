package com.julianagudelo.crediya.exception;

/** El pago no cumple las reglas: monto distinto a la cuota exacta, préstamo ya pagado, etc. */
public class PagoInvalidoException extends CrediYaException {

    public PagoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
