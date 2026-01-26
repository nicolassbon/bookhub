package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.excepcion.ListaVacia;
import com.tallerwebi.dominio.excepcion.ReseniaInexistente;
import com.tallerwebi.dominio.model.Comentario;
import com.tallerwebi.dominio.model.Resenia;
import com.tallerwebi.dominio.model.Usuario;

import java.util.List;

public interface ServicioComentario {
    void guardarComentario(Usuario usuario, Resenia resenia, String textoComentario) throws ReseniaInexistente;
    List<Comentario> obtenerComentariosPorResenia(Long idResenia) throws ListaVacia;

    Boolean esAutorDelComentario(Long id, Long userId);

    void eliminar(Long id);

    Comentario obtenerComentarioPorId(Long id);
}
