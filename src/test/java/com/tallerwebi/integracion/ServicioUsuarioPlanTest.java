package com.tallerwebi.integracion;

import com.tallerwebi.dominio.excepcion.PlanNoEncontrado;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.model.Plan;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioPlan;
import com.tallerwebi.dominio.repository.RepositorioPlan;
import com.tallerwebi.dominio.repository.RepositorioUsuario;
import com.tallerwebi.dominio.repository.RepositorioUsuarioPlan;
import com.tallerwebi.infraestructura.service.ServicioUsuarioPlanImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

public class ServicioUsuarioPlanTest {
    @Mock
    private RepositorioUsuario repositorioUsuario;

    @Mock
    private RepositorioPlan repositorioPlan;

    @Mock
    private RepositorioUsuarioPlan repositorioUsuarioPlan;

    @InjectMocks
    private ServicioUsuarioPlanImpl servicioUsuarioPlan;

    private UsuarioPlan usuarioPlanMock;
    private Usuario usuarioMock;
    private Plan planMock;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        usuarioPlanMock = mock(UsuarioPlan.class);
        usuarioMock = mock(Usuario.class);
        planMock = mock(Plan.class);
    }

    @Test(expected = UsuarioInexistente.class)
    public void queLanceExcepcionSiElUsuarioNoExiste() throws UsuarioInexistente {
        Long idUsuario = 1L;
        Long idPlan = 1L;

        when(repositorioUsuario.buscarUsuarioPorId(idUsuario)).thenReturn(null);

        servicioUsuarioPlan.actualizarPlanDelUsuarioPlan(idUsuario, idPlan);
    }

    @Test(expected = PlanNoEncontrado.class)
    public void queLanceExcepcionSiElPlanNoExiste() throws PlanNoEncontrado, UsuarioInexistente {
        Long idUsuario = 1L;
        Long idPlan = 1L;

        when(repositorioUsuario.buscarUsuarioPorId(idUsuario)).thenReturn(usuarioMock);
        when(repositorioPlan.buscarPlanPorId(idPlan)).thenReturn(null);

        servicioUsuarioPlan.actualizarPlanDelUsuarioPlan(idUsuario, idPlan);
    }

    @Test
    public void queActualicePlanDelUsuarioPlan_aBronceDesdePlata() throws PlanNoEncontrado, UsuarioInexistente {
        when(repositorioUsuario.buscarUsuarioPorId(anyLong())).thenReturn(usuarioMock);
        when(repositorioPlan.buscarPlanPorId(anyLong())).thenReturn(planMock);
        when(usuarioPlanMock.getPlan()).thenReturn(planMock);
        when(planMock.getNombre()).thenReturn("PLATA", "BRONCE");
        when(repositorioUsuarioPlan.buscarPlanPorUsuario(anyLong())).thenReturn(usuarioPlanMock);
        servicioUsuarioPlan.actualizarPlanDelUsuarioPlan(1L, 2L);
        verify(repositorioUsuarioPlan).guardarUsuarioPlan(any(UsuarioPlan.class));
    }

    @Test
    public void queVerifiquePlanesYActualiceVencidos() {
        UsuarioPlan usuarioPlanMock2 = mock(UsuarioPlan.class);
        Plan planBronce = mock(Plan.class);

        when(usuarioPlanMock.getFecha_plan_venc()).thenReturn(new Date(System.currentTimeMillis() - 100000)); // Fecha pasada
        when(usuarioPlanMock2.getFecha_plan_venc()).thenReturn(new Date(System.currentTimeMillis() + 100000)); // Fecha futura
        when(repositorioUsuarioPlan.obtenerUsuariosPlan()).thenReturn(List.of(usuarioPlanMock, usuarioPlanMock2));
        when(repositorioPlan.buscarPlanBronce()).thenReturn(planBronce);

        servicioUsuarioPlan.verificarPlanDelUsuario();

        verify(usuarioPlanMock).setPlan(planBronce);
        verify(repositorioUsuarioPlan).guardarUsuarioPlan(usuarioPlanMock);
        verify(usuarioPlanMock2, never()).setPlan(planBronce); // Verifica que no se actualiza el segundo plan
    }
}
