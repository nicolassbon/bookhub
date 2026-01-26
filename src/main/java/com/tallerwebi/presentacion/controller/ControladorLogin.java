package com.tallerwebi.presentacion.controller;

import com.tallerwebi.infraestructura.service.ServicioLogin;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.infraestructura.service.ServicioLogro;
import com.tallerwebi.infraestructura.service.ServicioUsuarioLogro;
import com.tallerwebi.presentacion.DatosLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;

@Controller
public class ControladorLogin {

    private ServicioLogin servicioLogin;
    private ServicioLogro servicioLogro;
    private ServicioUsuarioLogro servicioUsuarioLogro;

    @Autowired
    public ControladorLogin(ServicioLogin servicioLogin, ServicioLogro servicioLogro, ServicioUsuarioLogro servicioUsuarioLogro) {
        this.servicioLogin = servicioLogin;
        this.servicioLogro = servicioLogro;
        this.servicioUsuarioLogro = servicioUsuarioLogro;
    }

    @RequestMapping("/login")
    public ModelAndView irALogin() {

        ModelMap modelo = new ModelMap();
        modelo.put("datosLogin", new DatosLogin());
        return new ModelAndView("login", modelo);
    }

    @RequestMapping(path = "/validar-login", method = RequestMethod.POST)
    public ModelAndView validarLogin(@ModelAttribute("datosLogin") DatosLogin datosLogin, HttpServletRequest request) {
        ModelMap model = new ModelMap();

        Usuario usuarioBuscado = servicioLogin.consultarUsuario(datosLogin.getEmail(), datosLogin.getPassword());
        if (usuarioBuscado != null) {
            request.getSession().setAttribute("ROL", usuarioBuscado.getRol());
            request.getSession().setAttribute("USERID", usuarioBuscado.getId());
            request.getSession().setAttribute("USERNAME", usuarioBuscado.getNombre());
            request.getSession().setAttribute("USERNICKNAME", usuarioBuscado.getNombreUsuario());
            request.getSession().setAttribute("ANIOACTUAL", LocalDate.now().getYear());
            request.getSession().setAttribute("PLANACTUAL", usuarioBuscado.getPlan().getNombre());


            //Inicializa los logros predefinidos del usuario cuando inicia sesion
            servicioLogro.verificarYAsignarLogrosPredefinidos(usuarioBuscado);

            //Actualiza los logros cuando inicia sesion
            servicioUsuarioLogro.actualizarEstadoLogros(usuarioBuscado);

            System.out.println(usuarioBuscado.getTokenRecuperacion() + " token recuperacion");
            return new ModelAndView("redirect:/home");
        } else {
            model.put("error", "Usuario o clave incorrecta");
        }
        return new ModelAndView("login", model);
    }

    @RequestMapping(path = "/nuevo-usuario", method = RequestMethod.GET)
    public ModelAndView nuevoUsuario() {
        ModelMap model = new ModelMap();
        model.put("usuario", new Usuario());
        return new ModelAndView("nuevo-usuario", model);
    }

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public ModelAndView inicio() {
        return new ModelAndView("redirect:/login");
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public ModelAndView logout(HttpSession session) {
        // Invalida la session actual
        session.invalidate();
        // Redirige al login
        return new ModelAndView("redirect:/login");
    }
}

