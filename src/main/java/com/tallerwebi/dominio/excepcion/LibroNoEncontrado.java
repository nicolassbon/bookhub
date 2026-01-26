package com.tallerwebi.dominio.excepcion;

public class LibroNoEncontrado extends RuntimeException {
    public LibroNoEncontrado(String message) {
        super(message);
    }
}
