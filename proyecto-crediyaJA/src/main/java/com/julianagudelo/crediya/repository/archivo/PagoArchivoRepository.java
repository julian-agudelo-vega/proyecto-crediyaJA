package com.julianagudelo.crediya.repository.archivo;

import com.julianagudelo.crediya.model.Pago;
import com.julianagudelo.crediya.model.Prestamo;
import com.julianagudelo.crediya.repository.PagoRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda los pagos en pagos.txt.
 * Formato de cada línea:  id;prestamoId;numeroComprobante;fechaPago;montoPagado
 * (la fecha va en formato AAAA-MM-DD, por ejemplo 2026-10-05)
 */
public class PagoArchivoRepository implements PagoRepository {

    private static final int CAMPOS = 5;

    private final ArchivoTexto archivo;

    public PagoArchivoRepository(Path directorio) {
        this.archivo = new ArchivoTexto(directorio, "pagos.txt");
    }

    @Override
    public Pago guardar(Pago pago) {
        Pago guardado = new Pago(archivo.siguienteId(), pago.getNumeroComprobante(), pago.getFechaPago(),
                pago.getMontoPagado(), pago.getPrestamo());
        archivo.agregarLinea(String.join(ArchivoTexto.SEPARADOR,
                String.valueOf(guardado.getIdPago()),
                String.valueOf(guardado.getPrestamo().getId()),
                guardado.getNumeroComprobante(),
                guardado.getFechaPago().toString(),
                guardado.getMontoPagado().toPlainString()));
        return guardado;
    }

    @Override
    public List<Pago> listarPorPrestamo(Prestamo prestamo) {
        List<Pago> pagos = new ArrayList<>();
        for (String linea : archivo.leerLineas()) {
            String[] c = archivo.dividir(linea, CAMPOS);
            if (archivo.entero(c[1], linea) == prestamo.getId()) {
                pagos.add(new Pago(archivo.entero(c[0], linea), c[2], archivo.fecha(c[3], linea),
                        archivo.decimal(c[4], linea), prestamo));
            }
        }
        return pagos;
    }
}
