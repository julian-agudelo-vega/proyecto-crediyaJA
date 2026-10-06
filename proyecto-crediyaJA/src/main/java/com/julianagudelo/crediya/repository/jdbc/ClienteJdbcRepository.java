package com.julianagudelo.crediya.repository.jdbc;

import com.julianagudelo.crediya.exception.PersistenciaException;
import com.julianagudelo.crediya.model.Cliente;
import com.julianagudelo.crediya.repository.ClienteRepository;
import com.julianagudelo.crediya.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Guarda y consulta clientes en la tabla clientes de MySQL. */
public class ClienteJdbcRepository implements ClienteRepository {

    private static final String SELECT = "SELECT id, nombre, documento, correo, telefono FROM clientes";

    private final ConexionBD conexion;

    public ClienteJdbcRepository(ConexionBD conexion) {
        this.conexion = conexion;
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        String sql = "INSERT INTO clientes (nombre, documento, correo, telefono) VALUES (?, ?, ?, ?)";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getDocumento());
            ps.setString(3, cliente.getCorreo());
            ps.setString(4, cliente.getTelefono());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    return new Cliente(claves.getInt(1), cliente.getNombre(), cliente.getDocumento(),
                            cliente.getCorreo(), cliente.getTelefono());
                }
            }
            throw new PersistenciaException("MySQL no devolvió el id generado para el cliente.");
        } catch (SQLException e) {
            throw new PersistenciaException("Error de base de datos al guardar el cliente: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(int id) {
        List<Cliente> resultado = consultar(SELECT + " WHERE id = ?", String.valueOf(id));
        return resultado.stream().findFirst();
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(String documento) {
        List<Cliente> resultado = consultar(SELECT + " WHERE documento = ?", documento);
        return resultado.stream().findFirst();
    }

    @Override
    public List<Cliente> listarTodos() {
        return consultar(SELECT + " ORDER BY id");
    }

    private List<Cliente> consultar(String sql, String... parametros) {
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setString(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Cliente> clientes = new ArrayList<>();
                while (rs.next()) {
                    clientes.add(new Cliente(rs.getInt("id"), rs.getString("nombre"), rs.getString("documento"),
                            rs.getString("correo"), rs.getString("telefono")));
                }
                return clientes;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error de base de datos al consultar clientes: " + e.getMessage(), e);
        }
    }
}
