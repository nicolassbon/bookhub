package com.tallerwebi.infraestructura.repository;

import com.tallerwebi.dominio.model.Amistad;
import com.tallerwebi.dominio.model.ComentarioPublicacion;
import com.tallerwebi.dominio.model.Publicacion;
import com.tallerwebi.dominio.repository.RepositorioComentarioPublicacion;
import com.tallerwebi.dominio.repository.RepositorioPublicacion;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class RepositorioComentarioPublicacionImpl implements RepositorioComentarioPublicacion {

    private final SessionFactory sessionFactory;
    private final RepositorioPublicacion repositorioPublicacion;

    @Autowired
    public RepositorioComentarioPublicacionImpl(SessionFactory sessionFactory, RepositorioPublicacion repositorioPublicacion) {
        this.sessionFactory = sessionFactory;
        this.repositorioPublicacion = repositorioPublicacion;
    }


    @Override
    public void guardar(ComentarioPublicacion comentarioPublicacion) {
        Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(comentarioPublicacion);
    }

    @Override
    public List<ComentarioPublicacion> obtenerLosComentariosDeLaPublicacion(Long publicationId) {

        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria(ComentarioPublicacion.class);
        criteria.createAlias("publicacion", "p");
        criteria.add(Restrictions.eq("p.id", publicationId));
        criteria.addOrder(Order.desc("fechaHora"));

        return criteria.list();
    }

}
