package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.excepcion.ListaVacia;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.model.Accion;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioLibro;
import com.tallerwebi.dominio.model.UsuarioPlan;
import com.tallerwebi.infraestructura.service.ServicioUsuario;
import com.tallerwebi.infraestructura.service.ServicioUsuarioLibro;
import com.tallerwebi.infraestructura.service.ServicioUsuarioPlan;
import com.tallerwebi.infraestructura.service.ServicioValidacionPlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/desafios")
public class ControladorDesafio {

    private ServicioUsuario servicioUsuario;
    private ServicioUsuarioLibro servicioUsuarioLibro;
    private ServicioUsuarioPlan servicioUsuarioPlan;
    private ServicioValidacionPlan servicioValidacionPlan;

    @Autowired
    public ControladorDesafio(ServicioUsuario servicioUsuario, ServicioUsuarioLibro servicioUsuarioLibro, ServicioUsuarioPlan servicioUsuarioPlan, ServicioValidacionPlan servicioValidacionPlan) {
        this.servicioUsuario = servicioUsuario;
        this.servicioUsuarioLibro = servicioUsuarioLibro;
        this.servicioUsuarioPlan = servicioUsuarioPlan;
        this.servicioValidacionPlan = servicioValidacionPlan;
    }

    @RequestMapping(value = "/desafio-libros")
    public ModelAndView mostrarDesafios(HttpServletRequest request) {
        ModelMap model = new ModelMap();
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("USERID");
        Integer anioActual = (Integer) session.getAttribute("ANIOACTUAL");

        try {
            Usuario usuario = servicioUsuario.buscarUsuarioPorId(userId);

            if (!validarMetaUsuario(usuario))
                return new ModelAndView("redirect:/home");

            UsuarioPlan usuarioPlan = servicioUsuarioPlan.buscarUsuarioPlan(userId);
            model.addAttribute("usuarioPlan", usuarioPlan);

            if(!servicioValidacionPlan.puedeRealizarAccion(usuarioPlan, Accion.ELEGIR_META_DE_LECTURA))
                model.addAttribute("restriccionMeta", "Estas en el plan " + usuarioPlan.getPlan().getNombre() + " actualiza tu plan a PLATA u ORO para ver tu desafío de lectura y la de la comunidad.");


            model.addAttribute("usuario", usuario);

            List<Usuario> comunidadUsuarios = servicioUsuario.obtenerUsuariosDesafio(userId);
            model.addAttribute("comunidadUsuarios", comunidadUsuarios);
            System.out.println("Comunidad usuarios guardada");

            establecerInformacionDesafioLibro(model);
            establecerTiempoRestanteDesafio(model);

            List<UsuarioLibro> libros = servicioUsuarioLibro.buscarLibrosLeidosPorAño(anioActual, usuario);
            Integer cantidadLibrosLeidos = libros.size();
            model.addAttribute("cantidadLibrosLeidos", cantidadLibrosLeidos);

            int porcentajeLibrosLeidos = (int) Math.round((double) cantidadLibrosLeidos / (double) usuario.getMeta() * 100);
            model.addAttribute("porcentajeLibrosLeidos", porcentajeLibrosLeidos);

        } catch (UsuarioInexistente e) {
            return new ModelAndView("redirect:/login");
        } catch (ListaVacia e) {
            model.addAttribute("cantidadLibrosLeidos", 0);
            model.addAttribute("porcentajeLibrosLeidos", 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new ModelAndView("desafio-libros", model);
    }

    @RequestMapping(value = "/desafio-usuario/{id}")
    public ModelAndView mostrarDesafioUsuario(HttpServletRequest request, @PathVariable Long id) {
        ModelMap model = new ModelMap();
        HttpSession session = request.getSession();
        Integer anioActual = (Integer) session.getAttribute("ANIOACTUAL");

        try {
            Usuario usuario = servicioUsuario.buscarUsuarioPorId(id);

            if (!validarMetaUsuario(usuario))
                return new ModelAndView("redirect:/home");

            model.addAttribute("usuario", usuario);

            establecerInformacionDesafioLibro(model);
            establecerTiempoRestanteDesafio(model);

            List<UsuarioLibro> libros = servicioUsuarioLibro.buscarLibrosLeidosPorAño(anioActual, usuario);
            model.addAttribute("libros", libros);

            Integer cantidadLibrosLeidos = libros.size();
            model.addAttribute("cantidadLibrosLeidos", cantidadLibrosLeidos);
        } catch (UsuarioInexistente e) {
            return new ModelAndView("redirect:/login");
        } catch (ListaVacia e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("cantidadLibrosLeidos", 0);
        }

        return new ModelAndView("desafio-usuario", model);
    }

    private void establecerInformacionDesafioLibro(ModelMap model) {

        List<Usuario> listaUsuarios = servicioUsuario.obtenerUsuarios();

        Long cantidadParticipantes = 0L;
        Long cantidadLibrosPrometidos = 0L;
        Long promedioLibrosPrometidos = 0L;

        for (Usuario usuario : listaUsuarios) {
            if (usuario.getMeta() != null && usuario.getMeta() > 0) {
                cantidadParticipantes++;
                cantidadLibrosPrometidos += usuario.getMeta();
            }
        }

        promedioLibrosPrometidos = cantidadLibrosPrometidos / cantidadParticipantes;

        model.addAttribute("cantidadParticipantes", cantidadParticipantes);
        model.addAttribute("cantidadLibrosPrometidos", cantidadLibrosPrometidos);
        model.addAttribute("promedioLibrosPrometidos", promedioLibrosPrometidos);
    }

    public void establecerTiempoRestanteDesafio(ModelMap model) {
        // Fecha y hora actual
        LocalDateTime ahora = LocalDateTime.now();

        // Fecha y hora del final del desafío (31 de diciembre de 2024, 23:59:59)
        LocalDateTime finDesafio = LocalDateTime.of(2024, 12, 31, 23, 59, 59);

        // Calcular la diferencia entre las dos fechas
        Duration duracion = Duration.between(ahora, finDesafio);

        // Obtener días y horas restantes
        long diasRestantes = duracion.toDays();
        long horasRestantes = duracion.toHours() % 24; // Para obtener solo las horas restantes del día

        model.addAttribute("diasRestantes", diasRestantes);
        model.addAttribute("horasRestantes", horasRestantes);
    }

    public boolean validarMetaUsuario(Usuario usuario) {
        return usuario.getMeta() != null && usuario.getMeta() > 0;
    }
}
