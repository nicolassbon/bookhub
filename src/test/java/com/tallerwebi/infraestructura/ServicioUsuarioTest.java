package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.excepcion.ListaVacia;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.repository.RepositorioUsuario;
import com.tallerwebi.infraestructura.service.ServicioUsuario;
import com.tallerwebi.infraestructura.service.ServicioUsuarioImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServicioUsuarioTest {

    RepositorioUsuario repositorioUsuario = mock(RepositorioUsuario.class);
    ServicioUsuario servicioUsuario = new ServicioUsuarioImpl(repositorioUsuario);

    @Test
    public void siElUsuarioExisteLaBusquedaEsExitosa() throws UsuarioInexistente {

        Usuario usuario = new Usuario();
        usuario.setId(2L);

        when(repositorioUsuario.buscarUsuarioPorId(2L)).thenReturn(usuario);

        Usuario usuarioObtenido = whenBuscarUsuarioPorId(2L);

        assertThat(usuario, notNullValue());
        assertThat(usuarioObtenido.getId(), equalTo(2L));
    }

    @Test
    public void siElUsuarioNoExisteLaBusquedaFalla() {
        when(repositorioUsuario.buscarUsuarioPorId(2L)).thenReturn(null);
        assertThrows(UsuarioInexistente.class,() -> whenBuscarUsuarioPorId(2L));
    }

    private Usuario whenBuscarUsuarioPorId(Long id) throws UsuarioInexistente {
        return servicioUsuario.buscarUsuarioPorId(id);
    }
}
