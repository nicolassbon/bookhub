package com.tallerwebi.infraestructura.repository;

import com.tallerwebi.dominio.model.Amistad;

import java.util.List;

public interface RepositorioAmistad {
    Amistad encontrarAmistadPorUsuarios(Long usuarioId, Long amigoId);
    List<Amistad> listarSolicitudesDeAmistad(Long usuarioId);

    Boolean guardar(Amistad amistad);
    String verificacionDeAmistad(Long usuarioId, Long amigoId);
    Boolean eliminarAmistad(Long usuarioId, Long amigoId);
    List<Amistad> listarAmigosPorUsuario(Long userId);

    Amistad buscarAmistadPorIdDeSolicitud(Long requestId) throws Exception;

}
