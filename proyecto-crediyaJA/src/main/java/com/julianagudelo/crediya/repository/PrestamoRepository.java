package com.julianagudelo.crediya.repository;

import com.julianagudelo.crediya.model.Prestamo;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia de préstamos (implementaciones: archivo y MySQL).
 * Los préstamos que devuelve ya vienen con su cliente, su empleado y sus pagos cargados.
 */
public interface PrestamoRepository {

    /** Guarda un préstamo nuevo y devuelve una copia con el id asignado. */
    Prestamo guardar(Prestamo prestamo);

    /** Actualiza el estado guardado del préstamo (PENDIENTE o PAGADO) con el que tiene en memoria. */
    void actualizarEstado(Prestamo prestamo);

    Optional<Prestamo> buscarPorId(int id);

    List<Prestamo> listarTodos();

    List<Prestamo> listarPorCliente(int clienteId);
}
