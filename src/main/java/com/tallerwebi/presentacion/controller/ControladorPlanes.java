package com.tallerwebi.presentacion.controller;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.model.Plan;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.infraestructura.service.ServicioMercadoPago;
import com.tallerwebi.infraestructura.service.ServicioPlan;
import com.tallerwebi.infraestructura.service.ServicioUsuario;
import com.tallerwebi.dominio.model.UsuarioPlan;
import com.tallerwebi.infraestructura.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/planes")
public class ControladorPlanes {


    private ServicioPlan servicioPlan;
    private ServicioUsuario servicioUsuario;
    private ServicioUsuarioPlan servicioUsuarioPlan;
    private ServicioValidacionPlan servicioValidacionPlan;
    private ServicioMercadoPago servicioMercadoPago;

    @Autowired
    public ControladorPlanes(ServicioPlan servicioPlan, ServicioUsuario servicioUsuario, ServicioUsuarioPlan servicioUsuarioPlan, ServicioValidacionPlan servicioValidacionPlan,ServicioMercadoPago servicioMercadoPago) {
        this.servicioPlan = servicioPlan;
        this.servicioUsuario = servicioUsuario;
        this.servicioMercadoPago = servicioMercadoPago;
        this.servicioUsuarioPlan = servicioUsuarioPlan;
        this.servicioValidacionPlan = servicioValidacionPlan;
    }


    @RequestMapping("/mostrar")
    public String mostrar(ModelMap model, HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("USERID");

        if (userId != null) {
            //Usuario usuario = servicioUsuario.buscarUsuarioPorId(userId);
            //session.setAttribute("planAdquirido", usuario.getPlan().getId());
            UsuarioPlan usuarioPlan = servicioUsuarioPlan.buscarUsuarioPlan(userId);
            session.setAttribute("planAdquirido", usuarioPlan.getPlan().getId());
            model.addAttribute("usuario", usuarioPlan);
            model.addAttribute("fechaPlanVenc", usuarioPlan.getFecha_plan_venc());
        }

        return "planes";
    }

    @RequestMapping(value = "/actualizarPlan/{planId}", method = RequestMethod.POST)
    public String actualizarPlan(HttpServletRequest request, @PathVariable Long planId, ModelMap model, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("USERID");

        try {
            Usuario usuario = servicioUsuario.buscarUsuarioPorId(userId);

            servicioUsuarioPlan.actualizarPlanDelUsuarioPlan(userId, planId);
            UsuarioPlan usuarioPlan = servicioUsuarioPlan.buscarUsuarioPlan(userId);

            session.setAttribute("planAdquirido", planId);

            model.addAttribute("usuario", usuario);
            model.addAttribute("usuarioPlan", usuarioPlan);


            if (usuario.getPlan() != null) {
                model.addAttribute("plan", usuario.getPlan().getNombre());
            }

            return "redirect:/planes/mostrar";

        } catch (UsuarioInexistente e) {
            return "redirect:/login";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    @RequestMapping(value = "/detalleActualizacion/{planId}" , method = RequestMethod.GET)
    public ModelAndView detalleActualizacion(HttpServletRequest request, @PathVariable Long planId, ModelMap model) {
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("USERID");

        try {

            Plan planAcambiar = servicioPlan.buscarPlanPorId(planId);
            System.out.println(planAcambiar.getNombre() + "NOMBRE DE PLAN A CAMBIARRRR");
            UsuarioPlan usuarioPlan = servicioUsuarioPlan.buscarUsuarioPlan(userId);


            System.out.println(usuarioPlan.getPlan().getNombre() + "NOMBRE DE USUARIOPLAN A CAMBIARRRR");

            Double validacionDias = servicioValidacionPlan.calcularValidacionUsuarioPlan(planAcambiar, usuarioPlan);

            model.addAttribute("plan", planAcambiar);
            model.addAttribute("usuario", usuarioPlan);
            model.addAttribute("validacionDias", validacionDias);
            session.setAttribute("validacionDias", validacionDias); // Almacenar en sesión
            model.addAttribute("usuarioplan", usuarioPlan);

            Boolean irAMercadopago = servicioUsuarioPlan.validarMercadopago(userId, planId);
            model.addAttribute("irAMercadopago", irAMercadopago);

            return new ModelAndView("detalleActualizacionPlan");

        } catch (Exception e) {
            return new ModelAndView("redirect:/login") ;
        }


    }

    // Se debe pasar el id del plan
    @PostMapping("/pagar/{planId}")
    public String pagarPlan(@PathVariable Long planId, Model model,HttpSession session) {
        try {
            Double valorPago = (Double) session.getAttribute("validacionDias");
            String linkDePago = servicioMercadoPago.crearPreferencia(planId,valorPago);
            model.addAttribute("linkDePago", linkDePago);
            System.out.println("Link de pago: " + linkDePago);
            return "redireccionarPago"; // Vista para confirmar y redirigir
        } catch (MPException | MPApiException e) {
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/confirmarActualizar/{planId}")
    public String confirmarActualizarPlan(@PathVariable Long planId, Model model) {
        model.addAttribute("planId", planId);
        return "confirmarActualizarPlan";
    }
}
