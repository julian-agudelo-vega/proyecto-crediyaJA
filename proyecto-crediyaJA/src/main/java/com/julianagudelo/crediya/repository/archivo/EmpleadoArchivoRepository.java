package com.julianagudelo.crediya.repository.archivo;

import com.julianagudelo.crediya.model.Empleado;
import com.julianagudelo.crediya.repository.EmpleadoRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Guarda los empleados en empleados.txt.
 * Formato de cada línea:  id;nombre;documento;rol;correo;salario
 */
public class EmpleadoArchivoRepository implements EmpleadoRepository {

    private static final int CAMPOS = 6;

    private final ArchivoTexto archivo;

    public EmpleadoArchivoRepository(Path directorio) {
        this.archivo = new ArchivoTexto(directorio, "empleados.txt");
    }

    @Override
    public Empleado guardar(Empleado empleado) {
        Empleado guardado = new Empleado(archivo.siguienteId(), empleado.getNombre(), empleado.getDocumento(),
                empleado.getRol(), empleado.getCorreo(), empleado.getSalario());
        archivo.agregarLinea(String.join(ArchivoTexto.SEPARADOR,
                String.valueOf(guardado.getId()), guardado.getNombre(), guardado.getDocumento(),
                guardado.getRol(), guardado.getCorreo(), guardado.getSalario().toPlainString()));
        return guardado;
    }

    @Override
    public Optional<Empleado> buscarPorId(int id) {
        return listarTodos().stream().filter(e -> e.getId() == id).findFirst();
    }

    @Override
    public Optional<Empleado> buscarPorDocumento(String documento) {
        return listarTodos().stream().filter(e -> e.getDocumento().equals(documento)).findFirst();
    }

    @Override
    public List<Empleado> listarTodos() {
        List<Empleado> empleados = new ArrayList<>();
        for (String linea : archivo.leerLineas()) {
            String[] c = archivo.dividir(linea, CAMPOS);
            empleados.add(new Empleado(archivo.entero(c[0], linea), c[1], c[2], c[3], c[4],
                    archivo.decimal(c[5], linea)));
        }
        return empleados;
    }
}
