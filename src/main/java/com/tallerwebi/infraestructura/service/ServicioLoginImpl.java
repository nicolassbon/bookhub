package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.model.Plan;
import com.tallerwebi.dominio.repository.RepositorioPlan;
import com.tallerwebi.dominio.repository.RepositorioUsuario;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;

@Service
@Transactional
public class ServicioLoginImpl implements ServicioLogin {

    private RepositorioUsuario repositorioUsuario;
    private RepositorioPlan repositorioPlan;

    @Autowired
    public ServicioLoginImpl(RepositorioUsuario repositorioUsuario, RepositorioPlan repositorioPlan) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioPlan = repositorioPlan;
    }

    @Override
    public Usuario consultarUsuario(String email, String password) {
        return repositorioUsuario.buscarUsuario(email, password);
    }

    @Override
    public void registrar(String email, String password, String nombreUsuario, String nombre, LocalDate fechaNacimiento) throws UsuarioExistente, IllegalArgumentException {
        Plan plan = repositorioPlan.buscarPlanPorId(1L);
        if (repositorioUsuario.buscarPorEmail(email) != null) {
            throw new UsuarioExistente("El email ya está en uso.");
        }

        if (repositorioUsuario.buscarPorNombreUsuario(nombreUsuario) != null) {
            throw new UsuarioExistente("El nombre de usuario ya está en uso.");
        }


        repositorioUsuario.guardar(email, password, nombreUsuario, nombre, fechaNacimiento, plan);
    }

    @Override
    public Usuario buscar(String email) {
        return repositorioUsuario.buscar(email);
    }

    public boolean existeEmail(String email) {
        return repositorioUsuario.buscarPorEmail(email) != null;
    }

    public boolean existeNombreUsuario(String nombreUsuario) {
        return repositorioUsuario.buscarPorNombreUsuario(nombreUsuario) != null;
    }
}

