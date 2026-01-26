package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.excepcion.ListaDeReviewsVacias;
import com.tallerwebi.dominio.excepcion.ListaVacia;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.model.Resenia;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioLibro;
import com.tallerwebi.infraestructura.service.ServicioInicio;
import com.tallerwebi.infraestructura.service.ServicioUsuario;
import com.tallerwebi.infraestructura.service.ServicioUsuarioLibro;
import com.tallerwebi.dominio.model.*;
import com.tallerwebi.infraestructura.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;


@Controller
public class ControladorInicio {

    private final ServicioInicio servicioInicio;
    private final ServicioUsuario servicioUsuario;
    private final ServicioUsuarioLibro servicioUsuarioLibro;
    private final ServicioNotificacion servicioNotificacion;
    private final ServicioPublicacion servicioPublicacion;
    private final ServicioUsuarioNotificacion servicioUsuarioNotificacion;
    private final ServicioComentarioPublicacion servicioComentarioPublicacion;
    private final ServicioUsuarioGenero servicioUsuarioGenero;
    private final ServicioLibroGenero servicioLibroGenero;
    private final ServicioLibro servicioLibro;
    private final ServicioValidacionPlan servicioValidacionPlan;
    private final ServicioUsuarioPlan servicioUsuarioPlan;

    @Autowired
    public ControladorInicio(ServicioInicio servicioInicio,
                             ServicioUsuario servicioUsuario,
                             ServicioUsuarioLibro servicioUsuarioLibro,
                             ServicioUsuarioNotificacion servicioUsuarioNotificacion,
                             ServicioNotificacion servicioNotificacion,
                             ServicioPublicacion servicioPublicacion,
                             ServicioPublicacion publicacion,
                             ServicioComentarioPublicacion servicioComentarioPublicacion,
                             ServicioComentarioPublicacion comentarioPublicacion,
                             ServicioUsuarioGenero servicioUsuarioGenero,
                             ServicioLibroGenero servicioLibroGenero,
                             ServicioLibro servicioLibro,
                             ServicioValidacionPlan servicioValidacionPlan,
                             ServicioUsuarioPlan servicioUsuarioPlan) {

        this.servicioInicio = servicioInicio;
        this.servicioNotificacion = servicioNotificacion;
        this.servicioUsuario = servicioUsuario;
        this.servicioUsuarioLibro = servicioUsuarioLibro;
        this.servicioUsuarioNotificacion = servicioUsuarioNotificacion;
        this.servicioPublicacion = servicioPublicacion;
        this.servicioComentarioPublicacion = servicioComentarioPublicacion;
        this.servicioUsuarioGenero = servicioUsuarioGenero;
        this.servicioLibroGenero = servicioLibroGenero;
        this.servicioLibro = servicioLibro;
        this.servicioValidacionPlan = servicioValidacionPlan;
        this.servicioUsuarioPlan = servicioUsuarioPlan;
    }

    @GetMapping("/home")
    public String mostrarHome(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("USERID");
        Integer anioActual = (Integer) session.getAttribute("ANIOACTUAL");

        try {
            Usuario usuario = servicioUsuario.buscarUsuarioPorId(userId);
            model.addAttribute("usuario", usuario);

            UsuarioPlan usuarioPlan = servicioUsuarioPlan.buscarUsuarioPlan(userId);
            model.addAttribute("usuarioPlan", usuarioPlan);

            if (!servicioValidacionPlan.puedeRealizarAccion(usuarioPlan, Accion.OBTENER_RECOMENDACIONES)) {
                model.addAttribute("mensajePlanRecos", "Estas en el plan " + usuarioPlan.getPlan().getNombre() + " actualiza tu plan a ORO para obtener recomendaciones personalizadas.");
            }


            List<UsuarioGenero> list = servicioUsuarioGenero.obtenerGenerosDeUsuario(userId);

            if (!list.isEmpty()) {
                Random random = new Random();
                int randomIndex = random.nextInt(list.size());

                UsuarioGenero randomGenero = list.get(randomIndex);
                List<LibroGenero> libroPorGenero = servicioLibroGenero.obtenerLibroPorGenero(randomGenero.getGenero().getId());

                List<Libro> librosRecomendados = new ArrayList<>();
                int maxLibros = 2;

                for (LibroGenero libroGenero : libroPorGenero) {
                    if (librosRecomendados.size() >= maxLibros) {
                        break;
                    }

                    Libro libro = libroGenero.getLibro();
                    librosRecomendados.add(libro);
                }

                model.addAttribute("librosRecomendados", librosRecomendados);


            } else {
                List<Libro> dosLibrosRandom = servicioLibro.obtenerDosLibrosRandom();
                model.addAttribute("librosRecomendados", dosLibrosRandom);
            }

            List<UsuarioLibro> listadoComments = servicioUsuarioLibro.obtenerTodosLosComentariosDeMisAmigos(userId);

            model.addAttribute("comentariosDeAmigos", listadoComments);

            List<Publicacion> listadoAmigosPublicaciones = servicioPublicacion.obtenerTodasPublicacionesDeAmigos(userId);
            model.addAttribute("listadoAmigosPublicaciones", listadoAmigosPublicaciones);

            List<Long> publicacionIds = listadoAmigosPublicaciones.stream()
                    .map(Publicacion::getId)
                    .collect(Collectors.toList());

            Map<Long, List<ComentarioPublicacion>> comentariosPorPublicacion = new HashMap<>();

            for (Long publicacionId : publicacionIds) {
                List<ComentarioPublicacion> comentariosDeUnaPublicacion = servicioComentarioPublicacion.obtenerLosComentariosDeLaPublicacion(publicacionId);
                comentariosPorPublicacion.put(publicacionId, comentariosDeUnaPublicacion);
            }

            model.addAttribute("comentariosPorPublicacion", comentariosPorPublicacion);

            if(!servicioValidacionPlan.puedeRealizarAccion(usuarioPlan, Accion.VER_FORO)){
                model.addAttribute("mensajePlan", "Estas en el plan " + usuarioPlan.getPlan().getNombre() + " actualiza tu plan a ORO para participar en foros.");
                model.addAttribute("mensajePlan2", "Estas en el plan " + usuarioPlan.getPlan().getNombre() + " actualiza tu plan a ORO para ver las publicaciones de tus amigos.");
                model.addAttribute("mensajePlan3", "Estas en el plan " + usuarioPlan.getPlan().getNombre() + " actualiza tu plan a ORO para ver la actividad de tus amigos.");
            }


            List<UsuarioNotificacion> notificacionesUsuario = servicioUsuarioNotificacion.listarNotificacionesPorUsuario(userId);

            List<Notificacion> listaNotificaciones = new ArrayList<>();

            for (UsuarioNotificacion usuarioNotificacion : notificacionesUsuario) {
                Long notificacionId = usuarioNotificacion.getNotificacion().getId();
                Notificacion notificacion = servicioNotificacion.obtenerNotificacionPorId(notificacionId);
                listaNotificaciones.add(notificacion);
            }

            model.addAttribute("notificaciones", listaNotificaciones);

            List<UsuarioLibro> librosLeidos = servicioUsuarioLibro.buscarLibrosLeidosPorAño(anioActual, usuario);
            Integer cantidadLibrosLeidos = librosLeidos.size();
            model.addAttribute("cantidadLibrosLeidos", cantidadLibrosLeidos);

            Integer porcentajeLibrosLeidos = 0;

            if(usuario.getMeta() != null) {
                porcentajeLibrosLeidos = (int) Math.round((double) cantidadLibrosLeidos / (double) usuario.getMeta() * 100);
            }

            model.addAttribute("porcentajeLibrosLeidos", porcentajeLibrosLeidos);
            List<Resenia> reviews = servicioInicio.cargarTodasLasReviews();
            model.addAttribute("reviews", reviews);
        } catch (UsuarioInexistente e) {
            return "redirect:/login";
        } catch (ListaDeReviewsVacias ex) {
            model.addAttribute("MensajeNoReviews", ex.getMessage());
        } catch (ListaVacia e) {
            model.addAttribute("cantidadLibrosLeidos", 0);
            model.addAttribute("porcentajeLibrosLeidos", 0);
        } catch (MessagingException e) {
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "home";
    }


}


