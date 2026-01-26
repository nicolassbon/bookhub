package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.excepcion.PlanNoEncontrado;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.model.Plan;
import com.tallerwebi.dominio.model.UsuarioPlan;

import javax.mail.MessagingException;
import java.util.Date;

public interface ServicioUsuarioPlan {
    UsuarioPlan buscarUsuarioPlan(Long idUsuario) throws Exception;

    void verificarPlanDelUsuario();
    void actualizarPlanDelUsuarioPlan(Long idDelUsuario, Long idPlan) throws PlanNoEncontrado, UsuarioInexistente;
    Boolean validarMercadopago(Long idDelUsuario, Long idPlan) throws UsuarioInexistente;
    void crearPlanInicio (String email, Long planid);

}
