package com.julianagudelo.crediya.service;

import com.julianagudelo.crediya.exception.ClienteNoEncontradoException;
import com.julianagudelo.crediya.exception.EmpleadoNoEncontradoException;
import com.julianagudelo.crediya.exception.PrestamoNoEncontradoException;
import com.julianagudelo.crediya.model.Cliente;
import com.julianagudelo.crediya.model.Empleado;
import com.julianagudelo.crediya.model.Prestamo;
import com.julianagudelo.crediya.repository.ClienteRepository;
import com.julianagudelo.crediya.repository.EmpleadoRepository;
import com.julianagudelo.crediya.repository.PrestamoRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Operaciones de negocio sobre préstamos. */
public class PrestamoService {

    private final PrestamoRepository prestamos;
    private final ClienteRepository clientes;
    private final EmpleadoRepository empleados;

    public PrestamoService(PrestamoRepository prestamos, ClienteRepository clientes, EmpleadoRepository empleados) {
        this.prestamos = prestamos;
        this.clientes = clientes;
        this.empleados = empleados;
    }

    /**
     * Arma un préstamo nuevo SIN guardarlo, para poder mostrar el resumen (total y cuota)
     * antes de registrarlo. Aplica el interés fijo del 10% y toma la fecha de hoy como fecha
     * del préstamo. Valida cliente, empleado, monto y cuotas (3, 6 o 12).
     */
    public Prestamo preparar(int clienteId, int empleadoId, BigDecimal monto, int cuotas) {
        Cliente cliente = clientes.buscarPorId(clienteId)
                .orElseThrow(() -> new ClienteNoEncontradoException(clienteId));
        Empleado empleado = empleados.buscarPorId(empleadoId)
                .orElseThrow(() -> new EmpleadoNoEncontradoException(empleadoId));
        return new Prestamo(0, cliente, empleado, monto, Prestamo.INTERES_PORCENTAJE, cuotas, LocalDate.now());
    }

    /** Guarda un préstamo preparado y devuelve la versión con su id. */
    public Prestamo registrar(Prestamo prestamo) {
        return prestamos.guardar(prestamo);
    }

    public Prestamo buscarPorId(int id) {
        return prestamos.buscarPorId(id).orElseThrow(() -> new PrestamoNoEncontradoException(id));
    }

    public List<Prestamo> listarTodos() {
        return prestamos.listarTodos();
    }

    /** Préstamos de un cliente. Falla si el cliente no existe. */
    public List<Prestamo> listarPorCliente(int clienteId) {
        clientes.buscarPorId(clienteId).orElseThrow(() -> new ClienteNoEncontradoException(clienteId));
        return prestamos.listarPorCliente(clienteId);
    }
}
