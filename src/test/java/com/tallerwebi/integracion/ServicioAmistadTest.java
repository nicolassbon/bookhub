package com.tallerwebi.integracion;

import com.tallerwebi.dominio.model.Amistad;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.repository.RepositorioUsuario;
import com.tallerwebi.infraestructura.repository.RepositorioAmistad;
import com.tallerwebi.infraestructura.service.ServicioAmistadImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Date;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ServicioAmistadTest {

    @Mock
    private RepositorioUsuario repositorioUsuario;

    @Mock
    private RepositorioAmistad repositorioAmistad;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @InjectMocks
    private ServicioAmistadImpl servicioAmistad;

    @Before
    public void setup() {
        repositorioUsuario = mock(RepositorioUsuario.class);
        repositorioAmistad = mock(RepositorioAmistad.class);
        servicioAmistad = new ServicioAmistadImpl(repositorioUsuario, repositorioAmistad);
    }

    @Test
    public void deberiaEnviarSolicitudDeAmistadCorrectamente() throws Exception {
        Long idUsuario = 100L;
        Long idAmigo = 101L;

        Usuario usuario = new Usuario();
        usuario.setId(idUsuario);

        Usuario amigo = new Usuario();
        amigo.setId(idAmigo);

        when(repositorioUsuario.buscarUsuarioPorId(idUsuario)).thenReturn(usuario);
        when(repositorioUsuario.buscarUsuarioPorId(idAmigo)).thenReturn(amigo);
        when(repositorioAmistad.guardar(any(Amistad.class))).thenReturn(true);

        boolean resultado = servicioAmistad.enviarSolicitudDeAmistad(idUsuario, idAmigo);

        assertTrue(resultado);
        verify(repositorioAmistad).guardar(any(Amistad.class));
    }

    @Test
    public void deberiaLanzarExcepcionCuandoUsuarioNoExiste() throws Exception {
        Long idUsuario = 100L;
        Long idAmigo = 101L;

        when(repositorioUsuario.buscarUsuarioPorId(idUsuario)).thenReturn(null);

        thrown.expect(Exception.class);
        thrown.expectMessage("Usuario o amigo no encontrado");

        servicioAmistad.enviarSolicitudDeAmistad(idUsuario, idAmigo);

        verify(repositorioAmistad, never()).guardar(any(Amistad.class));
    }

    @Test
    public void deberiaLanzarExcepcionCuandoAmigoNoExiste() throws Exception {
        Long idUsuario = 100L;
        Long idAmigo = 101L;

        Usuario usuario = new Usuario();
        usuario.setId(idUsuario);

        when(repositorioUsuario.buscarUsuarioPorId(idUsuario)).thenReturn(usuario);
        when(repositorioUsuario.buscarUsuarioPorId(idAmigo)).thenReturn(null);

        thrown.expect(Exception.class);
        thrown.expectMessage("Usuario o amigo no encontrado");

        servicioAmistad.enviarSolicitudDeAmistad(idUsuario, idAmigo);

        verify(repositorioAmistad, never()).guardar(any(Amistad.class));
    }

    @Test
    public void deberiaAceptarSolicitudDeAmistadCorrectamente() throws Exception {
        Long idUsuario = 100L;
        Long idAmigo = 101L;
        Long idSolicitud = 1L;

        Usuario usuario = new Usuario();
        usuario.setId(idUsuario);

        Amistad amistad = new Amistad();
        amistad.setUsuario(usuario);
        amistad.setEstado("pendiente");

        when(repositorioUsuario.buscarUsuarioPorId(idUsuario)).thenReturn(usuario);
        when(repositorioAmistad.encontrarAmistadPorUsuarios(idAmigo, idUsuario)).thenReturn(amistad);
        when(repositorioAmistad.guardar(amistad)).thenReturn(true);

        boolean resultado = servicioAmistad.aceptarSolicitudDeAmistad(idUsuario, idAmigo, idSolicitud);

        assertTrue(resultado);
        verify(repositorioAmistad).guardar(amistad);
    }

    @Test
    public void deberiaLanzarExcepcionCuandoSolicitudDeAmistadNoExiste() throws Exception {
        Long idUsuario = 100L;
        Long idAmigo = 101L;
        Long idSolicitud = 1L;

        Usuario usuario = new Usuario();
        usuario.setId(idUsuario);

        when(repositorioUsuario.buscarUsuarioPorId(idUsuario)).thenReturn(usuario);
        when(repositorioAmistad.encontrarAmistadPorUsuarios(idAmigo, idUsuario)).thenReturn(null);

        thrown.expect(Exception.class);
        thrown.expectMessage("Solicitud de amistad no encontrada");

        servicioAmistad.aceptarSolicitudDeAmistad(idUsuario, idAmigo, idSolicitud);

        verify(repositorioAmistad, never()).guardar(any(Amistad.class));
    }

    @Test
    public void deberiaRechazarSolicitudDeAmistadCorrectamente() throws Exception {
        Long idUsuario = 100L;
        Long idAmigo = 101L;
        Long idSolicitud = 1L;

        Usuario usuario = new Usuario();
        usuario.setId(idUsuario);

        Amistad amistad = new Amistad();
        amistad.setUsuario(usuario);
        amistad.setEstado("pendiente");

        when(repositorioUsuario.buscarUsuarioPorId(idUsuario)).thenReturn(usuario);
        when(repositorioAmistad.encontrarAmistadPorUsuarios(idAmigo, idUsuario)).thenReturn(amistad);
        when(repositorioAmistad.guardar(amistad)).thenReturn(true);

        boolean resultado = servicioAmistad.rechazarSolicitudDeAmistad(idUsuario, idAmigo, idSolicitud);

        assertTrue(resultado);
        verify(repositorioAmistad).guardar(amistad);
    }
}
