package com.tallerwebi.dominio.repository;

import com.tallerwebi.dominio.model.Libro;

import java.util.List;

public interface RepositorioLibro {
    List<Libro> buscar(String query);
    Libro buscarLibroPorId(Long id);

    List<Libro> buscarDosLibrosRandom();
    void actualizarLibro(Libro libro);
}
