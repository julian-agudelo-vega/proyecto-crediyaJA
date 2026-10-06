package com.julianagudelo.crediya.ui;

import com.julianagudelo.crediya.exception.CrediYaException;
import com.julianagudelo.crediya.model.EstadoPrestamo;
import com.julianagudelo.crediya.model.Pago;
import com.julianagudelo.crediya.model.Prestamo;
import com.julianagudelo.crediya.service.PagoService;
import com.julianagudelo.crediya.service.PrestamoService;
import com.julianagudelo.crediya.util.Consola;
import com.julianagudelo.crediya.util.Formato;

import java.math.BigDecimal;
import java.util.List;

/** Menú de gestión de pagos: registrar un abono y ver el histórico. */
public class MenuPagos {

    private final PagoService pagoService;
    private final PrestamoService prestamoService;
    private final Consola consola;

    public MenuPagos(PagoService pagoService, PrestamoService prestamoService, Consola consola) {
        this.pagoService = pagoService;
        this.prestamoService = prestamoService;
        this.consola = consola;
    }

    public void mostrar() {
        int opcion;
        do {
            System.out.println("\n--- GESTIONAR PAGOS ---");
            System.out.println("1. Registrar pago de una cuota");
            System.out.println("2. Ver histórico de pagos de un préstamo");
            System.out.println("0. Volver");
            opcion = consola.leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> registrar();
                    case 2 -> historial();
                    case 0 -> { }
                    default -> System.out.println("Opción inválida.");
                }
            } catch (CrediYaException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    /** Flujo: buscar préstamo, mostrar cuota y saldo, pedir comprobante y monto, registrar. */
    private void registrar() {
        int id = consola.leerEntero("Id del préstamo: ");
        Prestamo prestamo = prestamoService.buscarPorId(id);
        System.out.println("\n" + VistaPrestamo.resumen(prestamo));

        if (prestamo.getEstado() == EstadoPrestamo.PAGADO) {
            System.out.println("\nEste préstamo ya está pagado por completo.");
            return;
        }

        System.out.println("\nValor de la cuota a pagar: " + Formato.dinero(prestamo.valorProximaCuota()));
        String comprobante = consola.leerTexto("Número de comprobante: ");
        BigDecimal monto = consola.leerMonto("Monto a pagar: ");

        Pago pago = pagoService.registrarPago(prestamo, comprobante, monto);
        System.out.println("\nPago #" + pago.getIdPago() + " registrado (fecha " + pago.getFechaPago() + ").");
        System.out.println("Saldo pendiente: " + Formato.dinero(prestamo.saldoPendiente()));
        System.out.println("Cuotas restantes: " + prestamo.cuotasRestantes());
        System.out.println("Estado del préstamo: " + prestamo.getEstado());
    }

    private void historial() {
        int id = consola.leerEntero("Id del préstamo: ");
        List<Pago> pagos = pagoService.historial(id);
        if (pagos.isEmpty()) {
            System.out.println("Este préstamo todavía no tiene pagos.");
            return;
        }
        for (int i = 0; i < pagos.size(); i++) {
            System.out.println(VistaPrestamo.linea(i + 1, pagos.get(i)));
        }
    }
}
