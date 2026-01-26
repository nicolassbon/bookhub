package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.excepcion.ListaDeReviewsVacias;
import com.tallerwebi.dominio.model.Resenia;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.repository.RepositorioResenia;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service("servicioInicio")
@Transactional
public class ServicioInicioImpl implements ServicioInicio {

    private final RepositorioResenia repositorioResenia;

    public ServicioInicioImpl(RepositorioResenia repositorioResenia) {
        this.repositorioResenia = repositorioResenia;
    }

    @Override
    public void buscar(String nombre) {

    }

    @Override
    public List<Resenia> cargarTodasLasReviews() throws ListaDeReviewsVacias {
        return null;
    }

    @Override
    public void recomendarLibro(Usuario usuario) {



    }
}
