package com.tallerwebi.integracion;

import com.tallerwebi.dominio.model.ComentarioPublicacion;
import com.tallerwebi.dominio.model.Publicacion;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.repository.RepositorioComentarioPublicacion;
import com.tallerwebi.dominio.repository.RepositorioPublicacion;
import com.tallerwebi.dominio.repository.RepositorioUsuario;
import com.tallerwebi.infraestructura.service.ServicioComentarioPublicacionImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.mail.MessagingException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ServicioComentarioPublicacionTest {

    @Mock
    private RepositorioComentarioPublicacion repositorioComentarioPublicacion;

    @Mock
    private RepositorioUsuario repositorioUsuario;

    @Mock
    private RepositorioPublicacion repositorioPublicacion;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @InjectMocks
    private ServicioComentarioPublicacionImpl servicioComentarioPublicacion;

    @Before
    public void setup() {
        repositorioComentarioPublicacion = mock(RepositorioComentarioPublicacion.class);
        repositorioUsuario = mock(RepositorioUsuario.class);
        repositorioPublicacion = mock(RepositorioPublicacion.class);
        servicioComentarioPublicacion = new ServicioComentarioPublicacionImpl(repositorioComentarioPublicacion, repositorioUsuario, repositorioPublicacion);
    }

    @Test
    public void deberiaCrearComentarioPublicacionCorrectamente() throws Exception {
        Long userId = 100L;
        Long publicationId = 200L;
        Usuario usuario = new Usuario();
        Publicacion publicacion = new Publicacion();

        when(repositorioUsuario.buscarUsuarioPorId(userId)).thenReturn(usuario);
        when(repositorioPublicacion.obtenerPublicacionPorId(publicationId)).thenReturn(publicacion);

        servicioComentarioPublicacion.crearComentarioPublicacion(publicationId, "Mensaje de prueba", userId);

        verify(repositorioComentarioPublicacion).guardar(any(ComentarioPublicacion.class));
    }

    @Test
    public void deberiaLanzarExcepcionSiUsuarioNoExiste() throws Exception {
        Long userId = 100L;
        Long publicationId = 200L;

        when(repositorioUsuario.buscarUsuarioPorId(userId)).thenReturn(null);

        thrown.expect(MessagingException.class);
        thrown.expectMessage("No existe usuario con ID " + userId);

        servicioComentarioPublicacion.crearComentarioPublicacion(publicationId, "Mensaje de prueba", userId);
    }

    @Test
    public void deberiaObtenerComentariosDeLaPublicacion() throws Exception {
        Long publicationId = 200L;
        List<ComentarioPublicacion> comentarios = Collections.singletonList(new ComentarioPublicacion());

        when(repositorioComentarioPublicacion.obtenerLosComentariosDeLaPublicacion(publicationId)).thenReturn(comentarios);

        List<ComentarioPublicacion> resultado = servicioComentarioPublicacion.obtenerLosComentariosDeLaPublicacion(publicationId);

        assertEquals(1, resultado.size());
        verify(repositorioComentarioPublicacion).obtenerLosComentariosDeLaPublicacion(publicationId);
    }
}
