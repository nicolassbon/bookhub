package com.tallerwebi.dominio.repository;

import com.tallerwebi.dominio.model.UsuarioPlan;

import java.util.List;

public interface RepositorioUsuarioPlan {
    List<UsuarioPlan> obtenerUsuariosPlan();
    UsuarioPlan buscarPlanPorUsuario(Long idUsuario);
    void guardarUsuarioPlan(UsuarioPlan usuarioPlan);
}
