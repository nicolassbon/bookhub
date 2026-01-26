package com.tallerwebi.infraestructura.repository;

import com.tallerwebi.dominio.model.TipoNotificacion;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioTipoNotificacionImpl implements RepositorioTipoNotificacion {

    private final SessionFactory sessionFactory;

    @Autowired
    public RepositorioTipoNotificacionImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public TipoNotificacion encontrarTipoNotificacionPorId(Long id) {
        Session session = sessionFactory.getCurrentSession();
        Criteria criteria = session.createCriteria(TipoNotificacion.class);
        criteria.add(Restrictions.eq("id", id));
        return (TipoNotificacion) criteria.uniqueResult();
    }
}
