package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.excepcion.ListaVacia;
import com.tallerwebi.dominio.excepcion.ReseniaInexistente;
import com.tallerwebi.dominio.model.Libro;
import com.tallerwebi.dominio.model.LikeDislike;
import com.tallerwebi.dominio.model.Resenia;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.repository.RepositorioLikeDislike;
import com.tallerwebi.dominio.model.UsuarioLibro;
import com.tallerwebi.dominio.repository.RepositorioResenia;
import com.tallerwebi.dominio.repository.RepositorioUsuario;
import com.tallerwebi.dominio.repository.RepositorioUsuarioLibro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class ServicioReseniaImpl implements ServicioResenia {

    private RepositorioResenia repositorioResenia;
    private RepositorioUsuarioLibro repositorioUsuarioLibro;
    private RepositorioUsuario repositorioUsuario;
    private RepositorioLikeDislike repositorioLikeDislike;

    @Autowired
    public ServicioReseniaImpl(RepositorioResenia repositorioResenia,RepositorioUsuarioLibro repositorioUsuarioLibro, RepositorioUsuario repositorioUsuario, RepositorioLikeDislike repositorioLikeDislike) {
        this.repositorioResenia = repositorioResenia;
        this.repositorioUsuarioLibro = repositorioUsuarioLibro;
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioLikeDislike = repositorioLikeDislike;
    }

    @Override
    public void guardarResenia(Usuario usuario, Libro libro, Integer puntuacion, String descripcion) {
        Resenia reseniaExistente = repositorioResenia.obtenerReseniaDelUsuario(usuario.getId(), libro.getId());
        UsuarioLibro usuarioLibro = repositorioUsuarioLibro.encontrarUsuarioIdYLibroId(usuario.getId(), libro.getId());

        if (reseniaExistente != null) {
            // Si ya existe una reseña, actualiza la descripcion y la puntuacion
            reseniaExistente.setDescripcion(descripcion);
            reseniaExistente.setPuntuacion(puntuacion);

            // Settear la resenia y la puntuacion en el UsuarioLibro
            usuarioLibro.setResenia(descripcion);
            usuarioLibro.setPuntuacion(puntuacion);

            repositorioResenia.guardar(reseniaExistente);
            repositorioUsuarioLibro.guardar(usuarioLibro);
        } else {
            // Si no existe una reseña, crea una nueva
            Resenia resenia = new Resenia();
            resenia.setUsuario(usuario);
            resenia.setLibro(libro);
            resenia.setPuntuacion(puntuacion);
            resenia.setDescripcion(descripcion);

            // Settear la resenia y la puntuacion en el UsuarioLibro
            usuarioLibro.setResenia(descripcion);
            usuarioLibro.setPuntuacion(puntuacion);

            repositorioResenia.guardar(resenia);
            repositorioUsuarioLibro.guardar(usuarioLibro);
        }
    }

    @Override
    public Resenia obtenerReseniaPorId(Long id) throws ReseniaInexistente {
        Resenia resenia = repositorioResenia.obtenerReseniaPorId(id);

        if (resenia == null) {
            throw new ReseniaInexistente("La resenia no existe.");
        }
        return resenia;
    }

    @Override
    public List<Resenia> obtenerReseniasDeOtrosUsuarios(Long userId,Long idLibro) {
        List<Resenia> resenias = repositorioResenia.obtenerReseniasDeOtrosUsuarios(userId,idLibro);
        return resenias;
    }

    @Override
    public Double calcularPromedioPuntuacion(Long idLibro) {
        List<Resenia> reseniasDelLibro = repositorioResenia.obtenerReseniasDelLibro(idLibro);


        if (reseniasDelLibro.isEmpty()) {
            return 0.0;
        }

        Integer cantidad = reseniasDelLibro.size();
        Integer suma = 0;

        for (Resenia resenia : reseniasDelLibro) {
            suma += resenia.getPuntuacion();
        }

        return (double) suma / cantidad;
    }

    @Override
    public Resenia obtenerReseniaDelUsuario(Long userId, Long idLibro) {
        Resenia resenia = repositorioResenia.obtenerReseniaDelUsuario(userId, idLibro);
        return resenia;
    }

    @Override
    public void reaccionar(Long idUsuario, Long idResenia, boolean esLike) {
        Usuario usuario = repositorioUsuario.buscarUsuarioPorId(idUsuario);
        Resenia resenia = repositorioResenia.obtenerReseniaPorId(idResenia);

        LikeDislike reaccionExistente = repositorioLikeDislike.obtenerReaccionDelUsuario(idUsuario,idResenia);

        if (reaccionExistente != null) {
            System.out.println("Actualiza el like o dislike");
            actualizarEliminarReaccion(resenia, reaccionExistente, esLike);
        } else {
            System.out.println("Crea una nueva reaccion");
            crearNuevaReaccion(resenia, usuario, esLike);
        }

        repositorioResenia.guardar(resenia);
    }

    @Override
    public Integer obtenerCantidadLikes(Long idResenia) {
        List<LikeDislike> likes = repositorioLikeDislike.obtenerLikesResenia(idResenia);

        if (likes.isEmpty()) {
            return 0;
        }

        return likes.size();
    }

    @Override
    public Integer obtenerCantidadDislikes(Long idResenia) {
        List<LikeDislike> dislikes = repositorioLikeDislike.obtenerDislikesResenia(idResenia);

        if (dislikes.isEmpty()) {
            return 0;
        }

        return dislikes.size();
    }

    @Override
    public LikeDislike obtenerReaccionUsuario(Long idResenia, Long userId) {
        return repositorioLikeDislike.obtenerReaccionDelUsuario(userId,idResenia);
    }

    @Override
    public List<Resenia> obtenerReseniasMasReacciones() throws ListaVacia {
        List<Resenia> resenias = repositorioResenia.obtenerReseniasMasReacciones();

        if(resenias.isEmpty()) {
            throw new ListaVacia("No hay reseñas aún!");
        }

        return resenias;
    }

    @Override
    public List<Resenia> obtenerReseniasPorTituloLibro(String valor) throws ListaVacia {
        List<Resenia> resenias = repositorioResenia.obtenerReseniasPorTituloLibro(valor);
        if(resenias.isEmpty()) {
            throw new ListaVacia("No hay reseñas de ese libro");
        }
        return resenias;
    }

    @Override
    public List<Resenia> obtenerReseniasPorUsuario(String valor) throws ListaVacia {
        List<Resenia> resenias = repositorioResenia.obtenerReseniasPorUsuario(valor);
        if(resenias.isEmpty()) {
            throw new ListaVacia("No hay reseñas de ese usuario");
        }
        return resenias;
    }

    @Override
    public List<Resenia> obtenerReseniasPorAutorLibro(String valor) throws ListaVacia {
        List<Resenia> resenias = repositorioResenia.obtenerReseniasPorAutorLibro(valor);
        if(resenias.isEmpty()) {
            throw new ListaVacia("No hay reseñas de ese autor");
        }
        return resenias;
    }

    @Override
    public List<Resenia> ordenarResenias(List<Resenia> resenias, String orden) {
        if (orden.equals("masPuntuacion")) {
            resenias.sort((r1, r2) -> Integer.compare(r2.getPuntuacion(), r1.getPuntuacion()));
        } else if (orden.equals("menosPuntuacion")) {
            resenias.sort((r1, r2) -> Integer.compare(r1.getPuntuacion(), r2.getPuntuacion()));
        }
        return resenias;
    }

    private void actualizarEliminarReaccion(Resenia resenia, LikeDislike reaccionExistente, boolean esLike) {

        if (reaccionExistente.getEsLike() == esLike) {
            System.out.println("Elimina la reaccion de la bdd");
            resenia.getReacciones().remove(reaccionExistente);
            repositorioLikeDislike.eliminar(reaccionExistente); // Elimina la reaccion si es igual a la existente
        } else {
            System.out.println("Cambia el like o dislike en la bdd");
            reaccionExistente.setEsLike(esLike); // Cambia de like a dislike, o viceversa
            resenia.getReacciones().add(reaccionExistente);
            repositorioLikeDislike.guardar(reaccionExistente);
        }

    }

    private void crearNuevaReaccion(Resenia resenia, Usuario usuario, boolean esLike) {
        LikeDislike nuevaReaccion = new LikeDislike();
        nuevaReaccion.setUsuario(usuario);
        nuevaReaccion.setResenia(resenia);
        nuevaReaccion.setEsLike(esLike);

        resenia.getReacciones().add(nuevaReaccion);
        repositorioLikeDislike.guardar(nuevaReaccion);
    }


}
