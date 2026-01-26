package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.excepcion.ListaVacia;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioLibro;

import javax.mail.MessagingException;
import java.util.List;

public interface ServicioUsuarioLibro {
    UsuarioLibro obtenerUsuarioLibro(Long usuarioId, Long libroId);
    void guardarUsuarioLibro(UsuarioLibro usuarioLibro);
    void crearOActualizarUsuarioLibro(Long usuarioId, Long libroId, String estadoDeLectura, Integer puntuacion, String reseña);
    List<UsuarioLibro> buscarPorEstadoDeLectura(String estadoDeLectura, Usuario usuario) throws ListaVacia;
    Double calcularPromedioDePuntuacion(Long libroId);

    List<UsuarioLibro> obtenerTodosLosComentariosDeMisAmigos(Long userId) throws MessagingException;
    void actualizarPaginasLeidas(Long usuarioId, Long libroId, Integer paginasLeidas);
    Double calcularProgresoDeLectura(Long usuarioId, Long libroId, Integer cantidadDePaginasLeidas);
    List<UsuarioLibro> buscarLibrosLeidosPorAño(Integer anio, Usuario usuario) throws ListaVacia;
}
