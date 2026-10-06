package com.julianagudelo.crediya.service;

import com.julianagudelo.crediya.exception.PrestamoNoEncontradoException;
import com.julianagudelo.crediya.model.EstadoPrestamo;
import com.julianagudelo.crediya.model.Pago;
import com.julianagudelo.crediya.model.Prestamo;
import com.julianagudelo.crediya.repository.PagoRepository;
import com.julianagudelo.crediya.repository.PrestamoRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Operaciones de negocio sobre pagos. */
public class PagoService {

    private final PrestamoRepository prestamos;
    private final PagoRepository pagos;

    public PagoService(PrestamoRepository prestamos, PagoRepository pagos) {
        this.prestamos = prestamos;
        this.pagos = pagos;
    }

    /**
     * Registra el pago de la próxima cuota de un préstamo. Orden de las operaciones:
     *  1. Se crea el pago con la fecha de hoy.
     *  2. Se VALIDA contra el préstamo (monto exacto de la cuota, préstamo no pagado).
     *     Si no es válido se lanza PagoInvalidoException y no se guarda nada.
     *  3. Se guarda el pago.
     *  4. Se agrega al préstamo en memoria (saldo, cuotas restantes y estado se actualizan).
     *  5. Si era la última cuota, se actualiza el estado PAGADO en la persistencia.
     *
     * @param prestamo préstamo ya cargado (se actualiza en memoria con el nuevo pago)
     * @return el pago guardado, con su id
     */
    public Pago registrarPago(Prestamo prestamo, String numeroComprobante, BigDecimal monto) {
        Pago nuevo = new Pago(0, numeroComprobante, LocalDate.now(), monto, prestamo);
        prestamo.validarPago(nuevo);
        Pago guardado = pagos.guardar(nuevo);
        prestamo.agregarPago(guardado);
        if (prestamo.getEstado() == EstadoPrestamo.PAGADO) {
            prestamos.actualizarEstado(prestamo);
        }
        return guardado;
    }

    /** Histórico de pagos de un préstamo, en orden de registro. */
    public List<Pago> historial(int prestamoId) {
        Prestamo prestamo = prestamos.buscarPorId(prestamoId)
                .orElseThrow(() -> new PrestamoNoEncontradoException(prestamoId));
        return prestamo.getPagos();
    }
}
