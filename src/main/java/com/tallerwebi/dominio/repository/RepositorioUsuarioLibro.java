package com.tallerwebi.dominio.repository;

import com.tallerwebi.dominio.model.Libro;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioLibro;

import java.time.LocalDate;
import java.util.List;

public interface RepositorioUsuarioLibro {
    UsuarioLibro encontrarUsuarioIdYLibroId(Long usuarioId, Long libroId);
    void guardar(UsuarioLibro usuarioLibro);
    List<UsuarioLibro> obtenerTodosLosComentariosDeMisAmigos(Long userId);
    List<UsuarioLibro> buscarPorEstadoDeLectura(String estadoDeLectura, Usuario usuario);
    List<UsuarioLibro> buscarLibroPorId(Long idLibro);
    List<UsuarioLibro> buscarLibrosLeidosPorAño(Integer anio, Usuario usuario);

    Integer buscarCantLibrosLeidosEntrePlazos(Usuario usuario, LocalDate fechaCreacion, LocalDate fechaFinalizacion);
}
