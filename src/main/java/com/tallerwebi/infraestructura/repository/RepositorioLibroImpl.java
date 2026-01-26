package com.tallerwebi.infraestructura.repository;

import com.tallerwebi.dominio.excepcion.LibroNoEncontrado;
import com.tallerwebi.dominio.model.Libro;
import com.tallerwebi.dominio.repository.RepositorioLibro;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.OrderBy;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class RepositorioLibroImpl implements RepositorioLibro {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioLibroImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Libro> buscar(String query) {
        Session session = sessionFactory.getCurrentSession();
        return session.createCriteria(Libro.class)
                .add(Restrictions.disjunction()
                        .add(Restrictions.ilike("titulo", query, MatchMode.ANYWHERE))
                        .add(Restrictions.ilike("autor", query, MatchMode.ANYWHERE)))
                .list();
    }

    @Override
    public Libro buscarLibroPorId(Long id) {
        Session session = sessionFactory.getCurrentSession();
        Criteria libro = session.createCriteria(Libro.class);
        libro.add(Restrictions.eq("id", id));
        return (Libro) libro.uniqueResult();
    }


    @Override
    public List<Libro> buscarDosLibrosRandom() {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria(Libro.class);
        List<Libro> allBooks = criteria.list();

        Collections.shuffle(allBooks);

        return allBooks.stream().limit(2).collect(Collectors.toList());
    }





    @Override
    public void actualizarLibro(Libro libro) {
        sessionFactory.getCurrentSession().saveOrUpdate(libro);
    }

}
