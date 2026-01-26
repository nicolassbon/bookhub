package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.model.TipoNotificacion;
import com.tallerwebi.infraestructura.repository.RepositorioTipoNotificacion;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class ServicioTipoNotificacionImpl implements ServicioTipoNotificacion{

    private final RepositorioTipoNotificacion tipoNotificacion;

    public ServicioTipoNotificacionImpl(RepositorioTipoNotificacion tipoNotificacion) {
        this.tipoNotificacion = tipoNotificacion;
    }

    @Override
    public String buscarTipoNotificacionPorId(Long id) throws Exception{
        TipoNotificacion notificacion = tipoNotificacion.encontrarTipoNotificacionPorId(id);
        if (notificacion == null) {
            throw new Exception("No se encontro ningun tipo de notificacion con el ID: " + id);
        }
        return notificacion.getDetalle();
    }
}
