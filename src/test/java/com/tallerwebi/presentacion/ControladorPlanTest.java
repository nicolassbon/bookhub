package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.model.Plan;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioPlan;
import com.tallerwebi.infraestructura.service.*;
import com.tallerwebi.presentacion.controller.ControladorPlanes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

public class ControladorPlanTest {
    ServicioPlan servicioPlan = mock(ServicioPlan.class);
    ServicioUsuario servicioUsuario = mock(ServicioUsuario.class);
    ServicioUsuarioPlan servicioUsuarioPlan = mock(ServicioUsuarioPlan.class);
    ServicioValidacionPlan servicioValidacionPlan = mock(ServicioValidacionPlan.class);
    ServicioMercadoPago servicioMercadoPago = mock(ServicioMercadoPago.class);
    ControladorPlanes controladorPlanes = new ControladorPlanes(servicioPlan, servicioUsuario, servicioUsuarioPlan, servicioValidacionPlan, servicioMercadoPago);

    private HttpServletRequest requestMock;
    private HttpSession sessionMock;

    @BeforeEach
    public void setUp() {
        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);

        ServletRequestAttributes attr = new ServletRequestAttributes(requestMock);
        RequestContextHolder.setRequestAttributes(attr);
    }

    @Test
    public void testMostrar() throws Exception {
        Long userId = 1L;
        when(requestMock.getSession()).thenReturn(sessionMock);
        when(sessionMock.getAttribute("USERID")).thenReturn(userId);
        UsuarioPlan usuarioPlanMock = mock(UsuarioPlan.class);
        when(servicioUsuarioPlan.buscarUsuarioPlan(userId)).thenReturn(usuarioPlanMock);
        when(usuarioPlanMock.getPlan()).thenReturn(mock(Plan.class));

        ModelMap modelMap = new ModelMap();
        String viewName = controladorPlanes.mostrar(modelMap, requestMock);

        assertThat(viewName, equalTo("planes"));
        assertThat(modelMap.get("usuario"), equalTo(usuarioPlanMock));
    }

    @Test
    public void testActualizarPlan() throws UsuarioInexistente {
        Long userId = 1L;
        Long planId = 2L;
        when(requestMock.getSession()).thenReturn(sessionMock);
        when(sessionMock.getAttribute("USERID")).thenReturn(userId);
        Usuario usuarioMock = mock(Usuario.class);
        when(servicioUsuario.buscarUsuarioPorId(userId)).thenReturn(usuarioMock);

        ModelMap modelMap = new ModelMap();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        String viewName = controladorPlanes.actualizarPlan(requestMock, planId, modelMap, redirectAttributes);

        assertThat(viewName, equalTo("redirect:/planes/mostrar"));
        verify(servicioUsuarioPlan, times(1)).actualizarPlanDelUsuarioPlan(userId, planId);
    }

    @Test
    public void testDetalleActualizacion() throws Exception {
        Long userId = 1L;
        Long planId = 2L;

        when(requestMock.getSession()).thenReturn(sessionMock);
        when(sessionMock.getAttribute("USERID")).thenReturn(userId);


        Plan planMock = mock(Plan.class);
        when(planMock.getNombre()).thenReturn("Plan de ejemplo");


        UsuarioPlan usuarioPlanMock = mock(UsuarioPlan.class);
        when(usuarioPlanMock.getPlan()).thenReturn(planMock);

        when(servicioPlan.buscarPlanPorId(planId)).thenReturn(planMock);
        when(servicioUsuarioPlan.buscarUsuarioPlan(userId)).thenReturn(usuarioPlanMock);


        Double validacionDiasMock = 10.0;
        when(servicioValidacionPlan.calcularValidacionUsuarioPlan(planMock, usuarioPlanMock)).thenReturn(validacionDiasMock);

        ModelMap modelMap = new ModelMap();
        ModelAndView mav = controladorPlanes.detalleActualizacion(requestMock, planId, modelMap);


        assertThat(mav.getViewName(), equalTo("detalleActualizacionPlan"));
        assertThat(modelMap.get("plan"), equalTo(planMock));
        assertThat(modelMap.get("usuario"), equalTo(usuarioPlanMock));
        assertThat(modelMap.get("validacionDias"), equalTo(validacionDiasMock));
    }

    @Test
    public void testConfirmarActualizarPlan() {
        Long planId = 1L;
        Model model = mock(Model.class);
        String viewName = controladorPlanes.confirmarActualizarPlan(planId, model);

        assertThat(viewName, equalTo("confirmarActualizarPlan"));
    }
}
