package com.julianagudelo.crediya.repository;

import com.julianagudelo.crediya.model.Pago;
import com.julianagudelo.crediya.model.Prestamo;

import java.util.List;

/** Contrato de persistencia de pagos (implementaciones: archivo y MySQL). */
public interface PagoRepository {

    /** Guarda un pago nuevo y devuelve una copia con el id asignado. */
    Pago guardar(Pago pago);

    /**
     * Devuelve los pagos guardados de un préstamo, en orden de registro.
     * Recibe el Prestamo (y no solo su id) porque cada Pago apunta a su Prestamo.
     * Esta consulta NO agrega los pagos al préstamo: eso lo hace quien arma el préstamo.
     */
    List<Pago> listarPorPrestamo(Prestamo prestamo);
}
