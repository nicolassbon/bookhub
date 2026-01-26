package com.tallerwebi.infraestructura.repository;


import com.tallerwebi.dominio.model.Notificacion;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioNotificacion;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RepositorioUsuarioNotificacionImpl implements RepositorioUsuarioNotificacion {

    private final SessionFactory sessionFactory;

    @Autowired
    public RepositorioUsuarioNotificacionImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<UsuarioNotificacion> listarNotificacionesPorUsuario(Long usuarioId) {
        Session session = sessionFactory.getCurrentSession();
        Criteria criteria = session.createCriteria(UsuarioNotificacion.class);
        criteria.add(Restrictions.eq("usuario.id", usuarioId));

        return criteria.list();
    }


    @Override
    public void guardar(UsuarioNotificacion usuarioNotificacion) {
        Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(usuarioNotificacion);
    }

    @Override
    public Long obtenerIdAmigo(Long friendId, Long requestId) {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria(UsuarioNotificacion.class);
        criteria.add(Restrictions.eq("usuario.id", friendId));
        criteria.add(Restrictions.eq("notificacion.id", requestId));

        UsuarioNotificacion usuarioNotificacion = (UsuarioNotificacion) criteria.uniqueResult();

        System.out.println(usuarioNotificacion.getUsuario() + " usuarioNotificacion");
        System.out.println(usuarioNotificacion.getId() + " usuarioNotificacion");

        return usuarioNotificacion != null ? usuarioNotificacion.getFriendId() : null;
    }



}