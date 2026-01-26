package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.model.*;
import com.tallerwebi.dominio.repository.*;
import com.tallerwebi.infraestructura.repository.RepositorioOnboardingImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class ServicioOnboardingImpl implements ServicioOnboarding {

    private final RepositorioOnboardingImpl repositorioOnboarding;
    private RepositorioUsuario repositorioUsuario;
    private RepositorioUsuarioGenero repositorioUsuarioGenero;
    private RepositorioUsuarioAutor repositorioUsuarioAutor;
    private RepositorioGenero repositorioGenero;
    private RepositorioAutor repositorioAutor;


    @Autowired
    public ServicioOnboardingImpl(RepositorioUsuario repositorioUsuario, RepositorioOnboardingImpl repositorioOnboarding,RepositorioGenero repositorioGenero,RepositorioUsuarioGenero repositorioUsuarioGenero, RepositorioUsuarioAutor repositorioUsuarioAutor, RepositorioAutor repositorioAutor) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioOnboarding = repositorioOnboarding;
        this.repositorioGenero = repositorioGenero;
        this.repositorioUsuarioGenero = repositorioUsuarioGenero;
        this.repositorioUsuarioAutor = repositorioUsuarioAutor;
        this.repositorioAutor = repositorioAutor;
    }

    @Override
    public List<Genero> obtenerGeneros() {
        return repositorioOnboarding.obtenerGeneros();
    }

    @Override
    public List<Autor> obtenerAutores() {
        return repositorioOnboarding.obtenerAutores();
    }

    @Override
    @Transactional
    public void guardarGeneros(Long usuarioId, List<Long> generosIds) {
        try {
            Usuario usuario = repositorioUsuario.buscarUsuarioPorId(usuarioId);
            if (usuario == null) {
                throw new IllegalArgumentException("Usuario no encontrado.");
            }

            for (Long generoId : generosIds) {
                Genero genero = repositorioGenero.buscarGeneroPorId(generoId);
                if (genero == null) {
                    throw new IllegalArgumentException("Género no encontrado.");
                }

                // Verificar si la relación ya existe
                UsuarioGenero usuarioGenero = repositorioUsuarioGenero.encontrarUsuarioIdYGeneroId(usuarioId, generoId);
                if (usuarioGenero == null) {
                    // Si no existe la relación, se crea una nueva
                    usuarioGenero = new UsuarioGenero();
                    usuarioGenero.setUsuario(usuario);
                    usuarioGenero.setGenero(genero);

                    repositorioUsuarioGenero.guardar(usuarioGenero);
                }
            }
        } catch (Exception e) {
            System.out.println("Error al guardar géneros: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void guardarAutores(Long usuarioId, List<Long> autoresIds) {
        try {
            Usuario usuario = repositorioUsuario.buscarUsuarioPorId(usuarioId);
            if (usuario == null) {
                throw new IllegalArgumentException("Usuario no encontrado.");
            }

            for (Long autorId : autoresIds) {
                Autor autor = repositorioAutor.buscarAutorPorId(autorId);
                if (autor == null) {
                    throw new IllegalArgumentException("Autor no encontrado.");
                }

                // Verificar si la relación ya existe
                UsuarioAutor usuarioAutor = repositorioUsuarioAutor.encontrarUsuarioIdYAutorId(usuarioId, autorId);
                if (usuarioAutor == null) {
                    // Si no existe la relación, se crea una nueva
                    usuarioAutor = new UsuarioAutor();
                    usuarioAutor.setUsuario(usuario);
                    usuarioAutor.setAutor(autor);

                    repositorioUsuarioAutor.guardar(usuarioAutor);
                }
            }
        } catch (Exception e) {
            System.out.println("Error al guardar autores: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void guardarMeta(Long usuarioId, Long meta) {
        try {
            Usuario usuario = repositorioUsuario.buscarUsuarioPorId(usuarioId);
            usuario.setMeta(meta);

        } catch (Exception e) {

            System.out.println("Error al guardar la meta: " + e.getMessage());
        }
    }
}
