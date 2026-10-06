# CrediYa S.A.S.

Sistema de consola en Java para gestionar empleados, clientes, préstamos y pagos de una empresa de créditos personales.

## Descripción

CrediYa S.A.S. otorga créditos personales y llevaba su control de préstamos y cobros en hojas de cálculo.
Este proyecto digitaliza ese control: permite registrar y consultar la información y guardarla de forma persistente,
a elección del usuario, en **archivos de texto** o en una base de datos **MySQL** (mediante JDBC).

## Objetivo

Desarrollar un sistema modular en Java que gestione préstamos y cobros de cartera aplicando Programación Orientada a Objetos
(herencia, polimorfismo, encapsulamiento), colecciones, archivos, persistencia con JDBC, principios SOLID,
manejo de excepciones, y expresiones lambda con Stream API en los reportes.

## Tecnologías utilizadas

- Java (JDK 21 o superior; el proyecto se desarrolló con un JDK más reciente)
- Maven (gestión del proyecto y de la dependencia del driver)
- MySQL 8.0 y JDBC (MySQL Connector/J)
- IntelliJ IDEA Community

## Requisitos

- JDK 21 o superior.
- IntelliJ IDEA (Community sirve).
- MySQL Server 8.0 con el servicio encendido (solo si vas a usar la persistencia en MySQL; con archivos no hace falta).
- Conexión a internet la primera vez, para que Maven descargue el driver de MySQL.

## Configuración

1. Abre la carpeta del proyecto en IntelliJ: **File → Open** y elige la carpeta `proyecto-crediyaJA` (la que contiene `pom.xml`).
2. Si IntelliJ pregunta, confía en el proyecto y deja que Maven importe las dependencias (barra de progreso abajo a la derecha).
3. Comprueba el JDK: **File → Project Structure → Project → SDK** debe ser tu JDK.
4. Si IntelliJ pregunta por cambios en el `pom.xml`, pulsa **Load Maven Changes** (icono de la "m" con una flecha).

### Si Maven no encuentra el Connector/J

El `pom.xml` usa `mysql-connector-j` versión `9.7.0`, que es compatible con MySQL Server 8.0.
Si IntelliJ marca la dependencia en rojo y no la descarga:

1. Abre `pom.xml`, clic derecho dentro de `<dependencies>` → **Generate → Dependency**.
2. Busca `mysql-connector-j` y elige una versión de la lista (la más reciente que aparezca sirve).
3. Reemplaza el número de `<version>` y pulsa **Load Maven Changes**.

## Configuración de MySQL

1. **Crear la base de datos.** Abre MySQL Workbench, conéctate a tu instancia local (`MySQL80`, usuario `root`, `localhost:3306`),
   abre el archivo `sql/crediya_db.sql` (File → Open SQL Script) y ejecútalo completo (icono del rayo).
   - Si ya habías ejecutado el script original del profesor, ejecuta antes `DROP DATABASE crediya_db;`
     (el script nuevo agrega la columna `numero_comprobante` y otros cambios; ver comentarios `[CAMBIO]` dentro del archivo).
2. **Crear el archivo de conexión.** En la raíz del proyecto, copia `config.properties.example` y llama a la copia `config.properties`
   (clic derecho sobre el archivo → Copy, y luego Paste con el nuevo nombre).
3. **Escribir tu contraseña.** Abre `config.properties` y reemplaza `ESCRIBA_AQUI_SU_CONTRASENA` por tu contraseña real de MySQL.
   Este archivo está en `.gitignore`: no se sube a GitHub.
4. Si al conectar aparece el error *"Public Key Retrieval is not allowed"*, agrega al final de `db.url`:
   `?allowPublicKeyRetrieval=true`

## Ejecución desde IntelliJ IDEA

1. Abre `src/main/java/com/julianagudelo/crediya/Main.java`.
2. Pulsa el triángulo verde junto a `public static void main` → **Run 'Main.main()'**.
3. El programa pregunta dónde guardar los datos: **1** archivos de texto (carpeta `data/`, se crea sola) o **2** MySQL.
4. Si MySQL falla (servicio apagado, contraseña incorrecta...), muestra el motivo y vuelve a preguntar.

Si en la consola de IntelliJ las tildes o la `¿` se ven como símbolos raros: **Run → Edit Configurations → Modify options → Add VM options**
y escribe `-Dstdout.encoding=UTF-8`.

### Ejecutar las pruebas automáticas

Abre `src/test/java/com/julianagudelo/crediya/PruebasCrediYa.java` y ejecuta su `main`. Imprime `OK` o `FALLO` por cada comprobación
(cálculos, reglas de pago, vencimientos, persistencia en archivos y reportes). No usa librerías externas y trabaja en
carpetas temporales, así que no toca tu carpeta `data/`.

## Estructura del proyecto

```
proyecto-crediyaJA/
├── pom.xml
├── README.md
├── .gitignore
├── config.properties.example      plantilla de conexión (sin contraseña real)
├── sql/crediya_db.sql             script de la base de datos
├── docs/
│   ├── uml-clases.md              diagrama UML (Mermaid)
│   └── GITHUB.md                  guía para subir a GitHub
└── src/
    ├── main/java/com/julianagudelo/crediya/
    │   ├── Main.java              punto de entrada: elige persistencia y arma la aplicación
    │   ├── model/                 Persona, Cliente, Empleado, Prestamo, Pago, EstadoPrestamo
    │   ├── exception/             excepciones propias (CrediYaException y sus hijas)
    │   ├── service/               lógica de negocio: Cliente, Empleado, Prestamo, Pago, Reporte
    │   ├── repository/            interfaces de acceso a datos + ArmadorPrestamo
    │   │   ├── archivo/           implementaciones con archivos .txt
    │   │   └── jdbc/              implementaciones con MySQL (JDBC)
    │   ├── util/                  Validaciones, Formato, Consola, ConexionBD
    │   └── ui/                    menús de consola
    └── test/java/com/julianagudelo/crediya/
        └── PruebasCrediYa.java    pruebas automáticas
```

## Funcionalidades

**Empleados:** registrar, listar y consultar por id. No se permiten documentos repetidos.

**Clientes:** registrar, listar y consultar los préstamos de un cliente. No se permiten documentos repetidos.

**Préstamos:** crear (cliente + empleado + monto + 3, 6 o 12 cuotas), con cálculo automático del interés, del total y de la cuota; listar y consultar.

**Pagos:** registrar el pago de la próxima cuota, actualizar el saldo y ver el histórico de pagos de un préstamo.

**Reportes** (con lambdas y Stream API): préstamos activos, préstamos vencidos y clientes morosos.

### Reglas de negocio

| Regla | Detalle |
|---|---|
| Interés | 10% del monto. |
| Monto total | monto + interés. |
| Cuotas permitidas | Solo 3, 6 o 12. |
| Valor de la cuota | total / cuotas, redondeado a 2 decimales. La **última cuota absorbe la diferencia de centavos** para que la suma de las cuotas sea exactamente el total. |
| Fecha de la 1.ª cuota | Un mes después de la fecha del préstamo; las demás, mensualmente. Si el mes no tiene ese día (ej. préstamo del 31 de enero), vence el último día del mes. |
| Pagos | Cada pago cubre la **siguiente cuota sin pagar, en orden**. El monto debe ser **exactamente** el valor de esa cuota: no se aceptan pagos menores, mayores, adelantados ni de varias cuotas juntas. |
| Fecha del pago | La fecha actual del sistema. |
| Comprobante | Lo escribe el usuario y se guarda tal cual. |
| Estado | `PENDIENTE` o `PAGADO`. Pasa a PAGADO al pagar la última cuota. "Vencido" **no** es un estado. |
| Préstamo activo | El que todavía debe cuotas (PENDIENTE). |
| Préstamo vencido | Tiene al menos una cuota cuya fecha ya pasó y no está pagada (el mismo día del vencimiento todavía no cuenta como vencido). |
| Cliente moroso | El que tiene al menos un préstamo vencido. |

## Ejemplos de uso

Crear un préstamo de $1,000,000 a 3 cuotas:

```
Monto solicitado:    $1,000,000.00
Interés (10.00%):    $100,000.00
Total a pagar:       $1,100,000.00
Cuotas:              3 de $366,666.67 (la última cuota es de $366,666.66 por ajuste de centavos)
Próxima cuota:       #1 por $366,666.67, vence el 2026-11-05
```

Intentar pagar un monto incorrecto:

```
Valor de la cuota a pagar: $366,666.67
Número de comprobante: 458721
Monto a pagar: 1000
Error: Pago rechazado: el monto debe ser exactamente $366,666.67 (cuota 1 de 3).
       No se aceptan pagos menores, mayores ni de varias cuotas juntas.
```

Pagar correctamente:

```
Monto a pagar: 366666.67
Pago #1 registrado (fecha 2026-10-05).
Saldo pendiente: $733,333.33
Cuotas restantes: 2
Estado del préstamo: PENDIENTE
```

Los montos se escriben sin comas ni separadores de miles: `1500000` o `1500000.50`.

## Persistencia

Al iniciar, el usuario elige una de las dos opciones. Los servicios usan solo las interfaces `*Repository`, así que la lógica de negocio es idéntica en ambos casos
(patrón *Repository*: una interfaz, dos implementaciones por entidad).

### Archivos de texto (carpeta `data/`)

UTF-8, una línea por registro, campos separados por `;`, sin encabezado, fechas en formato `AAAA-MM-DD`. La carpeta y los archivos se crean solos.
Los ids son incrementales (el mayor existente + 1), igual que `AUTO_INCREMENT` en MySQL.

| Archivo | Formato de cada línea |
|---|---|
| `clientes.txt` | `id;nombre;documento;correo;telefono` |
| `empleados.txt` | `id;nombre;documento;rol;correo;salario` |
| `prestamos.txt` | `id;clienteId;empleadoId;monto;interes;cuotas;fechaInicio;estado` |
| `pagos.txt` | `id;prestamoId;numeroComprobante;fechaPago;montoPagado` |

Por eso los textos no pueden contener `;` (el programa lo valida).

### MySQL (JDBC)

Base `crediya_db` con las tablas `empleados`, `clientes`, `prestamos` y `pagos` (ver `sql/crediya_db.sql`).
Las consultas SQL están únicamente en las clases del paquete `repository/jdbc`, con `PreparedStatement` y `try-with-resources` (las conexiones se cierran siempre).
Los datos de conexión salen de `config.properties`.

> Los archivos y MySQL son almacenes **independientes**: los datos guardados en uno no aparecen en el otro.

## Decisiones de diseño

- **Dinero con `BigDecimal`**, no `double`, porque la regla "el pago debe ser exactamente la cuota" exige comparaciones exactas.
- **Valores calculados, no almacenados**: total, cuota, saldo, cuotas restantes, próximo vencimiento y estado se calculan a partir de monto, interés, cuotas y pagos, así nunca quedan desincronizados.
- **Excepciones propias** (paquete `exception`), todas hijas de `CrediYaException`; los menús capturan `CrediYaException`, nunca `Exception` genérica.
- **Un solo patrón**, *Repository*, porque resuelve una necesidad real: dos formas de guardar lo mismo.

## Autores

- Julian Agudelo

## Más documentación

- Diagrama UML: [`docs/uml-clases.md`](docs/uml-clases.md)
- Subir el proyecto a GitHub: [`docs/GITHUB.md`](docs/GITHUB.md)
