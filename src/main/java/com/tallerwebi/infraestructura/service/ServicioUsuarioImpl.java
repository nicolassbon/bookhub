package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.excepcion.ListaVacia;
import com.tallerwebi.dominio.excepcion.QueryVacia;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.model.Libro;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.repository.RepositorioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
public class ServicioUsuarioImpl implements ServicioUsuario {

    RepositorioUsuario repositorioUsuario;

    @Autowired
    public ServicioUsuarioImpl(RepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
    }

    @Override
    public Set<Usuario> buscarUsuariosPorQuery(String query) throws Exception {
        if (query.isEmpty())
            throw new QueryVacia();

        List<Usuario> usuariosObtenidos = repositorioUsuario.buscarUsuariosPorQuery(query);

        if (usuariosObtenidos.isEmpty())
            throw new ListaVacia("No se encontraron libros que coincidan con la busqueda");

        return new HashSet<>(usuariosObtenidos);
    }

    @Override
    public Usuario buscarUsuarioPorId(Long id) throws UsuarioInexistente {
        Usuario usuario = repositorioUsuario.buscarUsuarioPorId(id);
        if (usuario == null)
            throw new UsuarioInexistente();

        return usuario;
    }

    @Override
    public void actualizarUsuario(Long idUsuarioActual, Usuario usuarioActualizado) throws UsuarioInexistente {
        Usuario usuarioExistente = repositorioUsuario.buscarUsuarioPorId(idUsuarioActual);

        if (usuarioExistente == null)
            throw new UsuarioInexistente();

        // Actualizar los campos del usuario existente
        usuarioExistente.setNombre(usuarioActualizado.getNombre());
        usuarioExistente.setNombreUsuario(usuarioActualizado.getNombreUsuario());
        usuarioExistente.setEmail(usuarioActualizado.getEmail());
        usuarioExistente.setEdad(usuarioActualizado.getEdad());
        usuarioExistente.setFechaNacimiento(usuarioActualizado.getFechaNacimiento());
        usuarioExistente.setMeta(usuarioActualizado.getMeta());
        usuarioExistente.setBiografia(usuarioActualizado.getBiografia());
        usuarioExistente.setImagenUrl(usuarioActualizado.getImagenUrl());

        // Guardar el usuario actualizado en la base de datos
        try {
            repositorioUsuario.guardarUsuario(usuarioExistente);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Usuario> obtenerUsuarios() {
        return repositorioUsuario.obtenerUsuarios();
    }

    @Override
    public List<Usuario> obtenerUsuariosDesafio(Long userId) {
        List<Usuario> usuariosDesafio = repositorioUsuario.obtenerUsuariosDesafio(userId);

        if (usuariosDesafio.isEmpty()) {
            System.out.println("Lista vacia");
        }

        return usuariosDesafio;
    }

    @Override
    public boolean existeNombreUsuario(String nombre, Long idUsuario) {
        Usuario usuario = repositorioUsuario.buscarPorNombreUsuario(nombre);
        // Verifica si existe el usuario y que no sea el actual
        return usuario != null && !(Objects.equals(usuario.getId(), idUsuario));
    }

    @Override
    public boolean existeEmailUsuario(String email, Long idUsuario) {
        Usuario usuario = repositorioUsuario.buscarPorEmail(email);
        return usuario != null && !(Objects.equals(usuario.getId(), idUsuario));
    }
}
