package com.tallerwebi.infraestructura;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.Assert.*;

import com.tallerwebi.dominio.model.UsuarioPlan;
import com.tallerwebi.infraestructura.repository.RepositorioUsuarioPlanImpl;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Order;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class RepositorioUsuarioPlanTest {
    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Criteria criteria;

    @InjectMocks
    private RepositorioUsuarioPlanImpl repositorioUsuarioPlan;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(sessionFactory.getCurrentSession()).thenReturn(session);
        when(session.createCriteria(UsuarioPlan.class)).thenReturn(criteria);
    }

    @Test
    public void testObtenerUsuariosPlan() {
        List<UsuarioPlan> listaUsuariosPlanMock = List.of(mock(UsuarioPlan.class), mock(UsuarioPlan.class));
        when(criteria.list()).thenReturn(listaUsuariosPlanMock);

        List<UsuarioPlan> result = repositorioUsuarioPlan.obtenerUsuariosPlan();

        verify(session).createCriteria(UsuarioPlan.class);
        verify(criteria).list();
        assertEquals(listaUsuariosPlanMock, result);
    }

    @Test
    public void testBuscarPlanPorUsuario() {
        UsuarioPlan usuarioPlanMock = mock(UsuarioPlan.class);
        when(criteria.uniqueResult()).thenReturn(usuarioPlanMock);
        when(criteria.add(any(org.hibernate.criterion.Criterion.class))).thenReturn(criteria);
        when(criteria.addOrder(any(Order.class))).thenReturn(criteria);
        when(criteria.setMaxResults(anyInt())).thenReturn(criteria);

        UsuarioPlan result = repositorioUsuarioPlan.buscarPlanPorUsuario(1L);

        verify(session).createCriteria(UsuarioPlan.class);
        verify(criteria).add(any(org.hibernate.criterion.Criterion.class)); // Acepta cualquier Restrictions
        verify(criteria).addOrder(any(Order.class));
        verify(criteria).setMaxResults(eq(1));
        verify(criteria).uniqueResult();
        assertEquals(usuarioPlanMock, result);
    }

    @Test
    public void testGuardarUsuarioPlan() {
        UsuarioPlan usuarioPlanMock = mock(UsuarioPlan.class);

        repositorioUsuarioPlan.guardarUsuarioPlan(usuarioPlanMock);

        verify(session).save(usuarioPlanMock);
    }

}
