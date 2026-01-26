package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.model.ComentarioPublicacion;
import com.tallerwebi.dominio.model.Publicacion;

import javax.mail.MessagingException;
import java.util.List;

public interface ServicioComentarioPublicacion {

    void crearComentarioPublicacion(Long publicationId, String mensaje, Long userId) throws MessagingException;
    List<ComentarioPublicacion> obtenerLosComentariosDeLaPublicacion(Long publicationId) throws MessagingException;
}
