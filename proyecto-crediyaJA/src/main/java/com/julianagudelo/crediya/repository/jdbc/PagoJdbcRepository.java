package com.julianagudelo.crediya.repository.jdbc;

import com.julianagudelo.crediya.exception.PersistenciaException;
import com.julianagudelo.crediya.model.Pago;
import com.julianagudelo.crediya.model.Prestamo;
import com.julianagudelo.crediya.repository.PagoRepository;
import com.julianagudelo.crediya.util.ConexionBD;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Guarda y consulta pagos en la tabla pagos de MySQL. */
public class PagoJdbcRepository implements PagoRepository {

    private final ConexionBD conexion;

    public PagoJdbcRepository(ConexionBD conexion) {
        this.conexion = conexion;
    }

    @Override
    public Pago guardar(Pago pago) {
        String sql = "INSERT INTO pagos (prestamo_id, numero_comprobante, fecha_pago, monto) VALUES (?, ?, ?, ?)";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pago.getPrestamo().getId());
            ps.setString(2, pago.getNumeroComprobante());
            ps.setDate(3, Date.valueOf(pago.getFechaPago()));
            ps.setBigDecimal(4, pago.getMontoPagado());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    return new Pago(claves.getInt(1), pago.getNumeroComprobante(), pago.getFechaPago(),
                            pago.getMontoPagado(), pago.getPrestamo());
                }
            }
            throw new PersistenciaException("MySQL no devolvió el id generado para el pago.");
        } catch (SQLException e) {
            throw new PersistenciaException("Error de base de datos al guardar el pago: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Pago> listarPorPrestamo(Prestamo prestamo) {
        String sql = "SELECT id, numero_comprobante, fecha_pago, monto FROM pagos WHERE prestamo_id = ? ORDER BY id";
        try (Connection con = conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, prestamo.getId());
            try (ResultSet rs = ps.executeQuery()) {
                List<Pago> pagos = new ArrayList<>();
                while (rs.next()) {
                    pagos.add(new Pago(rs.getInt("id"), rs.getString("numero_comprobante"),
                            rs.getDate("fecha_pago").toLocalDate(), rs.getBigDecimal("monto"), prestamo));
                }
                return pagos;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error de base de datos al consultar pagos: " + e.getMessage(), e);
        }
    }
}
