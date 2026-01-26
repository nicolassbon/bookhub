package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.excepcion.PlanNoEncontrado;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.model.Plan;
import com.tallerwebi.dominio.model.Usuario;
import org.springframework.stereotype.Service;


public interface ServicioPlan {
   Plan buscarPlanPorId(Long planId);
   //void actualizarPlanDelUsuario(Long idDelPlan, Long idUsuario) throws PlanNoEncontrado, UsuarioInexistente;
  //void verificarPlanes();


}
