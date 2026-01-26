package com.tallerwebi.dominio.repository;

import com.tallerwebi.dominio.model.Genero;
import com.tallerwebi.dominio.model.Libro;
import com.tallerwebi.dominio.model.LibroGenero;

import java.util.List;

public interface RepositorioLibroGenero {
    List<LibroGenero> obtenerLibroGeneros();
    List<LibroGenero> obtenerLibroPorGenero(Long generoId);
    List<LibroGenero> obtenerGeneros(Libro libro);
}
