package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.model.Logro;
import com.tallerwebi.dominio.model.Usuario;

import java.util.List;

public interface ServicioLogro {
    void verificarYAsignarLogrosPredefinidos(Usuario usuario);
}
