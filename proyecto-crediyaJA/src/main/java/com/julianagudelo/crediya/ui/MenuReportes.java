package com.julianagudelo.crediya.ui;

import com.julianagudelo.crediya.exception.CrediYaException;
import com.julianagudelo.crediya.model.Cliente;
import com.julianagudelo.crediya.model.Prestamo;
import com.julianagudelo.crediya.service.ReporteService;
import com.julianagudelo.crediya.util.Consola;

import java.time.LocalDate;
import java.util.List;

/** Menú de reportes: préstamos activos, préstamos vencidos y clientes morosos. */
public class MenuReportes {

    private final ReporteService servicio;
    private final Consola consola;

    public MenuReportes(ReporteService servicio, Consola consola) {
        this.servicio = servicio;
        this.consola = consola;
    }

    public void mostrar() {
        int opcion;
        do {
            System.out.println("\n--- REPORTES ---");
            System.out.println("1. Préstamos activos");
            System.out.println("2. Préstamos vencidos");
            System.out.println("3. Clientes morosos");
            System.out.println("0. Volver");
            opcion = consola.leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> prestamosActivos();
                    case 2 -> prestamosVencidos();
                    case 3 -> clientesMorosos();
                    case 0 -> { }
                    default -> System.out.println("Opción inválida.");
                }
            } catch (CrediYaException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private void prestamosActivos() {
        List<Prestamo> activos = servicio.prestamosActivos();
        System.out.println("\nPréstamos activos (con cuotas pendientes): " + activos.size());
        activos.forEach(p -> System.out.println(VistaPrestamo.linea(p)));
    }

    private void prestamosVencidos() {
        List<Prestamo> vencidos = servicio.prestamosVencidos(LocalDate.now());
        System.out.println("\nPréstamos vencidos (al " + LocalDate.now() + "): " + vencidos.size());
        vencidos.forEach(p -> System.out.println(VistaPrestamo.linea(p)));
    }

    private void clientesMorosos() {
        List<Cliente> morosos = servicio.clientesMorosos(LocalDate.now());
        System.out.println("\nClientes morosos (con al menos un préstamo vencido): " + morosos.size());
        morosos.forEach(c -> System.out.println(c.mostrarInformacion()));
    }
}
