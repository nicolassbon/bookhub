package com.tallerwebi.infraestructura.repository;

import com.tallerwebi.dominio.model.Logro;
import com.tallerwebi.dominio.repository.RepositorioLogro;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RepositorioLogroImpl implements RepositorioLogro {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioLogroImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void guardar(Logro logro) {
        Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(logro);
    }

    @Override
    public Logro buscarPorId(Long id) {
        Session session = sessionFactory.getCurrentSession();
        return (Logro) session.createCriteria(Logro.class)
                .add(Restrictions.eq("id", id))
                .uniqueResult();
    }

    @Override
    public List<Logro> obtenerLogrosPredefinidos() {
        Session session = sessionFactory.getCurrentSession();

        return session.createCriteria(Logro.class)
                .add(Restrictions.eq("esPredefinido", true))
                .list();
    }

    @Override
    public void borrar(Logro logro) {
        Session session = sessionFactory.getCurrentSession();
        session.delete(logro);
    }


}
