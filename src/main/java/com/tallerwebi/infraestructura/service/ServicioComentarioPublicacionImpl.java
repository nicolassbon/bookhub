    package com.tallerwebi.infraestructura.service;

    import com.tallerwebi.dominio.model.ComentarioPublicacion;
    import com.tallerwebi.dominio.model.Publicacion;
    import com.tallerwebi.dominio.model.Usuario;
    import com.tallerwebi.dominio.repository.RepositorioComentarioPublicacion;
    import com.tallerwebi.dominio.repository.RepositorioPublicacion;
    import com.tallerwebi.dominio.repository.RepositorioUsuario;
    import org.springframework.stereotype.Service;

    import javax.mail.MessagingException;
    import javax.transaction.Transactional;
    import java.util.Date;
    import java.util.List;

    @Service
    @Transactional

    public class ServicioComentarioPublicacionImpl implements ServicioComentarioPublicacion{

        private final RepositorioComentarioPublicacion repositorioComentarioPublicacion;
        private final RepositorioUsuario repositorioUsuario;
        private final RepositorioPublicacion repositorioPublicacion;

        public ServicioComentarioPublicacionImpl(RepositorioComentarioPublicacion repositorioComentarioPublicacion, RepositorioUsuario repositorioUsuario, RepositorioPublicacion repositorioPublicacion) {
            this.repositorioComentarioPublicacion = repositorioComentarioPublicacion;
            this.repositorioUsuario = repositorioUsuario;
            this.repositorioPublicacion = repositorioPublicacion;
        }


        @Override
        public void crearComentarioPublicacion(Long publicationId, String mensaje, Long userId) throws MessagingException {
            try {
                Usuario usuario = repositorioUsuario.buscarUsuarioPorId(userId);
                if (usuario == null) {
                    throw new Exception ("No existe usuario con ID " + userId);
                }

                Publicacion publicacion = repositorioPublicacion.obtenerPublicacionPorId(publicationId);
                if (publicacion == null) {
                    throw new Exception ("No existe publicacion con ID " + publicationId);
                }
                ComentarioPublicacion comentarioPublicacion = new ComentarioPublicacion();

                comentarioPublicacion.setMensaje(mensaje);
                comentarioPublicacion.setPublicacion(publicacion);
                comentarioPublicacion.setFechaHora(new Date());
                comentarioPublicacion.setUsuario(usuario);

                repositorioComentarioPublicacion.guardar(comentarioPublicacion);
            }catch (Exception error){
                throw new MessagingException(error.getMessage());
            }
        }

        @Override
        public List<ComentarioPublicacion> obtenerLosComentariosDeLaPublicacion(Long publicationId) throws MessagingException {
            try {
                List<ComentarioPublicacion> comentarioPublicacions = repositorioComentarioPublicacion.obtenerLosComentariosDeLaPublicacion(publicationId);
                System.out.println(comentarioPublicacions + " UISUARO AMIGO comentarioPublicacions");
                return comentarioPublicacions;
            } catch (Exception e) {
                throw new MessagingException(e.getMessage());
            }
        }

    }
