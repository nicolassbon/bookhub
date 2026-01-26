package com.tallerwebi.infraestructura.service;

import javax.mail.MessagingException;

public interface ServicioRecuperarContrasena {
    String generarTokenRecuperacion(String email) throws MessagingException;
    String verificarCodigoDeRecuperacion(String codigo, String email) throws MessagingException;
    String confirmacionAmbasContrasenas(String contrasena, String confirmacionContrasena, String email) throws MessagingException;
}
