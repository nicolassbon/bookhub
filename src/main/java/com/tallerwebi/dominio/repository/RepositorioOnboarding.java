package com.tallerwebi.dominio.repository;

import com.tallerwebi.dominio.model.Autor;
import com.tallerwebi.dominio.model.Genero;

import java.util.List;

public interface RepositorioOnboarding {
    List<Genero> obtenerGeneros();
    List<Autor> obtenerAutores();
    Genero obtenerGeneroPorId(Long idGenero);
    Autor obtenerAutorPorId(Long idAutor);
}
