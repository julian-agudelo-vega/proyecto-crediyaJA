package com.julianagudelo.crediya.model;

/**
 * Estados posibles de un préstamo. "Vencido" NO es un estado: se determina con
 * {@link Prestamo#estaVencido(java.time.LocalDate)} a partir de las fechas de las cuotas.
 */
public enum EstadoPrestamo {
    PENDIENTE,
    PAGADO
}
