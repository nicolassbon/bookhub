package com.tallerwebi.infraestructura.repository;

import com.tallerwebi.dominio.model.Notificacion;
import com.tallerwebi.dominio.model.TipoNotificacion;

public interface RepositorioNotificacion {
    Notificacion encontrarNotificacionPorId(Long id);
    void guardar(Notificacion notificacion);
    void reemplazarMensajeNotificacion(Notificacion notificacion, Long notificationId, TipoNotificacion notificationTypeId);
}
