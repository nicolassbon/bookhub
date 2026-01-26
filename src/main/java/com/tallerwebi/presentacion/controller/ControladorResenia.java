package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.excepcion.ListaVacia;
import com.tallerwebi.dominio.excepcion.ReseniaInexistente;
import com.tallerwebi.dominio.model.Comentario;
import com.tallerwebi.dominio.model.LikeDislike;
import com.tallerwebi.dominio.model.Resenia;
import com.tallerwebi.infraestructura.service.ServicioComentario;
import com.tallerwebi.infraestructura.service.ServicioResenia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/resenias")
public class ControladorResenia {

    private ServicioResenia servicioResenia;
    private ServicioComentario servicioComentario;

    @Autowired
    public ControladorResenia(ServicioResenia servicioResenia, ServicioComentario servicioComentario) {
        this.servicioResenia = servicioResenia;
        this.servicioComentario = servicioComentario;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ModelAndView verDetalleResenia(@PathVariable Long id, HttpServletRequest request) {
        ModelMap model = new ModelMap();
        Long userId = (Long) request.getSession().getAttribute("USERID");

        try {
            Resenia resenia = servicioResenia.obtenerReseniaPorId(id);
            System.out.println("Resenia obtenida: " + resenia.getDescripcion());  // Asegúrate de que la reseña está siendo obtenida correctamente
            model.addAttribute("resenia", resenia);

            LikeDislike reaccion = servicioResenia.obtenerReaccionUsuario(id, userId);
            model.addAttribute("reaccionUsuario", reaccion);

            Integer cantLikes = servicioResenia.obtenerCantidadLikes(id);
            model.addAttribute("cantLikes", cantLikes);

            Integer cantDislikes = servicioResenia.obtenerCantidadDislikes(id);
            model.addAttribute("cantDislikes", cantDislikes);

            List<Resenia> reseniasMasReacciones = servicioResenia.obtenerReseniasMasReacciones();
            for (Resenia r : reseniasMasReacciones) {
                System.out.println("Resenia: " + r.getDescripcion());
            }

            List<Comentario> comentarios = servicioComentario.obtenerComentariosPorResenia(id);
            model.addAttribute("comentarios", comentarios);

        } catch (ReseniaInexistente e) {
            model.addAttribute("errorResenia", e.getMessage());
        } catch (ListaVacia e) {
            model.addAttribute("errorComentarios", e.getMessage());
        }

        return new ModelAndView("detalleResenia", model);
    }

    @RequestMapping(value = "/{id}/reaccion", method = RequestMethod.POST)
    public ModelAndView reaccionar(@PathVariable Long id, @RequestParam boolean esLike, HttpServletRequest request) {
        ModelMap model = new ModelMap();
        Long userId = (Long) request.getSession().getAttribute("USERID");

        servicioResenia.reaccionar(userId, id, esLike);

        //Actualizar la info de la reseña en el modelo
        try {
            Resenia resenia = servicioResenia.obtenerReseniaPorId(id);
            model.addAttribute("resenia", resenia);

            LikeDislike reaccion = servicioResenia.obtenerReaccionUsuario(id, userId);
            model.addAttribute("reaccionUsuario", reaccion);

            Integer cantLikes = servicioResenia.obtenerCantidadLikes(id);
            model.addAttribute("cantLikes", cantLikes);

            Integer cantDislikes = servicioResenia.obtenerCantidadDislikes(id);
            model.addAttribute("cantDislikes", cantDislikes);

        } catch (ReseniaInexistente e) {
            model.addAttribute("errorResenia", e.getMessage());
        }
        return new ModelAndView("redirect:/resenias/" + id, model);
    }

    @RequestMapping(value = "/explorar", method = RequestMethod.GET)
    public ModelAndView explorarResenias(HttpServletRequest request,
                                         @RequestParam(required = false) String filtro,
                                         @RequestParam(required = false) String valor,
                                         @RequestParam(required = false) String orden) {
        ModelMap model = new ModelMap();
        Long userId = (Long) request.getSession().getAttribute("USERID");

        List<Resenia> resenias;
        if (filtro != null && !filtro.isEmpty() && valor != null && !valor.isEmpty()) {
            // Aca iria el metodo con los filtros
            try {
                resenias = obtenerReseniasConFiltro(filtro,valor);
                model.addAttribute("filtroSeleccionado", filtro);
                model.addAttribute("valorFiltro", valor);
            } catch (ListaVacia e) {
                model.addAttribute("errorFiltros",e.getMessage());
                resenias = new ArrayList<>();
            }
        } else {
            try {
                resenias = servicioResenia.obtenerReseniasMasReacciones();
            } catch (ListaVacia e) {
                model.addAttribute("errorResenia", e.getMessage());
                resenias = new ArrayList<>();
            }
        }

        if (orden != null && !orden.isEmpty()) {
            resenias = servicioResenia.ordenarResenias(resenias, orden);
        }

        model.addAttribute("resenias", resenias);


        return new ModelAndView("mostrar-resenias", model);
    }

    private List<Resenia> obtenerReseniasConFiltro(String filtro, String valor) throws ListaVacia {
        List<Resenia> resenias;
        switch (filtro) {
            case "libro":
                resenias = servicioResenia.obtenerReseniasPorTituloLibro(valor);
                break;
            case "autorResenia":
                resenias = servicioResenia.obtenerReseniasPorUsuario(valor);
                break;
            case "autorLibro":
                resenias = servicioResenia.obtenerReseniasPorAutorLibro(valor);
                break;
            default:
                resenias = servicioResenia.obtenerReseniasMasReacciones();
                break;
        }
        return resenias;
    }

}
