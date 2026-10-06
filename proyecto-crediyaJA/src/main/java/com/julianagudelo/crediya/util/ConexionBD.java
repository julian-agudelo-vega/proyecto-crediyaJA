package com.julianagudelo.crediya.util;

import com.julianagudelo.crediya.exception.PersistenciaException;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Entrega conexiones JDBC a MySQL. Los datos de conexión (url, usuario y contraseña) se leen
 * del archivo config.properties, que NO se sube a GitHub: así la contraseña nunca queda
 * escrita en el código.
 *
 * Cada repositorio pide una conexión nueva por operación y la cierra con try-with-resources.
 */
public class ConexionBD {

    private final String url;
    private final String usuario;
    private final String clave;

    public ConexionBD(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    /** Lee db.url, db.user y db.password del archivo de propiedades indicado. */
    public static ConexionBD desdeArchivo(Path ruta) {
        if (!Files.exists(ruta)) {
            throw new PersistenciaException("No se encontró el archivo '" + ruta.toAbsolutePath()
                    + "'. Copie config.properties.example como config.properties y complete sus datos.");
        }
        Properties propiedades = new Properties();
        try (Reader lector = Files.newBufferedReader(ruta)) {
            propiedades.load(lector);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo leer el archivo '" + ruta + "'.", e);
        }
        String url = propiedades.getProperty("db.url");
        String usuario = propiedades.getProperty("db.user");
        String clave = propiedades.getProperty("db.password");
        if (url == null || usuario == null || clave == null) {
            throw new PersistenciaException(
                    "El archivo '" + ruta + "' debe tener las propiedades db.url, db.user y db.password.");
        }
        return new ConexionBD(url.trim(), usuario.trim(), clave);
    }

    /** Abre una conexión nueva. Quien la reciba debe cerrarla (try-with-resources). */
    public Connection obtenerConexion() {
        try {
            return DriverManager.getConnection(url, usuario, clave);
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo conectar a MySQL. Verifique que el servicio MySQL esté "
                    + "encendido y que db.url, db.user y db.password de config.properties sean correctos. "
                    + "Detalle: " + e.getMessage(), e);
        }
    }

    /** Abre y cierra una conexión para comprobar que los datos de conexión funcionan. */
    public void probarConexion() {
        try (Connection conexion = obtenerConexion()) {
            if (!conexion.isValid(5)) {
                throw new PersistenciaException("La conexión a MySQL se abrió pero no responde.");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al verificar la conexión a MySQL: " + e.getMessage(), e);
        }
    }
}
