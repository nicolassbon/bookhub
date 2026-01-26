package com.tallerwebi.dominio.repository;

import com.tallerwebi.dominio.model.Genero;
import com.tallerwebi.dominio.model.UsuarioGenero;

import java.util.List;

public interface RepositorioUsuarioGenero {
    UsuarioGenero encontrarUsuarioIdYGeneroId(Long usuarioId, Long generoId);
    List<UsuarioGenero> obtenerGenerosDeUsuario(Long usuarioId);
    void guardar(UsuarioGenero usuarioGenero);

}
