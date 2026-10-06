package com.julianagudelo.crediya.ui;

import com.julianagudelo.crediya.exception.CrediYaException;
import com.julianagudelo.crediya.model.Empleado;
import com.julianagudelo.crediya.service.EmpleadoService;
import com.julianagudelo.crediya.util.Consola;

import java.math.BigDecimal;
import java.util.List;

/** Menú de gestión de empleados: registrar y consultar. */
public class MenuEmpleados {

    private final EmpleadoService servicio;
    private final Consola consola;

    public MenuEmpleados(EmpleadoService servicio, Consola consola) {
        this.servicio = servicio;
        this.consola = consola;
    }

    public void mostrar() {
        int opcion;
        do {
            System.out.println("\n--- GESTIONAR EMPLEADOS ---");
            System.out.println("1. Registrar empleado");
            System.out.println("2. Listar empleados");
            System.out.println("3. Consultar empleado por id");
            System.out.println("0. Volver");
            opcion = consola.leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> registrar();
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

    private void registrar() {
        String nombre = consola.leerTexto("Nombre: ");
        String documento = consola.leerTexto("Documento: ");
        String rol = consola.leerTexto("Rol: ");
        String correo = consola.leerTexto("Correo: ");
        BigDecimal salario = consola.leerMonto("Salario: ");
        Empleado empleado = servicio.registrar(nombre, documento, rol, correo, salario);
        System.out.println("Empleado registrado: " + empleado.mostrarInformacion());
    }

    private void listar() {
        List<Empleado> empleados = servicio.listar();
        if (empleados.isEmpty()) {
            System.out.println("No hay empleados registrados.");
            return;
        }
        empleados.forEach(e -> System.out.println(e.mostrarInformacion()));
    }

    private void consultar() {
        int id = consola.leerEntero("Id del empleado: ");
        System.out.println(servicio.buscarPorId(id).mostrarInformacion());
    }
}
