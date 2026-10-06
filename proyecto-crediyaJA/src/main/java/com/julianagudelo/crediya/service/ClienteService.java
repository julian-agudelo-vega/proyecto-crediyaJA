package com.julianagudelo.crediya.service;

import com.julianagudelo.crediya.exception.ClienteNoEncontradoException;
import com.julianagudelo.crediya.exception.DocumentoDuplicadoException;
import com.julianagudelo.crediya.model.Cliente;
import com.julianagudelo.crediya.repository.ClienteRepository;

import java.util.List;

/** Operaciones de negocio sobre clientes. */
public class ClienteService {

    private final ClienteRepository repositorio;

    public ClienteService(ClienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Registra un cliente nuevo. Valida los datos (al construir el Cliente) y rechaza
     * documentos repetidos.
     */
    public Cliente registrar(String nombre, String documento, String correo, String telefono) {
        Cliente nuevo = new Cliente(0, nombre, documento, correo, telefono);
        if (repositorio.buscarPorDocumento(nuevo.getDocumento()).isPresent()) {
            throw new DocumentoDuplicadoException("cliente", nuevo.getDocumento());
        }
        return repositorio.guardar(nuevo);
    }

    public List<Cliente> listar() {
        return repositorio.listarTodos();
    }

    public Cliente buscarPorId(int id) {
        return repositorio.buscarPorId(id).orElseThrow(() -> new ClienteNoEncontradoException(id));
    }
}
