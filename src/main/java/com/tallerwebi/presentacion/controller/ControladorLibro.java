package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.dominio.model.*;
import com.tallerwebi.infraestructura.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

@Controller
@RequestMapping("/libro")
public class ControladorLibro {

    private ServicioLibro servicioLibro;
    private ServicioUsuario servicioUsuario;
    private ServicioUsuarioLibro servicioUsuarioLibro;
    private ServicioLibroGenero servicioLibroGenero;
    private ServicioResenia servicioResenia;
    private ServicioUsuarioLogro servicioUsuarioLogro;
    private ServicioValidacionPlan servicioValidacionPlan;
    private ServicioUsuarioPlan servicioUsuarioPlan;

    @Autowired
    public ControladorLibro(ServicioLibro servicioLibro, ServicioUsuario servicioUsuario,
                            ServicioUsuarioLibro servicioUsuarioLibro, ServicioLibroGenero servicioLibroGenero,
                            ServicioResenia servicioResenia, ServicioUsuarioLogro servicioUsuarioLogro, ServicioValidacionPlan servicioValidacionPlan, ServicioUsuarioPlan servicioUsuarioPlan) {
        this.servicioLibro = servicioLibro;
        this.servicioUsuario = servicioUsuario;
        this.servicioUsuarioLibro = servicioUsuarioLibro;
        this.servicioLibroGenero = servicioLibroGenero;
        this.servicioResenia = servicioResenia;
        this.servicioUsuarioLogro = servicioUsuarioLogro;
        this.servicioValidacionPlan = servicioValidacionPlan;
        this.servicioUsuarioPlan = servicioUsuarioPlan;
    }

    @RequestMapping("/buscar")
    public ModelAndView buscarLibros(@RequestParam("query") String query) {
        ModelMap modelo = new ModelMap();

        Set<Libro> librosObtenidos = null;

        try {
            librosObtenidos = servicioLibro.buscar(query);
            modelo.addAttribute("libros", librosObtenidos);
        } catch (QueryVacia e) {
            modelo.addAttribute("error", "El campo de busqueda esta vacio");
        } catch (ListaVacia e) {
            modelo.addAttribute("error", e.getMessage());
        }

        modelo.addAttribute("query", query);
        return new ModelAndView("resultados_busqueda", modelo);
    }

    @RequestMapping(value = "/detalle/{id}", method = RequestMethod.GET)
    public String detalleLibro(ModelMap model, @PathVariable Long id) throws UsuarioInexistente {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attr.getRequest();
            HttpSession session = request.getSession();
            Long userId = (Long) session.getAttribute("USERID");

            System.out.println(userId + "USER IDDDDD");

            Libro libro = servicioLibro.obtenerIdLibro(id);
            model.addAttribute("libro", libro);

            UsuarioLibro usuarioLibro = servicioUsuarioLibro.obtenerUsuarioLibro(userId, id);
            model.addAttribute("usuarioLibro", usuarioLibro);

            Double promedioDePuntuacion = servicioResenia.calcularPromedioPuntuacion(id);
            model.addAttribute("promedioDePuntuacion", promedioDePuntuacion);

            double progreso = 0.0;
            if (usuarioLibro != null && usuarioLibro.getCantidadDePaginas() != null) {
                progreso = servicioUsuarioLibro.calcularProgresoDeLectura(userId, id, usuarioLibro.getCantidadDePaginas());
            }
            model.addAttribute("progreso", progreso);

            UsuarioPlan usuarioPlan = servicioUsuarioPlan.buscarUsuarioPlan(userId);
            if(!servicioValidacionPlan.puedeRealizarAccion(usuarioPlan, Accion.LEER_OTRAS_RESEÑAS)){
                model.addAttribute("reseniasDeOtrosUsuarios", null);
                model.addAttribute("mensajeDeRestriccion", "Estas en el plan " + usuarioPlan.getPlan().getNombre() + " actualiza tu plan a PLATA u ORO para ver reseñas de otros usuarios.");
            } else {
                List<Resenia> reseniasDeOtrosUsuarios = servicioResenia.obtenerReseniasDeOtrosUsuarios(userId, id);
                model.addAttribute("reseniasDeOtrosUsuarios", reseniasDeOtrosUsuarios);
            }
            


            /*Usuario usuario = servicioUsuario.buscarUsuarioPorId(userId);
            if (!usuario.getPlan().getPuedeLeerOtrasResenias()) {

                model.addAttribute("reseniasDeOtrosUsuarios", null);
                model.addAttribute("mensajeDeRestriccion", "Estas en el plan " + usuario.getPlan().getNombre() + " actualiza tu plan a PLATA u ORO para ver reseñas de otros usuarios.");
            } else {
                List<Resenia> reseniasDeOtrosUsuarios = servicioResenia.obtenerReseniasDeOtrosUsuarios(userId, id);
                model.addAttribute("reseniasDeOtrosUsuarios", reseniasDeOtrosUsuarios);
            }*/

            //System.out.println("ID DEL PLAN " + usuario.getPlan().getId());
            //System.out.println("PUEDE LEER OTRAS RESEÑAS?  " + usuario.getPlan().getPuedeLeerOtrasResenias());

            Resenia resenia = servicioResenia.obtenerReseniaDelUsuario(userId, id);
            model.addAttribute("resenia", resenia);

            List<LibroGenero> generos = servicioLibroGenero.obtenerGeneros(libro);
            model.addAttribute("generos", generos);

            return "infoLibro";
        } catch (LibroNoEncontrado e) {
            model.addAttribute("error", e.getMessage());
            return "errorLibroNoEncontrado";  // Redirige a una vista de error si el libro no se encuentra
        } catch (ListaVacia e) {
            model.addAttribute("errorGeneros", e.getMessage());
            return "infoLibro";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/cambiarEstadoDeLectura", method = RequestMethod.POST)
    public String cambiarEstadoDeLectura(ModelMap model, @RequestParam Long id, @RequestParam String status, @RequestParam(required = false) Integer cantidadDePaginasLeidas, RedirectAttributes redirectAttributes) {

        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("USERID");
        try {
            Libro libro = servicioLibro.obtenerIdLibro(id);
            Usuario usuario = servicioUsuario.buscarUsuarioPorId(userId);
            // Actualizar o crear la relación entre usuario y libro con el nuevo estado de lectura
            servicioUsuarioLibro.crearOActualizarUsuarioLibro(userId, id, status, null, null);

            if (status.equals("Leído")) {
                servicioUsuarioLogro.actualizarEstadoLogros(usuario);
                return "redirect:/libro/resena/" + id + "?usuarioId=" + userId;
            }

            if (status.equals("Leyendo")) {
                servicioUsuarioLibro.actualizarPaginasLeidas(userId, id, cantidadDePaginasLeidas);
            }

            if (status.equals("Leyendo") && cantidadDePaginasLeidas > libro.getCantidadDePaginas()) {
                redirectAttributes.addFlashAttribute("error", "Páginas leídas no pueden exceder la cantidad total de páginas del libro.");
                return "redirect:/libro/detalle/" + id + "?usuarioId=" + userId;
            }

            redirectAttributes.addFlashAttribute("mensaje", "Tu estado de lectura es: " + status);
            return "redirect:/libro/detalle/" + id + "?usuarioId=" + userId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/libro/detalle/" + id + "?usuarioId=" + userId;
        }
    }

    @RequestMapping(value = "/resena/{id}", method = RequestMethod.GET)
    public String mostrarResenia(ModelMap model, @PathVariable Long id) {
        try {
            Libro libro = servicioLibro.obtenerIdLibro(id);
            model.addAttribute("libro", libro);
            return "resenaLibro";
        } catch (LibroNoEncontrado e) {
            model.addAttribute("error", e.getMessage());
            return "errorLibroNoEncontrado";  // Redirige a una vista de error si el libro no se encuentra
        }
    }

    @RequestMapping(value = "/guardarResena", method = RequestMethod.POST)
    public String guardarResena(ModelMap model, @RequestParam Long id, @RequestParam Integer puntuacion, @RequestParam String reseña) {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attr.getRequest();
            HttpSession session = request.getSession();
            Long userId = (Long) session.getAttribute("USERID");
            Usuario usuario = servicioUsuario.buscarUsuarioPorId(userId);
            Libro libro = servicioLibro.obtenerIdLibro(id);
            System.out.println("Libro: " + libro.getTitulo());
            System.out.println("Usuario: " + usuario.getNombre());
            servicioResenia.guardarResenia(usuario, libro, puntuacion, reseña);
            return "redirect:/libro/detalle/" + id;
        } catch (LibroNoEncontrado e) {
            model.addAttribute("error", e.getMessage());
            return "errorLibroNoEncontrado";  // Redirige a una vista de error si el libro no se encuentra
        } catch (UsuarioInexistente e) {
            return "redirect:/login";
        }
    }

    @RequestMapping(value = "/misLibros", method = RequestMethod.GET)
    public ModelAndView mostrarLibros(HttpServletRequest request) {
        ModelMap model = new ModelMap();
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("USERID");
        Integer anioActual = (Integer) session.getAttribute("ANIOACTUAL");

        try {
            Usuario usuario = servicioUsuario.buscarUsuarioPorId(userId);
            model.addAttribute("usuario", usuario);
            // Por defecto muestra los libros en estado Quiero Leer
            List<UsuarioLibro> libros = servicioUsuarioLibro.buscarPorEstadoDeLectura("Quiero Leer", usuario);
            model.addAttribute("libros", libros);
        } catch (UsuarioInexistente e) {
            return new ModelAndView("redirect:/login");
        } catch (ListaVacia e) {
            model.addAttribute("error", e.getMessage());
        }

        model.addAttribute("categoriaActual", "Quiero leer");
        return new ModelAndView("mostrar-libros", model);
    }

    @RequestMapping(value = "/misLibros/estanteria")
    public ModelAndView cambiarCategoria(HttpServletRequest request, @RequestParam("estado") String estadoDeLectura) {
        ModelMap model = new ModelMap();
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("USERID");

        try {
            Usuario usuario = servicioUsuario.buscarUsuarioPorId(userId);
            model.addAttribute("usuario", usuario);
            List<UsuarioLibro> libros;

            if (estadoDeLectura.equals("Leído")) {
                // Obtener libros leidos por año
                libros = servicioUsuarioLibro.buscarLibrosLeidosPorAño(LocalDate.now().getYear(), usuario);
                Integer cantidadLibrosLeidos = libros.size();
                model.addAttribute("cantidadLibrosLeidos", cantidadLibrosLeidos);
                model.addAttribute("libros", libros);
            } else {
                libros = servicioUsuarioLibro.buscarPorEstadoDeLectura(estadoDeLectura, usuario);
                Map<UsuarioLibro, Double> librosConProgreso = new HashMap<>();

                // Calcular el progreso de cada libro
                for (UsuarioLibro usuarioLibro : libros) {
                    double progreso = 0.0;
                    if (usuarioLibro != null && usuarioLibro.getCantidadDePaginas() != null) {
                        progreso = servicioUsuarioLibro.calcularProgresoDeLectura(userId, usuarioLibro.getLibro().getId(), usuarioLibro.getCantidadDePaginas());
                    }
                    librosConProgreso.put(usuarioLibro, progreso);
                }
                model.addAttribute("librosConProgreso", librosConProgreso);
                model.addAttribute("libros", libros); // Tambien pasar la lista de libros
            }

        } catch (UsuarioInexistente e) {
            return new ModelAndView("redirect:/login");
        } catch (ListaVacia e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("cantidadLibrosLeidos", 0);
        }

        model.addAttribute("categoriaActual", estadoDeLectura);
        return new ModelAndView("mostrar-libros", model);
    }


}
