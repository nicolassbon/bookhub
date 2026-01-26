package com.tallerwebi.infraestructura.repository;

import com.tallerwebi.dominio.model.Autor;
import com.tallerwebi.dominio.model.Genero;
import com.tallerwebi.dominio.repository.RepositorioOnboarding;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.hibernate.SessionFactory;

import java.util.List;

@Repository("repositorioOnboarding")
public class RepositorioOnboardingImpl implements RepositorioOnboarding {

    private final SessionFactory sessionFactory;

    @Autowired
    public RepositorioOnboardingImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Genero> obtenerGeneros() {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Genero", Genero.class)
                .list();
    }

    @Override
    public List<Autor> obtenerAutores() {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Autor", Autor.class)
                .list();
    }

    @Override
    public Genero obtenerGeneroPorId(Long id) {
        Session session = sessionFactory.getCurrentSession();
        Criteria genero = session.createCriteria(Genero.class);
        genero.add(Restrictions.eq("id", id));
        Genero generoEncontrado = (Genero) genero.uniqueResult();
        return generoEncontrado;
    }

    @Override
    public Autor obtenerAutorPorId(Long id) {
        Session session = sessionFactory.getCurrentSession();
        Criteria autor = session.createCriteria(Autor.class);
        autor.add(Restrictions.eq("id", id));
        Autor autorEncontrado = (Autor) autor.uniqueResult();
        return autorEncontrado;
    }
}

