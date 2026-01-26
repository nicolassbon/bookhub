package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.excepcion.ListaVacia;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.model.Logro;
import com.tallerwebi.dominio.model.Publicacion;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioLogro;
import com.tallerwebi.infraestructura.service.ServicioComentarioPublicacion;
import com.tallerwebi.infraestructura.service.ServicioPublicacion;
import com.tallerwebi.infraestructura.service.ServicioUsuario;
import com.tallerwebi.infraestructura.service.ServicioUsuarioLogro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller

@RequestMapping("/publicaciones")
public class ControladorPublicacion {

    private ServicioPublicacion servicioPublicacion;
    private ServicioComentarioPublicacion servicioComentarioPublicacion;

    @Autowired
    public ControladorPublicacion(ServicioPublicacion servicioPublicacion, ServicioComentarioPublicacion servicioComentarioPublicacion) {
        this.servicioPublicacion = servicioPublicacion;
        this.servicioComentarioPublicacion = servicioComentarioPublicacion;
    }

    @PostMapping("/crear")
    public ModelAndView crearPublicacion(@RequestParam("contenido") String mensaje) {

        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attr.getRequest();
            HttpSession session = request.getSession();
            Long userId = (Long) session.getAttribute("USERID");
            servicioPublicacion.crearPublicacion(userId, mensaje);
        } catch (Exception e) {
            return new ModelAndView("redirect:/error");
        }

        return new ModelAndView("redirect:/home");
    }

    @PostMapping("/crear/comentario/{id}")
    public ModelAndView crearComentarioPublicacion(@RequestParam("comentario") String mensaje,@PathVariable Long id) {

        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attr.getRequest();
            HttpSession session = request.getSession();
            Long userId = (Long) session.getAttribute("USERID");

            System.out.println("userid " + id);
            System.out.println("msg " + mensaje);

            servicioComentarioPublicacion.crearComentarioPublicacion(id, mensaje, userId);
        } catch (Exception e) {
            return new ModelAndView("redirect:/error");
        }

        return new ModelAndView("redirect:/home");
    }

}
