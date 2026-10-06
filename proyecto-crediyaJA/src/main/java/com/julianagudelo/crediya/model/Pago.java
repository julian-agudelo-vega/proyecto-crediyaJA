package com.julianagudelo.crediya.model;

import com.julianagudelo.crediya.exception.DatosInvalidosException;
import com.julianagudelo.crediya.util.Validaciones;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Pago (abono) hecho a un préstamo. Solo representa datos: la regla de "el monto debe ser
 * exactamente el valor de la cuota" la valida {@link Prestamo#validarPago(Pago)}, porque
 * un Pago por sí solo no sabe cuánto vale una cuota.
 *
 * Es inmutable: no tiene setters.
 */
public class Pago {

    private final int idPago;
    private final String numeroComprobante;
    private final LocalDate fechaPago;
    private final BigDecimal montoPagado;
    private final Prestamo prestamo;

    /**
     * @param idPago Use 0 para un pago nuevo que todavía no se ha guardado.
     */
    public Pago(int idPago, String numeroComprobante, LocalDate fechaPago, BigDecimal montoPagado,
                Prestamo prestamo) {
        if (fechaPago == null) {
            throw new DatosInvalidosException("La fecha del pago es obligatoria.");
        }
        if (prestamo == null) {
            throw new DatosInvalidosException("El pago debe pertenecer a un préstamo.");
        }
        this.idPago = idPago;
        this.numeroComprobante = Validaciones.texto(numeroComprobante, "número de comprobante", 30);
        this.fechaPago = fechaPago;
        this.montoPagado = Validaciones.monto(montoPagado, "monto pagado", Validaciones.MAX_DECIMAL_12_2);
        this.prestamo = prestamo;
    }

    public int getIdPago() {
        return idPago;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public BigDecimal getMontoPagado() {
        return montoPagado;
    }

    public Prestamo getPrestamo() {
        return prestamo;
    }
}
