package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.excepcion.LibroNoEncontrado;
import com.tallerwebi.dominio.excepcion.PaginasExcedidas;
import com.tallerwebi.dominio.model.Libro;
import com.tallerwebi.dominio.model.UsuarioLibro;
import com.tallerwebi.dominio.repository.RepositorioLibro;
import com.tallerwebi.dominio.repository.RepositorioUsuarioLibro;
import com.tallerwebi.infraestructura.service.ServicioUsuarioLibroImpl;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.hamcrest.Matchers.is;

public class ServicioUsuarioLibroTest {

    private RepositorioUsuarioLibro repositorioUsuarioLibro = Mockito.mock(RepositorioUsuarioLibro.class);
    private RepositorioLibro repositorioLibro = Mockito.mock(RepositorioLibro.class);

    private ServicioUsuarioLibroImpl servicioUsuarioLibro = new ServicioUsuarioLibroImpl(repositorioUsuarioLibro, null, repositorioLibro);

    @Test
    public void siElLibroTienePuntuacionesCalculaElPromedioCorrectamente() {
        // Dado
        Long libroId = 1L;
        List<UsuarioLibro> usuariosLibro = new ArrayList<>();

        UsuarioLibro usuarioLibro1 = new UsuarioLibro();
        usuarioLibro1.setPuntuacion(4);
        usuariosLibro.add(usuarioLibro1);

        UsuarioLibro usuarioLibro2 = new UsuarioLibro();
        usuarioLibro2.setPuntuacion(5);
        usuariosLibro.add(usuarioLibro2);

        UsuarioLibro usuarioLibro3 = new UsuarioLibro();
        usuarioLibro3.setPuntuacion(3);
        usuariosLibro.add(usuarioLibro3);

        when(repositorioUsuarioLibro.buscarLibroPorId(libroId)).thenReturn(usuariosLibro);

        // Cuando
        Double promedio = servicioUsuarioLibro.calcularPromedioDePuntuacion(libroId);

        // Entonces
        assertThat(promedio, equalTo(4.0));  // Promedio: (4+5+3)/3 = 4.0
    }

    @Test
    public void siNoHayPuntuacionesDevuelveCero() {
        // Dado
        Long libroId = 1L;
        List<UsuarioLibro> usuariosLibro = new ArrayList<>();

        when(repositorioUsuarioLibro.buscarLibroPorId(libroId)).thenReturn(usuariosLibro);

        // Cuando
        Double promedio = servicioUsuarioLibro.calcularPromedioDePuntuacion(libroId);

        // Entonces
        assertThat(promedio, is(equalTo(0.0)));  // Esperamos que devuelva 0.0
    }

    @Test
    public void siTodosLosUsuariosTienenPuntuacionNulaDevuelveCero() {
        // Dado
        Long libroId = 1L;
        List<UsuarioLibro> usuariosLibro = new ArrayList<>();

        UsuarioLibro usuarioLibro1 = new UsuarioLibro();
        usuarioLibro1.setPuntuacion(null);
        usuariosLibro.add(usuarioLibro1);

        UsuarioLibro usuarioLibro2 = new UsuarioLibro();
        usuarioLibro2.setPuntuacion(null);
        usuariosLibro.add(usuarioLibro2);

        when(repositorioUsuarioLibro.buscarLibroPorId(libroId)).thenReturn(usuariosLibro);

        // Cuando
        Double promedio = servicioUsuarioLibro.calcularPromedioDePuntuacion(libroId);

        // Entonces
        assertThat(promedio, is(equalTo(0.0)));  // Esperamos que devuelva 0.0
    }

    @Test(expected = PaginasExcedidas.class)
    public void cuandoLasPaginasLeidasExcedenLasDelLibroDebeLanzarPaginasExcedidas() throws PaginasExcedidas {
        // given
        Long usuarioId = 1L;
        Long libroId = 1L;
        Integer paginasLeidas = 150;


        Libro libro = new Libro();
        libro.setId(libroId);
        libro.setTitulo("Libro de ejemplo");
        libro.setCantidadDePaginas(100);


        when(repositorioLibro.buscarLibroPorId(libroId)).thenReturn(libro);


        UsuarioLibro usuarioLibro = new UsuarioLibro();
        when(repositorioUsuarioLibro.encontrarUsuarioIdYLibroId(usuarioId, libroId)).thenReturn(usuarioLibro);

        // when
        servicioUsuarioLibro.actualizarPaginasLeidas(usuarioId, libroId, paginasLeidas);


    }

    @Test
    public void cuandoLasPaginasLeidasSonValidasDebeActualizarCorrectamente() throws PaginasExcedidas {
        //given
        Long usuarioId = 1L;
        Long libroId = 1L;
        Integer paginasLeidas = 80;


        Libro libro = new Libro();
        libro.setId(libroId);
        libro.setTitulo("Libro de ejemplo");
        libro.setCantidadDePaginas(100);


        when(repositorioLibro.buscarLibroPorId(libroId)).thenReturn(libro);


        UsuarioLibro usuarioLibro = new UsuarioLibro();
        when(repositorioUsuarioLibro.encontrarUsuarioIdYLibroId(usuarioId, libroId)).thenReturn(usuarioLibro);

        //when
        servicioUsuarioLibro.actualizarPaginasLeidas(usuarioId, libroId, paginasLeidas);

        //then
        assertEquals(paginasLeidas, usuarioLibro.getCantidadDePaginas());
    }

    @Test
    public void cuandoElProgresoEsValidoDevuelvePorcentajeCorrecto() {
        // given
        Long usuarioId = 1L;
        Long libroId = 1L;
        Integer paginasLeidas = 50;

        Libro libro = new Libro();
        libro.setId(libroId);
        libro.setTitulo("Libro de ejemplo");
        libro.setCantidadDePaginas(100);


        when(repositorioLibro.buscarLibroPorId(libroId)).thenReturn(libro);

        UsuarioLibro usuarioLibro = new UsuarioLibro();
        when(repositorioUsuarioLibro.encontrarUsuarioIdYLibroId(usuarioId, libroId)).thenReturn(usuarioLibro);

        // Cuando
        Double progreso = servicioUsuarioLibro.calcularProgresoDeLectura(usuarioId, libroId, paginasLeidas);

        // Entonces
        assertThat(progreso, equalTo(50.0));  // 50 páginas leídas de 100 => 50% de progreso
    }

    @Test
    public void cuandoElProgresoExcedeElMaximoDevuelve100Porciento() {
        // given
        Long usuarioId = 1L;
        Long libroId = 1L;
        Integer paginasLeidas = 150;

        Libro libro = new Libro();
        libro.setId(libroId);
        libro.setTitulo("Libro de ejemplo");
        libro.setCantidadDePaginas(100); // Total de páginas


        when(repositorioLibro.buscarLibroPorId(libroId)).thenReturn(libro);

        UsuarioLibro usuarioLibro = new UsuarioLibro();
        when(repositorioUsuarioLibro.encontrarUsuarioIdYLibroId(usuarioId, libroId)).thenReturn(usuarioLibro);

        // when
        Double progreso = servicioUsuarioLibro.calcularProgresoDeLectura(usuarioId, libroId, paginasLeidas);

        // then
        assertThat(progreso, equalTo(100.0));  // El progreso no puede exceder el 100%
    }

    @Test
    public void cuandoNoHayPaginasLeidasDevuelveCero() {
        // Dado
        Long usuarioId = 1L;
        Long libroId = 1L;
        Integer paginasLeidas = 0;

        Libro libro = new Libro();
        libro.setId(libroId);
        libro.setTitulo("Libro de ejemplo");
        libro.setCantidadDePaginas(100); // Total de páginas

        // Mock de los repositorios
        when(repositorioLibro.buscarLibroPorId(libroId)).thenReturn(libro);

        UsuarioLibro usuarioLibro = new UsuarioLibro();
        when(repositorioUsuarioLibro.encontrarUsuarioIdYLibroId(usuarioId, libroId)).thenReturn(usuarioLibro);

        // Cuando
        Double progreso = servicioUsuarioLibro.calcularProgresoDeLectura(usuarioId, libroId, paginasLeidas);

        // Entonces
        assertThat(progreso, equalTo(0.0));  // 0 páginas leídas => 0% de progreso
    }

    @Test
    public void cuandoElLibroNoExistaDevuelveCero() {
        // given
        Long usuarioId = 1L;
        Long libroId = 999L;  // ID de libro que no existe
        Integer paginasLeidas = 50;


        when(repositorioLibro.buscarLibroPorId(libroId)).thenReturn(null);  // El libro no existe

        // when
        Double progreso = servicioUsuarioLibro.calcularProgresoDeLectura(usuarioId, libroId, paginasLeidas);

        // then
        assertThat(progreso, equalTo(0.0));  // Como el libro no existe, el progreso será 0.0
    }
}
