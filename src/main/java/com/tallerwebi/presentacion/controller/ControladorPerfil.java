package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.excepcion.ListaVacia;
import com.tallerwebi.dominio.excepcion.QueryVacia;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.model.*;
import com.tallerwebi.infraestructura.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Controller
public class ControladorPerfil {

    private ServicioUsuario servicioUsuario;
    private ServicioUsuarioGenero servicioUsuarioGenero;
    private ServicioAmistad servicioAmistad;

    // Para que no de error el formato de las fechas cuando se edita el perfil
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                setValue(LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }

            @Override
            public String getAsText() {
                LocalDate value = (LocalDate) getValue();
                return value != null ? value.toString() : "";
            }
        });
    }

    @Autowired
    public ControladorPerfil(ServicioUsuario servicioUsuario, ServicioAmistad servicioAmistad, ServicioUsuarioGenero servicioUsuarioGenero) {
        this.servicioUsuario = servicioUsuario;
        this.servicioUsuarioGenero = servicioUsuarioGenero;
        this.servicioAmistad = servicioAmistad;
    }

    @RequestMapping(value = "/perfil/{id}", method = RequestMethod.GET)
    public ModelAndView mostrarPerfil( @PathVariable Long id) {
        ModelMap model = new ModelMap();
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        HttpSession session = request.getSession();
        Long idUsuario = (Long) session.getAttribute("USERID");
        try {

            Usuario usuario = servicioUsuario.buscarUsuarioPorId(id);
            model.addAttribute("usuario", usuario);



            if (!id.equals(idUsuario)) {
                String friends = servicioAmistad.verificacionDeAmistad(idUsuario, id);
                model.addAttribute("isFriend", friends);
            }

        } catch (UsuarioInexistente e) {
            return new ModelAndView("redirect:/login");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new ModelAndView("perfil", model);
    }

    @RequestMapping(value = "/mostrarEditarPerfil")
    public ModelAndView mostrarEditarPerfil(HttpServletRequest request) {
        ModelMap model = new ModelMap();
        HttpSession session = request.getSession();
        Long idUsuario = (Long) session.getAttribute("USERID");
        try {
            Usuario usuario = servicioUsuario.buscarUsuarioPorId(idUsuario);
            model.addAttribute("usuario", usuario);
            return new ModelAndView("editarPerfil", model);
        } catch (UsuarioInexistente e) {
            return new ModelAndView("redirect:/login");
        }
    }


    @RequestMapping("/buscar-usuarios")
    public ModelAndView buscarUsuarios(@RequestParam("query") String query) {
        ModelMap modelo = new ModelMap();

        Set<Usuario> usuariosObtenidos = null;

        try {
            usuariosObtenidos = servicioUsuario.buscarUsuariosPorQuery(query);
            modelo.addAttribute("listadoUsuarios", usuariosObtenidos);
        } catch (QueryVacia e) {
            modelo.addAttribute("error", "El campo de busqueda esta vacio");
        } catch (ListaVacia e) {
            modelo.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        modelo.addAttribute("query", query);
        return new ModelAndView("resultado_busqueda_usuarios", modelo);
    }

    @RequestMapping(value = "/editarPerfil", method = RequestMethod.POST, consumes = "multipart/form-data")
    public ModelAndView editarPerfil(@ModelAttribute Usuario usuario,
                                     @RequestParam("imagenPerfil") MultipartFile imagenPerfil,
                                     @RequestParam("imagenActual") String imagenActual,
                                     HttpServletRequest request) {
        ModelMap model = new ModelMap();

        // Obtener el id del usuario actual desde la sesión
        HttpSession session = request.getSession();
        Long idUsuarioActual = (Long) session.getAttribute("USERID");

        try {
            // Validar la imagen
            if (validarErroresImagenPerfil(usuario, imagenPerfil, imagenActual, idUsuarioActual, model)) {
                return new ModelAndView("editarPerfil", model);
            }

            // Validar nombre de usuario y correo electrónico
            if (validarErroresFormulario(usuario, idUsuarioActual, model)) {
                return new ModelAndView("editarPerfil", model);
            }

            // Actualizar el usuario en la base de datos
            servicioUsuario.actualizarUsuario(idUsuarioActual, usuario);

            // Redirigir al perfil
            return new ModelAndView("redirect:/perfil/" + idUsuarioActual, model);
        } catch (IOException e) {
            return new ModelAndView("editarPerfil", model);
        } catch (UsuarioInexistente e) {
            return new ModelAndView("redirect:/login");
        }
    }

    private boolean validarErroresImagenPerfil(Usuario usuario, MultipartFile imagenPerfil, String imagenActual, Long idUsuarioActual, ModelMap model) throws IOException {

        // Si no se sube una nueva imagen, mantener la imagen actual
        if (imagenPerfil.isEmpty()) {
            usuario.setImagenUrl(imagenActual);
            return false;
        }
        System.out.println("La imagen no esta vacia");

        // (solo jpg o png)
        if (!imagenPerfil.isEmpty() && !esFormatoValido(imagenPerfil.getContentType())) {
            model.addAttribute("errorFormato", "El formato de la imagen debe ser JPG o PNG.");
            usuario.setImagenUrl(imagenActual);
            return true;
        }

        System.out.println("La imagen es png o jpg");

        // Si el usuario tenía una imagen anterior, eliminarla del proyecto
        if (imagenActual != null && !imagenActual.isEmpty()) {
            eliminarImagen(imagenActual);
            System.out.println("Se elimina la imagen anterior");
        }

        System.out.println("Validaciones hechas");

        guardarNuevaImagen(usuario, imagenPerfil, idUsuarioActual);
        return false;
    }

    private boolean validarErroresFormulario(Usuario usuario, Long idUsuarioActual, ModelMap model) {
        if (servicioUsuario.existeNombreUsuario(usuario.getNombreUsuario(), idUsuarioActual)) {
            model.addAttribute("errorNombreUsuario", "El nombre de usuario ya existe. Por favor, elige otro.");
            model.addAttribute("usuario", usuario); // Mantener los datos del usuario en el formulario
            return true;
        }

        if (servicioUsuario.existeEmailUsuario(usuario.getEmail(), idUsuarioActual)) {
            model.addAttribute("errorEmail", "El email ya está registrado. Por favor, utiliza otro.");
            model.addAttribute("usuario", usuario); // Mantener los datos del usuario en el formulario
            return true;
        }

        return false;
    }

    private boolean esFormatoValido(String contentType) {
        return contentType.equals("image/jpeg") || contentType.equals("image/png");
    }

    private void guardarNuevaImagen(Usuario usuario, MultipartFile imagenPerfil, Long idUsuarioActual) throws IOException {
        String rutaImagenes = "src/main/webapp/resources/core/Images/";
        String nombreArchivo = idUsuarioActual + "-" + imagenPerfil.getOriginalFilename();
        File archivoDestino = new File(rutaImagenes + nombreArchivo);

        // Guardar el archivo en la ubicación especificada
        imagenPerfil.transferTo(archivoDestino);

        // Actualizar la URL de la imagen en el objeto usuario
        usuario.setImagenUrl("/Images/" + nombreArchivo);
    }

    private void eliminarImagen(String imagenUrl) {
        try {
            Path path = Paths.get("src/main/webapp/resources/core" + imagenUrl);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
