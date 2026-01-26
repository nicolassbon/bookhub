package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.model.Amistad;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.infraestructura.service.ServicioAmistad;
import com.tallerwebi.presentacion.controller.ControladorListadoAmigos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

public class ControladorListadoAmigoTest {

    private ServicioAmistad servicioAmistad;
    private ControladorListadoAmigos controladorListadoAmigos;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;

    @BeforeEach
    public void setUp() {
        servicioAmistad = mock(ServicioAmistad.class);
        controladorListadoAmigos = new ControladorListadoAmigos(servicioAmistad);

        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);

        ServletRequestAttributes attr = new ServletRequestAttributes(requestMock);
        RequestContextHolder.setRequestAttributes(attr);
    }

    @Test
    public void listadoAmigosDebeMostrarListaSiUsuarioTieneAmigos() {
        Long userId = 1L;

        // Creación de datos simulados
        Usuario usuario = new Usuario();
        usuario.setId(userId);
        usuario.setNombre("Usuario1");

        Usuario amigo1 = new Usuario();
        amigo1.setId(2L);
        amigo1.setNombre("Amigo1");

        Usuario amigo2 = new Usuario();
        amigo2.setId(3L);
        amigo2.setNombre("Amigo2");

        Amistad amistad1 = new Amistad();
        amistad1.setId(1L);
        amistad1.setUsuario(usuario);
        amistad1.setAmigo(amigo1);
        amistad1.setEstado("Aceptada");
        amistad1.setFechaSolicitud(new Date());
        amistad1.setFechaAceptada(new Date());

        Amistad amistad2 = new Amistad();
        amistad2.setId(2L);
        amistad2.setUsuario(usuario);
        amistad2.setAmigo(amigo2);
        amistad2.setEstado("Aceptada");
        amistad2.setFechaSolicitud(new Date());
        amistad2.setFechaAceptada(new Date());

        List<Amistad> listaAmigos = Arrays.asList(amistad1, amistad2);

        when(requestMock.getSession()).thenReturn(sessionMock);
        when(sessionMock.getAttribute("USERID")).thenReturn(userId);
        when(servicioAmistad.obtenerAmigos(userId)).thenReturn(listaAmigos);

        ModelAndView resultado = controladorListadoAmigos.listadoAmigos();

        verify(servicioAmistad).obtenerAmigos(userId);
        assertThat(resultado.getViewName(), equalTo("listadoAmigos"));

        ModelMap modelo = resultado.getModelMap();
        assertThat(modelo.get("listaAmigos"), equalTo(listaAmigos));
        assertThat(modelo.get("mostrarListado"), equalTo(true));
    }

    @Test
    public void listadoAmigosDebeRedirigirAErrorSiOcurreExcepcion() {
        Long userId = 1L;

        when(requestMock.getSession()).thenReturn(sessionMock);
        when(sessionMock.getAttribute("USERID")).thenReturn(userId);
        doThrow(new RuntimeException("Error al obtener amigos")).when(servicioAmistad).obtenerAmigos(userId);

        ModelAndView resultado = controladorListadoAmigos.listadoAmigos();

        verify(servicioAmistad).obtenerAmigos(userId);
        assertThat(resultado.getViewName(), equalTo("redirect:/error"));
    }
}
