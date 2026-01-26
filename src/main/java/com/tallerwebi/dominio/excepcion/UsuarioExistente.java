package com.tallerwebi.dominio.excepcion;

public class UsuarioExistente extends Exception {
    private final String mensaje;

    public UsuarioExistente(String mensaje) {
        super(mensaje);
        this.mensaje = mensaje;
    }

    @Override
    public String getMessage() {
        return mensaje;
    }
}

