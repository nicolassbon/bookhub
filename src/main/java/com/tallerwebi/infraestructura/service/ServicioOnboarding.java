package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.model.Autor;
import com.tallerwebi.dominio.model.Genero;
import com.tallerwebi.dominio.model.Libro;

import java.util.List;

public interface ServicioOnboarding {
    List<Genero> obtenerGeneros();
    List<Autor> obtenerAutores();
//    List<Libro> obtenerLibros();

    void guardarGeneros(Long usuarioId, List<Long> generos);
    void guardarAutores(Long usuarioId, List<Long> autores);
    void guardarMeta(Long usuarioId, Long meta);
//    void guardarLibros(Long usuarioId, List<Long> libros);
}
