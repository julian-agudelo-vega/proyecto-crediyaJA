package com.julianagudelo.crediya.repository;

import com.julianagudelo.crediya.model.Cliente;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia de clientes. Tiene dos implementaciones (archivo y MySQL):
 * los servicios solo conocen esta interfaz, no saben dónde se guardan los datos.
 */
public interface ClienteRepository {

    /** Guarda un cliente nuevo y devuelve una copia con el id asignado. */
    Cliente guardar(Cliente cliente);

    Optional<Cliente> buscarPorId(int id);

    Optional<Cliente> buscarPorDocumento(String documento);

    List<Cliente> listarTodos();
}
