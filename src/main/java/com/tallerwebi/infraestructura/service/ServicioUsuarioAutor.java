package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.model.UsuarioAutor;
import com.tallerwebi.dominio.model.UsuarioGenero;

public interface ServicioUsuarioAutor {
    UsuarioAutor obtenerUsuarioAutor(Long usuarioId, Long autorId);
    void guardarUsuarioAutor(UsuarioAutor usuarioAutor);
    void crearOActualizarUsuarioAutor(Long usuarioId, Long autorId);

}
