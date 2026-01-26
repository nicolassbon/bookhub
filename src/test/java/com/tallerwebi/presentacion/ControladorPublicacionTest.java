package com.tallerwebi.presentacion;

import com.tallerwebi.infraestructura.service.ServicioComentarioPublicacion;
import com.tallerwebi.infraestructura.service.ServicioPublicacion;
import com.tallerwebi.presentacion.controller.ControladorPublicacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

public class ControladorPublicacionTest {

    private ServicioPublicacion servicioPublicacion;
    private ServicioComentarioPublicacion servicioComentarioPublicacion;
    private ControladorPublicacion controladorPublicacion;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;

    @BeforeEach
    public void setUp() {
        servicioPublicacion = mock(ServicioPublicacion.class);
        servicioComentarioPublicacion = mock(ServicioComentarioPublicacion.class);
        controladorPublicacion = new ControladorPublicacion(servicioPublicacion, servicioComentarioPublicacion);

        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);

        ServletRequestAttributes attr = new ServletRequestAttributes(requestMock);
        RequestContextHolder.setRequestAttributes(attr);
    }

    @Test
    public void crearPublicacionDebeRedirigirAHomeSiEsExitosa() throws MessagingException {
        Long userId = 1L;
        String mensaje = "Contenido de prueba";

        when(requestMock.getSession()).thenReturn(sessionMock);
        when(sessionMock.getAttribute("USERID")).thenReturn(userId);

        var resultado = controladorPublicacion.crearPublicacion(mensaje);

        verify(servicioPublicacion).crearPublicacion(userId, mensaje);
        assertThat(resultado.getViewName(), equalTo("redirect:/home"));
    }

    @Test
    public void crearComentarioPublicacionDebeRedirigirAErrorSiOcurreExcepcion() throws MessagingException {
        Long userId = 1L;
        Long publicacionId = 10L;
        String mensaje = "Comentario de prueba";

        when(requestMock.getSession()).thenReturn(sessionMock);
        when(sessionMock.getAttribute("USERID")).thenReturn(userId);
        doThrow(new RuntimeException("Error al crear comentario")).when(servicioComentarioPublicacion).crearComentarioPublicacion(publicacionId, mensaje, userId);

        var resultado = controladorPublicacion.crearComentarioPublicacion(mensaje, publicacionId);

        verify(servicioComentarioPublicacion).crearComentarioPublicacion(publicacionId, mensaje, userId);
        assertThat(resultado.getViewName(), equalTo("redirect:/error"));
    }
}
