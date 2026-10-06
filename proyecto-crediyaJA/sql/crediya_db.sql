-- =====================================================================
-- CrediYa S.A.S. - Script de base de datos (MySQL 8.0)
-- Basado en el script entregado por el profesor, con los cambios
-- marcados como [CAMBIO] (el profesor indicó que se puede ampliar).
--
-- IMPORTANTE: este script supone una base de datos NUEVA.
-- Si ya ejecutaste el script original del profesor y tienes la base
-- crediya_db creada (sin datos que quieras conservar), bórrala primero:
--     DROP DATABASE crediya_db;
-- y luego ejecuta este script completo.
-- =====================================================================

CREATE DATABASE crediya_db;

USE crediya_db;

CREATE TABLE empleados (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL,                 -- [CAMBIO] NOT NULL
  documento VARCHAR(30) NOT NULL UNIQUE,       -- [CAMBIO] NOT NULL + UNIQUE (no se permiten documentos repetidos)
  rol VARCHAR(30) NOT NULL,                    -- [CAMBIO] NOT NULL
  correo VARCHAR(80) NOT NULL,                 -- [CAMBIO] NOT NULL
  salario DECIMAL(10,2) NOT NULL               -- [CAMBIO] NOT NULL
);

CREATE TABLE clientes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL,                 -- [CAMBIO] NOT NULL
  documento VARCHAR(30) NOT NULL UNIQUE,       -- [CAMBIO] NOT NULL + UNIQUE (no se permiten documentos repetidos)
  correo VARCHAR(80) NOT NULL,                 -- [CAMBIO] NOT NULL
  telefono VARCHAR(20) NOT NULL                -- [CAMBIO] NOT NULL
);

CREATE TABLE prestamos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  cliente_id INT NOT NULL,                     -- [CAMBIO] NOT NULL
  empleado_id INT NOT NULL,                    -- [CAMBIO] NOT NULL
  monto DECIMAL(12,2) NOT NULL,                -- [CAMBIO] NOT NULL
  interes DECIMAL(5,2) NOT NULL,               -- [CAMBIO] NOT NULL (se guarda como porcentaje: 10.00)
  cuotas INT NOT NULL,                         -- [CAMBIO] NOT NULL
  fecha_inicio DATE NOT NULL,                  -- [CAMBIO] NOT NULL
  estado VARCHAR(20) NOT NULL,                 -- [CAMBIO] NOT NULL (PENDIENTE o PAGADO)
  FOREIGN KEY (cliente_id) REFERENCES clientes(id),
  FOREIGN KEY (empleado_id) REFERENCES empleados(id)
);

CREATE TABLE pagos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  prestamo_id INT NOT NULL,                    -- [CAMBIO] NOT NULL
  numero_comprobante VARCHAR(30) NOT NULL,     -- [CAMBIO] columna nueva: el comprobante que escribe el usuario
  fecha_pago DATE NOT NULL,                    -- [CAMBIO] NOT NULL
  monto DECIMAL(12,2) NOT NULL,                -- [CAMBIO] NOT NULL y DECIMAL(12,2): una cuota puede superar el máximo de DECIMAL(10,2)
  FOREIGN KEY (prestamo_id) REFERENCES prestamos(id)
);
