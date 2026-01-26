package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.model.Libro;
import com.tallerwebi.dominio.model.Resenia;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioLibro;
import com.tallerwebi.dominio.repository.RepositorioResenia;
import com.tallerwebi.infraestructura.service.ServicioReseniaImpl;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

public class ServicioReseniaTest {

    private RepositorioResenia repositorioResenia = Mockito.mock(RepositorioResenia.class);

    private ServicioReseniaImpl servicioResenia = new ServicioReseniaImpl(repositorioResenia,null,null,null);

    @Test
    public void siElLibroTienePuntuacionesCalculaElPromedioCorrectamente() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        Usuario usuario2 = new Usuario();
        usuario2.setId(2L);
        Usuario usuario3 = new Usuario();
        usuario3.setId(3L);

        Libro libro = new Libro();
        libro.setId(1L);

        Resenia resenia = new Resenia();
        resenia.setLibro(libro);
        resenia.setUsuario(usuario);
        resenia.setPuntuacion(4);

        Resenia resenia2 = new Resenia();
        resenia2.setLibro(libro);
        resenia2.setUsuario(usuario2);
        resenia2.setPuntuacion(5);

        Resenia resenia3 = new Resenia();
        resenia3.setLibro(libro);
        resenia3.setUsuario(usuario3);
        resenia3.setPuntuacion(3);

        List<Resenia> resenias = new ArrayList<>();
        resenias.add(resenia);
        resenias.add(resenia2);
        resenias.add(resenia3);

        when(repositorioResenia.obtenerReseniasDelLibro(libro.getId())).thenReturn(resenias);

        // Cuando
        Double promedio = servicioResenia.calcularPromedioPuntuacion(libro.getId());

        // Entonces
        assertThat(promedio, equalTo(4.0));  // Promedio: (4+5+3)/3 = 4.0
    }

    @Test
    public void siNoHayPuntuacionesDevuelveCero() {
        Long idLibro = 1L;
        List<Resenia> resenias = new ArrayList<>();

        when(repositorioResenia.obtenerReseniasDelLibro(idLibro)).thenReturn(resenias);

        Double promedio = servicioResenia.calcularPromedioPuntuacion(idLibro);

        assertThat(promedio, is(equalTo(0.0)));
    }
}
