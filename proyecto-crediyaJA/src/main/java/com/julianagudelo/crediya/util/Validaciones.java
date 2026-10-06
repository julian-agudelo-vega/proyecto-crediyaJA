package com.julianagudelo.crediya.util;

import com.julianagudelo.crediya.exception.DatosInvalidosException;
import com.julianagudelo.crediya.exception.MontoInvalidoException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Validaciones reutilizables por las entidades del modelo.
 *
 * Los límites de longitud y de valor máximo NO son reglas de negocio: salen del tamaño
 * de las columnas de la base de datos (VARCHAR y DECIMAL), para que un dato válido en
 * Java nunca falle al guardarse en MySQL.
 */
public final class Validaciones {

    /** Máximo de una columna DECIMAL(10,2). */
    public static final BigDecimal MAX_DECIMAL_10_2 = new BigDecimal("99999999.99");

    /** Máximo de una columna DECIMAL(12,2). */
    public static final BigDecimal MAX_DECIMAL_12_2 = new BigDecimal("9999999999.99");

    private Validaciones() {
    }

    /**
     * Valida un texto obligatorio: no nulo, no vacío, sin ';' ni saltos de línea
     * (el punto y coma es el separador de los archivos .txt) y de longitud máxima dada.
     *
     * @return el texto sin espacios al inicio ni al final
     */
    public static String texto(String valor, String campo, int maxLongitud) {
        if (valor == null || valor.isBlank()) {
            throw new DatosInvalidosException("El campo '" + campo + "' es obligatorio.");
        }
        String limpio = valor.trim();
        if (limpio.length() > maxLongitud) {
            throw new DatosInvalidosException(
                    "El campo '" + campo + "' no puede superar " + maxLongitud + " caracteres.");
        }
        if (limpio.indexOf(';') >= 0 || limpio.indexOf('\n') >= 0 || limpio.indexOf('\r') >= 0) {
            throw new DatosInvalidosException(
                    "El campo '" + campo + "' no puede contener ';' ni saltos de línea.");
        }
        return limpio;
    }

    /** Igual que {@link #texto}, y además exige un formato mínimo de correo: algo@algo, sin espacios. */
    public static String correo(String valor, String campo, int maxLongitud) {
        String limpio = texto(valor, campo, maxLongitud);
        int arroba = limpio.indexOf('@');
        if (arroba <= 0 || arroba == limpio.length() - 1 || limpio.indexOf(' ') >= 0
                || limpio.indexOf('@', arroba + 1) >= 0) {
            throw new DatosInvalidosException("El campo '" + campo + "' no tiene un formato de correo válido.");
        }
        return limpio;
    }

    /**
     * Valida un monto: no nulo, mayor que cero, con máximo 2 decimales y sin superar el máximo.
     *
     * @return el monto con escala 2 (por ejemplo 1500 pasa a 1500.00)
     */
    public static BigDecimal monto(BigDecimal valor, String campo, BigDecimal maximo) {
        if (valor == null) {
            throw new MontoInvalidoException("El campo '" + campo + "' es obligatorio.");
        }
        if (valor.signum() <= 0) {
            throw new MontoInvalidoException("El campo '" + campo + "' debe ser mayor que cero.");
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            throw new MontoInvalidoException("El campo '" + campo + "' no puede tener más de 2 decimales.");
        }
        if (valor.compareTo(maximo) > 0) {
            throw new MontoInvalidoException("El campo '" + campo + "' supera el máximo permitido ("
                    + maximo.toPlainString() + ").");
        }
        return valor.setScale(2, RoundingMode.UNNECESSARY);
    }
}
