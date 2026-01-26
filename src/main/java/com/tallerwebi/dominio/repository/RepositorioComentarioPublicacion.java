package com.tallerwebi.dominio.repository;

import com.tallerwebi.dominio.model.ComentarioPublicacion;
import com.tallerwebi.dominio.model.Publicacion;

import java.util.List;

public interface RepositorioComentarioPublicacion {


    void guardar(ComentarioPublicacion comentarioPublicacion);
    List<ComentarioPublicacion> obtenerLosComentariosDeLaPublicacion(Long publicationId);
}
