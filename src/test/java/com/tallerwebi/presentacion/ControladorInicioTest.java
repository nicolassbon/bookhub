package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.infraestructura.service.*;

import com.tallerwebi.presentacion.controller.ControladorInicio;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class ControladorInicioTest {

    ServicioInicio servicioInicio = mock(ServicioInicio.class);
    ServicioUsuario servicioUsuario = mock(ServicioUsuario.class);
    ServicioUsuarioLibro servicioUsuarioLibro = mock(ServicioUsuarioLibro.class);

    ServicioUsuarioNotificacion servicioUsuarioNotificacion = mock(ServicioUsuarioNotificacion.class);
    ServicioNotificacion servicioNotificacion = mock(ServicioNotificacion.class);
    ServicioPublicacion servicioPublicacion = mock(ServicioPublicacion.class);
    ServicioComentarioPublicacion servicioComentarioPublicacion = mock(ServicioComentarioPublicacion.class);

    ServicioLibroGenero servicioLibroGenero = mock(ServicioLibroGenero.class);
    ServicioLibro servicioLibro = mock(ServicioLibro.class);
    ServicioValidacionPlan servicioValidacionPlan = mock(ServicioValidacionPlan.class);
    ServicioUsuarioPlan servicioUsuarioPlan = mock(ServicioUsuarioPlan.class);

    private ServicioUsuarioGenero servicioUsuarioGenero;
    ControladorInicio controladorInicio = new ControladorInicio(servicioInicio, servicioUsuario, servicioUsuarioLibro, servicioUsuarioNotificacion,servicioNotificacion, servicioPublicacion, servicioPublicacion, servicioComentarioPublicacion, servicioComentarioPublicacion, servicioUsuarioGenero,servicioLibroGenero, servicioLibro, servicioValidacionPlan, servicioUsuarioPlan);

    @Test
    public void queSeMuestreElNombreDelUsuarioCorrectamente() {

        Usuario usuario = new Usuario();
        usuario.setEmail("hola123@gmail.com");
        usuario.setPassword("hola123");
        //TODO


    }

    @Test
    public void queNoSePuedaMostrarUnaListaDeReviewsVacia() {

        //TODO CON MOCKITO

    }

}
