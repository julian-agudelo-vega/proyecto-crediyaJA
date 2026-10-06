package com.julianagudelo.crediya.ui;

import com.julianagudelo.crediya.exception.CrediYaException;
import com.julianagudelo.crediya.model.Cliente;
import com.julianagudelo.crediya.model.Empleado;
import com.julianagudelo.crediya.model.Prestamo;
import com.julianagudelo.crediya.service.ClienteService;
import com.julianagudelo.crediya.service.EmpleadoService;
import com.julianagudelo.crediya.service.PrestamoService;
import com.julianagudelo.crediya.util.Consola;

import java.math.BigDecimal;
import java.util.List;

/** Menú de gestión de préstamos: crear, listar y consultar. */
public class MenuPrestamos {

    private final PrestamoService prestamoService;
    private final ClienteService clienteService;
    private final EmpleadoService empleadoService;
    private final Consola consola;

    public MenuPrestamos(PrestamoService prestamoService, ClienteService clienteService,
                         EmpleadoService empleadoService, Consola consola) {
        this.prestamoService = prestamoService;
        this.clienteService = clienteService;
        this.empleadoService = empleadoService;
        this.consola = consola;
    }

    public void mostrar() {
        int opcion;
        do {
            System.out.println("\n--- GESTIONAR PRÉSTAMOS ---");
            System.out.println("1. Crear préstamo");
            System.out.println("2. Listar préstamos");
            System.out.println("3. Consultar préstamo por id");
            System.out.println("0. Volver");
            opcion = consola.leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> crear();
                    case 2 -> listar();
                    case 3 -> consultar();
                    case 0 -> { }
                    default -> System.out.println("Opción inválida.");
                }
            } catch (CrediYaException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    /** Flujo: cliente, empleado, monto, cuotas, resumen y registro. */
    private void crear() {
        List<Cliente> clientes = clienteService.listar();
        if (clientes.isEmpty()) {
            System.out.println("Primero debe registrar al menos un cliente.");
            return;
        }
        List<Empleado> empleados = empleadoService.listar();
        if (empleados.isEmpty()) {
            System.out.println("Primero debe registrar al menos un empleado.");
            return;
        }

        System.out.println("\nClientes:");
        clientes.forEach(c -> System.out.println("  " + c.mostrarInformacion()));
        int clienteId = consola.leerEntero("Id del cliente: ");

        System.out.println("\nEmpleados:");
        empleados.forEach(e -> System.out.println("  " + e.mostrarInformacion()));
        int empleadoId = consola.leerEntero("Id del empleado: ");

        BigDecimal monto = consola.leerMonto("Monto del préstamo: ");
        int cuotas = consola.leerEntero("Número de cuotas (3, 6 o 12): ");

        Prestamo preparado = prestamoService.preparar(clienteId, empleadoId, monto, cuotas);
        System.out.println("\n" + VistaPrestamo.resumen(preparado));

        Prestamo registrado = prestamoService.registrar(preparado);
        System.out.println("\nPréstamo registrado con id " + registrado.getId() + ".");
    }

    private void listar() {
        List<Prestamo> prestamos = prestamoService.listarTodos();
        if (prestamos.isEmpty()) {
            System.out.println("No hay préstamos registrados.");
            return;
        }
        prestamos.forEach(p -> System.out.println(VistaPrestamo.linea(p)));
    }

    private void consultar() {
        int id = consola.leerEntero("Id del préstamo: ");
        System.out.println(VistaPrestamo.resumen(prestamoService.buscarPorId(id)));
    }
}
