package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.infraestructura.service.ServicioPlan;
import com.tallerwebi.infraestructura.service.ServicioUsuarioPlan;
import com.tallerwebi.presentacion.controller.ControladorPago;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ControladorPagoTest {

    @Mock
    private HttpSession session;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private ServicioUsuarioPlan servicioUsuarioPlan;

    @InjectMocks
    private ControladorPago controladorPago;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPagoExitoso() throws UsuarioInexistente {
        String paymentId = "12345";
        String status = "approved";
        String externalReference = "1"; // ID del plan
        Long userId = 10L;

        // Configuración del mock de la sesión
        when(session.getAttribute("USERID")).thenReturn(userId);

        // Configuración del mock del servicioPlan
        doNothing().when(servicioUsuarioPlan).actualizarPlanDelUsuarioPlan(userId,Long.parseLong(externalReference));

        // Metodo principal
        String viewName = controladorPago.pagoExitoso(paymentId, status, externalReference, redirectAttributes, session);

        // Verificar que el mensaje de estado de pago se haya agregado correctamente
        verify(redirectAttributes, times(1)).addFlashAttribute(
                "mensajeEstadoPago", "FELICIDADES!! SE HA REALIZADO CORRECTAMENTE EL PAGO DEL PLAN"
        );

        // Verificar que el servicio de plan haya sido llamado para actualizar el plan del usuario
        verify(servicioUsuarioPlan, times(1)).actualizarPlanDelUsuarioPlan(userId,Long.parseLong(externalReference));

        // Verificar que el plan adquirido haya sido almacenado en la sesión
        verify(session, times(1)).setAttribute("planAdquirido", Long.parseLong(externalReference));

        // Verificar la redirección
        assertEquals("redirect:/planes/mostrar", viewName);
    }

    @Test
    void testPagoError() {
        // Datos simulados de la solicitud
        String paymentId = "12345";
        String status = "error";
        String externalReference = "1";

        // Ejecutar el controlador
        String viewName = controladorPago.pagoError(paymentId, status, externalReference, redirectAttributes);

        // Verificar el comportamiento
        verify(redirectAttributes, times(1)).addFlashAttribute("mensajeEstadoPago", "OCURRIO UN ERROR EN EL PAGO DEL PLAN!!");
        assertEquals("redirect:/planes/mostrar", viewName);
    }

    @Test
    void testPagoPendiente() {
        // Datos simulados de la solicitud
        String paymentId = "12345";
        String status = "pending";
        String externalReference = "1";

        // Ejecutar el controlador
        String viewName = controladorPago.pagoPendiente(paymentId, status, externalReference, redirectAttributes);

        // Verificar el comportamiento
        verify(redirectAttributes, times(1)).addFlashAttribute("mensajeEstadoPago", "EL PAGO DEL PLAN ESTA PENDIENTE DE CONFIRMACIÓN");
        assertEquals("redirect:/planes/mostrar", viewName);
    }
}
