package com.julianagudelo.crediya.service;

import com.julianagudelo.crediya.model.Prestamo;
import com.julianagudelo.crediya.repository.PrestamoRepository;
import com.julianagudelo.crediya.util.ConexionBD;

import java.time.LocalDate;
import java.util.List;

public class GestorPrestamos extends com.julianagudelo.crediya.GestorPrestamos {

    public GestorPrestamos() {
        super();
    }

    public GestorPrestamos(PrestamoRepository prestamos) {
        super(prestamos);
    }

    public GestorPrestamos(ConexionBD conexion) {
        super(conexion);
    }

    @Override
    public List<Prestamo> listarPrestamosActivos() {
        return super.listarPrestamosActivos();
    }

    @Override
    public List<Prestamo> listarPrestamosVencidos(LocalDate fechaReferencia) {
        return super.listarPrestamosVencidos(fechaReferencia);
    }

    @Override
    public void mostrarResumen() {
        super.mostrarResumen();
    }
}
