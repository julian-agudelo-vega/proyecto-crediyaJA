package com.julianagudelo.crediya.repository.archivo;

import com.julianagudelo.crediya.exception.PrestamoNoEncontradoException;
import com.julianagudelo.crediya.model.Prestamo;
import com.julianagudelo.crediya.repository.ArmadorPrestamo;
import com.julianagudelo.crediya.repository.PrestamoRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Guarda los préstamos en prestamos.txt.
 * Formato de cada línea:  id;clienteId;empleadoId;monto;interes;cuotas;fechaInicio;estado
 *
 * La columna estado se escribe para que el archivo sea legible, pero al cargar un préstamo
 * el estado se vuelve a calcular a partir de sus pagos (así nunca queda desincronizado).
 */
public class PrestamoArchivoRepository implements PrestamoRepository {

    private static final int CAMPOS = 8;

    private final ArchivoTexto archivo;
    private final ArmadorPrestamo armador;

    public PrestamoArchivoRepository(Path directorio, ArmadorPrestamo armador) {
        this.archivo = new ArchivoTexto(directorio, "prestamos.txt");
        this.armador = armador;
    }

    @Override
    public Prestamo guardar(Prestamo prestamo) {
        Prestamo guardado = new Prestamo(archivo.siguienteId(), prestamo.getCliente(), prestamo.getEmpleado(),
                prestamo.getMonto(), prestamo.getInteres(), prestamo.getCuotas(), prestamo.getFechaPrestamo());
        archivo.agregarLinea(aLinea(guardado));
        return guardado;
    }

    @Override
    public void actualizarEstado(Prestamo prestamo) {
        List<String> lineas = archivo.leerLineas();
        boolean encontrado = false;
        for (int i = 0; i < lineas.size(); i++) {
            String[] campos = archivo.dividir(lineas.get(i), CAMPOS);
            if (archivo.entero(campos[0], lineas.get(i)) == prestamo.getId()) {
                lineas.set(i, aLinea(prestamo));
                encontrado = true;
            }
        }
        if (!encontrado) {
            throw new PrestamoNoEncontradoException(prestamo.getId());
        }
        archivo.reescribir(lineas);
    }

    @Override
    public Optional<Prestamo> buscarPorId(int id) {
        for (String linea : archivo.leerLineas()) {
            String[] campos = archivo.dividir(linea, CAMPOS);
            if (archivo.entero(campos[0], linea) == id) {
                return Optional.of(armar(campos, linea));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Prestamo> listarTodos() {
        List<Prestamo> prestamos = new ArrayList<>();
        for (String linea : archivo.leerLineas()) {
            prestamos.add(armar(archivo.dividir(linea, CAMPOS), linea));
        }
        return prestamos;
    }

    @Override
    public List<Prestamo> listarPorCliente(int clienteId) {
        List<Prestamo> prestamos = new ArrayList<>();
        for (String linea : archivo.leerLineas()) {
            String[] campos = archivo.dividir(linea, CAMPOS);
            if (archivo.entero(campos[1], linea) == clienteId) {
                prestamos.add(armar(campos, linea));
            }
        }
        return prestamos;
    }

    private Prestamo armar(String[] c, String linea) {
        return armador.armar(
                archivo.entero(c[0], linea),
                archivo.entero(c[1], linea),
                archivo.entero(c[2], linea),
                archivo.decimal(c[3], linea),
                archivo.decimal(c[4], linea),
                archivo.entero(c[5], linea),
                archivo.fecha(c[6], linea));
    }

    private String aLinea(Prestamo p) {
        return String.join(ArchivoTexto.SEPARADOR,
                String.valueOf(p.getId()),
                String.valueOf(p.getCliente().getId()),
                String.valueOf(p.getEmpleado().getId()),
                p.getMonto().toPlainString(),
                p.getInteres().toPlainString(),
                String.valueOf(p.getCuotas()),
                p.getFechaPrestamo().toString(),
                p.getEstado().name());
    }
}
