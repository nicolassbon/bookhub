package com.tallerwebi.infraestructura.repository;


import com.tallerwebi.dominio.model.*;
import com.tallerwebi.dominio.repository.RepositorioResenia;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class RepositorioReseniaImpl implements RepositorioResenia {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioReseniaImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Resenia obtenerReseniaPorId(Long id) {
        Session session = sessionFactory.getCurrentSession();
        Criteria criteria = session.createCriteria(Resenia.class);
        criteria.add(Restrictions.eq("id", id));
        return (Resenia) criteria.uniqueResult();
    }

    @Override
    public void guardar(Resenia resenia) {
        Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(resenia);
    }

    @Override
    public List<Resenia> obtenerReseniasDeOtrosUsuarios(Long userId, Long idLibro) {
        Session session = sessionFactory.getCurrentSession();

        return session.createCriteria(Resenia.class)
                .createAlias("usuario", "u")
                .add(Restrictions.eq("libro.id", idLibro)) // Traer reseñas de ese libro especifico
                .add(Restrictions.ne("u.id", userId))  // Excluir reseñas del usuario actual
                .setMaxResults(4)  // Limitar a 4 resultados
                .list();
    }

    @Override
    public List<Resenia> obtenerReseniasDelLibro(Long idLibro) {
        Session session = sessionFactory.getCurrentSession();

        return session.createCriteria(Resenia.class)
                .createAlias("libro", "l")
                .add(Restrictions.eq("l.id", idLibro))
                .list();
    }

    @Override
    public Resenia obtenerReseniaDelUsuario(Long userId, Long idLibro) {
        Session session = sessionFactory.getCurrentSession();

        return (Resenia) session.createCriteria(Resenia.class)
                .createAlias("usuario", "u")
                .createAlias("libro", "l")
                .add(Restrictions.eq("u.id", userId))
                .add(Restrictions.eq("l.id", idLibro))
                .uniqueResult();
    }

    @Override
    public List<Resenia> obtenerTodasLasReseniasDelUsuario(Usuario usuario) {
        Session session = sessionFactory.getCurrentSession();

        return session.createCriteria(Resenia.class)
                .add(Restrictions.eq("usuario", usuario))
                .list();
    }

    @Override
    public List<Resenia> obtenerReseniasMasReacciones() {
        Session session = sessionFactory.getCurrentSession();

        Criteria criteria = session.createCriteria(Resenia.class, "resenia");

        // Crear un alias para la relación de reacciones
        criteria.createAlias("reacciones", "reaccion", JoinType.LEFT_OUTER_JOIN);

        // Proyección para contar las reacciones por cada reseña
        criteria.setProjection(Projections.projectionList()
                .add(Projections.groupProperty("id"), "reseniaId")
                .add(Projections.count("reaccion.id"), "cantReacciones")); // Contar reacciones

        // Ordenar por cant de reacciones
        criteria.addOrder(Order.desc("cantReacciones"));

        // Limita los resultados
        criteria.setMaxResults(8);

        // Ejecutar la consulta
        List<Object[]> resultados = criteria.list();

        // Sin esto no funciona
        // Criteria no devuelve las entidades completas -> lista de Object[], donde cada Object[]
        // contiene el id de la reseña y el conteo de reacciones
        List<Resenia> reseniasConMasReacciones = new ArrayList<>();

        // Se obtiene primero solo el id de las reseñas y luego las reseñas completas a partir de esos id
        for (Object[] fila : resultados) {
            // ID de la reseña
            Long reseniaId = (Long) fila[0];

            // Obtener la reseña completa por su ID
            Resenia resenia = session.get(Resenia.class, reseniaId);
            reseniasConMasReacciones.add(resenia);
        }

        return reseniasConMasReacciones;
    }

    @Override
    public List<Resenia> obtenerReseniasPorTituloLibro(String valor) {
        Session session = sessionFactory.getCurrentSession();

        return session.createCriteria(Resenia.class)
                .createAlias("libro", "l")
                .add(Restrictions.ilike("l.titulo", valor, MatchMode.ANYWHERE))
                .list();
    }

    @Override
    public List<Resenia> obtenerReseniasPorUsuario(String valor) {
        Session session = sessionFactory.getCurrentSession();

        return session.createCriteria(Resenia.class)
                .createAlias("usuario", "u")
                .add(Restrictions.disjunction()
                        .add(Restrictions.ilike("u.nombre", valor, MatchMode.ANYWHERE))
                        .add(Restrictions.ilike("u.nombreUsuario", valor, MatchMode.ANYWHERE)))
                .list();
    }

    @Override
    public List<Resenia> obtenerReseniasPorAutorLibro(String valor) {
        Session session = sessionFactory.getCurrentSession();

        return session.createCriteria(Resenia.class)
                .createAlias("libro", "l")
                .add(Restrictions.ilike("l.autor", valor, MatchMode.ANYWHERE))
                .list();
    }
}
