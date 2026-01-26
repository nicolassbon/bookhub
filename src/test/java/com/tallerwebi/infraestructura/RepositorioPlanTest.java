package com.tallerwebi.infraestructura;
import com.tallerwebi.dominio.model.Plan;
import com.tallerwebi.dominio.repository.RepositorioPlan;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SpringWebTestConfig.class, HibernateTestConfig.class})
public class RepositorioPlanTest {

    @Autowired
    SessionFactory sessionFactory;

    @Autowired
    RepositorioPlan repositorioPlan;

    @Test
    @Transactional
    @Rollback
    public void puedoBuscarPlanPorId() {
        Plan plan = new Plan();
        plan.setNombre("ORO");
        sessionFactory.getCurrentSession().save(plan);


        Plan planObtenido = repositorioPlan.buscarPlanPorId(plan.getId());

        assertThat(planObtenido, is(notNullValue()));
        assertThat(planObtenido.getNombre(), equalTo("ORO"));
    }

    @Test
    @Transactional
    @Rollback
    public void puedoBuscarPlanBronce() {
        Plan planBronce = new Plan();
        planBronce.setNombre("BRONCE");
        sessionFactory.getCurrentSession().save(planBronce);

        Plan planOro = new Plan();
        planOro.setNombre("ORO");
        sessionFactory.getCurrentSession().save(planOro);

        Plan planObtenido = repositorioPlan.buscarPlanBronce();

        assertThat(planObtenido, is(notNullValue()));
        assertThat(planObtenido.getNombre(), equalTo("BRONCE"));
    }

}
