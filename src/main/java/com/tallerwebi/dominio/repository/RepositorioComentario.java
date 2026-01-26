package com.tallerwebi.dominio.repository;

import com.tallerwebi.dominio.model.Comentario;

import java.util.List;

public interface RepositorioComentario {
    void guardarComentario(Comentario comentario);
    List<Comentario> obtenerComentariosPorResenia(Long idResenia);

    Comentario obtenerComentarioPorId(Long id);

    void eliminar(Comentario comentario);
}
