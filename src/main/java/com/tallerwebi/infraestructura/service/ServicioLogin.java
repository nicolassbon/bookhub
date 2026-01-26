package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;

import java.time.LocalDate;

public interface ServicioLogin {

    Usuario consultarUsuario(String email, String password);
    void registrar(String email, String password, String nombreUsuario, String nombre, LocalDate fechaNacimiento) throws UsuarioExistente, IllegalArgumentException;
    Usuario buscar(String email);
}
