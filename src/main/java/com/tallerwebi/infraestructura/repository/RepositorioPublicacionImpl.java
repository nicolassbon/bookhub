package com.tallerwebi.infraestructura.repository;
import com.tallerwebi.dominio.model.*;
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
public class RepositorioPublicacionImpl implements RepositorioPublicacion {

    private final SessionFactory sessionFactory;
    private final RepositorioAmistad repositorioAmistad;

    @Autowired
    public RepositorioPublicacionImpl(SessionFactory sessionFactory, RepositorioAmistad repositorioAmistad) {
        this.sessionFactory = sessionFactory;
        this.repositorioAmistad = repositorioAmistad;
    }


    @Override
    public void guardar(Publicacion publicacion) {
        Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(publicacion);
    }

    @Override
    public List<Publicacion> obtenerTodasPublicacionesDeAmigos(Long userId) {

        Session session = sessionFactory.getCurrentSession();

        List<Amistad> amistades = repositorioAmistad.listarAmigosPorUsuario(userId);
        List<Long> amigosIds = new ArrayList<>();

        for (Amistad amistad : amistades) {
            if (amistad.getAmigo() != null && amistad.getAmigo().getId() != null && !amistad.getAmigo().getId().equals(userId)) {
                amigosIds.add(amistad.getAmigo().getId());
            } else if (amistad.getUsuario() != null && amistad.getUsuario().getId() != null && !amistad.getUsuario().getId().equals(userId)) {
                amigosIds.add(amistad.getUsuario().getId());
            }
        }

        amigosIds.add(userId);

        Criteria criteria = session.createCriteria(Publicacion.class);
        criteria.createAlias("usuario", "u");
        criteria.add(Restrictions.in("u.id", amigosIds));
        criteria.addOrder(Order.desc("fechaHora"));

        return criteria.list();
    }

    @Override
    public Publicacion obtenerPublicacionPorId(Long publicationId) {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria(Publicacion.class);
        criteria.add(Restrictions.eq("id", publicationId));
        criteria.addOrder(Order.desc("fechaHora"));

        return (Publicacion) criteria.uniqueResult();
    }

}