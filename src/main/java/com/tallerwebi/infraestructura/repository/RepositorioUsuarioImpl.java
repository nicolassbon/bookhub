package com.tallerwebi.infraestructura.repository;

import com.tallerwebi.dominio.model.*;
import com.tallerwebi.dominio.repository.RepositorioUsuario;
import com.tallerwebi.dominio.model.Usuario;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Repository("repositorioUsuario")
public class RepositorioUsuarioImpl implements RepositorioUsuario {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioUsuarioImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Usuario buscarUsuario(String email, String password) {

        final Session session = sessionFactory.getCurrentSession();
        return (Usuario) session.createCriteria(Usuario.class)
                .add(Restrictions.eq("email", email))
                .add(Restrictions.eq("password", password))
                .uniqueResult();
    }

    @Override
    public List<Usuario> buscarUsuariosPorQuery(String query) {
        Session session = sessionFactory.getCurrentSession();
        return session.createCriteria(Usuario.class)
                .add(Restrictions.disjunction()
                        .add(Restrictions.ilike("nombreUsuario", query, MatchMode.ANYWHERE))
                        .add(Restrictions.ilike("nombre", query, MatchMode.ANYWHERE)))
                .list();
    }

    @Override
    public void guardar(String email, String password, String nombreUsuario, String nombre, LocalDate fechaNacimiento, Plan plan) {
        Session session = sessionFactory.getCurrentSession();
        Usuario usuario = new Usuario();

        usuario.setEmail(email);
        usuario.setPassword(password);
        usuario.setNombreUsuario(nombreUsuario);
        usuario.setNombre(nombre);
        usuario.setFechaNacimiento(fechaNacimiento);
        usuario.setRol("Usuario");
        usuario.setActivo(true);
        usuario.setPlan(plan);

        session.save(usuario);
    }

    @Override
    public Usuario buscar(String email) {
        return (Usuario) sessionFactory.getCurrentSession()
                .createCriteria(Usuario.class)
                .add(Restrictions.eq("email", email))
                .uniqueResult();
    }

    @Override
    public void modificar(Usuario usuario) {
        sessionFactory.getCurrentSession().update(usuario);
    }

    @Override
    public Usuario buscarUsuarioPorId(Long id) {
        Session session = sessionFactory.getCurrentSession();
        Criteria usuario = session.createCriteria(Usuario.class);
        usuario.add(Restrictions.eq("id", id));
        Usuario usuarioEncontrado = (Usuario) usuario.uniqueResult();
        return usuarioEncontrado;
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void guardarTokenDeRecuperacion(Usuario usuario, String token) {
        usuario.setTokenRecuperacion(token);
        entityManager.merge(usuario);
    }

//    @Override
//    public void guardarGeneros(Long usuarioId, List<Long> generos) {
//        Usuario usuario = buscarUsuarioPorId(usuarioId);
//
//        for (Long generoId : generos) {
//            Genero genero = repositorioOnboarding.obtenerGeneroPorId(generoId);
//            usuario.setGenero(genero);
//            guardarUsuario(usuario);
//        }
//    }

    @Override
    public void guardarUsuario(Usuario usuario) {
        Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(usuario);
    }

    @Override
    public void guardarUsuarioOnboarding(Usuario usuario) {
        sessionFactory.getCurrentSession().save(usuario);
    }

    @Override
    public Usuario buscarPorEmail(String email) {
        Session session = sessionFactory.getCurrentSession();
        return (Usuario) session.createCriteria(Usuario.class)
                .add(Restrictions.eq("email", email))
                .uniqueResult();
    }

    @Override
    public Usuario buscarPorNombreUsuario(String nombreUsuario) {
        Session session = sessionFactory.getCurrentSession();
        return (Usuario) session.createCriteria(Usuario.class)
                .add(Restrictions.eq("nombreUsuario", nombreUsuario))
                .uniqueResult();
    }

    @Override
    public List<Usuario> obtenerUsuarios() {
        Session session = sessionFactory.getCurrentSession();
        return session.createCriteria(Usuario.class).list();
    }

    @Override
    public List<Usuario> obtenerUsuariosDesafio(Long userId) {
        Session session = sessionFactory.getCurrentSession();

        // Cantidad total de usuarios que cumplen la condicion, excluyendo el usuario actual
        Long count = (Long) session.createCriteria(Usuario.class)
                .add(Restrictions.isNotNull("meta"))
                .add(Restrictions.ne("id", userId))
                .setProjection(Projections.rowCount())
                .uniqueResult();

        // Verificar si el conteo es mayor que 0
        if (count == null || count == 0) {
            // Retorna una lista vacía si no hay usuarios que cumplan la condición
            return Collections.emptyList();
        }

        // Genera un indice de arranque aleatorio
        int randomIndex = new Random().nextInt(count.intValue());

        return session.createCriteria(Usuario.class)
                .add(Restrictions.isNotNull("meta"))
                .add(Restrictions.ne("id", userId))
                .setFirstResult(randomIndex) // Empieza en un indice aleatorio
                .setMaxResults(4)
                .list();
    }

}

