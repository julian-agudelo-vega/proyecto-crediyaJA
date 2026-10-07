package com.julianagudelo.crediya;

import com.julianagudelo.crediya.exception.PersistenciaException;
import com.julianagudelo.crediya.model.EstadoPrestamo;
import com.julianagudelo.crediya.model.Prestamo;
import com.julianagudelo.crediya.repository.ArmadorPrestamo;
import com.julianagudelo.crediya.repository.ClienteRepository;
import com.julianagudelo.crediya.repository.EmpleadoRepository;
import com.julianagudelo.crediya.repository.PagoRepository;
import com.julianagudelo.crediya.repository.PrestamoRepository;
import com.julianagudelo.crediya.repository.jdbc.ClienteJdbcRepository;
import com.julianagudelo.crediya.repository.jdbc.EmpleadoJdbcRepository;
import com.julianagudelo.crediya.repository.jdbc.PagoJdbcRepository;
import com.julianagudelo.crediya.repository.jdbc.PrestamoJdbcRepository;
import com.julianagudelo.crediya.util.ConexionBD;
import com.julianagudelo.crediya.util.Formato;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class GestorPrestamos {

    private final PrestamoRepository prestamos;

    public GestorPrestamos() {
        this.prestamos = null;
    }

    public GestorPrestamos(PrestamoRepository prestamos) {
        this.prestamos = prestamos;
    }

    public GestorPrestamos(ConexionBD conexion) {
        this(construirRepositorio(conexion));
    }

    public List<Prestamo> cargarDatosPrestamos() {
        if (prestamos == null) {
            return new ArrayList<>();
        }
        try {
            return prestamos.listarTodos();
        } catch (PersistenciaException e) {
            System.err.println("Error de persistencia al cargar préstamos: " + e.getMessage());
            return new ArrayList<>();
        } catch (RuntimeException e) {
            System.err.println("Error inesperado al cargar préstamos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Prestamo> listarPrestamosActivos() {
        return filtrarPrestamosActivos(cargarDatosPrestamos());
    }

    public List<Prestamo> listarPrestamosVencidos() {
        return filtrarPrestamosVencidos(cargarDatosPrestamos());
    }

    public List<Prestamo> listarPrestamosVencidos(LocalDate fechaReferencia) {
        return filtrarPrestamosVencidos(cargarDatosPrestamos(), fechaReferencia);
    }

    public List<Prestamo> cargarPrestamosDesdeBD(ConexionBD conexion) {
        try {
            return construirRepositorio(conexion).listarTodos();
        } catch (PersistenciaException e) {
            System.err.println("No se pudieron cargar los préstamos desde MySQL: " + e.getMessage());
            return new ArrayList<>();
        } catch (RuntimeException e) {
            System.err.println("Error al consultar los préstamos desde MySQL: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Prestamo> cargarPrestamosDesdeBaseDatos(ConexionBD conexion) {
        return cargarPrestamosDesdeBD(conexion);
    }

    public List<Prestamo> cargarDatosPrestamos(ConexionBD conexion) {
        return cargarPrestamosDesdeBD(conexion);
    }

    public List<Prestamo> filtrarPrestamosActivos(List<Prestamo> lista) {
        if (lista == null || lista.isEmpty()) {
            return List.of();
        }
        try {
            return lista.stream()
                    .filter(p -> p != null && p.getEstado() == EstadoPrestamo.PENDIENTE)
                    .toList();
        } catch (RuntimeException e) {
            System.err.println("Error al filtrar préstamos activos: " + e.getMessage());
            return List.of();
        }
    }

    public List<Prestamo> listarPrestamosActivos(List<Prestamo> lista) {
        return filtrarPrestamosActivos(lista);
    }

    public List<Prestamo> filtrarPrestamosVencidos(List<Prestamo> lista) {
        return filtrarPrestamosVencidos(lista, LocalDate.now());
    }

    public List<Prestamo> filtrarPrestamosVencidos(List<Prestamo> lista, LocalDate fechaReferencia) {
        if (lista == null || lista.isEmpty()) {
            return List.of();
        }
        LocalDate referencia = fechaReferencia == null ? LocalDate.now() : fechaReferencia;
        try {
            return lista.stream()
                    .filter(p -> p != null && p.estaVencido(referencia))
                    .toList();
        } catch (RuntimeException e) {
            System.err.println("Error al filtrar préstamos vencidos: " + e.getMessage());
            return List.of();
        }
    }

    public List<Prestamo> listarPrestamosVencidos(List<Prestamo> lista, LocalDate fechaReferencia) {
        return filtrarPrestamosVencidos(lista, fechaReferencia);
    }

    public List<Prestamo> listarPrestamosVencidos(List<Prestamo> lista) {
        return filtrarPrestamosVencidos(lista);
    }

    public void mostrarResumen(List<Prestamo> lista) {
        mostrarResumen(lista, LocalDate.now());
    }

    public void mostrarResumen(List<Prestamo> lista, LocalDate fechaReferencia) {
        if (lista == null) {
            System.out.println("No hay préstamos para resumir.");
            return;
        }
        try {
            List<Prestamo> activos = filtrarPrestamosActivos(lista);
            List<Prestamo> vencidos = filtrarPrestamosVencidos(lista, fechaReferencia);
            BigDecimal montoTotalAdeudado = lista.stream()
                    .filter(p -> p != null)
                    .map(Prestamo::saldoPendiente)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            System.out.println("Préstamos activos: " + activos.size());
            System.out.println("Préstamos vencidos: " + vencidos.size());
            System.out.println("Monto total adeudado: " + Formato.dinero(montoTotalAdeudado));
        } catch (RuntimeException e) {
            System.err.println("Error al generar el resumen de préstamos: " + e.getMessage());
        }
    }

    public void mostrarResumen() {
        if (prestamos == null) {
            System.out.println("No hay un repositorio de préstamos configurado.");
            return;
        }
        try {
            mostrarResumen(prestamos.listarTodos());
        } catch (PersistenciaException e) {
            System.err.println("No se pudo cargar la información de préstamos: " + e.getMessage());
        }
    }

    private static PrestamoRepository construirRepositorio(ConexionBD conexion) {
        if (conexion == null) {
            throw new IllegalArgumentException("La conexión a MySQL es obligatoria.");
        }

        ClienteRepository clientes = new ClienteJdbcRepository(conexion);
        EmpleadoRepository empleados = new EmpleadoJdbcRepository(conexion);
        PagoRepository pagos = new PagoJdbcRepository(conexion);
        return new PrestamoJdbcRepository(conexion, new ArmadorPrestamo(clientes, empleados, pagos));
    }
}
