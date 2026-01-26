package com.tallerwebi.dominio.excepcion;

public class PaginasExcedidas extends RuntimeException {
    public PaginasExcedidas(String message) {
        super(message);
    }
}
