package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.model.*;
import com.tallerwebi.dominio.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class ServicioUsuarioAutorImpl implements ServicioUsuarioAutor {
    private final RepositorioUsuarioAutor repositorioUsuarioAutor;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioAutor repositorioAutor;

    @Autowired
    public ServicioUsuarioAutorImpl(RepositorioUsuarioAutor repositorioUsuarioAutor, RepositorioUsuario repositorioUsuario, RepositorioAutor repositorioAutor) {
        this.repositorioUsuarioAutor = repositorioUsuarioAutor;
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioAutor = repositorioAutor;
    }

    @Override
    public UsuarioAutor obtenerUsuarioAutor(Long usuarioId, Long autorId) {
        return repositorioUsuarioAutor.encontrarUsuarioIdYAutorId(usuarioId, autorId);
    }

    @Override
    public void guardarUsuarioAutor(UsuarioAutor usuarioAutor) {
        repositorioUsuarioAutor.guardar(usuarioAutor);
    }

    @Override
    public void crearOActualizarUsuarioAutor(Long usuarioId, Long autorId) {
        UsuarioAutor usuarioAutor = repositorioUsuarioAutor.encontrarUsuarioIdYAutorId(usuarioId, autorId);

        if (usuarioAutor == null) {
            // Si no existe, creo una nueva relación
            usuarioAutor = new UsuarioAutor();

            // Obtengo las entidades Usuario y Libro
            Usuario usuario = repositorioUsuario.buscarUsuarioPorId(usuarioId);
            Autor autor = repositorioAutor.buscarAutorPorId(autorId);

            if (usuario == null || autor == null) {
                throw new IllegalArgumentException("Usuario o Autor no encontrado.");
            }

            // Asigno las entidades a UsuarioLibro
            usuarioAutor.setUsuario(usuario);
            usuarioAutor.setAutor(autor);
        }

        // Guardo o actualizo la relación en la base de datos
        guardarUsuarioAutor(usuarioAutor);
    }

}
