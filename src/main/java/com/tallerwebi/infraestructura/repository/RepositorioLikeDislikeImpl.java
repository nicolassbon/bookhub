package com.tallerwebi.infraestructura.repository;

import com.tallerwebi.dominio.model.LikeDislike;
import com.tallerwebi.dominio.repository.RepositorioLikeDislike;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RepositorioLikeDislikeImpl implements RepositorioLikeDislike {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioLikeDislikeImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void guardar(LikeDislike likeDislike) {
        Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(likeDislike);
    }

    @Override
    public void eliminar(LikeDislike likeDislike) {
        Session session = sessionFactory.getCurrentSession();
        session.delete(likeDislike);
    }

    @Override
    public LikeDislike obtenerReaccionDelUsuario(Long userId, Long idResenia) {
        Session session = sessionFactory.getCurrentSession();

        return (LikeDislike) session.createCriteria(LikeDislike.class)
                .add(Restrictions.eq("usuario.id", userId))
                .add(Restrictions.eq("resenia.id", idResenia))
                .uniqueResult();
    }

    @Override
    public List<LikeDislike> obtenerLikesResenia(Long idResenia) {
        Session session = sessionFactory.getCurrentSession();

        return session.createCriteria(LikeDislike.class)
                .add(Restrictions.eq("resenia.id", idResenia))
                .add(Restrictions.eq("esLike", true))
                .list();
    }

    @Override
    public List<LikeDislike> obtenerDislikesResenia(Long idResenia) {
        Session session = sessionFactory.getCurrentSession();

        return session.createCriteria(LikeDislike.class)
                .add(Restrictions.eq("resenia.id", idResenia))
                .add(Restrictions.eq("esLike", false))
                .list();
    }
}
