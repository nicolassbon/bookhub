package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.model.UsuarioGenero;

import java.util.List;

public interface ServicioUsuarioGenero {
    UsuarioGenero obtenerUsuarioGenero(Long usuarioId, Long generoId);
    void guardarUsuarioGenero(UsuarioGenero usuarioGenero);

    List<UsuarioGenero> obtenerGenerosDeUsuario(Long usuarioId);
    void crearOActualizarUsuarioGenero(Long usuarioId, Long generoId);
}
