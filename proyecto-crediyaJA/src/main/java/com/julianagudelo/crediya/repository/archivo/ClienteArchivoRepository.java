package com.julianagudelo.crediya.repository.archivo;

import com.julianagudelo.crediya.model.Cliente;
import com.julianagudelo.crediya.repository.ClienteRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Guarda los clientes en clientes.txt.
 * Formato de cada línea:  id;nombre;documento;correo;telefono
 */
public class ClienteArchivoRepository implements ClienteRepository {

    private static final int CAMPOS = 5;

    private final ArchivoTexto archivo;

    public ClienteArchivoRepository(Path directorio) {
        this.archivo = new ArchivoTexto(directorio, "clientes.txt");
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        Cliente guardado = new Cliente(archivo.siguienteId(), cliente.getNombre(), cliente.getDocumento(),
                cliente.getCorreo(), cliente.getTelefono());
        archivo.agregarLinea(String.join(ArchivoTexto.SEPARADOR,
                String.valueOf(guardado.getId()), guardado.getNombre(), guardado.getDocumento(),
                guardado.getCorreo(), guardado.getTelefono()));
        return guardado;
    }

    @Override
    public Optional<Cliente> buscarPorId(int id) {
        return listarTodos().stream().filter(c -> c.getId() == id).findFirst();
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(String documento) {
        return listarTodos().stream().filter(c -> c.getDocumento().equals(documento)).findFirst();
    }

    @Override
    public List<Cliente> listarTodos() {
        List<Cliente> clientes = new ArrayList<>();
        for (String linea : archivo.leerLineas()) {
            String[] c = archivo.dividir(linea, CAMPOS);
            clientes.add(new Cliente(archivo.entero(c[0], linea), c[1], c[2], c[3], c[4]));
        }
        return clientes;
    }
}
