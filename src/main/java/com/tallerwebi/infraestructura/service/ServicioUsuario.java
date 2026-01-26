package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.model.Usuario;

import java.util.List;
import java.util.Set;

public interface ServicioUsuario {

    Set<Usuario> buscarUsuariosPorQuery(String query) throws Exception;
    Usuario buscarUsuarioPorId(Long id) throws UsuarioInexistente;

    boolean existeNombreUsuario(String nombreUsuario, Long idUsuarioActual);

    boolean existeEmailUsuario(String email, Long idUsuarioActual);

    void actualizarUsuario(Long idUsuarioActual, Usuario usuario) throws UsuarioInexistente;

    List<Usuario> obtenerUsuarios();

    List<Usuario> obtenerUsuariosDesafio(Long userId);
}
