package com.julianagudelo.crediya.service;

import com.julianagudelo.crediya.model.Cliente;
import com.julianagudelo.crediya.model.EstadoPrestamo;
import com.julianagudelo.crediya.model.Prestamo;
import com.julianagudelo.crediya.repository.PrestamoRepository;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reportes de cartera. Usan expresiones lambda y Stream API para filtrar y procesar
 * los préstamos. Reciben la fecha de referencia como parámetro (en vez de leer el reloj)
 * para que se puedan probar con cualquier fecha.
 *
 * Definiciones adoptadas en el diseño del proyecto:
 *  - Préstamo activo: el que sigue debiendo cuotas (estado PENDIENTE).
 *  - Préstamo vencido: tiene al menos una cuota cuya fecha ya pasó y no está pagada.
 *  - Cliente moroso: el que tiene al menos un préstamo vencido.
 */
public class ReporteService {

    private final PrestamoRepository prestamos;

    public ReporteService(PrestamoRepository prestamos) {
        this.prestamos = prestamos;
    }

    public List<Prestamo> prestamosActivos() {
        return prestamos.listarTodos().stream()
                .filter(p -> p.getEstado() == EstadoPrestamo.PENDIENTE)
                .toList();
    }

    public List<Prestamo> prestamosVencidos(LocalDate hoy) {
        return prestamos.listarTodos().stream()
                .filter(p -> p.estaVencido(hoy))
                .toList();
    }

    /** Clientes con al menos un préstamo vencido, sin repetir clientes, en orden de aparición. */
    public List<Cliente> clientesMorosos(LocalDate hoy) {
        return prestamosVencidos(hoy).stream()
                .map(Prestamo::getCliente)
                .collect(Collectors.toMap(Cliente::getId, c -> c, (primero, repetido) -> primero,
                        LinkedHashMap::new))
                .values().stream()
                .toList();
    }
}
