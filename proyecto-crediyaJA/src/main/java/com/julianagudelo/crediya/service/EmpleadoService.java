package com.julianagudelo.crediya.service;

import com.julianagudelo.crediya.exception.DocumentoDuplicadoException;
import com.julianagudelo.crediya.exception.EmpleadoNoEncontradoException;
import com.julianagudelo.crediya.model.Empleado;
import com.julianagudelo.crediya.repository.EmpleadoRepository;

import java.math.BigDecimal;
import java.util.List;

/** Operaciones de negocio sobre empleados. */
public class EmpleadoService {

    private final EmpleadoRepository repositorio;

    public EmpleadoService(EmpleadoRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Registra un empleado nuevo. Valida los datos (al construir el Empleado) y rechaza
     * documentos repetidos.
     */
    public Empleado registrar(String nombre, String documento, String rol, String correo, BigDecimal salario) {
        Empleado nuevo = new Empleado(0, nombre, documento, rol, correo, salario);
        if (repositorio.buscarPorDocumento(nuevo.getDocumento()).isPresent()) {
            throw new DocumentoDuplicadoException("empleado", nuevo.getDocumento());
        }
        return repositorio.guardar(nuevo);
    }

    public List<Empleado> listar() {
        return repositorio.listarTodos();
    }

    public Empleado buscarPorId(int id) {
        return repositorio.buscarPorId(id).orElseThrow(() -> new EmpleadoNoEncontradoException(id));
    }
}
