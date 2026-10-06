package com.julianagudelo.crediya.model;

import com.julianagudelo.crediya.exception.CuotasInvalidasException;
import com.julianagudelo.crediya.exception.DatosInvalidosException;
import com.julianagudelo.crediya.exception.PagoInvalidoException;
import com.julianagudelo.crediya.util.Formato;
import com.julianagudelo.crediya.util.Validaciones;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Préstamo otorgado por un empleado a un cliente.
 *
 * Reglas del dominio que viven aquí (porque salen solo de los datos del propio préstamo):
 *  - interés, monto total y valor de las cuotas,
 *  - saldo pendiente, cuotas pagadas y restantes,
 *  - fechas de vencimiento y verificación de vencimiento,
 *  - validación y registro de pagos en memoria.
 *
 * Los valores calculados (total, cuota, saldo, estado...) NO se guardan como atributos:
 * se calculan a partir de monto, interés, cuotas y la lista de pagos, para que nunca
 * queden desincronizados.
 */
public class Prestamo {

    /** Interés fijo del 10% sobre el monto (se guarda como porcentaje: 10.00). */
    public static final BigDecimal INTERES_PORCENTAJE = new BigDecimal("10.00");

    /** Únicas cantidades de cuotas permitidas. */
    public static final List<Integer> CUOTAS_PERMITIDAS = List.of(3, 6, 12);

    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final BigDecimal MAX_INTERES = new BigDecimal("999.99"); // DECIMAL(5,2)

    private final int id;
    private final Cliente cliente;
    private final Empleado empleado;
    private final BigDecimal monto;
    private final BigDecimal interes;
    private final int cuotas;
    private final LocalDate fechaPrestamo;
    private final List<Pago> pagos = new ArrayList<>();

    /**
     * @param id        Use 0 para un préstamo nuevo que todavía no se ha guardado.
     * @param interes   Porcentaje de interés (10.00 significa 10%).
     */
    public Prestamo(int id, Cliente cliente, Empleado empleado, BigDecimal monto, BigDecimal interes,
                    int cuotas, LocalDate fechaPrestamo) {
        if (cliente == null) {
            throw new DatosInvalidosException("El préstamo debe estar asociado a un cliente.");
        }
        if (empleado == null) {
            throw new DatosInvalidosException("El préstamo debe estar asociado a un empleado.");
        }
        if (fechaPrestamo == null) {
            throw new DatosInvalidosException("La fecha del préstamo es obligatoria.");
        }
        if (interes == null || interes.signum() < 0 || interes.compareTo(MAX_INTERES) > 0) {
            throw new DatosInvalidosException("El interés es inválido.");
        }
        if (!CUOTAS_PERMITIDAS.contains(cuotas)) {
            throw new CuotasInvalidasException(
                    "Cantidad de cuotas inválida (" + cuotas + "). Solo se permiten 3, 6 o 12.");
        }
        this.id = id;
        this.cliente = cliente;
        this.empleado = empleado;
        this.monto = Validaciones.monto(monto, "monto", Validaciones.MAX_DECIMAL_12_2);
        this.interes = interes.setScale(2, RoundingMode.HALF_UP);
        this.cuotas = cuotas;
        this.fechaPrestamo = fechaPrestamo;
    }

    // ---------------------------------------------------------------- cálculos

    /** Valor del interés: monto * porcentaje / 100, redondeado a 2 decimales. */
    public BigDecimal calcularInteres() {
        return monto.multiply(interes).divide(CIEN, 2, RoundingMode.HALF_UP);
    }

    /** Monto total a pagar: monto + interés. */
    public BigDecimal calcularMontoTotal() {
        return monto.add(calcularInteres());
    }

    /** Valor de la cuota mensual "normal" (cuotas 1 a n-1): total / cuotas, a 2 decimales. */
    public BigDecimal valorCuota() {
        return calcularMontoTotal().divide(BigDecimal.valueOf(cuotas), 2, RoundingMode.HALF_UP);
    }

    /**
     * Valor de una cuota específica. Todas valen {@link #valorCuota()} excepto la última,
     * que absorbe la diferencia de centavos del redondeo para que la suma de todas las
     * cuotas sea exactamente el monto total.
     *
     * @param numeroCuota de 1 a la cantidad de cuotas del préstamo
     */
    public BigDecimal valorCuota(int numeroCuota) {
        validarNumeroCuota(numeroCuota);
        BigDecimal normal = valorCuota();
        if (numeroCuota < cuotas) {
            return normal;
        }
        BigDecimal sumaAnteriores = normal.multiply(BigDecimal.valueOf(cuotas - 1L));
        return calcularMontoTotal().subtract(sumaAnteriores);
    }

    public int cuotasPagadas() {
        return pagos.size();
    }

    public int cuotasRestantes() {
        return cuotas - pagos.size();
    }

    /** Total del préstamo menos lo ya pagado. */
    public BigDecimal saldoPendiente() {
        BigDecimal pagado = pagos.stream()
                .map(Pago::getMontoPagado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return calcularMontoTotal().subtract(pagado);
    }

    /** PAGADO cuando no quedan cuotas por pagar; PENDIENTE en caso contrario. */
    public EstadoPrestamo getEstado() {
        return cuotasRestantes() == 0 ? EstadoPrestamo.PAGADO : EstadoPrestamo.PENDIENTE;
    }

    // ---------------------------------------------------------------- fechas

    /**
     * Fecha de vencimiento de una cuota: la cuota n vence n meses después de la fecha del préstamo.
     * Si ese mes no tiene el mismo día (por ejemplo, préstamo del 31 de enero), vence el
     * último día del mes. Se calcula siempre desde la fecha original para no acumular corrimientos.
     */
    public LocalDate fechaVencimientoCuota(int numeroCuota) {
        validarNumeroCuota(numeroCuota);
        return fechaPrestamo.plusMonths(numeroCuota);
    }

    /** Número de la cuota que toca pagar ahora (1 si no se ha pagado ninguna). Vacío si ya está pagado. */
    public Optional<Integer> numeroProximaCuota() {
        return getEstado() == EstadoPrestamo.PAGADO ? Optional.empty() : Optional.of(cuotasPagadas() + 1);
    }

    /** Fecha de vencimiento de la próxima cuota por pagar. Vacío si el préstamo ya está pagado. */
    public Optional<LocalDate> proximaFechaVencimiento() {
        return numeroProximaCuota().map(this::fechaVencimientoCuota);
    }

    /** Valor de la próxima cuota por pagar. Lanza excepción si el préstamo ya está pagado. */
    public BigDecimal valorProximaCuota() {
        int numero = numeroProximaCuota()
                .orElseThrow(() -> new PagoInvalidoException("El préstamo #" + id + " ya está pagado por completo."));
        return valorCuota(numero);
    }

    /**
     * Un préstamo está vencido si tiene al menos una cuota cuya fecha ya pasó y no está pagada.
     * Como las cuotas se pagan en orden, basta mirar la próxima cuota por pagar.
     * "Ya pasó" significa estrictamente anterior a la fecha de referencia: el mismo día
     * del vencimiento todavía no está vencida.
     */
    public boolean estaVencido(LocalDate hoy) {
        return proximaFechaVencimiento().map(fecha -> fecha.isBefore(hoy)).orElse(false);
    }

    // ---------------------------------------------------------------- pagos

    /**
     * Valida que el pago se pueda aplicar a este préstamo. No modifica nada.
     * Reglas: es de este préstamo, el préstamo no está pagado y el monto es EXACTAMENTE
     * el valor de la próxima cuota (ni menos, ni más, ni varias cuotas juntas).
     */
    public void validarPago(Pago pago) {
        if (pago == null) {
            throw new PagoInvalidoException("El pago es obligatorio.");
        }
        if (pago.getPrestamo() != this) {
            throw new PagoInvalidoException("El pago pertenece a otro préstamo.");
        }
        int numero = numeroProximaCuota()
                .orElseThrow(() -> new PagoInvalidoException("El préstamo #" + id + " ya está pagado por completo."));
        BigDecimal esperado = valorCuota(numero);
        if (pago.getMontoPagado().compareTo(esperado) != 0) {
            throw new PagoInvalidoException("Pago rechazado: el monto debe ser exactamente "
                    + Formato.dinero(esperado) + " (cuota " + numero + " de " + cuotas
                    + "). No se aceptan pagos menores, mayores ni de varias cuotas juntas.");
        }
    }

    /** Valida el pago y lo agrega a la lista de pagos. Si era la última cuota, el estado pasa a PAGADO. */
    public void agregarPago(Pago pago) {
        validarPago(pago);
        pagos.add(pago);
    }

    // ---------------------------------------------------------------- getters

    public int getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    /** Porcentaje de interés (10.00 = 10%). */
    public BigDecimal getInteres() {
        return interes;
    }

    public int getCuotas() {
        return cuotas;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    /** Histórico de pagos, de solo lectura. Para agregar un pago use {@link #agregarPago(Pago)}. */
    public List<Pago> getPagos() {
        return Collections.unmodifiableList(pagos);
    }

    private void validarNumeroCuota(int numeroCuota) {
        if (numeroCuota < 1 || numeroCuota > cuotas) {
            throw new DatosInvalidosException(
                    "La cuota " + numeroCuota + " no existe: este préstamo tiene " + cuotas + " cuotas.");
        }
    }
}
