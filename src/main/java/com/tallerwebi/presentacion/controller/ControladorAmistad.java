package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.excepcion.LibroNoEncontrado;
import com.tallerwebi.infraestructura.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/solicitud-amistad")

public class ControladorAmistad {
    private ServicioAmistad servicioAmistad;
    private ServicioNotificacion servicioNotificacion;
    private ServicioUsuarioNotificacion servicioUsuarioNotificacion;

    @Autowired
    public ControladorAmistad(ServicioAmistad servicioAmistad, ServicioNotificacion servicioNotificacion, ServicioUsuarioNotificacion servicioUsuarioNotificacion) {
        this.servicioAmistad = servicioAmistad;
        this.servicioNotificacion = servicioNotificacion;
        this.servicioUsuarioNotificacion = servicioUsuarioNotificacion;
    }

    @RequestMapping(value = "/{friendId}", method = RequestMethod.POST)
    public String enviarSolicitudAmistad(@PathVariable Long friendId) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("USERID");
        String username = (String) session.getAttribute("USERNAME");

        try {
            System.out.println(userId + " userId que quiere enviar la solicitud");
            System.out.println(friendId + " friendId que tiene que aceptarla");

            Boolean sent = servicioAmistad.enviarSolicitudDeAmistad(userId, friendId);
            if (sent) {
                servicioNotificacion.crearNotificacion(friendId, 2L, "Has recibido una solicitud de Amistad de " + username, userId);
                return "amigoAgregadoCorrectamente";
            } else {
                return "error";
            }
        } catch (Exception e) {
            return "error";
        }
    }


    @RequestMapping(value = "/aceptar-solicitud/{requestId}", method = RequestMethod.POST)
    public ModelAndView aceptarSolicitudAmistad(@PathVariable Long requestId) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("USERID");
        String username = (String) session.getAttribute("USERNAME");

        try {
            Long friendId = servicioUsuarioNotificacion.obtenerElIdDeAmigoPorIdDeNotificacion(userId, requestId);

            Boolean sent = servicioAmistad.aceptarSolicitudDeAmistad(userId,friendId, requestId);
            if (sent) {
                servicioNotificacion.editarNombreNotificacion(requestId, "Has aceptado la solicitud de " + username, 5L);
                servicioNotificacion.crearNotificacion(friendId, 5L, username + " ha aceptado tu solicitud de amistad", userId);
                return new ModelAndView("redirect:/home");

            } else {
                return new ModelAndView("redirect:/error");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ModelAndView("redirect:/error");
        }
    }

    @RequestMapping(value = "/rechazar-solicitud/{requestId}", method = RequestMethod.POST)
    public ModelAndView rechazarSolicitudAmistad(@PathVariable Long requestId) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("USERID");
        String username = (String) session.getAttribute("USERNAME");

        try {
            System.out.println(requestId + " request id");

            Long friendId = servicioUsuarioNotificacion.obtenerElIdDeAmigoPorIdDeNotificacion(userId, requestId);
            System.out.println(userId + " userid");
            System.out.println(friendId + " friend id");
            Boolean sent = servicioAmistad.rechazarSolicitudDeAmistad(userId, friendId, requestId);

            System.out.println(sent + " sent");
            if (sent) {
                servicioNotificacion.editarNombreNotificacion(requestId, "Has rechazado la solicitud de amistad", 5L);
                servicioNotificacion.crearNotificacion(friendId, 5L, username + " ha rechazado tu solicitud de amistad", userId);
                return new ModelAndView("redirect:/home");
            } else {
                return new ModelAndView("redirect:/error");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ModelAndView("redirect:/error");
        }
    }

    @RequestMapping(value = "/eliminar-amistad/{friendId}", method = RequestMethod.POST)
    public ModelAndView eliminarAmistad(@PathVariable Long friendId, RedirectAttributes redirectAttributes, HttpSession session) {
        Long userId = (Long) session.getAttribute("USERID");

        try {
            Boolean deleted = servicioAmistad.eliminarSolicitudAmistad(userId, friendId);

            if (deleted) {
                redirectAttributes.addFlashAttribute("notyMessage", "Amistad eliminada correctamente");
                redirectAttributes.addFlashAttribute("notyType", "success");
                return new ModelAndView("redirect:/listado-amigos");
            } else {
                redirectAttributes.addFlashAttribute("notyMessage", "No se pudo eliminar la amistad.");
                redirectAttributes.addFlashAttribute("notyType", "error");
                return new ModelAndView("redirect:/listado-amigos");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("notyMessage", "Ocurrió un error al intentar eliminar la amistad.");
            redirectAttributes.addFlashAttribute("notyType", "error");
            return new ModelAndView("redirect:/error");
        }
    }


}
