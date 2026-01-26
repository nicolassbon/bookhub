package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.excepcion.ListaVacia;
import com.tallerwebi.dominio.model.Logro;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioLogro;

import java.util.List;

public interface ServicioUsuarioLogro {
    void guardarLogroPersonalizado(Usuario usuario, String nombre,Integer objetivoLibros, Integer plazoDias);
    void actualizarEstadoLogros(Usuario usuario);
    List<UsuarioLogro> obtenerLogrosDelUsuario(Usuario usuario) throws ListaVacia;

    Boolean eliminarLogro(Long userId, Long logroId);
}
