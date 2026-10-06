package com.julianagudelo.crediya.model;

import com.julianagudelo.crediya.util.Formato;
import com.julianagudelo.crediya.util.Validaciones;

import java.math.BigDecimal;

/** Empleado de CrediYa: persona que gestiona los préstamos. */
public class Empleado extends Persona {

    private String rol;
    private BigDecimal salario;

    public Empleado(int id, String nombre, String documento, String rol, String correo, BigDecimal salario) {
        super(id, nombre, documento, correo);
        this.rol = Validaciones.texto(rol, "rol", 30);
        this.salario = Validaciones.monto(salario, "salario", Validaciones.MAX_DECIMAL_10_2);
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = Validaciones.texto(rol, "rol", 30);
    }

    public BigDecimal getSalario() {
        return salario;
    }

    public void setSalario(BigDecimal salario) {
        this.salario = Validaciones.monto(salario, "salario", Validaciones.MAX_DECIMAL_10_2);
    }

    @Override
    public String mostrarInformacion() {
        return "Empleado #" + getId()
                + " | " + getNombre()
                + " | Documento: " + getDocumento()
                + " | Rol: " + rol
                + " | Correo: " + getCorreo()
                + " | Salario: " + Formato.dinero(salario);
    }
}
