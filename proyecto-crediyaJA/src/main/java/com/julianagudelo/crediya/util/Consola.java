package com.julianagudelo.crediya.util;

import java.math.BigDecimal;
import java.util.Scanner;

/**
 * Lectura de datos por consola. Repite la pregunta hasta que el usuario escribe un valor
 * con formato correcto (número entero o monto). Las reglas de negocio las valida el modelo.
 *
 * Si la entrada se cierra (Ctrl+D o fin de archivo) lanza NoSuchElementException,
 * que Main captura para terminar el programa.
 */
public class Consola {

    private final Scanner scanner = new Scanner(System.in);

    public String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }

    public int leerEntero(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un número entero válido.");
            }
        }
    }

    public BigDecimal leerMonto(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            if (texto.contains(",")) {
                System.out.println("No use comas ni separadores de miles. Escriba por ejemplo 1500000 o 1500000.50");
                continue;
            }
            try {
                return new BigDecimal(texto);
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un monto numérico válido.");
            }
        }
    }
}
