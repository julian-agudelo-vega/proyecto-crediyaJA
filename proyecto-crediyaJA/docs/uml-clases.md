# Diagrama UML de clases - CrediYa S.A.S.

El diagrama está escrito en [Mermaid](https://mermaid.js.org/). GitHub lo dibuja automáticamente al abrir este archivo.
Para obtener una imagen (PNG/SVG) para la exposición: copia el bloque de código en <https://mermaid.live> y usa **Actions → PNG/SVG**.

## Modelo de dominio (las 5 clases pedidas + el enum de estado)

```mermaid
classDiagram
    direction LR

    class Persona {
        <<abstract>>
        -int id
        -String nombre
        -String documento
        -String correo
        +getId() int
        +getNombre() String
        +setNombre(String)
        +getDocumento() String
        +getCorreo() String
        +setCorreo(String)
        +mostrarInformacion()* String
    }

    class Cliente {
        -String telefono
        +getTelefono() String
        +setTelefono(String)
        +mostrarInformacion() String
    }

    class Empleado {
        -String rol
        -BigDecimal salario
        +getRol() String
        +setRol(String)
        +getSalario() BigDecimal
        +setSalario(BigDecimal)
        +mostrarInformacion() String
    }

    class Prestamo {
        -int id
        -BigDecimal monto
        -BigDecimal interes
        -int cuotas
        -LocalDate fechaPrestamo
        -List~Pago~ pagos
        +calcularInteres() BigDecimal
        +calcularMontoTotal() BigDecimal
        +valorCuota() BigDecimal
        +valorCuota(int) BigDecimal
        +saldoPendiente() BigDecimal
        +cuotasPagadas() int
        +cuotasRestantes() int
        +getEstado() EstadoPrestamo
        +fechaVencimientoCuota(int) LocalDate
        +proximaFechaVencimiento() Optional~LocalDate~
        +estaVencido(LocalDate) boolean
        +validarPago(Pago)
        +agregarPago(Pago)
    }

    class Pago {
        -int idPago
        -String numeroComprobante
        -LocalDate fechaPago
        -BigDecimal montoPagado
        +getIdPago() int
        +getNumeroComprobante() String
        +getFechaPago() LocalDate
        +getMontoPagado() BigDecimal
    }

    class EstadoPrestamo {
        <<enumeration>>
        PENDIENTE
        PAGADO
    }

    Persona <|-- Cliente
    Persona <|-- Empleado

    Cliente "1" <-- "0..*" Prestamo : cliente
    Empleado "1" <-- "0..*" Prestamo : empleado
    Prestamo "1" *-- "0..*" Pago : pagos
    Pago "0..*" --> "1" Prestamo : prestamo
    Prestamo ..> EstadoPrestamo : calcula
```

### Relaciones

| Relación | Significado |
|---|---|
| `Cliente` y `Empleado` heredan de `Persona` | Comparten id, nombre, documento y correo. `Persona` es abstracta. |
| `Cliente` 1 — N `Prestamo` | Un cliente puede tener muchos préstamos; cada préstamo pertenece a un cliente. |
| `Empleado` 1 — N `Prestamo` | Un empleado gestiona muchos préstamos; cada préstamo tiene un empleado. |
| `Prestamo` 1 — N `Pago` | Un préstamo tiene muchos pagos (composición: los pagos existen dentro de su préstamo). |
| `Pago` N — 1 `Prestamo` | Cada pago apunta al préstamo al que abona. |
| `Prestamo` usa `EstadoPrestamo` | El estado se calcula: PAGADO cuando no quedan cuotas, PENDIENTE en caso contrario. |

## Arquitectura por capas (clases de apoyo)

Estas clases no son del dominio, pero explican cómo se organiza la aplicación.

```mermaid
classDiagram
    direction TB

    class MenuPrincipal
    class ClienteService
    class EmpleadoService
    class PrestamoService
    class PagoService
    class ReporteService

    class ClienteRepository {
        <<interface>>
    }
    class EmpleadoRepository {
        <<interface>>
    }
    class PrestamoRepository {
        <<interface>>
    }
    class PagoRepository {
        <<interface>>
    }

    class ClienteArchivoRepository
    class ClienteJdbcRepository
    class PrestamoArchivoRepository
    class PrestamoJdbcRepository
    class ConexionBD

    MenuPrincipal ..> ClienteService
    MenuPrincipal ..> PrestamoService
    MenuPrincipal ..> PagoService
    MenuPrincipal ..> ReporteService
    MenuPrincipal ..> EmpleadoService

    ClienteService --> ClienteRepository
    EmpleadoService --> EmpleadoRepository
    PrestamoService --> PrestamoRepository
    PagoService --> PagoRepository
    PagoService --> PrestamoRepository
    ReporteService --> PrestamoRepository

    ClienteRepository <|.. ClienteArchivoRepository
    ClienteRepository <|.. ClienteJdbcRepository
    PrestamoRepository <|.. PrestamoArchivoRepository
    PrestamoRepository <|.. PrestamoJdbcRepository
    ClienteJdbcRepository --> ConexionBD
    PrestamoJdbcRepository --> ConexionBD
```

Para no sobrecargar el diagrama solo se dibujan `Cliente` y `Prestamo` en las dos implementaciones;
`Empleado` y `Pago` siguen exactamente el mismo esquema (interfaz + implementación de archivo + implementación JDBC).

Por qué estas clases extra aparecen aquí: **los servicios dependen de las interfaces `*Repository`, no de las
implementaciones**. Por eso se puede elegir archivos o MySQL al iniciar sin cambiar nada de la lógica de negocio
(patrón *Repository*).
