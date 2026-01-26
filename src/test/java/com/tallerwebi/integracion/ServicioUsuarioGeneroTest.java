package com.tallerwebi.integracion;

import com.tallerwebi.dominio.model.Genero;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioGenero;
import com.tallerwebi.dominio.repository.RepositorioGenero;
import com.tallerwebi.dominio.repository.RepositorioUsuario;
import com.tallerwebi.dominio.repository.RepositorioUsuarioGenero;
import com.tallerwebi.infraestructura.service.ServicioUsuarioGeneroImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ServicioUsuarioGeneroTest {

    @Mock
    private RepositorioUsuarioGenero repositorioUsuarioGenero;

    @Mock
    private RepositorioUsuario repositorioUsuario;

    @Mock
    private RepositorioGenero repositorioGenero;

    @InjectMocks
    private ServicioUsuarioGeneroImpl servicioUsuarioGenero;

    @Before
    public void setup() {
        repositorioUsuarioGenero = mock(RepositorioUsuarioGenero.class);
        repositorioUsuario = mock(RepositorioUsuario.class);
        repositorioGenero = mock(RepositorioGenero.class);
        servicioUsuarioGenero = new ServicioUsuarioGeneroImpl(repositorioUsuarioGenero, repositorioUsuario, repositorioGenero);
    }

    @Test
    public void deberiaRetornarUsuarioGeneroCorrectamente() {
        Long usuarioId = 1L;
        Long generoId = 2L;
        UsuarioGenero usuarioGenero = new UsuarioGenero();

        when(repositorioUsuarioGenero.encontrarUsuarioIdYGeneroId(usuarioId, generoId)).thenReturn(usuarioGenero);

        UsuarioGenero resultado = servicioUsuarioGenero.obtenerUsuarioGenero(usuarioId, generoId);

        verify(repositorioUsuarioGenero, times(1)).encontrarUsuarioIdYGeneroId(usuarioId, generoId);
        assertEquals(usuarioGenero, resultado);
    }

    @Test
    public void deberiaGuardarUsuarioGenero() {
        UsuarioGenero usuarioGenero = new UsuarioGenero();

        servicioUsuarioGenero.guardarUsuarioGenero(usuarioGenero);

        verify(repositorioUsuarioGenero, times(1)).guardar(usuarioGenero);
    }

    @Test
    public void deberiaRetornarListaDeGenerosDeUsuario() {
        Long usuarioId = 1L;
        UsuarioGenero usuarioGenero1 = new UsuarioGenero();
        UsuarioGenero usuarioGenero2 = new UsuarioGenero();
        List<UsuarioGenero> listaEsperada = Arrays.asList(usuarioGenero1, usuarioGenero2);

        when(repositorioUsuarioGenero.obtenerGenerosDeUsuario(usuarioId)).thenReturn(listaEsperada);

        List<UsuarioGenero> resultado = servicioUsuarioGenero.obtenerGenerosDeUsuario(usuarioId);

        verify(repositorioUsuarioGenero, times(1)).obtenerGenerosDeUsuario(usuarioId);
        assertEquals(listaEsperada, resultado);
    }

    @Test
    public void deberiaCrearOActualizarUsuarioGenero() {
        Long usuarioId = 1L;
        Long generoId = 2L;

        Usuario usuario = new Usuario();
        Genero genero = new Genero();
        UsuarioGenero usuarioGenero = null;

        when(repositorioUsuarioGenero.encontrarUsuarioIdYGeneroId(usuarioId, generoId)).thenReturn(usuarioGenero);
        when(repositorioUsuario.buscarUsuarioPorId(usuarioId)).thenReturn(usuario);
        when(repositorioGenero.buscarGeneroPorId(generoId)).thenReturn(genero);

        servicioUsuarioGenero.crearOActualizarUsuarioGenero(usuarioId, generoId);

        verify(repositorioUsuarioGenero, times(1)).encontrarUsuarioIdYGeneroId(usuarioId, generoId);
        verify(repositorioUsuario, times(1)).buscarUsuarioPorId(usuarioId);
        verify(repositorioGenero, times(1)).buscarGeneroPorId(generoId);
        verify(repositorioUsuarioGenero, times(1)).guardar(any(UsuarioGenero.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deberiaLanzarExcepcionSiUsuarioOGeneroNoExisten() {
        Long usuarioId = 1L;
        Long generoId = 2L;

        when(repositorioUsuarioGenero.encontrarUsuarioIdYGeneroId(usuarioId, generoId)).thenReturn(null);
        when(repositorioUsuario.buscarUsuarioPorId(usuarioId)).thenReturn(null);

        servicioUsuarioGenero.crearOActualizarUsuarioGenero(usuarioId, generoId);
    }
}
