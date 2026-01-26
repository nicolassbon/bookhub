package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.excepcion.LibroNoEncontrado;
import com.tallerwebi.dominio.excepcion.ListaVacia;
import com.tallerwebi.dominio.excepcion.PaginasExcedidas;
import com.tallerwebi.dominio.model.Libro;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioLibro;
import com.tallerwebi.dominio.repository.RepositorioLibro;
import com.tallerwebi.dominio.repository.RepositorioUsuario;
import com.tallerwebi.dominio.repository.RepositorioUsuarioLibro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ServicioUsuarioLibroImpl implements ServicioUsuarioLibro {

    private final RepositorioUsuarioLibro repositorioUsuarioLibro;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioLibro repositorioLibro;

    @Autowired
    public ServicioUsuarioLibroImpl(RepositorioUsuarioLibro repositorioUsuarioLibro, RepositorioUsuario repositorioUsuario, RepositorioLibro repositorioLibro) {
        this.repositorioUsuarioLibro = repositorioUsuarioLibro;
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioLibro = repositorioLibro;
    }

    @Override
    public UsuarioLibro obtenerUsuarioLibro(Long usuarioId, Long libroId) {
        return repositorioUsuarioLibro.encontrarUsuarioIdYLibroId(usuarioId, libroId);
    }

    @Override
    public void guardarUsuarioLibro(UsuarioLibro usuarioLibro) {
        repositorioUsuarioLibro.guardar(usuarioLibro);
    }

    @Override
    public void crearOActualizarUsuarioLibro(Long usuarioId, Long libroId, String estadoDeLectura, Integer puntuacion, String resenia) {
        UsuarioLibro usuarioLibro = repositorioUsuarioLibro.encontrarUsuarioIdYLibroId(usuarioId, libroId);

        if (usuarioLibro == null) {
            // Si no existe, creo una nueva relación
            usuarioLibro = new UsuarioLibro();

            // Obtengo las entidades Usuario y Libro
            Usuario usuario = repositorioUsuario.buscarUsuarioPorId(usuarioId);
            Libro libro = repositorioLibro.buscarLibroPorId(libroId);

            if (usuario == null || libro == null) {
                throw new IllegalArgumentException("Usuario o Libro no encontrado.");
            }

            // Asigno las entidades a UsuarioLibro
            usuarioLibro.setUsuario(usuario);
            usuarioLibro.setLibro(libro);
        }

        // Actualizo los atributos
        usuarioLibro.setEstadoDeLectura(estadoDeLectura);
        usuarioLibro.setPuntuacion(puntuacion);
        usuarioLibro.setResenia(resenia);

        if (estadoDeLectura.equalsIgnoreCase("Leído")) {
            usuarioLibro.setFechaLeido(LocalDate.now());
        }

        // Guardo o actualizo la relación en la base de datos
        guardarUsuarioLibro(usuarioLibro);
    }

    @Override
    public List<UsuarioLibro> buscarPorEstadoDeLectura(String estadoDeLectura, Usuario usuario) throws ListaVacia {
        List<UsuarioLibro> librosObtenidos = repositorioUsuarioLibro.buscarPorEstadoDeLectura(estadoDeLectura, usuario);

        if (librosObtenidos.isEmpty())
            throw new ListaVacia("No tiene libros con este estado");

        return librosObtenidos;
    }

    @Override
    public Double calcularPromedioDePuntuacion(Long libroId) {
        List<UsuarioLibro> usuariosLibro = repositorioUsuarioLibro.buscarLibroPorId(libroId);
        if (usuariosLibro.isEmpty()) {
            return 0.0;
        }

        Integer cantidad = 0;
        Integer suma = 0;

        for (UsuarioLibro usuarioLibro : usuariosLibro) {
            if (usuarioLibro.getPuntuacion() != null) {
                suma += usuarioLibro.getPuntuacion();
                cantidad++;
            }

        }

        if (cantidad == 0) {
            return 0.0;
        }


        double promedio = (double) suma / cantidad;


        double promedioRedondeado = Math.round(promedio * 10.0) / 10.0;

        return promedioRedondeado;
    }

    @Transactional
    @Override
    public List<UsuarioLibro> obtenerTodosLosComentariosDeMisAmigos(Long userId) throws MessagingException {
        try {
            List<UsuarioLibro> comentariosDeMisAmigos = repositorioUsuarioLibro.obtenerTodosLosComentariosDeMisAmigos(userId);
            return comentariosDeMisAmigos;
        } catch (Exception e) {
            throw new MessagingException(e.getMessage());
        }

    }

    @Override
    public void actualizarPaginasLeidas(Long usuarioId, Long libroId, Integer paginasLeidas) throws PaginasExcedidas {
        UsuarioLibro usuarioLibro = obtenerUsuarioLibro(usuarioId, libroId);
        Libro libro = repositorioLibro.buscarLibroPorId(libroId);

        if(libro == null){
            throw new LibroNoEncontrado("Libro no encontrado");
        }

        if(paginasLeidas > libro.getCantidadDePaginas()){
            throw new PaginasExcedidas("Error. " + libro.getTitulo() +  " tiene " + libro.getCantidadDePaginas() + " páginas");
        }

        usuarioLibro.setCantidadDePaginas(paginasLeidas);
        guardarUsuarioLibro(usuarioLibro);
    }

    @Override
    public Double calcularProgresoDeLectura(Long usuarioId, Long libroId, Integer cantidadDePaginasLeidas) {
        UsuarioLibro usuarioLibro = obtenerUsuarioLibro(usuarioId, libroId);
        Libro libro = repositorioLibro.buscarLibroPorId(libroId);

        if(libro == null || libro.getCantidadDePaginas() == null || usuarioLibro == null){
            return 0.0;
        }

        double progreso = (double) cantidadDePaginasLeidas/libro.getCantidadDePaginas()*100;
        return (double) Math.round(Math.min(progreso, 100.0));
    }

    @Override
    public List<UsuarioLibro> buscarLibrosLeidosPorAño(Integer anio, Usuario usuario) throws ListaVacia {
        List<UsuarioLibro> librosLeidos = repositorioUsuarioLibro.buscarLibrosLeidosPorAño(anio, usuario);

        if (librosLeidos.isEmpty())
            throw new ListaVacia("No tiene libros leidos este año.");

        return librosLeidos;
    }

}
