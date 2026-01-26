package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.excepcion.LibroNoEncontrado;
import com.tallerwebi.dominio.excepcion.ListaVacia;
import com.tallerwebi.dominio.excepcion.QueryVacia;
import com.tallerwebi.dominio.model.Libro;
import com.tallerwebi.dominio.repository.RepositorioLibro;
import com.tallerwebi.infraestructura.service.ServicioLibroImpl;
import com.tallerwebi.infraestructura.service.ServicioLibro;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

public class ServicioLibroTest {

    RepositorioLibro repositorioLibro = Mockito.mock(RepositorioLibro.class);
    ServicioLibro servicioLibro = new ServicioLibroImpl(repositorioLibro);

    @Test
    public void siLaQueryDeBusquedaContieneTextoLaBusquedaEsExitosa() throws QueryVacia, ListaVacia {
        //given
        givenExistenLibros();
        when(repositorioLibro.buscar("amor")).thenReturn(new ArrayList<>(
                List.of(new Libro())));
        //when
        Set<Libro>librosObtenidos = whenBuscarLibros("amor");
        //then
        thenLaBusquedaEsExitosa(librosObtenidos);
    }

    @Test
    public void siLaQueryDeBusquedaEstaVaciaArrojaExcepcion() {
        givenExistenLibros();
        String queryVacia = "";
        assertThrows(QueryVacia.class, () -> whenBuscarLibros(queryVacia));
    }

    @Test
    public void siLaListaDeLibrosObtenidosEstaVaciaArrojaExcepcion() {
        givenExistenLibros();
        when(repositorioLibro.buscar("error")).thenReturn(new ArrayList<>());
        assertThrows(ListaVacia.class,() -> whenBuscarLibros("error"));
    }

    @Test
    public void siElLibroExisteSeDevuelveExitosamente(){
        //given
        Long idLibro = 1L;
        Libro libroMock = new Libro();
        libroMock.setId(idLibro);
        when(repositorioLibro.buscarLibroPorId(idLibro)).thenReturn(libroMock);

        //when
        Libro libroObtenido = servicioLibro.obtenerIdLibro(idLibro);

        //then
        assertThat(libroObtenido, notNullValue());
        assertThat(libroObtenido.getId(), equalTo(idLibro));
    }

    @Test
    public void siElLibroNoExisteLanzaExcepcionLibroNoEncontrado(){
        Long idLibro = 1L;
        when(repositorioLibro.buscarLibroPorId(idLibro)).thenReturn(null);

        assertThrows(LibroNoEncontrado.class, () -> servicioLibro.obtenerIdLibro(idLibro));
    }

    @Test
    public void siElLibroSePuedeActualizarSeDevuelveExitosamente(){
        //given
        Long idLibro = 1L;
        Libro libroMock = new Libro();
        libroMock.setId(idLibro);
        libroMock.setTitulo("amor");

        //when
        servicioLibro.actualizarLibro(libroMock);

        //then
        Mockito.verify(repositorioLibro).actualizarLibro(libroMock);
    }

    @Test
    public void deberiaRetornarDosLibrosRandom() {
        //given
        Libro libro1 = new Libro();
        libro1.setId(1L);
        libro1.setTitulo("Libro 1");

        Libro libro2 = new Libro();
        libro2.setId(2L);
        libro2.setTitulo("Libro 2");

        List<Libro> librosMock = List.of(libro1, libro2);
        when(repositorioLibro.buscarDosLibrosRandom()).thenReturn(librosMock);

        List<Libro> librosObtenidos = servicioLibro.obtenerDosLibrosRandom();

        assertThat(librosObtenidos, notNullValue());
        assertThat(librosObtenidos.size(), equalTo(2));
        assertThat(librosObtenidos, equalTo(librosMock));
        Mockito.verify(repositorioLibro, times(1)).buscarDosLibrosRandom();
    }



    private void givenExistenLibros() {
    }

    private Set<Libro> whenBuscarLibros(String query) throws QueryVacia, ListaVacia {
        return servicioLibro.buscar(query);
    }

    private void thenLaBusquedaEsExitosa(Set<Libro> librosObtenidos) {
        assertThat(librosObtenidos, notNullValue());
    }

}
