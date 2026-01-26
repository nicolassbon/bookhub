package com.tallerwebi.integracion;

import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.repository.RepositorioUsuario;
import com.tallerwebi.infraestructura.service.ServicioRecuperarContrasenaImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.mail.MessagingException;
import javax.persistence.EntityManager;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ServicioRecuperarContrasenaTest {

    @Mock
    private RepositorioUsuario usuarioRepositorio;
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Mock
    private EntityManager entityManager;
    @InjectMocks
    private ServicioRecuperarContrasenaImpl servicioRecuperarContrasena;

    @Before
    public void setup() {
        usuarioRepositorio = mock(RepositorioUsuario.class);
        entityManager = mock(EntityManager.class);
        servicioRecuperarContrasena = new ServicioRecuperarContrasenaImpl(usuarioRepositorio);
        servicioRecuperarContrasena.setEntityManager(entityManager);
    }

    @Test
    public void deberiaGenerarTokenDeRecuperacion() throws MessagingException {
        String email = "usuario@correo.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);

        when(usuarioRepositorio.buscar(email)).thenReturn(usuario);

        String token = servicioRecuperarContrasena.generarTokenRecuperacion(email);

        assertNotNull(token);
        assertEquals(5, token.length());
        verify(usuarioRepositorio).guardarTokenDeRecuperacion(usuario, token);
        verify(usuarioRepositorio).buscar(email);
    }

    @Test
    public void deberiaVerificarCodigoDeRecuperacionCorrectamente() throws MessagingException {
        String email = "usuario@correo.com";
        String codigo = "12345";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setTokenRecuperacion(codigo);

        when(usuarioRepositorio.buscar(email)).thenReturn(usuario);

        String resultado = servicioRecuperarContrasena.verificarCodigoDeRecuperacion(codigo, email);
        assertEquals("Codigo correcto", resultado);
    }

    @Test
    public void deberiaLanzarExcepcionSiCodigoEsIncorrecto() throws MessagingException {
        String email = "usuario@correo.com";
        String codigoIncorrecto = "54321";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setTokenRecuperacion("12345");

        when(usuarioRepositorio.buscar(email)).thenReturn(usuario);

        thrown.expect(MessagingException.class);
        thrown.expectMessage("Codigo incorrecto");

        servicioRecuperarContrasena.verificarCodigoDeRecuperacion(codigoIncorrecto, email);
    }

    @Test
    public void deberiaLanzarExcepcionSiLasContrasenasNoCoinciden() throws MessagingException {
        String email = "usuario@correo.com";
        String contrasena = "nuevaContrasena";
        String confirmacionContrasena = "otraContrasena";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);

        when(usuarioRepositorio.buscar(email)).thenReturn(usuario);

        thrown.expect(MessagingException.class);
        thrown.expectMessage("Las contraseñas no coinciden.");

        servicioRecuperarContrasena.confirmacionAmbasContrasenas(contrasena, confirmacionContrasena, email);
    }

    @Test
    public void deberiaGenerarNuevoTokenSiElAnteriorAunExiste() throws MessagingException {
        String email = "usuario@correo.com";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setTokenRecuperacion("12345");

        when(usuarioRepositorio.buscar(email)).thenReturn(usuario);

        String nuevoToken = servicioRecuperarContrasena.generarTokenRecuperacion(email);

        assertNotNull(nuevoToken);
        assertNotEquals("12345", nuevoToken);
        verify(usuarioRepositorio).guardarTokenDeRecuperacion(usuario, nuevoToken);
    }

    @Test
    public void deberiaDeshabilitarTokenTrasCambioDeContrasena() throws MessagingException {
        String email = "usuario@correo.com";
        String contrasena = "nuevaContrasena";
        String confirmacionContrasena = "nuevaContrasena";
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setTokenRecuperacion("12345");

        when(usuarioRepositorio.buscar(email)).thenReturn(usuario);

        String resultado = servicioRecuperarContrasena.confirmacionAmbasContrasenas(contrasena, confirmacionContrasena, email);

        assertEquals("Contraseña modificada correctamente", resultado);
        assertNull(usuario.getTokenRecuperacion());
        verify(entityManager).merge(usuario);
        verify(usuarioRepositorio).buscar(email);
    }

    @Test
    public void deberiaDeGenerarUnTokenDeCincoDigitos() throws MessagingException {
        String resultado = servicioRecuperarContrasena.generarCodigoAleatorioDe5Digitos();

        assertEquals(5, resultado.length());
        assertNotNull(resultado);
    }


}
