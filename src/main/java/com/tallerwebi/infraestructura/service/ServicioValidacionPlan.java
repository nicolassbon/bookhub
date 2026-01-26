package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.model.Accion;
import com.tallerwebi.dominio.model.Plan;
import com.tallerwebi.dominio.model.UsuarioPlan;

public interface ServicioValidacionPlan {
    Double calcularValidacionUsuarioPlan(Plan planAcambiar, UsuarioPlan planActual);
    Boolean puedeRealizarAccion(UsuarioPlan usuarioPlan, Accion accion);
}
