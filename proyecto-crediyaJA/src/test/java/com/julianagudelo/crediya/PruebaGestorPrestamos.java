package com.julianagudelo.crediya;

import com.julianagudelo.crediya.model.Cliente;
import com.julianagudelo.crediya.model.Empleado;
import com.julianagudelo.crediya.model.Prestamo;
import com.julianagudelo.crediya.repository.ArmadorPrestamo;
import com.julianagudelo.crediya.repository.archivo.ClienteArchivoRepository;
import com.julianagudelo.crediya.repository.archivo.EmpleadoArchivoRepository;
import com.julianagudelo.crediya.repository.archivo.PagoArchivoRepository;
import com.julianagudelo.crediya.repository.archivo.PrestamoArchivoRepository;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/** Prueba aislada del gestor con datos temporales; no requiere MySQL ni modifica data/. */
public class PruebaGestorPrestamos {

    public static void main(String[] args) throws Exception {
        Path directorio = Files.createTempDirectory("crediya-gestor");
        ClienteArchivoRepository clientes = new ClienteArchivoRepository(directorio);
        EmpleadoArchivoRepository empleados = new EmpleadoArchivoRepository(directorio);
        PagoArchivoRepository pagos = new PagoArchivoRepository(directorio);
        PrestamoArchivoRepository prestamos = new PrestamoArchivoRepository(directorio,
                new ArmadorPrestamo(clientes, empleados, pagos));

        Cliente cliente = clientes.guardar(new Cliente(0, "Cliente de prueba", "100",
                "cliente@prueba.com", "3000000"));
        Empleado empleado = empleados.guardar(new Empleado(0, "Empleado de prueba", "200",
                "Asesor", "empleado@prueba.com", new BigDecimal("2500000")));

        Prestamo vencido = prestamos.guardar(new Prestamo(0, cliente, empleado, new BigDecimal("100000"),
                Prestamo.INTERES_PORCENTAJE, 3, LocalDate.of(2026, 1, 1)));
        Prestamo vigente = prestamos.guardar(new Prestamo(0, cliente, empleado, new BigDecimal("200000"),
                Prestamo.INTERES_PORCENTAJE, 3, LocalDate.of(2026, 2, 15)));

        GestorPrestamos gestor = new GestorPrestamos(prestamos);
        LocalDate referencia = LocalDate.of(2026, 3, 1);

        comprobar("carga ambos préstamos", gestor.cargarDatosPrestamos().size() == 2);
        comprobar("identifica ambos como activos", ids(gestor.listarPrestamosActivos())
                .equals(List.of(vencido.getId(), vigente.getId())));
        comprobar("identifica solo el préstamo vencido", ids(gestor.listarPrestamosVencidos(referencia))
                .equals(List.of(vencido.getId())));
        System.out.println("Prueba del gestor completada correctamente.");
    }

    private static List<Integer> ids(List<Prestamo> prestamos) {
        return prestamos.stream().map(Prestamo::getId).toList();
    }

    private static void comprobar(String descripcion, boolean condicion) {
        if (!condicion) {
            throw new AssertionError("FALLO: " + descripcion);
        }
        System.out.println("OK: " + descripcion);
    }
}
