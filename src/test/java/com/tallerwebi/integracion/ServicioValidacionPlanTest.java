package com.tallerwebi.integracion;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.tallerwebi.dominio.model.Accion;
import com.tallerwebi.dominio.model.Plan;
import com.tallerwebi.dominio.model.UsuarioPlan;
import com.tallerwebi.dominio.repository.RepositorioPlan;
import com.tallerwebi.dominio.repository.RepositorioUsuarioPlan;
import com.tallerwebi.infraestructura.service.ServicioValidacionPlanImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@RunWith(MockitoJUnitRunner.class)
public class ServicioValidacionPlanTest {
    @Mock
    private RepositorioUsuarioPlan repositorioUsuarioPlan;

    @Mock
    private RepositorioPlan repositorioPlan;

    @InjectMocks
    private ServicioValidacionPlanImpl servicioValidacionPlan;

    private UsuarioPlan usuarioPlanMock;
    private Plan planMock;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        usuarioPlanMock = mock(UsuarioPlan.class);
        planMock = mock(Plan.class);
    }

    @Test
    public void testCalcularValidacionUsuarioPlanDentroDe15Dias() {
    /*
    Date fechaAdquisicion = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10)); // Hace 10 días
    Date fechaVencimiento = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10)); // En 10 días

    // Configurar mocks
    when(usuarioPlanMock.getFecha_plan_adquirido()).thenReturn(fechaAdquisicion);
    when(usuarioPlanMock.getFecha_plan_venc()).thenReturn(fechaVencimiento);
    when(planMock.getPrecio()).thenReturn(2000.0); // Nuevo plan
    when(usuarioPlanMock.getPrecio()).thenReturn(1000.0); // Plan actual

    // Ejecutar método
    Double resultado = servicioValidacionPlan.calcularValidacionUsuarioPlan(planMock, usuarioPlanMock);

    // Verificar valores retornados
    assertNotNull("El resultado no debe ser nulo", resultado);
    assertEquals("El precio del nuevo plan debe ser retornado", (Double) 2000.0, resultado);

    // Verificar invocaciones de mocks
    verify(usuarioPlanMock, times(1)).getFecha_plan_adquirido();
    verify(usuarioPlanMock, times(1)).getFecha_plan_venc();
    verify(planMock, times(1)).getPrecio();

     */
    }

    @Test
    public void testCalcularValidacionUsuarioPlanMasDe15Dias() {
        Date fechaAdquisicion = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(20));
        Date fechaVencimiento = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10));

        when(usuarioPlanMock.getFecha_plan_adquirido()).thenReturn(fechaAdquisicion);
        when(usuarioPlanMock.getFecha_plan_venc()).thenReturn(fechaVencimiento);
        when(planMock.getPrecio()).thenReturn(2000.0);
        when(usuarioPlanMock.getPrecio()).thenReturn(1000.0);

        Double resultado = servicioValidacionPlan.calcularValidacionUsuarioPlan(planMock, usuarioPlanMock);
        assertEquals((Double) 1000.0, resultado);
    }

    @Test
    public void testCalcularValidacionUsuarioPlanPrecioNegativo() {
        Date fechaAdquisicion = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(20));
        Date fechaVencimiento = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10));

        when(usuarioPlanMock.getFecha_plan_adquirido()).thenReturn(fechaAdquisicion);
        when(usuarioPlanMock.getFecha_plan_venc()).thenReturn(fechaVencimiento);
        when(planMock.getPrecio()).thenReturn(1000.0);
        when(usuarioPlanMock.getPrecio()).thenReturn(2000.0);

        Double resultado = servicioValidacionPlan.calcularValidacionUsuarioPlan(planMock, usuarioPlanMock);
        assertEquals((Double) 0.0, resultado);
    }

    @Test
    public void testPuedeRealizarAccionElegirMetaDeLectura() {
        when(usuarioPlanMock.getPlan()).thenReturn(planMock);
        when(planMock.getId()).thenReturn(2L);

        assertTrue(servicioValidacionPlan.puedeRealizarAccion(usuarioPlanMock, Accion.ELEGIR_META_DE_LECTURA));
    }

    @Test
    public void testPuedeRealizarAccionObtenerLogros() {
        when(usuarioPlanMock.getPlan()).thenReturn(planMock);
        when(planMock.getId()).thenReturn(3L);

        assertTrue(servicioValidacionPlan.puedeRealizarAccion(usuarioPlanMock, Accion.OBTENER_LOGROS));
    }

    @Test
    public void testNoPuedeRealizarAccionLeerOtrasResenias() {
        when(usuarioPlanMock.getPlan()).thenReturn(planMock);
        when(planMock.getId()).thenReturn(1L);

        assertFalse(servicioValidacionPlan.puedeRealizarAccion(usuarioPlanMock, Accion.LEER_OTRAS_RESEÑAS));
    }

    @Test
    public void testNoPuedeRealizarAccionNull() {
        assertFalse(servicioValidacionPlan.puedeRealizarAccion(null, Accion.ESCRIBIR_RESEÑAS));
    }
}