package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.model.*;
import com.tallerwebi.dominio.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tallerwebi.dominio.repository.RepositorioUsuario;

import java.util.List;

@Service
@Transactional
public class ServicioUsuarioGeneroImpl implements ServicioUsuarioGenero {

    private final RepositorioUsuarioGenero repositorioUsuarioGenero;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioGenero repositorioGenero;

    @Autowired
    public ServicioUsuarioGeneroImpl(RepositorioUsuarioGenero repositorioUsuarioGenero, RepositorioUsuario repositorioUsuario, RepositorioGenero repositorioGenero) {
        this.repositorioUsuarioGenero = repositorioUsuarioGenero;
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioGenero = repositorioGenero;
    }

    @Override
    public UsuarioGenero obtenerUsuarioGenero(Long usuarioId, Long generoId) {
        return repositorioUsuarioGenero.encontrarUsuarioIdYGeneroId(usuarioId, generoId);
    }

    @Override
    public void guardarUsuarioGenero(UsuarioGenero usuarioGenero) {
        repositorioUsuarioGenero.guardar(usuarioGenero);
    }

    @Override
    public List<UsuarioGenero> obtenerGenerosDeUsuario(Long usuarioId) {

        List<UsuarioGenero> list = repositorioUsuarioGenero.obtenerGenerosDeUsuario(usuarioId);
        return list;
    }

    @Override
    public void crearOActualizarUsuarioGenero(Long usuarioId, Long generoId) {
        UsuarioGenero usuarioGenero = repositorioUsuarioGenero.encontrarUsuarioIdYGeneroId(usuarioId, generoId);

        if (usuarioGenero == null) {
            usuarioGenero = new UsuarioGenero();

            Usuario usuario = repositorioUsuario.buscarUsuarioPorId(usuarioId);
            Genero genero = repositorioGenero.buscarGeneroPorId(generoId);

            if (usuario == null || genero == null) {
                throw new IllegalArgumentException("Usuario o Genero no encontrado.");
            }

            usuarioGenero.setUsuario(usuario);
            usuarioGenero.setGenero(genero);
        }

        guardarUsuarioGenero(usuarioGenero);
    }

}

