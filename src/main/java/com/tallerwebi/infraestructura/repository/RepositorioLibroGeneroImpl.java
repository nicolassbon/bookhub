package com.tallerwebi.infraestructura.repository;

import com.tallerwebi.dominio.model.Genero;
import com.tallerwebi.dominio.model.Libro;
import com.tallerwebi.dominio.model.LibroGenero;
import com.tallerwebi.dominio.repository.RepositorioLibroGenero;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RepositorioLibroGeneroImpl implements RepositorioLibroGenero {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioLibroGeneroImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<LibroGenero> obtenerLibroGeneros() {
        Session session = sessionFactory.getCurrentSession();
        return session.createCriteria(LibroGenero.class).list();
    }

    @Override
    public List<LibroGenero> obtenerLibroPorGenero(Long generoId) {
        Session session = sessionFactory.getCurrentSession();
        Criteria criteria = session.createCriteria(LibroGenero.class);
        criteria.add(Restrictions.eq("genero.id", generoId));
        return criteria.list();
    }

    @Override
    public List<LibroGenero> obtenerGeneros(Libro libro) {
        Session session = sessionFactory.getCurrentSession();
        return session.createCriteria(LibroGenero.class)
                .add(Restrictions.eq("libro", libro))
                .list();
    }


}
