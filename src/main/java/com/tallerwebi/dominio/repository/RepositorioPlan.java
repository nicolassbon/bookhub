package com.tallerwebi.dominio.repository;

import com.tallerwebi.dominio.model.Plan;
import com.tallerwebi.dominio.model.UsuarioPlan;

public interface RepositorioPlan {
    Plan buscarPlanPorId(Long idPlan);
    Plan buscarPlanBronce();

}
