package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.model.Logro;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioLogro;
import com.tallerwebi.dominio.repository.RepositorioLogro;
import com.tallerwebi.dominio.repository.RepositorioUsuario;
import com.tallerwebi.dominio.repository.RepositorioUsuarioLogro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class ServicioLogroImpl implements ServicioLogro {

    private RepositorioLogro repositorioLogro;
    private RepositorioUsuarioLogro repositorioUsuarioLogro;
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    public ServicioLogroImpl(RepositorioLogro repositorioLogro, RepositorioUsuarioLogro repositorioUsuarioLogro, RepositorioUsuario repositorioUsuario) {
        this.repositorioLogro = repositorioLogro;
        this.repositorioUsuarioLogro = repositorioUsuarioLogro;
        this.repositorioUsuario = repositorioUsuario;
    }

    @Override
    public void verificarYAsignarLogrosPredefinidos(Usuario usuario) {
        // Solo asignar logros si aun no los tiene asignados
        if (usuario.getLogrosAsignados() == null || !usuario.getLogrosAsignados()) {
            List<Logro> logrosPredefinidos = repositorioLogro.obtenerLogrosPredefinidos();

            // Recorre todos los logros predefinidos y los asigna
            for (Logro logro : logrosPredefinidos) {
                UsuarioLogro usuarioLogro = new UsuarioLogro();
                usuarioLogro.setUsuario(usuario);
                usuarioLogro.setLogro(logro);
                usuarioLogro.setEstadoLogro("EN_PROGRESO"); // Estado inicial
                usuarioLogro.setPlazoDias(0);
                repositorioUsuarioLogro.guardar(usuarioLogro);
            }

            System.out.println("Logros predefinidos asignados al usuario " + usuario.getNombre());
            usuario.setLogrosAsignados(true);
            repositorioUsuario.guardarUsuario(usuario);
        }
    }

    /*
     for (Logro logro : logrosPredefinidos) {
                if (repositorioUsuarioLogro.buscarUsuarioLogro(usuario, logro) == null) {
                    // Si no tiene asignado el logro, lo asignamos
                    UsuarioLogro usuarioLogro = new UsuarioLogro();
                    usuarioLogro.setUsuario(usuario);
                    usuarioLogro.setLogro(logro);
                    usuarioLogro.setEstadoLogro("EN_PROGRESO"); // Estado inicial
                    usuarioLogro.setPlazoDias(0);
                    repositorioUsuarioLogro.guardar(usuarioLogro);
                }
            }
     */

}
