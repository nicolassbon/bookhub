package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.excepcion.ListaVacia;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.model.Accion;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioLogro;
import com.tallerwebi.dominio.model.UsuarioPlan;
import com.tallerwebi.infraestructura.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/logros")
public class ControladorLogro {

    private ServicioUsuario servicioUsuario;
    private ServicioUsuarioLogro servicioUsuarioLogro;
    private ServicioUsuarioPlan servicioUsuarioPlan;
    private ServicioValidacionPlan servicioValidacionPlan;

    @Autowired
    public ControladorLogro(ServicioUsuario servicioUsuario, ServicioUsuarioLogro servicioUsuarioLogro, ServicioUsuarioPlan servicioUsuarioPlan, ServicioValidacionPlan servicioValidacionPlan) {
        this.servicioUsuario = servicioUsuario;
        this.servicioUsuarioLogro = servicioUsuarioLogro;
        this.servicioUsuarioPlan = servicioUsuarioPlan;
        this.servicioValidacionPlan = servicioValidacionPlan;
    }

    @RequestMapping("/mis-logros")
    public ModelAndView mostrarLogros(HttpServletRequest request) {
        ModelMap model = new ModelMap();

        Long userId = (Long) request.getSession().getAttribute("USERID");

        try {
            Usuario usuario = servicioUsuario.buscarUsuarioPorId(userId);
            model.addAttribute("usuario", usuario);

            UsuarioPlan usuarioPlan = servicioUsuarioPlan.buscarUsuarioPlan(userId);
            model.addAttribute("usuarioPlan", usuarioPlan);

            if(!servicioValidacionPlan.puedeRealizarAccion(usuarioPlan, Accion.OBTENER_LOGROS)){
                model.addAttribute("logrosUsuario", null);
                model.addAttribute("mensajeDeRestriccion", "Estas en el plan " + usuarioPlan.getPlan().getNombre() + " actualiza tu plan a ORO para obtener logros.");
            }else {
                List<UsuarioLogro> logrosUsuario = servicioUsuarioLogro.obtenerLogrosDelUsuario(usuario);
                model.addAttribute("logrosUsuario", logrosUsuario);
            }


        } catch (UsuarioInexistente e) {
            return new ModelAndView("redirect:/login");
        } catch (ListaVacia e) {
            model.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new ModelAndView("mostrar-logros", model);
    }

    @RequestMapping("/guardarLogro")
    public ModelAndView guardarLogro(@RequestParam("nombre") String nombre,
                                     @RequestParam("objetivo") Integer objetivo,
                                     @RequestParam(value = "plazo", required = false) Integer plazo,
                                     HttpServletRequest request) {
        ModelMap model = new ModelMap();

        Long userId = (Long) request.getSession().getAttribute("USERID");

        try {
            Usuario usuario = servicioUsuario.buscarUsuarioPorId(userId);
            model.addAttribute("usuario", usuario);

            if (plazo == null) {
                plazo = 0;
            }
            servicioUsuarioLogro.guardarLogroPersonalizado(usuario, nombre, objetivo, plazo);

        } catch (UsuarioInexistente e) {
            return new ModelAndView("redirect:/login");
        }

        return new ModelAndView("redirect:/logros/mis-logros", model);
    }

    @RequestMapping("/eliminar-logro")
    public ModelAndView eliminarLogro(@RequestParam("logroId") Long logroId, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        Long userId = (Long) request.getSession().getAttribute("USERID");

        if (servicioUsuarioLogro.eliminarLogro(userId,logroId)) {
            redirectAttributes.addFlashAttribute("mensaje", "Logro eliminado");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "No se pudo eliminar el logro");
        }

        return new ModelAndView("redirect:/logros/mis-logros");
    }
}
