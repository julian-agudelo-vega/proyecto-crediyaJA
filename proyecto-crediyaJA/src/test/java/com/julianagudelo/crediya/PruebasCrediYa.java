package com.julianagudelo.crediya;

import com.julianagudelo.crediya.exception.ClienteNoEncontradoException;
import com.julianagudelo.crediya.exception.CuotasInvalidasException;
import com.julianagudelo.crediya.exception.DatosInvalidosException;
import com.julianagudelo.crediya.exception.DocumentoDuplicadoException;
import com.julianagudelo.crediya.exception.MontoInvalidoException;
import com.julianagudelo.crediya.exception.PagoInvalidoException;
import com.julianagudelo.crediya.exception.PersistenciaException;
import com.julianagudelo.crediya.model.Cliente;
import com.julianagudelo.crediya.model.Empleado;
import com.julianagudelo.crediya.model.EstadoPrestamo;
import com.julianagudelo.crediya.model.Pago;
import com.julianagudelo.crediya.model.Persona;
import com.julianagudelo.crediya.model.Prestamo;
import com.julianagudelo.crediya.repository.ArmadorPrestamo;
import com.julianagudelo.crediya.repository.archivo.ClienteArchivoRepository;
import com.julianagudelo.crediya.repository.archivo.EmpleadoArchivoRepository;
import com.julianagudelo.crediya.repository.archivo.PagoArchivoRepository;
import com.julianagudelo.crediya.repository.archivo.PrestamoArchivoRepository;
import com.julianagudelo.crediya.service.ClienteService;
import com.julianagudelo.crediya.service.EmpleadoService;
import com.julianagudelo.crediya.service.PagoService;
import com.julianagudelo.crediya.service.PrestamoService;
import com.julianagudelo.crediya.service.ReporteService;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Pruebas automáticas SIN librerías externas: se ejecutan con el botón verde de IntelliJ
 * (clic en el play junto a main). Imprime OK / FALLO por cada comprobación y termina con
 * código de salida 1 si algo falla.
 *
 * Los datos que usan (nombres, documentos...) son ficticios y existen solo dentro de estas
 * pruebas; las pruebas de archivos trabajan en una carpeta temporal, no tocan tu carpeta data/.
 */
public class PruebasCrediYa {

    private static int exitos = 0;
    private static int fallos = 0;

    public static void main(String[] args) throws IOException {
        pruebasModelo();
        pruebasCalculos();
        pruebasFechasYVencimiento();
        pruebasPagos();
        pruebasPersistenciaArchivos();
        pruebasServiciosYReportes();

        System.out.println("\nResultado: " + exitos + " correctas, " + fallos + " con fallo.");
        if (fallos > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------ modelo

    private static void pruebasModelo() {
        System.out.println("== Modelo y validaciones ==");
        Cliente cliente = cliente(1, "100");
        Persona persona = cliente; // polimorfismo
        verificar("mostrarInformacion polimórfico de Cliente incluye teléfono",
                persona.mostrarInformacion().contains("Teléfono"));
        Persona empleado = empleado(1, "200");
        verificar("mostrarInformacion polimórfico de Empleado incluye rol y salario",
                empleado.mostrarInformacion().contains("Rol") && empleado.mostrarInformacion().contains("Salario"));

        esperar("nombre vacío se rechaza", DatosInvalidosException.class,
                () -> new Cliente(0, "  ", "1", "a@b.com", "3"));
        esperar("correo sin @ se rechaza", DatosInvalidosException.class,
                () -> new Cliente(0, "Ana", "1", "correo-malo", "3"));
        esperar("texto con ';' se rechaza (separador de archivos)", DatosInvalidosException.class,
                () -> new Cliente(0, "Ana;Mora", "1", "a@b.com", "3"));
        esperar("salario negativo se rechaza", MontoInvalidoException.class,
                () -> new Empleado(0, "Luis", "2", "Asesor", "l@b.com", new BigDecimal("-5")));
        esperar("préstamo con 5 cuotas se rechaza", CuotasInvalidasException.class,
                () -> prestamo("1000000", 5, LocalDate.of(2026, 1, 5)));
        esperar("préstamo con monto 0 se rechaza", MontoInvalidoException.class,
                () -> prestamo("0", 3, LocalDate.of(2026, 1, 5)));
        esperar("monto con 3 decimales se rechaza", MontoInvalidoException.class,
                () -> prestamo("1000.123", 3, LocalDate.of(2026, 1, 5)));
        for (int cuotas : List.of(3, 6, 12)) {
            final int c = cuotas;
            verificar("préstamo con " + c + " cuotas se acepta", prestamo("1000000", c, LocalDate.of(2026, 1, 5)) != null);
        }
    }

    // ------------------------------------------------------------------ cálculos

    private static void pruebasCalculos() {
        System.out.println("== Cálculos del préstamo ==");
        Prestamo p3 = prestamo("1000000", 3, LocalDate.of(2026, 1, 5));
        verificar("interés = 10% del monto (100000.00)", p3.calcularInteres().compareTo(new BigDecimal("100000.00")) == 0);
        verificar("total = monto + interés (1100000.00)", p3.calcularMontoTotal().compareTo(new BigDecimal("1100000.00")) == 0);
        verificar("cuota normal a 3 cuotas = 366666.67", p3.valorCuota().compareTo(new BigDecimal("366666.67")) == 0);
        verificar("última cuota a 3 cuotas = 366666.66 (absorbe los centavos)",
                p3.valorCuota(3).compareTo(new BigDecimal("366666.66")) == 0);
        verificar("la suma de las cuotas es exactamente el total (3 cuotas)", sumaCuotas(p3).compareTo(p3.calcularMontoTotal()) == 0);

        Prestamo p6 = prestamo("1000000", 6, LocalDate.of(2026, 1, 5));
        verificar("la suma de las cuotas es exactamente el total (6 cuotas)", sumaCuotas(p6).compareTo(p6.calcularMontoTotal()) == 0);
        Prestamo p12 = prestamo("1000000", 12, LocalDate.of(2026, 1, 5));
        verificar("cuota normal a 12 cuotas = 91666.67", p12.valorCuota().compareTo(new BigDecimal("91666.67")) == 0);
        verificar("última cuota a 12 cuotas = 91666.63", p12.valorCuota(12).compareTo(new BigDecimal("91666.63")) == 0);
        verificar("la suma de las cuotas es exactamente el total (12 cuotas)", sumaCuotas(p12).compareTo(p12.calcularMontoTotal()) == 0);

        Prestamo exacto = prestamo("900000", 3, LocalDate.of(2026, 1, 5));
        verificar("monto divisible: las 3 cuotas valen igual (330000.00)",
                exacto.valorCuota(1).compareTo(exacto.valorCuota(3)) == 0
                        && exacto.valorCuota(1).compareTo(new BigDecimal("330000.00")) == 0);

        verificar("saldo inicial = total", p3.saldoPendiente().compareTo(p3.calcularMontoTotal()) == 0);
        verificar("estado inicial PENDIENTE", p3.getEstado() == EstadoPrestamo.PENDIENTE);
        esperar("cuota número 4 de un préstamo de 3 no existe", DatosInvalidosException.class, () -> p3.valorCuota(4));
    }

    // ------------------------------------------------------------------ fechas

    private static void pruebasFechasYVencimiento() {
        System.out.println("== Fechas y vencimiento ==");
        Prestamo p = prestamo("1000000", 3, LocalDate.of(2026, 1, 5));
        verificar("cuota 1 vence un mes después (2026-02-05)", p.fechaVencimientoCuota(1).equals(LocalDate.of(2026, 2, 5)));
        verificar("cuota 3 vence tres meses después (2026-04-05)", p.fechaVencimientoCuota(3).equals(LocalDate.of(2026, 4, 5)));

        Prestamo fin = prestamo("1000000", 3, LocalDate.of(2026, 1, 31));
        verificar("préstamo del 31-ene: cuota 1 vence el 28-feb (último día del mes)",
                fin.fechaVencimientoCuota(1).equals(LocalDate.of(2026, 2, 28)));
        verificar("préstamo del 31-ene: cuota 2 vence el 31-mar (sin corrimiento acumulado)",
                fin.fechaVencimientoCuota(2).equals(LocalDate.of(2026, 3, 31)));

        verificar("el día del vencimiento todavía NO está vencido", !p.estaVencido(LocalDate.of(2026, 2, 5)));
        verificar("el día siguiente SÍ está vencido", p.estaVencido(LocalDate.of(2026, 2, 6)));
        verificar("antes del vencimiento no está vencido", !p.estaVencido(LocalDate.of(2026, 1, 20)));
        verificar("próxima fecha de vencimiento = 2026-02-05",
                p.proximaFechaVencimiento().orElseThrow().equals(LocalDate.of(2026, 2, 5)));

        pagar(p, "100001");
        verificar("al pagar la cuota 1 la próxima fecha pasa a 2026-03-05",
                p.proximaFechaVencimiento().orElseThrow().equals(LocalDate.of(2026, 3, 5)));
        verificar("pagada la cuota 1, el 6-feb ya no está vencido", !p.estaVencido(LocalDate.of(2026, 2, 6)));
        verificar("pero el 6-mar sí (cuota 2 sin pagar)", p.estaVencido(LocalDate.of(2026, 3, 6)));
    }

    // ------------------------------------------------------------------ pagos

    private static void pruebasPagos() {
        System.out.println("== Reglas de pagos ==");
        Prestamo p = prestamo("1000000", 3, LocalDate.of(2026, 1, 5));
        esperar("pagar menos que la cuota se rechaza", PagoInvalidoException.class,
                () -> p.validarPago(pagoDe(p, "1", "366666.66")));
        esperar("pagar más que la cuota se rechaza", PagoInvalidoException.class,
                () -> p.validarPago(pagoDe(p, "2", "366666.68")));
        esperar("pagar dos cuotas juntas se rechaza", PagoInvalidoException.class,
                () -> p.validarPago(pagoDe(p, "3", "733333.34")));
        verificar("tras los rechazos no se registró ningún pago", p.cuotasPagadas() == 0);

        pagar(p, "100001");
        verificar("cuota 1 aceptada: 1 pagada, 2 restantes", p.cuotasPagadas() == 1 && p.cuotasRestantes() == 2);
        verificar("saldo tras cuota 1 = 733333.33", p.saldoPendiente().compareTo(new BigDecimal("733333.33")) == 0);
        verificar("sigue PENDIENTE", p.getEstado() == EstadoPrestamo.PENDIENTE);

        esperar("la cuota 2 no acepta el valor de la última (cuotas distintas)", PagoInvalidoException.class,
                () -> p.validarPago(pagoDe(p, "4", "366666.66")));
        pagar(p, "100002");
        pagar(p, "100003");
        verificar("al pagar la última cuota el estado es PAGADO", p.getEstado() == EstadoPrestamo.PAGADO);
        verificar("saldo final exactamente 0.00", p.saldoPendiente().compareTo(BigDecimal.ZERO) == 0);
        verificar("cuotas restantes 0", p.cuotasRestantes() == 0);
        verificar("histórico con 3 pagos", p.getPagos().size() == 3);
        esperar("un préstamo pagado no acepta más pagos", PagoInvalidoException.class,
                () -> p.validarPago(pagoDe(p, "5", "366666.67")));
        esperar("el histórico devuelto es de solo lectura", UnsupportedOperationException.class,
                () -> p.getPagos().clear());

        Prestamo otro = prestamo("1000000", 3, LocalDate.of(2026, 1, 5));
        esperar("un pago de otro préstamo se rechaza", PagoInvalidoException.class,
                () -> otro.validarPago(pagoDe(p, "6", "366666.67")));
        esperar("comprobante vacío se rechaza", DatosInvalidosException.class,
                () -> pagoDe(otro, " ", "366666.67"));
    }

    // ------------------------------------------------------------------ archivos

    private static void pruebasPersistenciaArchivos() throws IOException {
        System.out.println("== Persistencia en archivos (carpeta temporal) ==");
        Path dir = Files.createTempDirectory("crediya-prueba");
        ClienteArchivoRepository clientes = new ClienteArchivoRepository(dir);
        EmpleadoArchivoRepository empleados = new EmpleadoArchivoRepository(dir);
        PagoArchivoRepository pagos = new PagoArchivoRepository(dir);
        PrestamoArchivoRepository prestamos = new PrestamoArchivoRepository(dir,
                new ArmadorPrestamo(clientes, empleados, pagos));

        verificar("archivos vacíos: listas vacías", clientes.listarTodos().isEmpty() && prestamos.listarTodos().isEmpty());

        Cliente c1 = clientes.guardar(cliente(0, "100"));
        Cliente c2 = clientes.guardar(cliente(0, "101"));
        verificar("ids incrementales de clientes (1 y 2)", c1.getId() == 1 && c2.getId() == 2);
        Empleado e1 = empleados.guardar(empleado(0, "200"));
        verificar("empleado guardado con id 1 y salario intacto",
                e1.getId() == 1 && e1.getSalario().compareTo(new BigDecimal("2500000")) == 0);
        verificar("buscarPorDocumento encuentra al cliente", clientes.buscarPorDocumento("101").isPresent());
        verificar("buscarPorId inexistente da vacío", clientes.buscarPorId(99).isEmpty());

        Prestamo nuevo = new Prestamo(0, c1, e1, new BigDecimal("1000000"), Prestamo.INTERES_PORCENTAJE, 3,
                LocalDate.of(2026, 1, 5));
        Prestamo guardado = prestamos.guardar(nuevo);
        verificar("préstamo guardado con id 1", guardado.getId() == 1);

        Pago pago1 = pagos.guardar(new Pago(0, "100001", LocalDate.of(2026, 2, 4), new BigDecimal("366666.67"), guardado));
        guardado.agregarPago(pago1);
        verificar("pago guardado con id 1", pago1.getIdPago() == 1);

        // Se "reinicia" la aplicación: repositorios nuevos que leen de los mismos archivos.
        ClienteArchivoRepository clientes2 = new ClienteArchivoRepository(dir);
        EmpleadoArchivoRepository empleados2 = new EmpleadoArchivoRepository(dir);
        PagoArchivoRepository pagos2 = new PagoArchivoRepository(dir);
        PrestamoArchivoRepository prestamos2 = new PrestamoArchivoRepository(dir,
                new ArmadorPrestamo(clientes2, empleados2, pagos2));
        Prestamo cargado = prestamos2.buscarPorId(1).orElseThrow();
        verificar("tras recargar: cliente y empleado correctos",
                cargado.getCliente().getDocumento().equals("100") && cargado.getEmpleado().getDocumento().equals("200"));
        verificar("tras recargar: monto, interés y cuotas correctos",
                cargado.getMonto().compareTo(new BigDecimal("1000000")) == 0
                        && cargado.getInteres().compareTo(new BigDecimal("10")) == 0 && cargado.getCuotas() == 3);
        verificar("tras recargar: el pago sigue y el saldo es 733333.33",
                cargado.getPagos().size() == 1 && cargado.saldoPendiente().compareTo(new BigDecimal("733333.33")) == 0);
        verificar("tras recargar: la fecha del préstamo se conserva", cargado.getFechaPrestamo().equals(LocalDate.of(2026, 1, 5)));
        verificar("listarPorCliente devuelve el préstamo del cliente 1", prestamos2.listarPorCliente(1).size() == 1);
        verificar("listarPorCliente del cliente 2 está vacío", prestamos2.listarPorCliente(2).isEmpty());

        Pago pago2 = pagos2.guardar(new Pago(0, "100002", LocalDate.of(2026, 3, 4), new BigDecimal("366666.67"), cargado));
        cargado.agregarPago(pago2);
        Pago pago3 = pagos2.guardar(new Pago(0, "100003", LocalDate.of(2026, 4, 4), new BigDecimal("366666.66"), cargado));
        cargado.agregarPago(pago3);
        prestamos2.actualizarEstado(cargado);
        String contenido = Files.readString(dir.resolve("prestamos.txt"));
        verificar("prestamos.txt refleja el estado PAGADO", contenido.contains(";PAGADO"));
        verificar("pagos.txt tiene 3 líneas", Files.readAllLines(dir.resolve("pagos.txt")).size() == 3);
        verificar("formato de línea de cliente: id;nombre;documento;correo;telefono",
                Files.readAllLines(dir.resolve("clientes.txt")).get(0).split(";", -1).length == 5);

        Prestamo recargado = new PrestamoArchivoRepository(dir,
                new ArmadorPrestamo(clientes2, empleados2, pagos2)).buscarPorId(1).orElseThrow();
        verificar("tras recargar con los 3 pagos el estado es PAGADO", recargado.getEstado() == EstadoPrestamo.PAGADO);

        Files.writeString(dir.resolve("clientes.txt"), "esto no es una línea válida\n");
        esperar("una línea corrupta lanza PersistenciaException", PersistenciaException.class,
                () -> new ClienteArchivoRepository(dir).listarTodos());
    }

    // ------------------------------------------------------------------ servicios y reportes

    private static void pruebasServiciosYReportes() throws IOException {
        System.out.println("== Servicios y reportes con Stream (carpeta temporal) ==");
        Path dir = Files.createTempDirectory("crediya-servicios");
        ClienteArchivoRepository clientes = new ClienteArchivoRepository(dir);
        EmpleadoArchivoRepository empleados = new EmpleadoArchivoRepository(dir);
        PagoArchivoRepository pagos = new PagoArchivoRepository(dir);
        PrestamoArchivoRepository prestamos = new PrestamoArchivoRepository(dir,
                new ArmadorPrestamo(clientes, empleados, pagos));

        ClienteService clienteService = new ClienteService(clientes);
        EmpleadoService empleadoService = new EmpleadoService(empleados);
        PrestamoService prestamoService = new PrestamoService(prestamos, clientes, empleados);
        PagoService pagoService = new PagoService(prestamos, pagos);
        ReporteService reportes = new ReporteService(prestamos);

        Cliente ana = clienteService.registrar("Cliente Uno", "100", "uno@prueba.com", "3000001");
        Cliente beto = clienteService.registrar("Cliente Dos", "101", "dos@prueba.com", "3000002");
        Cliente carla = clienteService.registrar("Cliente Tres", "102", "tres@prueba.com", "3000003");
        Empleado emp = empleadoService.registrar("Empleado Uno", "200", "Asesor", "emp@prueba.com", new BigDecimal("2500000"));

        esperar("cliente con documento repetido se rechaza", DocumentoDuplicadoException.class,
                () -> clienteService.registrar("Otro", "100", "otro@prueba.com", "3000009"));
        esperar("empleado con documento repetido se rechaza", DocumentoDuplicadoException.class,
                () -> empleadoService.registrar("Otro", "200", "Asesor", "o@prueba.com", new BigDecimal("1000000")));
        esperar("préstamo con cliente inexistente se rechaza", ClienteNoEncontradoException.class,
                () -> prestamoService.preparar(99, emp.getId(), new BigDecimal("500000"), 3));

        // Fecha base fija para que los reportes sean deterministas.
        LocalDate hoy = LocalDate.of(2026, 6, 15);

        // Préstamo A (cliente Uno): inició 2026-01-10, 3 cuotas, nada pagado -> vencido.
        Prestamo a = prestamos.guardar(new Prestamo(0, ana, emp, new BigDecimal("1000000"), Prestamo.INTERES_PORCENTAJE, 3,
                LocalDate.of(2026, 1, 10)));
        // Préstamo B (cliente Uno): inició 2026-06-01, no vence todavía.
        Prestamo b = prestamos.guardar(new Prestamo(0, ana, emp, new BigDecimal("600000"), Prestamo.INTERES_PORCENTAJE, 6,
                LocalDate.of(2026, 6, 1)));
        // Préstamo C (cliente Dos): inició 2026-03-01, 3 cuotas, se paga completo -> PAGADO.
        Prestamo c = prestamos.guardar(new Prestamo(0, beto, emp, new BigDecimal("900000"), Prestamo.INTERES_PORCENTAJE, 3,
                LocalDate.of(2026, 3, 1)));
        // Préstamo D (cliente Tres): inició 2026-04-20, paga la cuota 1 pero no la 2 (vence 2026-06-20 -> aún no vencido).
        Prestamo d = prestamos.guardar(new Prestamo(0, carla, emp, new BigDecimal("300000"), Prestamo.INTERES_PORCENTAJE, 3,
                LocalDate.of(2026, 4, 20)));

        // Pagos a través del servicio (valida, guarda y actualiza estado).
        Prestamo cCargado = prestamoService.buscarPorId(c.getId());
        pagoService.registrarPago(cCargado, "900001", cCargado.valorProximaCuota());
        pagoService.registrarPago(cCargado, "900002", cCargado.valorProximaCuota());
        pagoService.registrarPago(cCargado, "900003", cCargado.valorProximaCuota());
        verificar("tres pagos por servicio dejan el préstamo C en PAGADO", cCargado.getEstado() == EstadoPrestamo.PAGADO);
        verificar("el estado PAGADO quedó guardado y se recarga bien",
                prestamoService.buscarPorId(c.getId()).getEstado() == EstadoPrestamo.PAGADO);
        esperar("el servicio rechaza pagar un préstamo ya pagado", PagoInvalidoException.class,
                () -> pagoService.registrarPago(cCargado, "900004", new BigDecimal("330000")));

        Prestamo dCargado = prestamoService.buscarPorId(d.getId());
        esperar("el servicio rechaza un monto distinto a la cuota", PagoInvalidoException.class,
                () -> pagoService.registrarPago(dCargado, "300001", new BigDecimal("1000")));
        verificar("tras el rechazo no se guardó ningún pago",
                prestamoService.buscarPorId(d.getId()).getPagos().isEmpty());
        pagoService.registrarPago(dCargado, "300001", dCargado.valorProximaCuota());
        verificar("el histórico del préstamo D tiene 1 pago", pagoService.historial(d.getId()).size() == 1);

        // Reportes
        List<Prestamo> activos = reportes.prestamosActivos();
        verificar("activos: A, B y D (C está pagado)", ids(activos).equals(List.of(a.getId(), b.getId(), d.getId())));
        List<Prestamo> vencidos = reportes.prestamosVencidos(hoy);
        verificar("vencidos al 2026-06-15: solo A", ids(vencidos).equals(List.of(a.getId())));
        List<Cliente> morosos = reportes.clientesMorosos(hoy);
        verificar("morosos: solo el cliente Uno, sin repetir", morosos.size() == 1 && morosos.get(0).getId() == ana.getId());
        verificar("antes de la primera cuota no hay vencidos", reportes.prestamosVencidos(LocalDate.of(2026, 1, 10)).isEmpty());
        verificar("más adelante D también vence (2026-06-21) y hay 2 morosos",
                reportes.clientesMorosos(LocalDate.of(2026, 6, 21)).size() == 2);
        verificar("listarPorCliente de Uno devuelve 2 préstamos", prestamoService.listarPorCliente(ana.getId()).size() == 2);
        esperar("listarPorCliente de un cliente inexistente falla", ClienteNoEncontradoException.class,
                () -> prestamoService.listarPorCliente(99));
    }

    // ------------------------------------------------------------------ utilidades de prueba

    private static Cliente cliente(int id, String documento) {
        return new Cliente(id, "Cliente de prueba", documento, "cliente@prueba.com", "3000000");
    }

    private static Empleado empleado(int id, String documento) {
        return new Empleado(id, "Empleado de prueba", documento, "Asesor", "empleado@prueba.com", new BigDecimal("2500000"));
    }

    private static Prestamo prestamo(String monto, int cuotas, LocalDate fecha) {
        return new Prestamo(0, cliente(1, "100"), empleado(1, "200"), new BigDecimal(monto), Prestamo.INTERES_PORCENTAJE,
                cuotas, fecha);
    }

    private static Pago pagoDe(Prestamo p, String comprobante, String monto) {
        return new Pago(0, comprobante, LocalDate.of(2026, 2, 1), new BigDecimal(monto), p);
    }

    /** Paga la próxima cuota con su valor exacto. */
    private static void pagar(Prestamo p, String comprobante) {
        p.agregarPago(pagoDe(p, comprobante, p.valorProximaCuota().toPlainString()));
    }

    private static BigDecimal sumaCuotas(Prestamo p) {
        BigDecimal suma = BigDecimal.ZERO;
        for (int i = 1; i <= p.getCuotas(); i++) {
            suma = suma.add(p.valorCuota(i));
        }
        return suma;
    }

    private static List<Integer> ids(List<Prestamo> prestamos) {
        return prestamos.stream().map(Prestamo::getId).toList();
    }

    private static void verificar(String descripcion, boolean condicion) {
        if (condicion) {
            exitos++;
            System.out.println("  OK    " + descripcion);
        } else {
            fallos++;
            System.out.println("  FALLO " + descripcion);
        }
    }

    private static void esperar(String descripcion, Class<? extends Throwable> tipo, Runnable accion) {
        try {
            accion.run();
            fallos++;
            System.out.println("  FALLO " + descripcion + " (no lanzó " + tipo.getSimpleName() + ")");
        } catch (Throwable t) {
            if (tipo.isInstance(t)) {
                exitos++;
                System.out.println("  OK    " + descripcion);
            } else {
                fallos++;
                System.out.println("  FALLO " + descripcion + " (lanzó " + t.getClass().getSimpleName() + ": " + t.getMessage() + ")");
            }
        }
    }
}
