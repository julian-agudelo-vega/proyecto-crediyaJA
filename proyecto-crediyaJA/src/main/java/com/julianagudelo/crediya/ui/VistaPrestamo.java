package com.julianagudelo.crediya.ui;

import com.julianagudelo.crediya.model.Pago;
import com.julianagudelo.crediya.model.Prestamo;
import com.julianagudelo.crediya.util.Formato;

import java.math.BigDecimal;

/** Convierte préstamos y pagos en texto para mostrarlos en consola. */
final class VistaPrestamo {

    private VistaPrestamo() {
    }

    /** Resumen detallado de un préstamo (nuevo o ya registrado). */
    static String resumen(Prestamo p) {
        StringBuilder sb = new StringBuilder();
        sb.append(p.getId() == 0 ? "PRÉSTAMO (aún no registrado)" : "PRÉSTAMO #" + p.getId()).append('\n');
        sb.append("  Cliente:             ").append(p.getCliente().getNombre())
                .append(" (documento ").append(p.getCliente().getDocumento()).append(")\n");
        sb.append("  Empleado:            ").append(p.getEmpleado().getNombre())
                .append(" (documento ").append(p.getEmpleado().getDocumento()).append(")\n");
        sb.append("  Fecha del préstamo:  ").append(p.getFechaPrestamo()).append('\n');
        sb.append("  Monto solicitado:    ").append(Formato.dinero(p.getMonto())).append('\n');
        sb.append("  Interés (").append(p.getInteres().toPlainString()).append("%):   ")
                .append(Formato.dinero(p.calcularInteres())).append('\n');
        sb.append("  Total a pagar:       ").append(Formato.dinero(p.calcularMontoTotal())).append('\n');
        sb.append("  Cuotas:              ").append(p.getCuotas()).append(" de ")
                .append(Formato.dinero(p.valorCuota()));
        BigDecimal ultima = p.valorCuota(p.getCuotas());
        if (ultima.compareTo(p.valorCuota()) != 0) {
            sb.append(" (la última cuota es de ").append(Formato.dinero(ultima)).append(" por ajuste de centavos)");
        }
        sb.append('\n');
        sb.append("  Cuotas pagadas:      ").append(p.cuotasPagadas())
                .append(" | Restantes: ").append(p.cuotasRestantes()).append('\n');
        sb.append("  Saldo pendiente:     ").append(Formato.dinero(p.saldoPendiente())).append('\n');
        if (p.numeroProximaCuota().isPresent()) {
            sb.append("  Próxima cuota:       #").append(p.numeroProximaCuota().get())
                    .append(" por ").append(Formato.dinero(p.valorProximaCuota()))
                    .append(", vence el ").append(p.proximaFechaVencimiento().get()).append('\n');
        }
        sb.append("  Estado:              ").append(p.getEstado());
        return sb.toString();
    }

    /** Una sola línea por préstamo, para listados. */
    static String linea(Prestamo p) {
        String proxima = p.proximaFechaVencimiento().map(f -> "próx. vencimiento " + f).orElse("sin cuotas pendientes");
        return "#" + p.getId()
                + " | Cliente: " + p.getCliente().getNombre()
                + " | Total: " + Formato.dinero(p.calcularMontoTotal())
                + " | Saldo: " + Formato.dinero(p.saldoPendiente())
                + " | Cuotas restantes: " + p.cuotasRestantes()
                + " | " + proxima
                + " | " + p.getEstado();
    }

    /** Una línea por pago, para el histórico. */
    static String linea(int numeroCuota, Pago pago) {
        return "Cuota " + numeroCuota
                + " | Pago #" + pago.getIdPago()
                + " | Comprobante: " + pago.getNumeroComprobante()
                + " | Fecha: " + pago.getFechaPago()
                + " | Monto: " + Formato.dinero(pago.getMontoPagado());
    }
}
