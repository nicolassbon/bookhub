package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.excepcion.PlanNoEncontrado;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.model.Plan;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.repository.RepositorioPlan;
import com.tallerwebi.dominio.repository.RepositorioUsuario;
import com.tallerwebi.dominio.repository.RepositorioUsuarioPlan;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ServicioPlanImpl implements ServicioPlan {

    private RepositorioUsuario repositorioUsuario;
    private RepositorioPlan repositorioPlan;
    private RepositorioUsuarioPlan repositorioUsuarioPlan;

    public ServicioPlanImpl(RepositorioUsuario repositorioUsuario, RepositorioPlan repositorioPlan, RepositorioUsuarioPlan repositorioUsuarioPlan) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioPlan = repositorioPlan;
        this.repositorioUsuarioPlan = repositorioUsuarioPlan;
    }

    @Override
    public Plan buscarPlanPorId(Long planId){
        System.out.println("Buscando plan por id en SERVICIOOO: " + planId);
       Plan plan = repositorioPlan.buscarPlanPorId(planId);
       return plan;
    }


    /*
    @Override
    public void actualizarPlanDelUsuario(Long idDelPlan, Long idUsuario) throws PlanNoEncontrado, UsuarioInexistente {

            Plan plan = repositorioPlan.buscarPlanPorId(idDelPlan);
            Usuario usuario =  repositorioUsuario.buscarUsuarioPorId(idUsuario);

            if(usuario == null) {
                throw new UsuarioInexistente();
            }

            if(plan == null) {
                throw new PlanNoEncontrado("No se encontro el plan con el ID: " + idDelPlan);
            }

            usuario.setPlan(plan);
            usuario.setFecha_plan_adquirido(new Date());
            usuario.setFecha_plan_venc(calcularFechaVencimiento(plan));
            repositorioUsuario.guardarUsuario(usuario);

    }



    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    public void verificarPlanes() {
        List<Usuario> usuarios = repositorioUsuario.obtenerUsuarios();

        Plan planBronce = repositorioPlan.buscarPlanBronce();
        for (Usuario usuario : usuarios) {
            if (usuario.getFecha_plan_venc().before(new Date())) {
                usuario.setPlan(planBronce);
                repositorioUsuario.guardarUsuario(usuario);
            }
        }
    }

    private Date calcularFechaVencimiento(Plan plan) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if (!plan.getNombre().equals("BRONCE")) {
            calendar.add(Calendar.MONTH, 1);
        } else {
            calendar.add(Calendar.YEAR, 100);
        }
        return calendar.getTime();
    }

     */
}
