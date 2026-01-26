package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.infraestructura.service.ServicioAmistad;
import com.tallerwebi.infraestructura.service.ServicioUsuario;
import com.tallerwebi.infraestructura.service.ServicioUsuarioGenero;
import com.tallerwebi.infraestructura.service.ServicioUsuarioLibro;
import com.tallerwebi.presentacion.controller.ControladorPerfil;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.Mockito.*;

public class ControladorPerfilTest {

    ServicioUsuario servicioUsuario = mock(ServicioUsuario.class);
    ServicioAmistad servicioAmistad = mock(ServicioAmistad.class);
    ServicioUsuarioLibro servicioUsuarioLibro = mock(ServicioUsuarioLibro.class);
    ServicioUsuarioGenero servicioUsuarioGenero = mock(ServicioUsuarioGenero.class);
    ControladorPerfil controladorPerfil = new ControladorPerfil(servicioUsuario, servicioAmistad, servicioUsuarioGenero);

    @Test
    public void siElIdDelUsuarioTienePerfilMostrarlo() throws Exception {
        givenExisteUnUsuarioRegistrado();

        ModelAndView mav = controladorPerfil.mostrarPerfil(1L);

        thenSeMuestraElPerfil(mav);
    }

    @Test
    public void siElIdDelUsuarioNoTienePerfilRedireccionaAlLogin() throws UsuarioInexistente {
        doThrow(UsuarioInexistente.class).when(servicioUsuario).buscarUsuarioPorId(3L);

        ModelAndView mav = controladorPerfil.mostrarPerfil(3L);

        thenSeRedireccionaAlLogin(mav);
    }


    private void givenExisteUnUsuarioRegistrado() {
    }

    private void thenSeMuestraElPerfil(ModelAndView mav) {
        assertThat(mav.getViewName(), equalToIgnoringCase("perfil"));
    }

    private void thenSeRedireccionaAlLogin(ModelAndView mav) {
        assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/login"));
    }
}
