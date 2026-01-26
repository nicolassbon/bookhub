package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.excepcion.LibroNoEncontrado;
import com.tallerwebi.dominio.model.Libro;
import com.tallerwebi.dominio.repository.RepositorioLibro;
import com.tallerwebi.dominio.excepcion.ListaVacia;
import com.tallerwebi.dominio.excepcion.QueryVacia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class ServicioLibroImpl implements ServicioLibro {

    RepositorioLibro repositorioLibro;

    @Autowired
    public ServicioLibroImpl(RepositorioLibro repositorioLibro) {
        this.repositorioLibro = repositorioLibro;
    }

    @Override
    public Set<Libro> buscar(String query) throws QueryVacia, ListaVacia {
        if (query.isEmpty())
            throw new QueryVacia();

        List<Libro> librosObtenidos = repositorioLibro.buscar(query);

        if (librosObtenidos.isEmpty())
            throw new ListaVacia("No se encontraron libros que coincidan con la busqueda");

        return new HashSet<>(librosObtenidos);
    }

    @Override
    public Libro obtenerIdLibro(Long id) {
        Libro libro = repositorioLibro.buscarLibroPorId(id);
        if (libro == null) {
            throw new LibroNoEncontrado("Libro no encontrado con ID: " + id);
        }
        return libro;
    }

    @Override
    public List<Libro> obtenerDosLibrosRandom() {
        List<Libro> listaoDeLibros = repositorioLibro.buscarDosLibrosRandom();

        return listaoDeLibros;
    }

    @Override
    public void actualizarLibro(Libro libro) {
        repositorioLibro.actualizarLibro(libro);
    }

}
