package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.model.*;
import com.tallerwebi.dominio.repository.RepositorioPublicacion;
import com.tallerwebi.dominio.repository.RepositorioUsuario;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@Transactional

public class ServicioPublicacionImpl implements ServicioPublicacion{


    private final RepositorioPublicacion repositorioPublicacion;
    private final RepositorioUsuario repositorioUsuario;

    public ServicioPublicacionImpl(RepositorioPublicacion repositorioPublicacion, RepositorioUsuario repositorioUsuario) {
        this.repositorioPublicacion = repositorioPublicacion;
        this.repositorioUsuario = repositorioUsuario;
    }


    @Override
    public void crearPublicacion(Long userId, String mensaje) throws MessagingException {
        try {
            Usuario usuario = repositorioUsuario.buscarUsuarioPorId(userId);

            if (usuario == null) {
                throw new Exception ("No existe usuario con ID " + userId);
            }
            Publicacion publicacion = new Publicacion();

            publicacion.setMensaje(mensaje);
            publicacion.setUsuario(usuario);
            publicacion.setFechaHora(new Date());

            repositorioPublicacion.guardar(publicacion);
        }catch (Exception error){
            throw new MessagingException(error.getMessage());
        }
    }
    @Override
    public List<Publicacion> obtenerTodasPublicacionesDeAmigos(Long userId) throws MessagingException {
        try {
            List<Publicacion> publicacionesDeAmigos = repositorioPublicacion.obtenerTodasPublicacionesDeAmigos(userId);
            System.out.println(publicacionesDeAmigos + " UISUARO AMIGO PUBLICACION LISTADO");
            return publicacionesDeAmigos;
        } catch (Exception e) {
            throw new MessagingException(e.getMessage());
        }
    }
}
