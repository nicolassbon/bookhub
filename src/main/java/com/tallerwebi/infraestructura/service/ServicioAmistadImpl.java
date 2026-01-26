package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.model.Amistad;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.repository.RepositorioUsuario;
import com.tallerwebi.infraestructura.repository.RepositorioAmistad;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ServicioAmistadImpl implements ServicioAmistad {
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioAmistad repositorioAmistad;

    public ServicioAmistadImpl(RepositorioUsuario repositorioUsuario, RepositorioAmistad repositorioAmistad) {
        this.repositorioAmistad = repositorioAmistad;
        this.repositorioUsuario = repositorioUsuario;
    }


    @Override
    public boolean enviarSolicitudDeAmistad(Long userId, Long friendId) throws Exception {
        Usuario usuario = repositorioUsuario.buscarUsuarioPorId(userId);
        Usuario friend = repositorioUsuario.buscarUsuarioPorId(friendId);
        try {

            if (usuario == null || friend == null) {
                throw new Exception("Usuario o amigo no encontrado");

            } else {
                Amistad solicitud = new Amistad();
                solicitud.setUsuario(usuario);
                solicitud.setEstado("pendiente");
                solicitud.setAmigo(friend);
                solicitud.setFechaSolicitud(new Date());
                Boolean saved = repositorioAmistad.guardar(solicitud);
                if (saved) {
                    return true;
                } else {
                    return false;
                }
            }

        } catch (Exception error){
            throw new Exception(error.getMessage());
        }
    }

    @Override
    public boolean eliminarSolicitudAmistad(Long userId, Long friendId) throws Exception {
        Usuario usuario = repositorioUsuario.buscarUsuarioPorId(userId);
        Usuario friend = repositorioUsuario.buscarUsuarioPorId(friendId);
        try {

            if (usuario == null || friend == null) {
                throw new Exception("Usuario o amigo no encontrado");

            } else {
                Boolean saved = repositorioAmistad.eliminarAmistad(userId, friendId);
                if (saved) {
                    return true;
                } else {
                    return false;
                }
            }

        } catch (Exception error){
            throw new Exception(error.getMessage());
        }
    }

    @Override
    public boolean aceptarSolicitudDeAmistad(Long userId, Long friendId, Long requestId) throws Exception {
        Usuario usuario = repositorioUsuario.buscarUsuarioPorId(userId);
        try {

            if (usuario == null) {
                throw new Exception("Usuario o amigo no encontrado");

            } else {
                Amistad solicitudPorUser = repositorioAmistad.encontrarAmistadPorUsuarios(friendId, userId);

                if (solicitudPorUser == null) {
                    throw new Exception("Solicitud de amistad no encontrada");
                }
                solicitudPorUser.setEstado("aceptada");
                solicitudPorUser.setFechaAceptada(new Date());

                Boolean saved = repositorioAmistad.guardar(solicitudPorUser);
                return saved;
            }

        } catch (Exception error){
            throw new Exception(error.getMessage());
        }
    }

    @Override
    public boolean rechazarSolicitudDeAmistad(Long userId, Long friendId, Long requestId) throws Exception {
        Usuario usuario = repositorioUsuario.buscarUsuarioPorId(userId);
        if (usuario == null) {
            throw new Exception("Usuario no encontrado");
        }

        try {

            Amistad solicitudPorUser = repositorioAmistad.encontrarAmistadPorUsuarios(friendId, userId);

            if (solicitudPorUser == null) {
                throw new Exception("Solicitud de amistad no encontrada");
            }
            solicitudPorUser.setEstado("rechazada");

            Boolean saved = repositorioAmistad.guardar(solicitudPorUser);
            return saved;
        } catch (Exception error){
            throw new Exception(error.getMessage());
        }
    }

    @Override
    public List<Amistad> obtenerAmigos(Long userId) {
        return repositorioAmistad.listarAmigosPorUsuario(userId);
    }

    @Override
    public String verificacionDeAmistad(Long userId, Long friendId) throws Exception {
        Usuario usuario = repositorioUsuario.buscarUsuarioPorId(userId);
        Usuario friend = repositorioUsuario.buscarUsuarioPorId(friendId);
        try {

            if (usuario == null || friend == null) {
                throw new Exception("Usuario o amigo no encontrado");

            } else {
                String friends = repositorioAmistad.verificacionDeAmistad(userId, friendId);
                return friends;
            }

        } catch (Exception error){
            throw new Exception(error.getMessage());
        }
    }


}
