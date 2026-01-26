package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.model.Libro;
import com.tallerwebi.dominio.model.Resenia;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.repository.RepositorioResenia;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import org.hamcrest.MatcherAssert;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.transaction.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SpringWebTestConfig.class, HibernateTestConfig.class})
public class RepositorioReseniaTest {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private RepositorioResenia repositorioResenia;

    @Test
    @Transactional
    @Rollback
    public void queSePuedaGuardarUnaResenia() {
        Usuario usuario = new Usuario();
        sessionFactory.getCurrentSession().save(usuario);

        Libro libro = new Libro();
        libro.setTitulo("Harry Potter");
        sessionFactory.getCurrentSession().save(libro);

        Resenia resenia = new Resenia();
        resenia.setUsuario(usuario);
        resenia.setLibro(libro);
        resenia.setPuntuacion(4);
        resenia.setDescripcion("Muy bueno");
        repositorioResenia.guardar(resenia);

        Long reseniaId = resenia.getId();

        Resenia reseniaEncontrada = repositorioResenia.obtenerReseniaPorId(reseniaId);

        assertThat(reseniaEncontrada, notNullValue());
    }

    @Test
    @Transactional
    @Rollback
    public void queSePuedaObtenerReseniasDeOtrosUsuarios() {
        Usuario usuario = new Usuario();
        sessionFactory.getCurrentSession().save(usuario);

        Libro libro = new Libro();
        libro.setTitulo("Harry Potter");
        sessionFactory.getCurrentSession().save(libro);

        //Resenia de este usuario
        Resenia resenia = new Resenia();
        resenia.setUsuario(usuario);
        resenia.setLibro(libro);
        resenia.setPuntuacion(4);
        resenia.setDescripcion("Muy bueno");
        repositorioResenia.guardar(resenia);

        Usuario usuario2 = new Usuario();
        sessionFactory.getCurrentSession().save(usuario2);

        //Resenia de usuario 2
        Resenia resenia2 = new Resenia();
        resenia2.setUsuario(usuario2);
        resenia2.setLibro(libro);
        resenia2.setPuntuacion(3);
        resenia2.setDescripcion("Muy bueno 2");
        repositorioResenia.guardar(resenia2);

        Usuario usuario3 = new Usuario();
        sessionFactory.getCurrentSession().save(usuario3);

        //Resenia de usuario 2
        Resenia resenia3 = new Resenia();
        resenia3.setUsuario(usuario3);
        resenia3.setLibro(libro);
        resenia3.setPuntuacion(3);
        resenia3.setDescripcion("Muy bueno 3");
        repositorioResenia.guardar(resenia3);

        Long usuarioActual = usuario.getId();

        List<Resenia> reseniasDeOtrosUsuarios = repositorioResenia.obtenerReseniasDeOtrosUsuarios(usuarioActual, libro.getId());

        assertThat(reseniasDeOtrosUsuarios,is(not(empty())));
        assertThat(reseniasDeOtrosUsuarios.size(),is(2));
        assertThat(reseniasDeOtrosUsuarios.get(0).getDescripcion(),equalTo("Muy bueno 2"));
    }

    @Test
    @Transactional
    @Rollback
    public void queSePuedaObtenerReseniasDeUnLibro() {
        Usuario usuario = new Usuario();
        sessionFactory.getCurrentSession().save(usuario);

        Libro libro = new Libro();
        libro.setTitulo("Harry Potter");
        sessionFactory.getCurrentSession().save(libro);

        //Resenia de este usuario
        Resenia resenia = new Resenia();
        resenia.setUsuario(usuario);
        resenia.setLibro(libro);
        resenia.setPuntuacion(4);
        resenia.setDescripcion("Muy bueno");
        repositorioResenia.guardar(resenia);

        Usuario usuario2 = new Usuario();
        sessionFactory.getCurrentSession().save(usuario2);

        //Resenia de usuario 2
        Resenia resenia2 = new Resenia();
        resenia2.setUsuario(usuario2);
        resenia2.setLibro(libro);
        resenia2.setPuntuacion(3);
        resenia2.setDescripcion("Muy bueno 2");
        repositorioResenia.guardar(resenia2);

        //Resenia de otro libro
        Libro libro2 = new Libro();
        libro2.setTitulo("El principito");
        sessionFactory.getCurrentSession().save(libro2);

        Resenia resenia3 = new Resenia();
        resenia3.setUsuario(usuario2);
        resenia3.setLibro(libro2);
        resenia3.setPuntuacion(5);
        resenia3.setDescripcion("El mejor");
        repositorioResenia.guardar(resenia3);

        List<Resenia> reseniasDelLibro = repositorioResenia.obtenerReseniasDelLibro(libro.getId());

        assertThat(reseniasDelLibro,is(not(empty())));
        assertThat(reseniasDelLibro.size(),is(2));
        assertThat(reseniasDelLibro.get(0).getDescripcion(),equalTo("Muy bueno"));
    }

    @Test
    @Transactional
    @Rollback
    public void queSePuedaObtenerReseniaDeUnUsuarioAcercaDeCiertoLibro() {
        Usuario usuario = new Usuario();
        sessionFactory.getCurrentSession().save(usuario);

        Libro libro = new Libro();
        libro.setTitulo("Harry Potter");
        sessionFactory.getCurrentSession().save(libro);

        Resenia resenia = new Resenia();
        resenia.setUsuario(usuario);
        resenia.setLibro(libro);
        resenia.setPuntuacion(4);
        resenia.setDescripcion("Muy bueno");
        repositorioResenia.guardar(resenia);

        Libro libro2 = new Libro();
        libro2.setTitulo("El Principito");
        sessionFactory.getCurrentSession().save(libro2);

        Resenia resenia2 = new Resenia();
        resenia2.setUsuario(usuario);
        resenia2.setLibro(libro2);
        resenia2.setPuntuacion(5);
        resenia2.setDescripcion("Excelente");
        repositorioResenia.guardar(resenia2);

        Resenia reseniaEncontrada = repositorioResenia.obtenerReseniaDelUsuario(usuario.getId(),libro2.getId());

        assertThat(reseniaEncontrada, notNullValue());
        assertThat(reseniaEncontrada.getDescripcion(),equalTo("Excelente"));
        assertThat(reseniaEncontrada.getLibro().getTitulo(),equalTo("El Principito"));
    }




}
