package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.model.Notificacion;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioGenero;
import com.tallerwebi.dominio.model.UsuarioNotificacion;
import com.tallerwebi.infraestructura.repository.RepositorioUsuarioNotificacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class ServicioUsuarioNotificacionImpl implements ServicioUsuarioNotificacion{
    private final RepositorioUsuarioNotificacion repositorioUsuarioNotificacion;

    @Autowired
    public ServicioUsuarioNotificacionImpl(RepositorioUsuarioNotificacion repositorioUsuarioNotificacion) {
        this.repositorioUsuarioNotificacion = repositorioUsuarioNotificacion;
    }

    @Override
    public List<UsuarioNotificacion> listarNotificacionesPorUsuario(Long usuarioId) {
        return repositorioUsuarioNotificacion.listarNotificacionesPorUsuario(usuarioId);
    }
    @Override
    public Long obtenerElIdDeAmigoPorIdDeNotificacion(Long friendId, Long requestId) {
        return repositorioUsuarioNotificacion.obtenerIdAmigo(friendId, requestId);
    }


}
