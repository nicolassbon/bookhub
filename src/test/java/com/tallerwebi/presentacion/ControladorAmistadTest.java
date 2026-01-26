package com.tallerwebi.presentacion;

import com.tallerwebi.infraestructura.service.ServicioAmistad;
import com.tallerwebi.infraestructura.service.ServicioNotificacion;
import com.tallerwebi.infraestructura.service.ServicioUsuarioNotificacion;
import com.tallerwebi.presentacion.controller.ControladorAmistad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

public class ControladorAmistadTest {

    private ServicioAmistad servicioAmistad;
    private ServicioNotificacion servicioNotificacion;
    private ServicioUsuarioNotificacion servicioUsuarioNotificacion;
    private ControladorAmistad controladorAmistad;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;

    @BeforeEach
    public void setUp() {
        servicioAmistad = mock(ServicioAmistad.class);
        servicioNotificacion = mock(ServicioNotificacion.class);
        servicioUsuarioNotificacion = mock(ServicioUsuarioNotificacion.class);
        controladorAmistad = new ControladorAmistad(servicioAmistad, servicioNotificacion, servicioUsuarioNotificacion);

        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);

        when(requestMock.getSession()).thenReturn(sessionMock);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(requestMock));
    }

    @Test
    public void enviarSolicitudAmistadDebeRetornarAmigoAgregadoCorrectamenteSiExitoso() throws Exception {
        Long userId = 1L;
        Long friendId = 2L;
        String username = "testUser";

        when(sessionMock.getAttribute("USERID")).thenReturn(userId);
        when(sessionMock.getAttribute("USERNAME")).thenReturn(username);
        when(servicioAmistad.enviarSolicitudDeAmistad(userId, friendId)).thenReturn(true);

        String resultado = controladorAmistad.enviarSolicitudAmistad(friendId);

        verify(servicioAmistad).enviarSolicitudDeAmistad(userId, friendId);
        verify(servicioNotificacion).crearNotificacion(friendId, 2L, "Has recibido una solicitud de Amistad de " + username, userId);
        assertThat(resultado, equalTo("amigoAgregadoCorrectamente"));
    }

    @Test
    public void enviarSolicitudAmistadDebeRetornarErrorSiFalla() throws Exception {
        Long userId = 1L;
        Long friendId = 2L;

        when(sessionMock.getAttribute("USERID")).thenReturn(userId);
        when(servicioAmistad.enviarSolicitudDeAmistad(userId, friendId)).thenReturn(false);

        String resultado = controladorAmistad.enviarSolicitudAmistad(friendId);

        assertThat(resultado, equalTo("error"));
    }

    @Test
    public void aceptarSolicitudAmistadDebeRedirigirAHomeSiExitoso() throws Exception {
        Long userId = 1L;
        Long requestId = 10L;
        Long friendId = 2L;
        String username = "testUser";

        when(sessionMock.getAttribute("USERID")).thenReturn(userId);
        when(sessionMock.getAttribute("USERNAME")).thenReturn(username);
        when(servicioUsuarioNotificacion.obtenerElIdDeAmigoPorIdDeNotificacion(userId, requestId)).thenReturn(friendId);
        when(servicioAmistad.aceptarSolicitudDeAmistad(userId, friendId, requestId)).thenReturn(true);

        ModelAndView resultado = controladorAmistad.aceptarSolicitudAmistad(requestId);

        verify(servicioNotificacion).editarNombreNotificacion(requestId, "Has aceptado la solicitud de " + username, 5L);
        verify(servicioNotificacion).crearNotificacion(friendId, 5L, username + " ha aceptado tu solicitud de amistad", userId);
        assertThat(resultado.getViewName(), equalTo("redirect:/home"));
    }

    @Test
    public void aceptarSolicitudAmistadDebeRedirigirAErrorSiFalla() throws Exception {
        Long userId = 1L;
        Long requestId = 10L;
        Long friendId = 2L;

        when(sessionMock.getAttribute("USERID")).thenReturn(userId);
        when(servicioUsuarioNotificacion.obtenerElIdDeAmigoPorIdDeNotificacion(userId, requestId)).thenReturn(friendId);
        when(servicioAmistad.aceptarSolicitudDeAmistad(userId, friendId, requestId)).thenReturn(false);

        ModelAndView resultado = controladorAmistad.aceptarSolicitudAmistad(requestId);

        assertThat(resultado.getViewName(), equalTo("redirect:/error"));
    }
}
