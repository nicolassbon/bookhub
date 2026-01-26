package com.tallerwebi.integracion;

import com.tallerwebi.dominio.model.TipoNotificacion;
import com.tallerwebi.infraestructura.repository.RepositorioTipoNotificacion;
import com.tallerwebi.infraestructura.service.ServicioTipoNotificacionImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ServicioTipoNotificacionTest {

    @Mock
    private RepositorioTipoNotificacion repositorioTipoNotificacion;

    @InjectMocks
    private ServicioTipoNotificacionImpl servicioTipoNotificacion;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        repositorioTipoNotificacion = mock(RepositorioTipoNotificacion.class);
        servicioTipoNotificacion = new ServicioTipoNotificacionImpl(repositorioTipoNotificacion);
    }

    @Test
    public void deberiaRetornarTipoNotificacionCorrectamente() throws Exception {
        Long id = 1L;
        TipoNotificacion tipoNotificacion = new TipoNotificacion();
        tipoNotificacion.setDetalle("Alerta");

        when(repositorioTipoNotificacion.encontrarTipoNotificacionPorId(id)).thenReturn(tipoNotificacion);

        String detalle = servicioTipoNotificacion.buscarTipoNotificacionPorId(id);

        verify(repositorioTipoNotificacion, times(1)).encontrarTipoNotificacionPorId(id);
        assertTrue(detalle.equals("Alerta"));
    }

    @Test
    public void deberiaLanzarExcepcionSiTipoNotificacionNoExiste() throws Exception {
        Long id = 1L;

        when(repositorioTipoNotificacion.encontrarTipoNotificacionPorId(id)).thenReturn(null);

        thrown.expect(Exception.class);
        thrown.expectMessage("No se encontro ningun tipo de notificacion con el ID: " + id);

        servicioTipoNotificacion.buscarTipoNotificacionPorId(id);

        verify(repositorioTipoNotificacion, times(1)).encontrarTipoNotificacionPorId(id);
    }
}
