package com.tallerwebi.dominio.repository;

import com.tallerwebi.dominio.model.UsuarioAutor;

public interface RepositorioUsuarioAutor {
    UsuarioAutor encontrarUsuarioIdYAutorId(Long usuarioId, Long autorId);
    void guardar(UsuarioAutor usuarioAutor);
}
