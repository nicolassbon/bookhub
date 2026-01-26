package com.tallerwebi.infraestructura.repository;

import com.tallerwebi.dominio.model.Plan;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioPlan;
import com.tallerwebi.dominio.repository.RepositorioPlan;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioPlanImpl implements RepositorioPlan {

    private SessionFactory sessionFactory;

    public RepositorioPlanImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Plan buscarPlanPorId(Long idPlan) {
        Session session = sessionFactory.getCurrentSession();

        return (Plan) session.createCriteria(Plan.class)
                .add(Restrictions.eq("id", idPlan))
                .uniqueResult();
    }

    @Override
    public Plan buscarPlanBronce() {
        Session session = sessionFactory.getCurrentSession();
        return (Plan) session.createCriteria(Plan.class)
                .add(Restrictions.eq("nombre", "BRONCE"))
                .uniqueResult();
    }


}
