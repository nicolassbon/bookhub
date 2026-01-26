package com.tallerwebi.integracion;

import com.tallerwebi.dominio.model.Notificacion;
import com.tallerwebi.dominio.model.TipoNotificacion;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioNotificacion;
import com.tallerwebi.dominio.repository.RepositorioUsuario;
import com.tallerwebi.infraestructura.repository.RepositorioNotificacion;
import com.tallerwebi.infraestructura.repository.RepositorioTipoNotificacion;
import com.tallerwebi.infraestructura.repository.RepositorioUsuarioNotificacion;
import com.tallerwebi.infraestructura.service.ServicioNotificacionImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Date;

import static org.mockito.Mockito.*;

public class ServicioNotificacionTest {

    @Mock
    private RepositorioNotificacion repositorioNotificacion;

    @Mock
    private RepositorioTipoNotificacion repositorioTipoNotificacion;

    @Mock
    private RepositorioUsuario repositorioUsuario;

    @Mock
    private RepositorioUsuarioNotificacion repositorioUsuarioNotificacion;

    @InjectMocks
    private ServicioNotificacionImpl servicioNotificacion;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        repositorioNotificacion = mock(RepositorioNotificacion.class);
        repositorioTipoNotificacion = mock(RepositorioTipoNotificacion.class);
        repositorioUsuario = mock(RepositorioUsuario.class);
        repositorioUsuarioNotificacion = mock(RepositorioUsuarioNotificacion.class);
        servicioNotificacion = new ServicioNotificacionImpl(repositorioNotificacion, repositorioTipoNotificacion, repositorioUsuario, repositorioUsuarioNotificacion);
    }

    @Test
    public void deberiaCrearNotificacionCorrectamente() throws Exception {
        Long userId = 100L;
        Long tipoNotificacionId = 1L;
        String mensaje = "Nueva notificación";
        Usuario usuario = new Usuario();
        usuario.setId(userId);
        TipoNotificacion tipoNotificacion = new TipoNotificacion();

        when(repositorioUsuario.buscarUsuarioPorId(userId)).thenReturn(usuario);
        when(repositorioTipoNotificacion.encontrarTipoNotificacionPorId(tipoNotificacionId)).thenReturn(tipoNotificacion);

        servicioNotificacion.crearNotificacion(userId, tipoNotificacionId, mensaje, 3L);

        verify(repositorioNotificacion, times(1)).guardar(any(Notificacion.class));
        verify(repositorioUsuarioNotificacion, times(1)).guardar(any(UsuarioNotificacion.class));
    }

    @Test
    public void deberiaLanzarExcepcionSiUsuarioNoExiste() throws Exception {
        Long userId = 100L;
        Long tipoNotificacionId = 1L;
        String mensaje = "Nueva notificación";

        when(repositorioUsuario.buscarUsuarioPorId(userId)).thenReturn(null);

        thrown.expect(Exception.class);
        thrown.expectMessage("No existe usuario con ID " + userId);

        servicioNotificacion.crearNotificacion(userId, tipoNotificacionId, mensaje, 3L);

        verify(repositorioNotificacion, never()).guardar(any(Notificacion.class));
        verify(repositorioUsuarioNotificacion, never()).guardar(any(UsuarioNotificacion.class));
    }

    @Test
    public void deberiaLanzarExcepcionSiTipoNotificacionNoExiste() throws Exception {
        Long userId = 100L;
        Long tipoNotificacionId = 1L;
        String mensaje = "Nueva notificación";
        Usuario usuario = new Usuario();
        usuario.setId(userId);

        when(repositorioUsuario.buscarUsuarioPorId(userId)).thenReturn(usuario);
        when(repositorioTipoNotificacion.encontrarTipoNotificacionPorId(tipoNotificacionId)).thenReturn(null);

        thrown.expect(Exception.class);
        thrown.expectMessage("No se encontró tipo de notificación con el ID " + tipoNotificacionId);

        servicioNotificacion.crearNotificacion(userId, tipoNotificacionId, mensaje, 3L);

        verify(repositorioNotificacion, never()).guardar(any(Notificacion.class));
        verify(repositorioUsuarioNotificacion, never()).guardar(any(UsuarioNotificacion.class));
    }
}
