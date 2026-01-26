package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.excepcion.LibroNoEncontrado;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.model.Amistad;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.infraestructura.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller

public class ControladorListadoAmigos {
    private ServicioAmistad servicioAmistad;

    @Autowired
    public ControladorListadoAmigos(ServicioAmistad servicioAmistad) {
        this.servicioAmistad = servicioAmistad;
    }

    @RequestMapping(value = "/listado-amigos", method = RequestMethod.GET)
    public ModelAndView listadoAmigos() {
        ModelMap model = new ModelMap();
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        HttpSession session = request.getSession();
        Long idUsuario = (Long) session.getAttribute("USERID");

        try {

            List<Amistad> listaAmigos = servicioAmistad.obtenerAmigos(idUsuario);

            model.addAttribute("listaAmigos", listaAmigos);
            model.addAttribute("mostrarListado", true);

            return new ModelAndView("listadoAmigos", model);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ModelAndView("redirect:/error");
        }
    }

}
