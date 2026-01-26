package com.tallerwebi.integracion;

import com.tallerwebi.dominio.model.Publicacion;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.repository.RepositorioPublicacion;
import com.tallerwebi.dominio.repository.RepositorioUsuario;
import com.tallerwebi.infraestructura.service.ServicioPublicacionImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.mail.MessagingException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ServicioPublicacionTest {

    @Mock
    private RepositorioPublicacion repositorioPublicacion;

    @Mock
    private RepositorioUsuario repositorioUsuario;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @InjectMocks
    private ServicioPublicacionImpl servicioPublicacion;

    @Before
    public void setup() {
        repositorioPublicacion = mock(RepositorioPublicacion.class);
        repositorioUsuario = mock(RepositorioUsuario.class);
        servicioPublicacion = new ServicioPublicacionImpl(repositorioPublicacion, repositorioUsuario);
    }

    @Test
    public void deberiaCrearPublicacionCorrectamente() throws Exception {
        Long userId = 100L;
        Usuario usuario = new Usuario();

        when(repositorioUsuario.buscarUsuarioPorId(userId)).thenReturn(usuario);

        servicioPublicacion.crearPublicacion(userId, "Mensaje de prueba");

        verify(repositorioPublicacion).guardar(any(Publicacion.class));
    }

    @Test
    public void deberiaLanzarExcepcionSiUsuarioNoExiste() throws Exception {
        Long userId = 100L;

        when(repositorioUsuario.buscarUsuarioPorId(userId)).thenReturn(null);

        thrown.expect(MessagingException.class);
        thrown.expectMessage("No existe usuario con ID " + userId);

        servicioPublicacion.crearPublicacion(userId, "Mensaje de prueba");
    }

    @Test
    public void deberiaObtenerPublicacionesDeAmigos() throws Exception {
        Long userId = 100L;
        List<Publicacion> publicaciones = Collections.singletonList(new Publicacion());

        when(repositorioPublicacion.obtenerTodasPublicacionesDeAmigos(userId)).thenReturn(publicaciones);

        List<Publicacion> resultado = servicioPublicacion.obtenerTodasPublicacionesDeAmigos(userId);

        assertEquals(1, resultado.size());
        verify(repositorioPublicacion).obtenerTodasPublicacionesDeAmigos(userId);
    }
}
