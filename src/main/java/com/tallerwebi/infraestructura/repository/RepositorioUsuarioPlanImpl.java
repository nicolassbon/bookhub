package com.tallerwebi.infraestructura.repository;

import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioPlan;
import com.tallerwebi.dominio.repository.RepositorioUsuarioPlan;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class RepositorioUsuarioPlanImpl implements RepositorioUsuarioPlan {

    private SessionFactory sessionFactory;

    public RepositorioUsuarioPlanImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<UsuarioPlan> obtenerUsuariosPlan() {
        Session session = sessionFactory.getCurrentSession();
        return session.createCriteria(UsuarioPlan.class).list();
    }

    @Override
    public UsuarioPlan buscarPlanPorUsuario(Long idUsuario) {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria(UsuarioPlan.class);
        criteria.add(Restrictions.eq("usuario.id", idUsuario));
        criteria.addOrder(Order.desc("fecha_plan_adquirido"));
        criteria.setMaxResults(1);

        return (UsuarioPlan) criteria.uniqueResult();
    }

    @Override
    public void guardarUsuarioPlan(UsuarioPlan usuarioPlan) {
        Session session = sessionFactory.getCurrentSession();
        session.save(usuarioPlan);
    }

}
