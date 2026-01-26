package com.tallerwebi.dominio.repository;

import com.tallerwebi.dominio.model.Publicacion;

import java.util.List;

public interface RepositorioPublicacion {
    void guardar(Publicacion publicacion);
    List<Publicacion> obtenerTodasPublicacionesDeAmigos(Long userId);

    Publicacion obtenerPublicacionPorId(Long publicationId);
}
