package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.model.Libro;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioLibro;
import com.tallerwebi.dominio.repository.RepositorioLibro;
import com.tallerwebi.dominio.repository.RepositorioUsuarioLibro;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SpringWebTestConfig.class, HibernateTestConfig.class})
public class RepositorioUsuarioLibroTest {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private RepositorioLibro repositorioLibro;

    @Autowired
    private RepositorioUsuarioLibro repositorioUsuarioLibro;

    @Test
    @Transactional
    @Rollback
    public void queSePuedaGuardarYEncontrarUsuarioLibro() {
        // Creo y guardo un usuario de prueba
        Usuario usuario = new Usuario();
        usuario.setId(2L);
        sessionFactory.getCurrentSession().save(usuario); // Guardo usuario

        // Creo y guardo un libro de prueba
        Libro libro = new Libro();
        libro.setId(1L);
        sessionFactory.getCurrentSession().save(libro); // Guardo libro

        // Creo un nuevo objeto UsuarioLibro
        UsuarioLibro usuarioLibro = new UsuarioLibro();
        usuarioLibro.setUsuario(usuario);
        usuarioLibro.setLibro(libro);
        usuarioLibro.setEstadoDeLectura("Leyendo");

        // Guardo el objeto en la base de datos
        repositorioUsuarioLibro.guardar(usuarioLibro);

        // Busco el objeto por usuario y libro
        UsuarioLibro encontrado = repositorioUsuarioLibro.encontrarUsuarioIdYLibroId(usuario.getId(), libro.getId());

        // Verifico que el objeto encontrado no es nulo
        assertThat(encontrado, is(notNullValue()));
        // Verifico que los IDs coinciden
        assertThat(encontrado.getUsuario().getId(), is(usuario.getId()));
        assertThat(encontrado.getLibro().getId(), is(libro.getId()));
    }

    @Test
    @Transactional
    @Rollback
    public void queSePuedaBuscarPorEstadoDeLectura() {
        Usuario usuario = new Usuario();
        usuario.setId(2L);
        sessionFactory.getCurrentSession().save(usuario);

        Libro libro1 = new Libro();
        libro1.setId(1L);
        sessionFactory.getCurrentSession().save(libro1);

        Libro libro2 = new Libro();
        libro2.setId(2L);
        sessionFactory.getCurrentSession().save(libro2);

        UsuarioLibro usuarioLibro1 = new UsuarioLibro();
        usuarioLibro1.setUsuario(usuario);
        usuarioLibro1.setLibro(libro1);
        usuarioLibro1.setEstadoDeLectura("Leyendo");
        repositorioUsuarioLibro.guardar(usuarioLibro1);

        UsuarioLibro usuarioLibro2 = new UsuarioLibro();
        usuarioLibro2.setUsuario(usuario);
        usuarioLibro2.setLibro(libro2);
        usuarioLibro2.setEstadoDeLectura("Leído");
        repositorioUsuarioLibro.guardar(usuarioLibro2);

        // Buscar libros con estado de lectura "Leyendo"
        List<UsuarioLibro> libros = repositorioUsuarioLibro.buscarPorEstadoDeLectura("Leyendo", usuario);

        // Verificar el tamaño de la lista
        assertThat(libros.size(), is(1));

        // Verificar que el libro recuperado es el correcto
        assertThat(libros.get(0).getLibro().getId(), is(libro1.getId()));

        // Verificar el estado de lectura del primer resultado
        assertThat(libros.get(0).getEstadoDeLectura(), is("Leyendo"));
    }

    @Test
    @Transactional
    @Rollback
    public void queSePuedaBuscarLibrosPorId() {
        // Creo y guardo un usuario de prueba
        Usuario usuario = new Usuario();
        usuario.setId(2L);
        sessionFactory.getCurrentSession().save(usuario); // Guardo usuario

        // Creo y guardo un libro de prueba
        Libro libro = new Libro();
        libro.setId(1L);
        sessionFactory.getCurrentSession().save(libro); // Guardo libro

        // Creo y guardo un segundo libro de prueba
        Libro otroLibro = new Libro();
        otroLibro.setId(2L);
        sessionFactory.getCurrentSession().save(otroLibro); // Guardo otro libro

        // Creo dos registros de UsuarioLibro con el mismo libro pero diferente usuario o estado
        UsuarioLibro usuarioLibro1 = new UsuarioLibro();
        usuarioLibro1.setUsuario(usuario);
        usuarioLibro1.setLibro(libro);
        usuarioLibro1.setEstadoDeLectura("Leyendo");
        repositorioUsuarioLibro.guardar(usuarioLibro1);

        UsuarioLibro usuarioLibro2 = new UsuarioLibro();
        usuarioLibro2.setUsuario(usuario);
        usuarioLibro2.setLibro(otroLibro);
        usuarioLibro2.setEstadoDeLectura("Leído");
        repositorioUsuarioLibro.guardar(usuarioLibro2);

        List<UsuarioLibro> resultados = repositorioUsuarioLibro.buscarLibroPorId(libro.getId());

        // Verifico que se recupera solo un registro con el id del libro especificado
        assertThat(resultados, hasSize(1)); // Verifico que solo hay un resultado
        assertThat(resultados.get(0).getLibro().getId(), is(libro.getId())); // Verifico que el libro es el correcto


    }
}
