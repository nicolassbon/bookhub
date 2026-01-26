package com.tallerwebi.dominio.excepcion;

public class UsuarioPlanNoEncontrado extends RuntimeException {
    public UsuarioPlanNoEncontrado(String message) {
        super(message);
    }
}
