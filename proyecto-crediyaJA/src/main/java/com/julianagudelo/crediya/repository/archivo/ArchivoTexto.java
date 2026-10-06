package com.julianagudelo.crediya.repository.archivo;

import com.julianagudelo.crediya.exception.PersistenciaException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilidad interna de las implementaciones de archivo: lee y escribe líneas de un .txt
 * (UTF-8, una línea por registro, campos separados por ';') y convierte texto a números
 * y fechas con errores claros. Concentra todo el manejo de IOException en un solo lugar.
 */
final class ArchivoTexto {

    static final String SEPARADOR = ";";

    private final Path ruta;

    ArchivoTexto(Path directorio, String nombreArchivo) {
        this.ruta = directorio.resolve(nombreArchivo);
    }

    /** Devuelve las líneas no vacías del archivo. Si el archivo no existe todavía, devuelve una lista vacía. */
    List<String> leerLineas() {
        if (!Files.exists(ruta)) {
            return new ArrayList<>();
        }
        try {
            List<String> lineas = new ArrayList<>();
            for (String linea : Files.readAllLines(ruta, StandardCharsets.UTF_8)) {
                if (!linea.isBlank()) {
                    lineas.add(linea);
                }
            }
            return lineas;
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo leer el archivo " + ruta + ".", e);
        }
    }

    /** Agrega una línea al final, creando la carpeta y el archivo si no existen. */
    void agregarLinea(String linea) {
        crearDirectorio();
        try {
            Files.writeString(ruta, linea + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo escribir en el archivo " + ruta + ".", e);
        }
    }

    /** Reemplaza todo el contenido del archivo por las líneas dadas. */
    void reescribir(List<String> lineas) {
        crearDirectorio();
        try {
            Files.write(ruta, lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo escribir en el archivo " + ruta + ".", e);
        }
    }

    /** Siguiente id incremental: el mayor id existente + 1 (1 si el archivo está vacío). */
    int siguienteId() {
        int maximo = 0;
        for (String linea : leerLineas()) {
            String[] campos = linea.split(SEPARADOR, -1);
            maximo = Math.max(maximo, entero(campos[0], linea));
        }
        return maximo + 1;
    }

    /** Separa una línea en campos y comprueba que tenga la cantidad esperada. */
    String[] dividir(String linea, int camposEsperados) {
        String[] campos = linea.split(SEPARADOR, -1);
        if (campos.length != camposEsperados) {
            throw new PersistenciaException("Línea con formato inválido en " + ruta.getFileName()
                    + " (se esperaban " + camposEsperados + " campos): " + linea);
        }
        return campos;
    }

    int entero(String texto, String linea) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw datoInvalido(linea, e);
        }
    }

    BigDecimal decimal(String texto, String linea) {
        try {
            return new BigDecimal(texto.trim());
        } catch (NumberFormatException e) {
            throw datoInvalido(linea, e);
        }
    }

    LocalDate fecha(String texto, String linea) {
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw datoInvalido(linea, e);
        }
    }

    private PersistenciaException datoInvalido(String linea, Exception causa) {
        return new PersistenciaException("Dato inválido en " + ruta.getFileName() + ": " + linea, causa);
    }

    private void crearDirectorio() {
        try {
            Files.createDirectories(ruta.toAbsolutePath().getParent());
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo crear la carpeta de datos " + ruta.getParent() + ".", e);
        }
    }
}
