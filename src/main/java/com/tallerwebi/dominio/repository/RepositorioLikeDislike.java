package com.tallerwebi.dominio.repository;

import com.tallerwebi.dominio.model.LikeDislike;

import java.util.List;

public interface RepositorioLikeDislike {
    void guardar(LikeDislike likeDislike);
    void eliminar(LikeDislike likeDislike);
    LikeDislike obtenerReaccionDelUsuario(Long userId, Long idResenia);
    List<LikeDislike> obtenerLikesResenia(Long idResenia);
    List<LikeDislike> obtenerDislikesResenia(Long idResenia);
}
