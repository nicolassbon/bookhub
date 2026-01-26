package com.tallerwebi.integracion;

import com.tallerwebi.dominio.excepcion.ListaVacia;
import com.tallerwebi.dominio.model.Libro;
import com.tallerwebi.dominio.model.LibroGenero;
import com.tallerwebi.dominio.repository.RepositorioLibroGenero;
import com.tallerwebi.infraestructura.service.ServicioLibroGeneroImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ServicioLibroGeneroTest {

    @Mock
    private RepositorioLibroGenero repositorioLibroGenero;

    @InjectMocks
    private ServicioLibroGeneroImpl servicioLibroGenero;

    @Before
    public void setup() {
        repositorioLibroGenero = mock(RepositorioLibroGenero.class);
        servicioLibroGenero = new ServicioLibroGeneroImpl(repositorioLibroGenero);
    }

    @Test
    public void deberiaRetornarLibrosPorGenero() {
        Long generoId = 1L;
        LibroGenero libroGenero1 = new LibroGenero();
        LibroGenero libroGenero2 = new LibroGenero();
        List<LibroGenero> listaEsperada = Arrays.asList(libroGenero1, libroGenero2);

        when(repositorioLibroGenero.obtenerLibroPorGenero(generoId)).thenReturn(listaEsperada);

        List<LibroGenero> resultado = servicioLibroGenero.obtenerLibroPorGenero(generoId);

        verify(repositorioLibroGenero, times(1)).obtenerLibroPorGenero(generoId);
        assertEquals(listaEsperada, resultado);
    }

    @Test
    public void deberiaRetornarGenerosDeLibro() throws ListaVacia {
        Libro libro = new Libro();
        LibroGenero genero1 = new LibroGenero();
        LibroGenero genero2 = new LibroGenero();
        List<LibroGenero> listaEsperada = Arrays.asList(genero1, genero2);

        when(repositorioLibroGenero.obtenerGeneros(libro)).thenReturn(listaEsperada);

        List<LibroGenero> resultado = servicioLibroGenero.obtenerGeneros(libro);

        verify(repositorioLibroGenero, times(1)).obtenerGeneros(libro);
        assertEquals(listaEsperada, resultado);
    }

    @Test
    public void deberiaLanzarExcepcionSiLibroNoTieneGeneros() {
        Libro libro = new Libro();

        when(repositorioLibroGenero.obtenerGeneros(libro)).thenReturn(Collections.emptyList());

        ListaVacia excepcion = assertThrows(ListaVacia.class, () -> servicioLibroGenero.obtenerGeneros(libro));

        assertEquals("El libro no tiene generos asignados en la BD", excepcion.getMessage());
        verify(repositorioLibroGenero, times(1)).obtenerGeneros(libro);
    }
}
