package com.tallerwebi.dominio.repository;

import com.tallerwebi.dominio.model.Logro;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioLogro;

import java.util.List;

public interface RepositorioUsuarioLogro {
    void guardar(UsuarioLogro usuario);
    UsuarioLogro buscarUsuarioLogro(Long userId, Long logroId);
    List<UsuarioLogro> obtenerLogrosPorUsuario(Usuario usuario);
    void borrar(UsuarioLogro usuarioLogro);
}
