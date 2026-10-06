package com.julianagudelo.crediya.repository.jdbc;

import com.julianagudelo.crediya.exception.PersistenciaException;
import com.julianagudelo.crediya.exception.PrestamoNoEncontradoException;
import com.julianagudelo.crediya.model.Prestamo;
import com.julianagudelo.crediya.repository.ArmadorPrestamo;
import com.julianagudelo.crediya.repository.PrestamoRepository;
import com.julianagudelo.crediya.util.ConexionBD;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Guarda y consulta préstamos en la tabla prestamos de MySQL. */
public class PrestamoJdbcRepository implements PrestamoRepository {

    private static final String SELECT =
            "SELECT id, cliente_id, empleado_id, monto, interes, cuotas, fecha_inicio FROM prestamos";

    /** Fila tal como está en la tabla, antes de armar el Prestamo completo. */
    private record FilaPrestamo(int id, int clienteId, int empleadoId, BigDecimal monto, BigDecimal interes,
                                int cuotas, LocalDate fechaInicio) {
    }

    private final ConexionBD conexion;
    private final ArmadorPrestamo armador;

    public PrestamoJdbcRepository(ConexionBD conexion, ArmadorPrestamo armador) {
        this.conexion = conexion;
        this.armador = armador;
    }

    @Override
    public Prestamo guardar(Prestamo prestamo) {
        String sql = "INSERT INTO prestamos (cliente_id, empleado_id, monto, interes, cuotas, fecha_inicio, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, prestamo.getCliente().getId());
            ps.setInt(2, prestamo.getEmpleado().getId());
            ps.setBigDecimal(3, prestamo.getMonto());
            ps.setBigDecimal(4, prestamo.getInteres());
            ps.setInt(5, prestamo.getCuotas());
            ps.setDate(6, Date.valueOf(prestamo.getFechaPrestamo()));
            ps.setString(7, prestamo.getEstado().name());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    return new Prestamo(claves.getInt(1), prestamo.getCliente(), prestamo.getEmpleado(),
                            prestamo.getMonto(), prestamo.getInteres(), prestamo.getCuotas(),
                            prestamo.getFechaPrestamo());
                }
            }
            throw new PersistenciaException("MySQL no devolvió el id generado para el préstamo.");
        } catch (SQLException e) {
            throw new PersistenciaException("Error de base de datos al guardar el préstamo: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizarEstado(Prestamo prestamo) {
        String sql = "UPDATE prestamos SET estado = ? WHERE id = ?";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, prestamo.getEstado().name());
            ps.setInt(2, prestamo.getId());
            if (ps.executeUpdate() == 0) {
                throw new PrestamoNoEncontradoException(prestamo.getId());
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error de base de datos al actualizar el préstamo: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Prestamo> buscarPorId(int id) {
        return consultar(SELECT + " WHERE id = ?", id).stream().findFirst().map(this::armar);
    }

    @Override
    public List<Prestamo> listarTodos() {
        return consultar(SELECT + " ORDER BY id").stream().map(this::armar).toList();
    }

    @Override
    public List<Prestamo> listarPorCliente(int clienteId) {
        return consultar(SELECT + " WHERE cliente_id = ? ORDER BY id", clienteId).stream()
                .map(this::armar).toList();
    }

    /**
     * Lee las filas y cierra la conexión ANTES de armar los préstamos, porque armar un
     * préstamo hace más consultas (cliente, empleado, pagos).
     */
    private List<FilaPrestamo> consultar(String sql, int... parametros) {
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setInt(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<FilaPrestamo> filas = new ArrayList<>();
                while (rs.next()) {
                    filas.add(new FilaPrestamo(rs.getInt("id"), rs.getInt("cliente_id"), rs.getInt("empleado_id"),
                            rs.getBigDecimal("monto"), rs.getBigDecimal("interes"), rs.getInt("cuotas"),
                            rs.getDate("fecha_inicio").toLocalDate()));
                }
                return filas;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error de base de datos al consultar préstamos: " + e.getMessage(), e);
        }
    }

    private Prestamo armar(FilaPrestamo f) {
        return armador.armar(f.id(), f.clienteId(), f.empleadoId(), f.monto(), f.interes(), f.cuotas(),
                f.fechaInicio());
    }
}
