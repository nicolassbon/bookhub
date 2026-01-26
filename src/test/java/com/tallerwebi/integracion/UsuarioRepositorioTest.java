package com.tallerwebi.integracion;

import org.junit.Before;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.repository.RepositorioUsuario;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UsuarioRepositorioTest {


    private RepositorioUsuario usuarioRepositorio;

    @Before
    public void setup() {
        usuarioRepositorio = mock(RepositorioUsuario.class);
    }

    @Test
    public void deberiaBuscarUsuarioPorEmail() {
        String email = "test@correo.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);

        when(usuarioRepositorio.buscar(email)).thenReturn(usuario);

        Usuario usuarioBuscado = usuarioRepositorio.buscar(email);

        assertNotNull(usuarioBuscado);
        assertEquals(email, usuarioBuscado.getEmail());
    }

    @Test
    public void deberiaDevolverNuloSiElMailQueLeMandoAlMetodoEsOtro() {
        String email = "test@correo.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);

        when(usuarioRepositorio.buscar(email)).thenReturn(usuario);

        Usuario usuarioBuscado = usuarioRepositorio.buscar("test2@correo.com");

        assertNull(usuarioBuscado);
    }

    @Test
    public void deberiaGuardarTokenDeRecuperacion() {
        Usuario usuario = new Usuario();
        usuario.setEmail("test@correo.com");

        String tokenRecuperacion = "token123";

        usuarioRepositorio.guardarTokenDeRecuperacion(usuario, tokenRecuperacion);

        verify(usuarioRepositorio).guardarTokenDeRecuperacion(usuario, tokenRecuperacion);
    }
}
