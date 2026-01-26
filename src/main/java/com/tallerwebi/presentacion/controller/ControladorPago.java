package com.tallerwebi.presentacion.controller;

import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.infraestructura.service.ServicioPlan;
import com.tallerwebi.infraestructura.service.ServicioUsuarioPlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/pago")
public class ControladorPago {

    private ServicioUsuarioPlan servicioUsuarioPlan;

    @Autowired
    public ControladorPago(ServicioUsuarioPlan servicioUsuarioPlan) {
        this.servicioUsuarioPlan = servicioUsuarioPlan;
    }

    @GetMapping("/exito")
    public String pagoExitoso(@RequestParam("payment_id") String paymentId,
                              @RequestParam("status") String status,
                              @RequestParam("external_reference") String externalReference,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {
        // Recupera el ID del plan desde la referencia externa
        Long planId = Long.parseLong(externalReference);
        System.out.println("Pago exitoso. ID de pago: " + paymentId + ", Estado: " + status);
        String mensajeEstadoPago = "FELICIDADES!! SE HA REALIZADO CORRECTAMENTE EL PAGO DEL PLAN";
        redirectAttributes.addFlashAttribute("mensajeEstadoPago", mensajeEstadoPago);

        // Llamada directa al servicio que actualiza el plan
        try {
            Long userId = (Long) session.getAttribute("USERID");
            servicioUsuarioPlan.actualizarPlanDelUsuarioPlan(userId, planId);
            session.setAttribute("planAdquirido", planId);
        } catch (UsuarioInexistente e) {
            return "redirect:/login";
        }

        return "redirect:/planes/mostrar";
    }

    @GetMapping("/error")
    public String pagoError(@RequestParam(value = "payment_id", required = false) String paymentId,
                            @RequestParam(value = "status", required = false) String status,
                            @RequestParam(value = "external_reference", required = false) String externalReference,
                            RedirectAttributes redirectAttributes) {
        System.out.println("Error en el pago. ID de pago: " + paymentId + ", Estado: " + status);
        String mensajeEstadoPago = "OCURRIO UN ERROR EN EL PAGO DEL PLAN!!";
        redirectAttributes.addFlashAttribute("mensajeEstadoPago", mensajeEstadoPago);
        return "redirect:/planes/mostrar";
    }

    @GetMapping("/pendiente")
    public String pagoPendiente(@RequestParam("payment_id") String paymentId,
                                @RequestParam("status") String status,
                                @RequestParam("external_reference") String externalReference,
                                RedirectAttributes redirectAttributes) {
        System.out.println("Pago pendiente. ID de pago: " + paymentId + ", Estado: " + status);
        String mensajeEstadoPago = "EL PAGO DEL PLAN ESTA PENDIENTE DE CONFIRMACIÓN";
        redirectAttributes.addFlashAttribute("mensajeEstadoPago", mensajeEstadoPago);
        return "redirect:/planes/mostrar";
    }
}
