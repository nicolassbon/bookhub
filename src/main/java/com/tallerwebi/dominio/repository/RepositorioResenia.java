package com.tallerwebi.dominio.repository;

import com.tallerwebi.dominio.model.*;

import java.util.List;


public interface RepositorioResenia {

    void guardar(Resenia resenia);

    Resenia obtenerReseniaPorId(Long id);

    List<Resenia> obtenerReseniasDeOtrosUsuarios(Long userId,Long idLibro);

    List<Resenia> obtenerReseniasDelLibro(Long idLibro);

    Resenia obtenerReseniaDelUsuario(Long userId, Long idLibro);

    List<Resenia> obtenerTodasLasReseniasDelUsuario(Usuario usuario);


    List<Resenia> obtenerReseniasMasReacciones();

    List<Resenia> obtenerReseniasPorTituloLibro(String valor);

    List<Resenia> obtenerReseniasPorUsuario(String valor);

    List<Resenia> obtenerReseniasPorAutorLibro(String valor);
}
