package com.tallerwebi.presentacion.controller;

import com.tallerwebi.infraestructura.service.ServicioRecuperarContrasena;
import com.tallerwebi.infraestructura.service.ServicioRecuperarContrasenaImpl;
import com.tallerwebi.presentacion.DatosNuevaContrasena;
import com.tallerwebi.presentacion.DatosRecuperarContrasena;
import com.tallerwebi.presentacion.DatosVerificacionCodigo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;

@Controller
public class ControladorRecuperarContrasena {

    private final ServicioRecuperarContrasenaImpl servicioRecuperarContrasena;

    @Autowired
    public ControladorRecuperarContrasena(ServicioRecuperarContrasenaImpl servicioRecuperarContrasena) {
        this.servicioRecuperarContrasena = servicioRecuperarContrasena;
    }

    @RequestMapping(path = "/verificar-codigo", method = RequestMethod.POST)
    public ModelAndView verificarCodigo(@RequestParam("codigo") String codigo, @RequestParam("email") String email, HttpSession session) {
        ModelMap model = new ModelMap();

        try {
            servicioRecuperarContrasena.verificarCodigoDeRecuperacion(codigo, email);
        } catch (MessagingException e) {
            model.put("error", e.getMessage());

            model.put("verificacionCodigo", new DatosVerificacionCodigo());
            model.put("email", email);

            return new ModelAndView("verificar-codigo", model);
        }
        return new ModelAndView("redirect:/nueva-contrasena");
    }


    @RequestMapping(path = "/nueva-contrasena", method = RequestMethod.POST)
    public ModelAndView nuevaContrasena(@RequestParam("contrasena") String contrasena, @RequestParam("confirmacionContrasena") String confirmacionContrasena, @RequestParam("email") String email) throws MessagingException {
        ModelMap model = new ModelMap();

        try{
            servicioRecuperarContrasena.confirmacionAmbasContrasenas(contrasena, confirmacionContrasena, email);

        } catch (Exception e) {
            model.put("nuevaContrasena", new DatosNuevaContrasena());
            model.put("email", email);
            model.put("error", e.getMessage());
            return new ModelAndView("nueva-contrasena", model);

        }
        return new ModelAndView("redirect:/codigo-verificado-ok");
    }

    @RequestMapping(path = "/recuperar-contrasena", method = RequestMethod.POST)
    public ModelAndView recuperarContrasena(@RequestParam("email") String email, HttpSession session) throws MessagingException {
        ModelMap model = new ModelMap();
        try {
            servicioRecuperarContrasena.generarTokenRecuperacion(email);
            session.setAttribute("email", email);
        } catch (Exception e) {
            model.put("error", e.getMessage());

            model.put("datosRecuperarContrasena", new DatosRecuperarContrasena());
            model.put("email", email);
            return new ModelAndView("recuperar-contrasena", model);

        }
        return new ModelAndView("redirect:/verificar-codigo");
    }


    @RequestMapping(path = "/recuperar-contrasena", method = RequestMethod.GET)
    public ModelAndView recuperarContrasena() {
        ModelMap model = new ModelMap();
        model.put("datosRecuperarContrasena", new DatosRecuperarContrasena());
        return new ModelAndView("recuperar-contrasena", model);
    }

    @RequestMapping(path = "/verificar-codigo", method = RequestMethod.GET)
    public ModelAndView verificarCodigo(HttpSession session) {
        ModelMap model = new ModelMap();
        model.put("verificacionCodigo", new DatosVerificacionCodigo());
        String email = (String) session.getAttribute("email");
        model.put("email", email);
        return new ModelAndView("verificar-codigo", model);
    }

    @RequestMapping(path = "/codigo-verificado-ok", method = RequestMethod.GET)
    public ModelAndView codigoVerificadoOk() {
        ModelMap model = new ModelMap();
        return new ModelAndView("codigo-verificado-ok", model);
    }
    @RequestMapping(path = "/nueva-contrasena", method = RequestMethod.GET)
    public ModelAndView nuevaContrasena(HttpSession session) {
        ModelMap model = new ModelMap();
        model.put("nuevaContrasena", new DatosNuevaContrasena());
        String email = (String) session.getAttribute("email");
        model.put("email", email);
        return new ModelAndView("nueva-contrasena", model);
    }

}
