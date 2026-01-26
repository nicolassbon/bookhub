package com.tallerwebi.infraestructura.repository;

import com.tallerwebi.dominio.model.Genero;
import com.tallerwebi.dominio.repository.RepositorioGenero;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class RepositorioGeneroImpl implements RepositorioGenero {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioGeneroImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Genero buscarGeneroPorId(Long id) {
        Session session = sessionFactory.getCurrentSession();
        Criteria genero = session.createCriteria(Genero.class);
        genero.add(Restrictions.eq("id", id));
        return (Genero) genero.uniqueResult();
    }
}


