package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.model.*;
import com.tallerwebi.infraestructura.service.ServicioOnboarding;
import com.tallerwebi.infraestructura.service.ServicioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/onboarding")
public class ControladorOnboarding {

    private final ServicioOnboarding servicioOnboarding;

    @Autowired
    public ControladorOnboarding(ServicioOnboarding servicioOnboarding) {
        this.servicioOnboarding = servicioOnboarding;
    }

    //    @RequestMapping(value = "/mostrarOnboarding/{id}", method = RequestMethod.GET)
//    public ModelAndView mostrarFormularioOnboarding(@PathVariable Long id) {
//        ModelMap model = new ModelMap();
//        model.put("userId", id);
//        List<Genero> generos = servicioOnboarding.obtenerGeneros();
//        model.put("generos", generos);
//        System.out.println(generos);
//        return new ModelAndView("onboarding", model);
//    }

    @RequestMapping(value = "/mostrarOnboarding/{id}/{paso}", method = RequestMethod.GET)
    public ModelAndView mostrarFormularioOnboarding(@PathVariable Long id, @PathVariable Integer paso) {
        ModelMap model = new ModelMap();
        model.put("userId", id);

        List<Genero> generos = servicioOnboarding.obtenerGeneros();
        model.put("generos", generos);

        List<Autor> autores = servicioOnboarding.obtenerAutores(); // Asegúrate de tener este método en tu servicio
        model.put("autores", autores);

        model.put("paso", paso); // Añade el paso actual
        return new ModelAndView("onboarding", model);
    }

    @RequestMapping(value = "/mostrarRegistroExitoso", method = RequestMethod.GET)
    public ModelAndView mostrarRegistroExitoso() {
        ModelMap model = new ModelMap();
        return new ModelAndView("registro-exitoso", model);
    }

    @RequestMapping(value = "/guardarGeneros", method = RequestMethod.POST)
    public String guardarGeneros(@RequestParam List<Long> generos) {
        System.out.println(generos + "generos");
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("USERID");
        if (userId != null) {
            servicioOnboarding.guardarGeneros(userId, generos);
            return "redirect:/onboarding/mostrarOnboarding/" + userId + "/2";

//            return "redirect:/onboarding/mostrarRegistroExitoso";
        } else {
            return "redirect:/login";
        }
    }

    @RequestMapping(value = "/guardarAutores", method = RequestMethod.POST)
    public String guardarAutores(@RequestParam List<Long> autores) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("USERID");
        if (userId != null) {
            servicioOnboarding.guardarAutores(userId, autores);
            return "redirect:/onboarding/mostrarOnboarding/" + userId + "/3";
        } else {
            return "redirect:/login";
        }
    }

    @RequestMapping(value = "/guardarMeta", method = RequestMethod.POST)
    public String guardarMeta(@RequestParam Long metaLibros, @RequestParam String redirectUrl) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("USERID");

        if (userId == null)
            return "redirect:/login";

        servicioOnboarding.guardarMeta(userId, metaLibros);

        return "redirect:" + redirectUrl;
    }

}
