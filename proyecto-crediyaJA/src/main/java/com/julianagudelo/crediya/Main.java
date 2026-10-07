package com.julianagudelo.crediya;

import com.julianagudelo.crediya.exception.PersistenciaException;
import com.julianagudelo.crediya.model.Cliente;
import com.julianagudelo.crediya.model.Empleado;
import com.julianagudelo.crediya.model.Prestamo;
import com.julianagudelo.crediya.model.Pago;
import com.julianagudelo.crediya.repository.ArmadorPrestamo;
import com.julianagudelo.crediya.repository.ClienteRepository;
import com.julianagudelo.crediya.repository.EmpleadoRepository;
import com.julianagudelo.crediya.repository.PagoRepository;
import com.julianagudelo.crediya.repository.PrestamoRepository;
import com.julianagudelo.crediya.repository.archivo.ClienteArchivoRepository;
import com.julianagudelo.crediya.repository.archivo.EmpleadoArchivoRepository;
import com.julianagudelo.crediya.repository.archivo.PagoArchivoRepository;
import com.julianagudelo.crediya.repository.archivo.PrestamoArchivoRepository;
import com.julianagudelo.crediya.repository.jdbc.ClienteJdbcRepository;
import com.julianagudelo.crediya.repository.jdbc.EmpleadoJdbcRepository;
import com.julianagudelo.crediya.repository.jdbc.PagoJdbcRepository;
import com.julianagudelo.crediya.repository.jdbc.PrestamoJdbcRepository;
import com.julianagudelo.crediya.service.ClienteService;
import com.julianagudelo.crediya.service.EmpleadoService;
import com.julianagudelo.crediya.service.PagoService;
import com.julianagudelo.crediya.service.PrestamoService;
import com.julianagudelo.crediya.service.ReporteService;
import com.julianagudelo.crediya.ui.MenuClientes;
import com.julianagudelo.crediya.ui.MenuEmpleados;
import com.julianagudelo.crediya.ui.MenuPagos;
import com.julianagudelo.crediya.ui.MenuPrincipal;
import com.julianagudelo.crediya.ui.MenuPrestamos;
import com.julianagudelo.crediya.ui.MenuReportes;
import com.julianagudelo.crediya.util.ConexionBD;
import com.julianagudelo.crediya.util.Consola;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.NoSuchElementException;

public class Main {

    /** Carpeta (relativa a donde se ejecuta el programa) con los archivos .txt. */
    private static final Path CARPETA_DATOS = Path.of("data");

    /** Archivo con los datos de conexin a MySQL (no se sube a GitHub). */
    private static final Path ARCHIVO_CONFIG = Path.of("config.properties");

    public static void main(String[] args) {
        Consola consola = new Consola();
        try {
            System.out.println("=== CREDIYA S.A.S. ===");
            elegirPersistencia(consola).mostrar();
        } catch (NoSuchElementException e) {
            System.out.println("\nEntrada finalizada. Hasta pronto.");
        }
    }

    private static MenuPrincipal elegirPersistencia(Consola consola) {
        while (true) {
            System.out.println("\nDnde desea guardar los datos?");
            System.out.println("1. Archivos de texto (carpeta data/)");
            System.out.println("2. Base de datos MySQL");
            int opcion = consola.leerEntero("Opcion: ");
            if (opcion == 1) {
                // Modo archivo: repositorios vacíos (usuario registra desde el menú)
                return crearConArchivos(consola, false);
            }
            if (opcion == 2) {
                try {
                    return crearConMySql(consola, true); // Siempre pregunta si quiere datos de ejemplo
                } catch (PersistenciaException e) {
                    System.out.println("No se pudo usar MySQL: " + e.getMessage());
                }
            } else {
                System.out.println("Opcion invalida.");
            }
        }
    }

    private static boolean preguntarDatosEjemplo(Consola consola) {
        System.out.println("Cargar datos de ejemplo?");
        System.out.println("1. Si");
        System.out.println("2. No");
        int opcion = consola.leerEntero("Opcion: ");
        return opcion == 1;
    }

    private static MenuPrincipal crearConArchivos(Consola consola, boolean cargarEjemplo) {
        // Modo archivo: repositorios vacíos (el usuario registra desde los menús)
        ClienteRepository clientes = new ClienteArchivoRepository(CARPETA_DATOS);
        EmpleadoRepository empleados = new EmpleadoArchivoRepository(CARPETA_DATOS);
        PagoRepository pagos = new PagoArchivoRepository(CARPETA_DATOS);
        PrestamoRepository prestamos = new PrestamoArchivoRepository(CARPETA_DATOS,
                new ArmadorPrestamo(clientes, empleados, pagos));

        return ensamblar("archivos de texto (" + CARPETA_DATOS + "/)", consola, clientes, empleados, prestamos, pagos);
    }

    private static MenuPrincipal crearConMySql(Consola consola, boolean cargarEjemplo) throws PersistenciaException {
        ConexionBD conexion = ConexionBD.desdeArchivo(ARCHIVO_CONFIG);
        conexion.probarConexion();

        // Inicializar repositorios
        ClienteRepository clientes = new ClienteJdbcRepository(conexion);
        EmpleadoRepository empleados = new EmpleadoJdbcRepository(conexion);
        PagoRepository pagos = new PagoJdbcRepository(conexion);
        PrestamoRepository prestamos = new PrestamoJdbcRepository(conexion,
                new ArmadorPrestamo(clientes, empleados, pagos));

        if (cargarEjemplo) {
            insertarDatosEjemploMySQL();
        }

        return ensamblar("base de datos MySQL", consola, clientes, empleados, prestamos, pagos);
    }

private static void insertarDatosEjemploMySQL() throws PersistenciaException {
        try (Connection con = ConexionBD.desdeArchivo(ARCHIVO_CONFIG).obtenerConexion()) {
            try (Statement stmt = con.createStatement()) {
                stmt.executeUpdate("DELETE FROM pagos");
                stmt.executeUpdate("DELETE FROM prestamos");
                stmt.executeUpdate("DELETE FROM clientes");
                stmt.executeUpdate("DELETE FROM empleados");

                // Insertar datos de ejemplo directamente en las tablas
                // Orden: empleados, clientes, prestamos, pagos

                // 1. Empleados
                stmt.executeUpdate("INSERT INTO empleados (id, nombre, documento, rol, correo, salario) VALUES (1, 'María González', '12345678', 'Gerente', 'maria@crediya.com', 5000000.00)");
                stmt.executeUpdate("INSERT INTO empleados (id, nombre, documento, rol, correo, salario) VALUES (2, 'Carlos Rodríguez', '87654321', 'Asesor', 'carlos@crediya.com', 3000000.00)");

                // 2. Clientes
                stmt.executeUpdate("INSERT INTO clientes (id, nombre, documento, correo, telefono) VALUES (1, 'Ana Martínez', '100', 'ana@test.com', '3000001')");
                stmt.executeUpdate("INSERT INTO clientes (id, nombre, documento, correo, telefono) VALUES (2, 'Pedro Sánchez', '101', 'pedro@test.com', '3000002')");
                stmt.executeUpdate("INSERT INTO clientes (id, nombre, documento, correo, telefono) VALUES (3, 'Laura Gómez', '102', 'laura@test.com', '3000003')");

                // 3. Préstamos
                // Préstamo A: cliente 1, empleado 1, 3 cuotas, estado PENDIENTE (2 pagos)
                stmt.executeUpdate("INSERT INTO prestamos (id, cliente_id, empleado_id, monto, interes, cuotas, fecha_inicio, estado) VALUES (1, 1, 1, 1000000.00, 10.00, 3, '2026-01-10', 'PENDIENTE')");

                // Préstamo B: cliente 2, empleado 2, 6 cuotas, estado PENDIENTE (0 pagos)
                stmt.executeUpdate("INSERT INTO prestamos (id, cliente_id, empleado_id, monto, interes, cuotas, fecha_inicio, estado) VALUES (2, 2, 2, 600000.00, 10.00, 6, '2026-06-01', 'PENDIENTE')");

                // Préstamo C: cliente 3, empleado 1, 3 cuotas, estado PAGADO (3 pagos)
                stmt.executeUpdate("INSERT INTO prestamos (id, cliente_id, empleado_id, monto, interes, cuotas, fecha_inicio, estado) VALUES (3, 3, 1, 900000.00, 10.00, 3, '2026-03-01', 'PAGADO')");

                // 4. Pagos
                // Préstamo A: 2 pagos (366,666.67 y 366,666.66)
                stmt.executeUpdate("INSERT INTO pagos (id, prestamo_id, fecha_pago, monto, numero_comprobante) VALUES (1, 1, '2026-02-05', 366666.67, 'NC-001')");
                stmt.executeUpdate("INSERT INTO pagos (id, prestamo_id, fecha_pago, monto, numero_comprobante) VALUES (2, 1, '2026-03-05', 366666.66, 'NC-002')");

                // Préstamo B: sin pagos (no insertamos registros en la tabla pagos para ese préstamo)

                // Préstamo C: 3 pagos completos (valores que suman 1,000,000)
                stmt.executeUpdate("INSERT INTO pagos (id, prestamo_id, fecha_pago, monto, numero_comprobante) VALUES (3, 3, '2026-04-05', 330000.00, 'NC-003')");
                stmt.executeUpdate("INSERT INTO pagos (id, prestamo_id, fecha_pago, monto, numero_comprobante) VALUES (4, 3, '2026-05-05', 330000.00, 'NC-004')");
                stmt.executeUpdate("INSERT INTO pagos (id, prestamo_id, fecha_pago, monto, numero_comprobante) VALUES (5, 3, '2026-06-05', 340000.00, 'NC-005')");

                System.out.println("Datos de ejemplo cargados exitosamente en MySQL.");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al insertar datos de ejemplo en MySQL: " + e.getMessage(), e);
        }
    }

    /** Conecta servicios y menús. Es igual para las dos persistencias porque solo usa las interfaces. */
    private static MenuPrincipal ensamblar(String descripcion, Consola consola, ClienteRepository clientes,
                                           EmpleadoRepository empleados, PrestamoRepository prestamos,
                                           PagoRepository pagos) {
        EmpleadoService empleadoService = new EmpleadoService(empleados);
        ClienteService clienteService = new ClienteService(clientes);
        PrestamoService prestamoService = new PrestamoService(prestamos, clientes, empleados);
        PagoService pagoService = new PagoService(prestamos, pagos);
        ReporteService reporteService = new ReporteService(prestamos);

        return new MenuPrincipal(descripcion, consola,
                new MenuEmpleados(empleadoService, consola),
                new MenuClientes(clienteService, prestamoService, consola),
                new MenuPrestamos(prestamoService, clienteService, empleadoService, consola),
                new MenuPagos(pagoService, prestamoService, consola),
                new MenuReportes(reporteService, consola));
}}
