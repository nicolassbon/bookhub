package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.model.*;
import com.tallerwebi.dominio.repository.RepositorioUsuario;
import com.tallerwebi.infraestructura.repository.RepositorioNotificacion;
import com.tallerwebi.infraestructura.repository.RepositorioTipoNotificacion;
import com.tallerwebi.infraestructura.repository.RepositorioUsuarioNotificacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;

@Service
@Transactional
public class ServicioNotificacionImpl implements ServicioNotificacion{
    private final RepositorioNotificacion repositorioNotificacion;
    private final RepositorioTipoNotificacion repositorioTipoNotificacion;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioUsuarioNotificacion repositorioUsuarioNotificacion;

    @Autowired
    public ServicioNotificacionImpl(RepositorioNotificacion repositorioNotificacion, RepositorioTipoNotificacion repositorioTipoNotificacion, RepositorioUsuario repositorioUsuario, RepositorioUsuarioNotificacion repositorioUsuarioNotificacion) {
        this.repositorioNotificacion = repositorioNotificacion;
        this.repositorioTipoNotificacion = repositorioTipoNotificacion;
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioUsuarioNotificacion = repositorioUsuarioNotificacion;
    }

    @Override
    public Notificacion obtenerNotificacionPorId(Long notificationId) {
        return repositorioNotificacion.encontrarNotificacionPorId(notificationId);
    }

    @Override
    public void crearNotificacion(Long userId, Long tipoNotificacion, String mensaje, Long friendId) throws Exception {
        try {
            Usuario usuario = repositorioUsuario.buscarUsuarioPorId(userId);

            if (usuario == null) {
                throw new Exception ("No existe usuario con ID " + userId);
            }
            Notificacion notificacion = new Notificacion();
            TipoNotificacion notifyType = repositorioTipoNotificacion.encontrarTipoNotificacionPorId(tipoNotificacion);
            if (notifyType == null) {
                throw new Exception("No se encontró tipo de notificación con el ID " + tipoNotificacion);
            }
            notificacion.setMensaje(mensaje);
            notificacion.setTipo(notifyType);
            notificacion.setFechaCreacion(new Date());
            repositorioNotificacion.guardar(notificacion);


            UsuarioNotificacion usuarioNotificacion = new UsuarioNotificacion();
            usuarioNotificacion.setUsuario(usuario);
            usuarioNotificacion.setNotificacion(notificacion);
            usuarioNotificacion.setLeida(false);
            usuarioNotificacion.setFechaRecibida(new Date());
            usuarioNotificacion.setFriendId(friendId);

            repositorioUsuarioNotificacion.guardar(usuarioNotificacion);
        }catch (Exception error){
            throw new Exception(error.getMessage());
        }
    }

    @Override
    public void editarNombreNotificacion(Long notificationId, String message, Long notificationTypeId) throws Exception {
        try {
            TipoNotificacion notifyType = repositorioTipoNotificacion.encontrarTipoNotificacionPorId(notificationTypeId);

            Notificacion notificacion = new Notificacion();

            notificacion.setMensaje(message);
            repositorioNotificacion.reemplazarMensajeNotificacion(notificacion, notificationId, notifyType);


        }catch (Exception error){
            throw new Exception(error.getMessage());
        }
    }


}
