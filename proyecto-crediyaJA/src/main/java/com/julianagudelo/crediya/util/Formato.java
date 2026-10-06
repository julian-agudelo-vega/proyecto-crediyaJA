package com.julianagudelo.crediya.util;

import java.math.BigDecimal;
import java.util.Locale;

/** Formateo de valores para mostrarlos en consola. */
public final class Formato {

    private Formato() {
    }

    /** Ejemplo: 1100000 se muestra como $1,100,000.00 (Locale.US para que sea igual en cualquier equipo). */
    public static String dinero(BigDecimal valor) {
        return String.format(Locale.US, "$%,.2f", valor);
    }
}
