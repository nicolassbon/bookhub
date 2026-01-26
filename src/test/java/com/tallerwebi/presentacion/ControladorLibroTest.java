package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.excepcion.LibroNoEncontrado;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.model.Libro;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioLibro;
import com.tallerwebi.infraestructura.service.*;
import com.tallerwebi.dominio.excepcion.ListaVacia;
import com.tallerwebi.dominio.excepcion.QueryVacia;
import com.tallerwebi.presentacion.controller.ControladorLibro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.Mockito.*;

public class ControladorLibroTest {

    ServicioLibro servicioLibro = mock(ServicioLibro.class);
    ServicioUsuario servicioUsuario = mock(ServicioUsuario.class);
    ServicioUsuarioLibro servicioUsuarioLibro = mock(ServicioUsuarioLibro.class);
    ServicioLibroGenero servicioLibroGenero = mock(ServicioLibroGenero.class);
    ServicioResenia servicioResenia = mock(ServicioResenia.class);
    ServicioValidacionPlan servicioValidacionPlan = mock(ServicioValidacionPlan.class);
    ServicioUsuarioPlan servicioUsuarioPlan = mock(ServicioUsuarioPlan.class);
    ControladorLibro controladorLibro = new ControladorLibro(servicioLibro, servicioUsuario,
            servicioUsuarioLibro, servicioLibroGenero, servicioResenia, null, servicioValidacionPlan, servicioUsuarioPlan);

    private HttpServletRequest requestMock;
    private HttpSession sessionMock;

    @BeforeEach
    public void setUp() {
        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);

        ServletRequestAttributes attr = new ServletRequestAttributes(requestMock);
        RequestContextHolder.setRequestAttributes(attr);
    }

    @Test
    public void siLaQueryDeBusquedaContieneTextoLaBusquedaEsExitosa() throws ListaVacia, QueryVacia {
        //given
        givenExistenLibros();
        when(servicioLibro.buscar("amor")).thenReturn(new HashSet<>(
                Set.of(new Libro())));
        //when
        ModelAndView mav = whenBuscarLibro("amor");
        //then
        thenLaBusquedaEsExitosa(mav);
    }

    @Test
    public void siLaQueryDeBusquedaEstaVaciaMuestraError() throws ListaVacia, QueryVacia {
        givenExistenLibros();
        doThrow(QueryVacia.class).when(servicioLibro).buscar("");

        ModelAndView mav = whenBuscarLibro("");

        thenLaBusquedaFalla(mav, "El campo de busqueda esta vacio");
    }

    @Test
    public void siLaListaDeLibrosObtenidosEstaVaciaMuestraError() throws ListaVacia, QueryVacia {
        givenExistenLibros();
        String mensajeError = "No se encontraron libros que coincidan con la busqueda";

        doThrow(new ListaVacia(mensajeError)).when(servicioLibro).buscar("*-,vasda");

        ModelAndView mav = whenBuscarLibro("*-,vasda");

        thenLaBusquedaFalla(mav, mensajeError);
    }

    /*@Test
    public void siElLibroExisteDevuelveDetalleLibro() throws LibroNoEncontrado, UsuarioInexistente {
        Long userId = 70L;

        when(requestMock.getSession()).thenReturn(sessionMock);
        when(sessionMock.getAttribute("USERID")).thenReturn(userId);


        // Given
        Long libroId = 1L;
        Libro libro = new Libro();
        when(servicioLibro.obtenerIdLibro(libroId)).thenReturn(libro);
        when(servicioUsuarioLibro.obtenerUsuarioLibro(userId, libroId)).thenReturn(new UsuarioLibro());

        // When
        String vista = controladorLibro.detalleLibro(new ModelMap(), libroId);

        // Then
        assertThat(vista, equalTo("infoLibro"));
    }
*/
    @Test
    public void siElEstadoDeLecturaEsCambiadoExitosamenteRedirigeALaVistaDeDetalle() {
        Long userId = 70L;

        when(requestMock.getSession()).thenReturn(sessionMock);
        when(sessionMock.getAttribute("USERID")).thenReturn(userId);

        // Given
        Long libroId = 1L;
        String nuevoEstado = "Leído";
        Integer cantidadDePaginas = 520;

        // When
        String vista = controladorLibro.cambiarEstadoDeLectura(new ModelMap(), libroId, nuevoEstado, cantidadDePaginas, new RedirectAttributesModelMap());

        // Then
        assertThat(vista, equalTo("redirect:/libro/detalle/" + libroId + "?usuarioId=" + userId));
    }

    @Test
    public void siElLibroExisteMuestraLaResena() throws LibroNoEncontrado {
        // Given
        Long libroId = 1L;
        Libro libro = new Libro(); // Crea un libro simulado
        when(servicioLibro.obtenerIdLibro(libroId)).thenReturn(libro);

        // When
        String vista = controladorLibro.mostrarResenia(new ModelMap(), libroId);

        // Then
        assertThat(vista, equalTo("resenaLibro"));
    }

    @Test
    public void siLaResenaSeGuardaCorrectamenteRedirigeAlDetalleDelLibro() throws LibroNoEncontrado, UsuarioInexistente {
        Long userId = 70L;

        Usuario usuario = new Usuario();
        usuario.setId(userId);

        when(requestMock.getSession()).thenReturn(sessionMock);
        when(sessionMock.getAttribute("USERID")).thenReturn(userId);
        when(servicioUsuario.buscarUsuarioPorId(userId)).thenReturn(usuario);

        // Given
        Long libroId = 1L;
        Libro libro = new Libro();
        libro.setId(libroId);
        libro.setTitulo("Libro 1");

        Integer puntuacion = 5;
        String reseña = "Excelente libro";

        when(servicioLibro.obtenerIdLibro(libroId)).thenReturn(libro);

        // When
        String vista = controladorLibro.guardarResena(new ModelMap(), libroId, puntuacion, reseña);

        // Then
        assertThat(vista, equalTo("redirect:/libro/detalle/" + libroId));
    }

    private void givenExistenLibros() {
    }

    private ModelAndView whenBuscarLibro(String query) {
        return controladorLibro.buscarLibros(query);
    }

    private void thenLaBusquedaEsExitosa(ModelAndView mav) {
        assertThat(mav.getViewName(), equalToIgnoringCase("resultados_busqueda"));
    }

    private void thenLaBusquedaFalla(ModelAndView mav, String mensajeError) {
        assertThat(mav.getModel().get("error").toString(), equalToIgnoringCase(mensajeError));
    }
}
