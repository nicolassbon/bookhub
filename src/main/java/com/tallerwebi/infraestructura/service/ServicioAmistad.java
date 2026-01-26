package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.model.Amistad;

import java.util.List;

public interface ServicioAmistad {
    boolean enviarSolicitudDeAmistad(Long userId, Long friendId) throws Exception;
    boolean eliminarSolicitudAmistad(Long userId, Long friendId) throws Exception;
    boolean aceptarSolicitudDeAmistad(Long userId, Long friendId, Long requestId) throws Exception;
    boolean rechazarSolicitudDeAmistad(Long userId, Long friendId, Long requestId) throws Exception;
    List<Amistad> obtenerAmigos(Long userId);
    String verificacionDeAmistad(Long userId, Long friendId) throws Exception;
}
