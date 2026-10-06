package com.julianagudelo.crediya.repository.jdbc;

import com.julianagudelo.crediya.exception.PersistenciaException;
import com.julianagudelo.crediya.model.Empleado;
import com.julianagudelo.crediya.repository.EmpleadoRepository;
import com.julianagudelo.crediya.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Guarda y consulta empleados en la tabla empleados de MySQL. */
public class EmpleadoJdbcRepository implements EmpleadoRepository {

    private static final String SELECT = "SELECT id, nombre, documento, rol, correo, salario FROM empleados";

    private final ConexionBD conexion;

    public EmpleadoJdbcRepository(ConexionBD conexion) {
        this.conexion = conexion;
    }

    @Override
    public Empleado guardar(Empleado empleado) {
        String sql = "INSERT INTO empleados (nombre, documento, rol, correo, salario) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, empleado.getNombre());
            ps.setString(2, empleado.getDocumento());
            ps.setString(3, empleado.getRol());
            ps.setString(4, empleado.getCorreo());
            ps.setBigDecimal(5, empleado.getSalario());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    return new Empleado(claves.getInt(1), empleado.getNombre(), empleado.getDocumento(),
                            empleado.getRol(), empleado.getCorreo(), empleado.getSalario());
                }
            }
            throw new PersistenciaException("MySQL no devolvió el id generado para el empleado.");
        } catch (SQLException e) {
            throw new PersistenciaException("Error de base de datos al guardar el empleado: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Empleado> buscarPorId(int id) {
        List<Empleado> resultado = consultar(SELECT + " WHERE id = ?", String.valueOf(id));
        return resultado.stream().findFirst();
    }

    @Override
    public Optional<Empleado> buscarPorDocumento(String documento) {
        List<Empleado> resultado = consultar(SELECT + " WHERE documento = ?", documento);
        return resultado.stream().findFirst();
    }

    @Override
    public List<Empleado> listarTodos() {
        return consultar(SELECT + " ORDER BY id");
    }

    private List<Empleado> consultar(String sql, String... parametros) {
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setString(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Empleado> empleados = new ArrayList<>();
                while (rs.next()) {
                    empleados.add(new Empleado(rs.getInt("id"), rs.getString("nombre"), rs.getString("documento"),
                            rs.getString("rol"), rs.getString("correo"), rs.getBigDecimal("salario")));
                }
                return empleados;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error de base de datos al consultar empleados: " + e.getMessage(), e);
        }
    }
}
