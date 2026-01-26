package com.tallerwebi.infraestructura.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.tallerwebi.dominio.model.Plan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Arrays;

@Service
@Transactional
public class ServicioMercadoPagoImpl implements ServicioMercadoPago {

    private ServicioPlan servicioPlan;

    @Autowired
    public ServicioMercadoPagoImpl(ServicioPlan servicioPlan) {
        this.servicioPlan = servicioPlan;
    }

    @Override
    public String crearPreferencia(Long idPlan,Double valorPago) throws MPException, MPApiException {
        // Aca busco el plan por el id
        Plan plan = servicioPlan.buscarPlanPorId(idPlan);

        if(plan == null) {
            throw new MPException("Plan no encontrado");
        }

        // Seteo el codigo de acceso del usuario de prueba vendedor
        MercadoPagoConfig.setAccessToken("APP_USR-1161986002564820-112507-a3751c8d816ca6e05db73ad7ff938d68-2115199025");

        // Si el plan tiene precio uso ese, sino es 0
        // Aca se calcularia la logica para aplicar el descuento al precio
        BigDecimal precioPlan = (valorPago != null) ? new BigDecimal(valorPago) : BigDecimal.ZERO;

        if(precioPlan.equals(BigDecimal.ZERO)) {
            throw new MPException("Calculo del precio fallido");
        }

        // Crear el item (producto/plan que se está comprando)
        PreferenceItemRequest item = PreferenceItemRequest.builder()
                .title("Suscripción " + plan.getNombre()) // Nombre del producto o plan
                .quantity(1)          // Cantidad
                .currencyId("ARS")    // Moneda
                .unitPrice(precioPlan)    // Precio unitario
                .build();

        // Configurar las URLs de retorno
        String baseUrl = "http://localhost:8080/spring";
        PreferenceBackUrlsRequest backUrlsRequest = PreferenceBackUrlsRequest.builder()
                .success(baseUrl + "/pago/exito")    // Redirigir cuando el pago es exitoso
                .failure(baseUrl + "/pago/error")   // Redirigir cuando el pago falla
                .pending(baseUrl + "/pago/pendiente") // Redirigir cuando el pago está pendiente
                .build();

        // Crear la preferencia
        PreferenceRequest request = PreferenceRequest.builder()
                .items(Arrays.asList(item)) // Agregar el item
                .backUrls(backUrlsRequest)        // Configurar las URLs de retorno
                .autoReturn("approved")    // Auto retorno cuando el pago es aprobado
                .externalReference(idPlan.toString()) // Se asigna el planId
                .build();

        // Se crea un cliente de Mercado Pago que se utiliza que va a tener la solicitud de creación de la preferencia de pago.
        PreferenceClient client = new PreferenceClient();

        // Guardar la preferencia y obtener la respuesta
        Preference preference = client.create(request);

        // Retornar el link de pago
        return preference.getInitPoint();
    }
}
