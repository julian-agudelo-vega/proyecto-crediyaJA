package com.julianagudelo.crediya.model;

import com.julianagudelo.crediya.util.Validaciones;

/**
 * Clase abstracta con los datos comunes de Cliente y Empleado.
 *
 * Encapsulamiento: todos los atributos son privados. id y documento no tienen setter
 * porque el sistema no permite cambiarlos; nombre y correo sí se pueden modificar.
 */
public abstract class Persona {

    private final int id;
    private String nombre;
    private final String documento;
    private String correo;

    /**
     * @param id Identificador. Use 0 para una persona nueva que todavía no se ha guardado:
     *           el repositorio devuelve una copia con el id real.
     */
    protected Persona(int id, String nombre, String documento, String correo) {
        this.id = id;
        this.nombre = Validaciones.texto(nombre, "nombre", 80);
        this.documento = Validaciones.texto(documento, "documento", 30);
        this.correo = Validaciones.correo(correo, "correo", 80);
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = Validaciones.texto(nombre, "nombre", 80);
    }

    public String getDocumento() {
        return documento;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = Validaciones.correo(correo, "correo", 80);
    }

    /**
     * Comportamiento polimórfico: cada subclase devuelve su propia descripción.
     */
    public abstract String mostrarInformacion();
}
