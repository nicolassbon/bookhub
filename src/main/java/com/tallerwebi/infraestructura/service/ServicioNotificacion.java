package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.model.Notificacion;
import com.tallerwebi.dominio.model.TipoNotificacion;

public interface ServicioNotificacion
{
    Notificacion obtenerNotificacionPorId(Long notificationId);
    void crearNotificacion(Long userId, Long tipoNotificacion, String mensaje, Long friendId) throws Exception;
    void editarNombreNotificacion(Long notificationId, String message, Long notificationTypeId) throws Exception;

}
