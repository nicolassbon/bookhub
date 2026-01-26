package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.excepcion.ReseniaInexistente;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.model.Comentario;
import com.tallerwebi.dominio.model.LikeDislike;
import com.tallerwebi.dominio.model.Resenia;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.infraestructura.service.ServicioComentario;
import com.tallerwebi.infraestructura.service.ServicioResenia;
import com.tallerwebi.infraestructura.service.ServicioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/comentarios")
public class ControladorComentario {

    private ServicioComentario servicioComentario;
    private ServicioUsuario servicioUsuario;
    private ServicioResenia servicioResenia;

    @Autowired
    public ControladorComentario(ServicioComentario servicioComentario, ServicioUsuario servicioUsuario, ServicioResenia servicioResenia) {
        this.servicioComentario = servicioComentario;
        this.servicioUsuario = servicioUsuario;
        this.servicioResenia = servicioResenia;
    }

    @RequestMapping(value = "/{id}/guardarComentario", method = RequestMethod.POST)
    public ModelAndView guardarComentario(@PathVariable Long id, @RequestParam String texto, HttpServletRequest request) {
        Long userId = (Long) request.getSession().getAttribute("USERID");

        try {
            Usuario usuario = servicioUsuario.buscarUsuarioPorId(userId);
            Resenia resenia = servicioResenia.obtenerReseniaPorId(id);

            servicioComentario.guardarComentario(usuario, resenia, texto);

            return new ModelAndView("redirect:/resenias/" + id);
        } catch (ReseniaInexistente e) {
            return new ModelAndView("redirect:/resenias/" + id).addObject("errorResenia", e.getMessage());
        } catch (UsuarioInexistente e) {
            return new ModelAndView("redirect:/login");
        }
    }

    @RequestMapping(value = "/eliminar/{id}", method = RequestMethod.POST)
    public ModelAndView eliminarComentario(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        Long userId = (Long) request.getSession().getAttribute("USERID");

        Comentario comentario = servicioComentario.obtenerComentarioPorId(id);
        Resenia resenia = comentario.getResenia();

        if (servicioComentario.esAutorDelComentario(id, userId)) {
            servicioComentario.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Comentario eliminado con éxito.");
        } else {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para eliminar este comentario.");
        }

        return new ModelAndView("redirect:/resenias/" + resenia.getId());
    }

}
