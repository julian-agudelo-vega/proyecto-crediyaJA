package com.julianagudelo.crediya.ui;

import com.julianagudelo.crediya.util.Consola;

/** Menú principal: reparte el control a los menús de cada módulo. */
public class MenuPrincipal {

    private final String descripcionPersistencia;
    private final Consola consola;
    private final MenuEmpleados menuEmpleados;
    private final MenuClientes menuClientes;
    private final MenuPrestamos menuPrestamos;
    private final MenuPagos menuPagos;
    private final MenuReportes menuReportes;

    public MenuPrincipal(String descripcionPersistencia, Consola consola, MenuEmpleados menuEmpleados,
                         MenuClientes menuClientes, MenuPrestamos menuPrestamos, MenuPagos menuPagos,
                         MenuReportes menuReportes) {
        this.descripcionPersistencia = descripcionPersistencia;
        this.consola = consola;
        this.menuEmpleados = menuEmpleados;
        this.menuClientes = menuClientes;
        this.menuPrestamos = menuPrestamos;
        this.menuPagos = menuPagos;
        this.menuReportes = menuReportes;
    }

    public void mostrar() {
        int opcion;
        do {
            System.out.println("\n=== CREDIYA S.A.S. ===");
            System.out.println("Persistencia: " + descripcionPersistencia);
            System.out.println("1. Gestionar empleados");
            System.out.println("2. Gestionar clientes");
            System.out.println("3. Gestionar préstamos");
            System.out.println("4. Gestionar pagos");
            System.out.println("5. Reportes");
            System.out.println("6. Salir");
            opcion = consola.leerEntero("Opción: ");
            switch (opcion) {
                case 1 -> menuEmpleados.mostrar();
                case 2 -> menuClientes.mostrar();
                case 3 -> menuPrestamos.mostrar();
                case 4 -> menuPagos.mostrar();
                case 5 -> menuReportes.mostrar();
                case 6 -> System.out.println("Hasta pronto.");
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 6);
    }
}
