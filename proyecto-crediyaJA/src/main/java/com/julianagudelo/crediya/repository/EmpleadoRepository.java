package com.julianagudelo.crediya.repository;

import com.julianagudelo.crediya.model.Empleado;

import java.util.List;
import java.util.Optional;

/** Contrato de persistencia de empleados (implementaciones: archivo y MySQL). */
public interface EmpleadoRepository {

    /** Guarda un empleado nuevo y devuelve una copia con el id asignado. */
    Empleado guardar(Empleado empleado);

    Optional<Empleado> buscarPorId(int id);

    Optional<Empleado> buscarPorDocumento(String documento);

    List<Empleado> listarTodos();
}
