package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.model.Publicacion;

import javax.mail.MessagingException;
import java.util.List;

public interface ServicioPublicacion {

    void crearPublicacion(Long userId, String mensaje) throws MessagingException;
    List<Publicacion> obtenerTodasPublicacionesDeAmigos(Long userId) throws MessagingException;
}
