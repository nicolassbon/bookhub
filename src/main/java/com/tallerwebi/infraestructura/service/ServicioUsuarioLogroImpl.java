package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.excepcion.ListaVacia;
import com.tallerwebi.dominio.model.Logro;
import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.model.UsuarioLogro;
import com.tallerwebi.dominio.repository.RepositorioLogro;
import com.tallerwebi.dominio.repository.RepositorioUsuarioLibro;
import com.tallerwebi.dominio.repository.RepositorioUsuarioLogro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class ServicioUsuarioLogroImpl implements ServicioUsuarioLogro {

    private RepositorioUsuarioLogro repositorioUsuarioLogro;

    private RepositorioLogro repositorioLogro;

    private RepositorioUsuarioLibro repositorioUsuarioLibro;

    private ServicioNotificacion servicioNotificacion;

    @Autowired
    public ServicioUsuarioLogroImpl(RepositorioUsuarioLogro repositorioUsuarioLogro, RepositorioLogro repositorioLogro,
                                    RepositorioUsuarioLibro repositorioUsuarioLibro, ServicioNotificacion servicioNotificacion) {
        this.repositorioUsuarioLogro = repositorioUsuarioLogro;
        this.repositorioLogro = repositorioLogro;
        this.repositorioUsuarioLibro = repositorioUsuarioLibro;
        this.servicioNotificacion = servicioNotificacion;
    }

    @Override
    public void guardarLogroPersonalizado(Usuario usuario, String nombre, Integer objetivoLibros, Integer plazoDias) {
        Logro logroPersonalizado = new Logro();
        logroPersonalizado.setNombre(nombre);
        logroPersonalizado.setObjetivoLibros(objetivoLibros);
        logroPersonalizado.setEsPredefinido(false);
        repositorioLogro.guardar(logroPersonalizado);

        UsuarioLogro usuarioLogro = new UsuarioLogro();
        usuarioLogro.setUsuario(usuario);
        usuarioLogro.setLogro(logroPersonalizado);
        usuarioLogro.setPlazoDias(plazoDias);
        repositorioUsuarioLogro.guardar(usuarioLogro);
    }

    @Override
    public void actualizarEstadoLogros(Usuario usuario) {
        List<UsuarioLogro> logrosUsuario = repositorioUsuarioLogro.obtenerLogrosPorUsuario(usuario);

        // Recorre la lista de logros del usuario
        // Filtra solo los logros en progreso
        // Actualiza el estado del logro, si se completo o termino el tiempo
        logrosUsuario.stream()
                .filter(logro -> logro.getEstadoLogro().equals("EN_PROGRESO"))
                .forEach(this::actualizarEstadoLogro);
    }

    private void actualizarEstadoLogro(UsuarioLogro usuarioLogro) {

        Long userId = usuarioLogro.getUsuario().getId();

        // Esto puede dar error, depende del id que se haya creado de 'estado logro' en la tabla tipo_notificacion
        Long tipoNotificacion = 3L;

        // Verifica que el plazo de tiempo no se haya terminado
        // Si se termino, el logro queda en estado NO_COMPLETADO y crea el mensaje de que no se completo
        if (plazoVencido(usuarioLogro)) {
            usuarioLogro.setEstadoLogro("NO_COMPLETADO");
            String mensaje = "No se ha completado el logro: '" + usuarioLogro.getLogro().getNombre() + "' en el plazo establecido";
            try {
                servicioNotificacion.crearNotificacion(userId, tipoNotificacion, mensaje, userId);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        if (verificarProgreso(usuarioLogro)) {
            usuarioLogro.setEstadoLogro("COMPLETADO");
            String mensaje = "¡Felicidades! Has completado el logro: " + usuarioLogro.getLogro().getNombre();
            try {
                servicioNotificacion.crearNotificacion(userId, tipoNotificacion, mensaje, userId);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        repositorioUsuarioLogro.guardar(usuarioLogro);
    }

    @Override
    public List<UsuarioLogro> obtenerLogrosDelUsuario(Usuario usuario) throws ListaVacia {
        List<UsuarioLogro> logrosUsuario = repositorioUsuarioLogro.obtenerLogrosPorUsuario(usuario);

        if (logrosUsuario.isEmpty()) {
            throw new ListaVacia("No tienes logros creados!");
        }

        return logrosUsuario;
    }

    @Override
    public Boolean eliminarLogro(Long userId, Long logroId) {

        boolean logroEliminado = false;
        UsuarioLogro usuarioLogro = repositorioUsuarioLogro.buscarUsuarioLogro(userId, logroId);


        if (usuarioLogro != null) {
            Logro logro = repositorioLogro.buscarPorId(usuarioLogro.getLogro().getId());
            repositorioUsuarioLogro.borrar(usuarioLogro);
            logroEliminado = true;

            if (!logro.getEsPredefinido()) {
                repositorioLogro.borrar(logro);
            }
        }

        return logroEliminado;
    }

    private boolean verificarProgreso(UsuarioLogro usuarioLogro) {
        LocalDate fechaFinalizacion = obtenerFechaDeFinalizacionLogro(usuarioLogro);
        Integer librosLeidos = 0;

        if (fechaFinalizacion != null) {
            librosLeidos = repositorioUsuarioLibro.buscarCantLibrosLeidosEntrePlazos(usuarioLogro.getUsuario(), usuarioLogro.getFechaCreacion(), fechaFinalizacion);
        } else {
            librosLeidos = repositorioUsuarioLibro.buscarLibrosLeidosPorAño(LocalDate.now().getYear(), usuarioLogro.getUsuario()).size();
        }

        return librosLeidos > usuarioLogro.getLogro().getObjetivoLibros();
    }

    private LocalDate obtenerFechaDeFinalizacionLogro(UsuarioLogro usuarioLogro) {
        LocalDate fechaFinalizacion = null;

        if (usuarioLogro.getPlazoDias() != null && usuarioLogro.getPlazoDias() > 0) {
            fechaFinalizacion = usuarioLogro.getFechaCreacion().plusDays(usuarioLogro.getPlazoDias());
        }

        return fechaFinalizacion;
    }

    private boolean plazoVencido(UsuarioLogro usuarioLogro) {

        if (usuarioLogro.getPlazoDias() == null || usuarioLogro.getPlazoDias() == 0) {
            return false;
        }

        long diasTranscurridos = ChronoUnit.DAYS.between(usuarioLogro.getFechaCreacion(), LocalDate.now());
        return diasTranscurridos > usuarioLogro.getPlazoDias();
    }
}
