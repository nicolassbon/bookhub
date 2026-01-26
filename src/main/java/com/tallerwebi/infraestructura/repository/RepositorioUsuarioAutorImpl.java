package com.tallerwebi.infraestructura.repository;

import com.tallerwebi.dominio.model.UsuarioAutor;

import com.tallerwebi.dominio.repository.RepositorioUsuarioAutor;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioUsuarioAutorImpl implements RepositorioUsuarioAutor {
    public final SessionFactory sessionFactory;

    @Autowired
    public RepositorioUsuarioAutorImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public UsuarioAutor encontrarUsuarioIdYAutorId(Long usuarioId, Long autorId) {
        Session session = sessionFactory.getCurrentSession();
        Criteria criteria = session.createCriteria(UsuarioAutor.class);


        criteria.add(Restrictions.eq("usuario.id", usuarioId));
        criteria.add(Restrictions.eq("autor.id", autorId));

        return (UsuarioAutor) criteria.uniqueResult();
    }

    @Override
    public void guardar(UsuarioAutor usuarioAutor) {
        Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(usuarioAutor);
    }
}
