package com.julianagudelo.crediya.model;

import com.julianagudelo.crediya.util.Validaciones;

/** Cliente de CrediYa: persona que solicita préstamos. */
public class Cliente extends Persona {

    private String telefono;

    public Cliente(int id, String nombre, String documento, String correo, String telefono) {
        super(id, nombre, documento, correo);
        this.telefono = Validaciones.texto(telefono, "teléfono", 20);
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = Validaciones.texto(telefono, "teléfono", 20);
    }

    @Override
    public String mostrarInformacion() {
        return "Cliente #" + getId()
                + " | " + getNombre()
                + " | Documento: " + getDocumento()
                + " | Correo: " + getCorreo()
                + " | Teléfono: " + telefono;
    }
}
