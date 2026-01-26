package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.excepcion.PlanNoEncontrado;
import com.tallerwebi.dominio.excepcion.UsuarioPlanNoEncontrado;
import com.tallerwebi.dominio.model.Accion;
import com.tallerwebi.dominio.model.Plan;
import com.tallerwebi.dominio.model.UsuarioPlan;
import com.tallerwebi.dominio.repository.RepositorioPlan;
import com.tallerwebi.dominio.repository.RepositorioUsuarioPlan;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class ServicioValidacionPlanImpl implements ServicioValidacionPlan {

    private RepositorioUsuarioPlan repositorioUsuarioPlan;
    private RepositorioPlan repositorioPlan;

    public ServicioValidacionPlanImpl(RepositorioUsuarioPlan repositorioUsuarioPlan, RepositorioPlan repositorioPlan) {
        this.repositorioUsuarioPlan = repositorioUsuarioPlan;
        this.repositorioPlan = repositorioPlan;
    }

    @Override
    public Double calcularValidacionUsuarioPlan(Plan planAcambiar, UsuarioPlan planActual) {

        Date fechaAdquisicion = planActual.getFecha_plan_adquirido();
        Date fechaVencimiento = planActual.getFecha_plan_venc();


        if (fechaAdquisicion.after(fechaVencimiento)) {
            System.out.println("La fecha de adquisición es posterior a la fecha de vencimiento. Operación no válida.");
            return null;
        }


        long diferenciaMillis = fechaVencimiento.getTime() - fechaAdquisicion.getTime();
        long diasDeAdquisicion = TimeUnit.MILLISECONDS.toDays(diferenciaMillis);

        System.out.println(diasDeAdquisicion + " días entre adquisición y vencimiento.");


        if (diasDeAdquisicion < 15) {

            return planAcambiar.getPrecio();
        } else {

            return planAcambiar.getPrecio() - planActual.getPrecio() < 0 ? 0 : planAcambiar.getPrecio() - planActual.getPrecio();
        }
    }

    @Override
    public Boolean puedeRealizarAccion(UsuarioPlan usuarioPlan, Accion accion) {

        if (usuarioPlan == null || accion == null) {
            return false;
        }

        switch (accion) {
            case ESCRIBIR_RESEÑAS:
                return true; // porque todos los planes pueden hacerlo

            case ELEGIR_META_DE_LECTURA:
            case LEER_OTRAS_RESEÑAS:
            case OBTENER_RECOMENDACIONES:
                return usuarioPlan.getPlan().getId() >= 2; //solo los usuarios con planes PLATA y ORO pueden hacerlo

            case OBTENER_LOGROS:
            case VER_FORO:
                return usuarioPlan.getPlan().getId() == 3; // solo usuarios con el plan ORO pueden hacerlo

            default:
                return false;
        }
    }

}
