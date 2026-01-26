package com.tallerwebi.presentacion;

public class DatosNuevaContrasena {
    private String contrasena;
    private String confirmacionContrasena;

    public DatosNuevaContrasena() {
    }

    public DatosNuevaContrasena(String contrasena, String confirmacionContrasena) {
        this.contrasena = contrasena;
        this.confirmacionContrasena = confirmacionContrasena;
    }
    public String getContrasena() {
        return contrasena;
    }

    public String getConfirmacionContrasena() {
        return confirmacionContrasena;
    }
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public void setConfirmacionContrasena(String confirmacionContrasena) {
        this.confirmacionContrasena = confirmacionContrasena;
    }
}
