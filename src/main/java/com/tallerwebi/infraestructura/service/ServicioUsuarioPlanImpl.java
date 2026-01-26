package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.excepcion.PlanNoEncontrado;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.model.Plan;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioPlan;
import com.tallerwebi.dominio.repository.RepositorioPlan;
import com.tallerwebi.dominio.repository.RepositorioUsuario;
import com.tallerwebi.dominio.repository.RepositorioUsuarioPlan;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ServicioUsuarioPlanImpl implements ServicioUsuarioPlan {

    private final ServicioValidacionPlanImpl servicioValidacionPlanImpl;
    private RepositorioUsuario repositorioUsuario;
    private RepositorioPlan repositorioPlan;
    private RepositorioUsuarioPlan repositorioUsuarioPlan;
    private ServicioPlan servicioPlan;
    private ServicioLogin servicioLogin;

    public ServicioUsuarioPlanImpl(RepositorioUsuario repositorioUsuario, RepositorioPlan repositorioPlan, RepositorioUsuarioPlan repositorioUsuarioPlan, ServicioValidacionPlanImpl servicioValidacionPlanImpl, ServicioPlan servicioPlan, ServicioLogin servicioLogin) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioPlan = repositorioPlan;
        this.repositorioUsuarioPlan = repositorioUsuarioPlan;
        this.servicioValidacionPlanImpl = servicioValidacionPlanImpl;
        this.servicioPlan = servicioPlan;
        this.servicioLogin = servicioLogin;
    }

    @Override
    public UsuarioPlan buscarUsuarioPlan(Long idUsuario) throws Exception {

        try {
            UsuarioPlan usuarioPlan = repositorioUsuarioPlan.buscarPlanPorUsuario(idUsuario);
            return usuarioPlan;

        } catch (Exception e){
            System.out.println("Error al buscar el usuario plan" + e.getMessage());
            return null;
        }
    }


    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    public void verificarPlanDelUsuario() {
        List<UsuarioPlan> usuariosPlan = repositorioUsuarioPlan.obtenerUsuariosPlan();

        Plan planBronce = repositorioPlan.buscarPlanBronce();
        for (UsuarioPlan up : usuariosPlan) {
            if (up.getFecha_plan_venc().before(new Date())) {
                up.setPlan(planBronce);
                repositorioUsuarioPlan.guardarUsuarioPlan(up);
            }
        }
    }

    @Override
    public void actualizarPlanDelUsuarioPlan(Long idDelUsuario, Long idPlan) throws PlanNoEncontrado, UsuarioInexistente {
        UsuarioPlan usuarioPlan = repositorioUsuarioPlan.buscarPlanPorUsuario(idDelUsuario);
        Usuario usuario = repositorioUsuario.buscarUsuarioPorId(idDelUsuario);
        Plan plan = repositorioPlan.buscarPlanPorId(idPlan);

        if (usuario == null) {
            throw new UsuarioInexistente();
        }

        if (plan == null) {
            throw new PlanNoEncontrado("No se encontro el plan con el ID: " + idPlan);
        }

        if(usuarioPlan.getPlan().getNombre().equals("PLATA") && plan.getNombre().equals("BRONCE")){
            UsuarioPlan nuevoUsuarioPlan = new UsuarioPlan();
            nuevoUsuarioPlan.setPlan(plan);
            nuevoUsuarioPlan.setPrecio(plan.getPrecio());
            nuevoUsuarioPlan.setUsuario(usuario);
            nuevoUsuarioPlan.setFecha_plan_adquirido(new Date());
            nuevoUsuarioPlan.setFecha_plan_venc(calcularFechaVencimientoDelPlanDelUsuario(plan));
            repositorioUsuarioPlan.guardarUsuarioPlan(nuevoUsuarioPlan);
        }

        if(usuarioPlan.getPlan().getNombre().equals("ORO") && plan.getNombre().equals("BRONCE")){
            UsuarioPlan nuevoUsuarioPlan = new UsuarioPlan();
            nuevoUsuarioPlan.setPlan(plan);
            nuevoUsuarioPlan.setPrecio(plan.getPrecio());
            nuevoUsuarioPlan.setUsuario(usuario);
            nuevoUsuarioPlan.setFecha_plan_adquirido(new Date());
            nuevoUsuarioPlan.setFecha_plan_venc(calcularFechaVencimientoDelPlanDelUsuario(plan));
            repositorioUsuarioPlan.guardarUsuarioPlan(nuevoUsuarioPlan);
        }

        if(usuarioPlan.getPlan().getNombre().equals("ORO") && plan.getNombre().equals("PLATA")){
            UsuarioPlan nuevoUsuarioPlan = new UsuarioPlan();
            nuevoUsuarioPlan.setPlan(plan);
            nuevoUsuarioPlan.setPrecio(plan.getPrecio());
            nuevoUsuarioPlan.setUsuario(usuario);
            nuevoUsuarioPlan.setFecha_plan_adquirido(new Date());
            nuevoUsuarioPlan.setFecha_plan_venc(calcularFechaVencimientoDelPlanDelUsuario(plan));
            repositorioUsuarioPlan.guardarUsuarioPlan(nuevoUsuarioPlan);
        }

        if(usuarioPlan.getPlan().getNombre().equals("BRONCE") && plan.getNombre().equals("PLATA")){
            UsuarioPlan nuevoUsuarioPlan = new UsuarioPlan();
            nuevoUsuarioPlan.setPlan(plan);
            nuevoUsuarioPlan.setPrecio(plan.getPrecio());
            nuevoUsuarioPlan.setUsuario(usuario);
            nuevoUsuarioPlan.setFecha_plan_adquirido(new Date());
            nuevoUsuarioPlan.setFecha_plan_venc(calcularFechaVencimientoDelPlanDelUsuario(plan));
            repositorioUsuarioPlan.guardarUsuarioPlan(nuevoUsuarioPlan);
        }

        if(usuarioPlan.getPlan().getNombre().equals("PLATA") && plan.getNombre().equals("ORO")){
            UsuarioPlan nuevoUsuarioPlan = new UsuarioPlan();
            nuevoUsuarioPlan.setPlan(plan);
            nuevoUsuarioPlan.setPrecio(servicioValidacionPlanImpl.calcularValidacionUsuarioPlan(plan,usuarioPlan));
            nuevoUsuarioPlan.setUsuario(usuario);
            nuevoUsuarioPlan.setFecha_plan_adquirido(new Date());
            nuevoUsuarioPlan.setFecha_plan_venc(calcularFechaVencimientoDelPlanDelUsuario(plan));
            repositorioUsuarioPlan.guardarUsuarioPlan(nuevoUsuarioPlan);
        }

        if(usuarioPlan.getPlan().getNombre().equals("BRONCE") && plan.getNombre().equals("ORO")){
            UsuarioPlan nuevoUsuarioPlan = new UsuarioPlan();
            nuevoUsuarioPlan.setPlan(plan);
            nuevoUsuarioPlan.setPrecio(plan.getPrecio());
            nuevoUsuarioPlan.setUsuario(usuario);
            nuevoUsuarioPlan.setFecha_plan_adquirido(new Date());
            nuevoUsuarioPlan.setFecha_plan_venc(calcularFechaVencimientoDelPlanDelUsuario(plan));
            repositorioUsuarioPlan.guardarUsuarioPlan(nuevoUsuarioPlan);
        }

    }

    @Override
    public Boolean validarMercadopago(Long idDelUsuario,Long idPlan) throws UsuarioInexistente {
        UsuarioPlan usuarioPlan = repositorioUsuarioPlan.buscarPlanPorUsuario(idDelUsuario);
        Plan plan = repositorioPlan.buscarPlanPorId(idPlan);

        if (usuarioPlan == null) {
            throw new UsuarioInexistente();
        }

        if (plan == null) {
            throw new PlanNoEncontrado("No se encontro el plan con el ID: " + idPlan);
        }

        return (usuarioPlan.getPlan().getNombre().equals("PLATA") && plan.getNombre().equals("ORO")) || (usuarioPlan.getPlan().getNombre().equals("BRONCE") && (plan.getNombre().equals("ORO") || plan.getNombre().equals("PLATA")));
    }

    @Override
    public void crearPlanInicio (String email, Long planid){

        Usuario usuario = servicioLogin.buscar(email);

        UsuarioPlan nuevoUsuarioPlan = new UsuarioPlan();
        Plan plan = servicioPlan.buscarPlanPorId(planid);
        nuevoUsuarioPlan.setPlan(plan);
        nuevoUsuarioPlan.setPrecio(plan.getPrecio());
        nuevoUsuarioPlan.setUsuario(usuario);
        nuevoUsuarioPlan.setFecha_plan_adquirido(new Date());
        nuevoUsuarioPlan.setFecha_plan_venc(calcularFechaVencimientoDelPlanDelUsuario(plan));
        repositorioUsuarioPlan.guardarUsuarioPlan(nuevoUsuarioPlan);
    }

    public Date calcularFechaVencimientoDelPlanDelUsuario(Plan plan) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if (!plan.getNombre().equals("BRONCE")) {
            calendar.add(Calendar.MONTH, 1);
        } else {
            calendar.add(Calendar.YEAR, 100);
        }
        return calendar.getTime();
    }


}
