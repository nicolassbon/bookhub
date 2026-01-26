package com.tallerwebi.dominio.excepcion;

public class ListaDeReviewsVacias extends RuntimeException {
    public ListaDeReviewsVacias(String message) {
        super(message);
    }
}
