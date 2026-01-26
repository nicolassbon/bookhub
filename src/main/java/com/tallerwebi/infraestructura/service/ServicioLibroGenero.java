package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.excepcion.ListaVacia;
import com.tallerwebi.dominio.model.Libro;
import com.tallerwebi.dominio.model.LibroGenero;

import java.util.List;

public interface ServicioLibroGenero {
    List<LibroGenero> obtenerLibroPorGenero(Long generoId);
    List<LibroGenero> obtenerGeneros(Libro libro) throws ListaVacia;
}
