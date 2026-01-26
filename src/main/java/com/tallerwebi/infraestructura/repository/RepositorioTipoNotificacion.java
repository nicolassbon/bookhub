package com.tallerwebi.infraestructura.repository;

import com.tallerwebi.dominio.model.TipoNotificacion;

public interface RepositorioTipoNotificacion {

    TipoNotificacion encontrarTipoNotificacionPorId(Long id);
}
