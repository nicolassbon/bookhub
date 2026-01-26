package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.model.UsuarioNotificacion;

import java.util.List;

public interface ServicioUsuarioNotificacion {
    List<UsuarioNotificacion> listarNotificacionesPorUsuario(Long usuarioId);
    Long  obtenerElIdDeAmigoPorIdDeNotificacion(Long friendId, Long requestId);
}
