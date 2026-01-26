package com.tallerwebi.infraestructura.repository;

import com.tallerwebi.dominio.model.Autor;
import com.tallerwebi.dominio.repository.RepositorioAutor;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioAutorImpl implements RepositorioAutor {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioAutorImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Autor buscarAutorPorId(Long id) {
        Session session = sessionFactory.getCurrentSession();
        Criteria autor = session.createCriteria(Autor.class);
        autor.add(Restrictions.eq("id", id));
        return (Autor) autor.uniqueResult();
    }
}
