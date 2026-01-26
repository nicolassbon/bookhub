package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.excepcion.ListaVacia;
import com.tallerwebi.dominio.model.Libro;
import com.tallerwebi.dominio.model.LibroGenero;
import com.tallerwebi.dominio.repository.RepositorioLibroGenero;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class ServicioLibroGeneroImpl implements ServicioLibroGenero {

    private RepositorioLibroGenero repositorioLibroGenero;

    @Autowired
    public ServicioLibroGeneroImpl(RepositorioLibroGenero repositorioLibroGenero) {
        this.repositorioLibroGenero = repositorioLibroGenero;
    }

    @Override
    public List<LibroGenero> obtenerLibroPorGenero(Long generoId) {
        List<LibroGenero> listaLibroPorGenero = repositorioLibroGenero.obtenerLibroPorGenero(generoId);
        return listaLibroPorGenero;
    }

    @Override
    public List<LibroGenero> obtenerGeneros(Libro libro) throws ListaVacia {
        List<LibroGenero> generosDelLibro = repositorioLibroGenero.obtenerGeneros(libro);

        if(generosDelLibro.isEmpty()) {
            throw new ListaVacia("El libro no tiene generos asignados en la BD");
        }
        return generosDelLibro;
    }
}
