package com.julianagudelo.crediya.ui;

import com.julianagudelo.crediya.exception.CrediYaException;
import com.julianagudelo.crediya.model.Cliente;
import com.julianagudelo.crediya.model.Prestamo;
import com.julianagudelo.crediya.service.ClienteService;
import com.julianagudelo.crediya.service.PrestamoService;
import com.julianagudelo.crediya.util.Consola;

import java.util.List;

/** Menú de gestión de clientes: registrar, listar y consultar sus préstamos. */
public class MenuClientes {

    private final ClienteService clienteService;
    private final PrestamoService prestamoService;
    private final Consola consola;

    public MenuClientes(ClienteService clienteService, PrestamoService prestamoService, Consola consola) {
        this.clienteService = clienteService;
        this.prestamoService = prestamoService;
        this.consola = consola;
    }

    public void mostrar() {
        int opcion;
        do {
            System.out.println("\n--- GESTIONAR CLIENTES ---");
            System.out.println("1. Registrar cliente");
            System.out.println("2. Listar clientes");
            System.out.println("3. Consultar préstamos de un cliente");
            System.out.println("0. Volver");
            opcion = consola.leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> registrar();
                    case 2 -> listar();
                    case 3 -> consultarPrestamos();
                    case 0 -> { }
                    default -> System.out.println("Opción inválida.");
                }
            } catch (CrediYaException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private void registrar() {
        String nombre = consola.leerTexto("Nombre: ");
        String documento = consola.leerTexto("Documento: ");
        String correo = consola.leerTexto("Correo: ");
        String telefono = consola.leerTexto("Teléfono: ");
        Cliente cliente = clienteService.registrar(nombre, documento, correo, telefono);
        System.out.println("Cliente registrado: " + cliente.mostrarInformacion());
    }

    private void listar() {
        List<Cliente> clientes = clienteService.listar();
        if (clientes.isEmpty()) {
            System.out.println("No hay clientes registrados.");
            return;
        }
        clientes.forEach(c -> System.out.println(c.mostrarInformacion()));
    }

    private void consultarPrestamos() {
        int id = consola.leerEntero("Id del cliente: ");
        Cliente cliente = clienteService.buscarPorId(id);
        System.out.println(cliente.mostrarInformacion());
        List<Prestamo> prestamos = prestamoService.listarPorCliente(id);
        if (prestamos.isEmpty()) {
            System.out.println("Este cliente no tiene préstamos.");
            return;
        }
        prestamos.forEach(p -> System.out.println("\n" + VistaPrestamo.resumen(p)));
    }
}
