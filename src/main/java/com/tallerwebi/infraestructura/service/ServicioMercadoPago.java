package com.tallerwebi.infraestructura.service;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;

public interface ServicioMercadoPago {
    String crearPreferencia(Long idPlan,Double valorPago) throws MPException, MPApiException;
}
