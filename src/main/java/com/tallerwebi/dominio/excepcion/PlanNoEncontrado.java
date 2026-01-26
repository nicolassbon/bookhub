package com.tallerwebi.dominio.excepcion;

public class PlanNoEncontrado extends RuntimeException {
    public PlanNoEncontrado(String message) {
        super(message);
    }
}
