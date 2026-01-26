package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.excepcion.ListaVacia;
import com.tallerwebi.dominio.excepcion.ReseniaInexistente;
import com.tallerwebi.dominio.model.Comentario;
import com.tallerwebi.dominio.model.Resenia;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.repository.RepositorioComentario;
import com.tallerwebi.dominio.repository.RepositorioResenia;
import com.tallerwebi.dominio.repository.RepositorioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ServicioComentarioImpl implements ServicioComentario {

    private RepositorioComentario repositorioComentario;
    private RepositorioResenia repositorioResenia;

    @Autowired
    public ServicioComentarioImpl(RepositorioComentario repositorioComentario, RepositorioResenia repositorioResenia) {
        this.repositorioComentario = repositorioComentario;
        this.repositorioResenia = repositorioResenia;
    }

    @Override
    public void guardarComentario(Usuario usuario, Resenia resenia, String textoComentario) throws ReseniaInexistente {

        if (resenia == null) {
            throw new ReseniaInexistente("La resenia no existe.");
        }

        Comentario comentario = new Comentario();
        comentario.setResenia(resenia);
        comentario.setUsuario(usuario);
        comentario.setTexto(textoComentario);

        repositorioComentario.guardarComentario(comentario);

    }

    @Override
    public List<Comentario> obtenerComentariosPorResenia(Long idResenia) throws ListaVacia {
        List<Comentario> comentarios = repositorioComentario.obtenerComentariosPorResenia(idResenia);

        if (comentarios.isEmpty()) {
            throw new ListaVacia("La reseña aún no tiene comentarios.");
        }
        return comentarios;
    }

    @Override
    public Boolean esAutorDelComentario(Long id, Long userId) {
        Comentario comentario = repositorioComentario.obtenerComentarioPorId(id);
        return comentario.getUsuario().getId().equals(userId);
    }

    @Override
    public void eliminar(Long id) {
        Comentario comentario = repositorioComentario.obtenerComentarioPorId(id);
        repositorioComentario.eliminar(comentario);
    }

    @Override
    public Comentario obtenerComentarioPorId(Long id) {
        Comentario comentario = repositorioComentario.obtenerComentarioPorId(id);
        return comentario;
    }

}
