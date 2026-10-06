package com.julianagudelo.crediya.repository;

import com.julianagudelo.crediya.exception.ClienteNoEncontradoException;
import com.julianagudelo.crediya.exception.EmpleadoNoEncontradoException;
import com.julianagudelo.crediya.model.Cliente;
import com.julianagudelo.crediya.model.Empleado;
import com.julianagudelo.crediya.model.Pago;
import com.julianagudelo.crediya.model.Prestamo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Reconstruye un Prestamo completo a partir de los datos guardados: busca su cliente y su
 * empleado, crea el préstamo y vuelve a registrar sus pagos. Así la regla de "cómo se
 * carga un préstamo" está en un solo lugar y la comparten las dos persistencias
 * (archivo y MySQL).
 *
 * Al volver a agregar los pagos con {@link Prestamo#agregarPago(Pago)} se revalidan,
 * de modo que el estado del préstamo siempre sale coherente con sus pagos.
 */
public class ArmadorPrestamo {

    private final ClienteRepository clientes;
    private final EmpleadoRepository empleados;
    private final PagoRepository pagos;

    public ArmadorPrestamo(ClienteRepository clientes, EmpleadoRepository empleados, PagoRepository pagos) {
        this.clientes = clientes;
        this.empleados = empleados;
        this.pagos = pagos;
    }

    public Prestamo armar(int id, int clienteId, int empleadoId, BigDecimal monto, BigDecimal interes,
                          int cuotas, LocalDate fechaPrestamo) {
        Cliente cliente = clientes.buscarPorId(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoException(clienteId));
        Empleado empleado = empleados.buscarPorId(empleadoId)
                .orElseThrow(() -> new EmpleadoNoEncontradoException(empleadoId));

        Prestamo prestamo = new Prestamo(id, cliente, empleado, monto, interes, cuotas, fechaPrestamo);
        for (Pago pago : pagos.listarPorPrestamo(prestamo)) {
            prestamo.agregarPago(pago);
        }
        return prestamo;
    }
}
