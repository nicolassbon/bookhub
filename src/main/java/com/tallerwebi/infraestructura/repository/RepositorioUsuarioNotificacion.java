package com.tallerwebi.infraestructura.repository;
import com.tallerwebi.dominio.model.UsuarioNotificacion;

import java.util.List;

public interface RepositorioUsuarioNotificacion {
    List<UsuarioNotificacion> listarNotificacionesPorUsuario(Long usuarioId);
    void guardar(UsuarioNotificacion usuarioNotificacion);

    Long obtenerIdAmigo(Long friendId, Long requestId);
}
