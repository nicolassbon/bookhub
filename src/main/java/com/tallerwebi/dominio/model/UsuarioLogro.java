package com.tallerwebi.dominio.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "usuario_logro")
public class UsuarioLogro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "logro_id")
    private Logro logro;

    private String estadoLogro; //EN_PROGRESO, COMPLETADO, NO_COMPLETADO

    private LocalDate fechaCreacion;

    private Integer plazoDias;

    public UsuarioLogro() {
        fechaCreacion = LocalDate.now();
        estadoLogro = "EN_PROGRESO";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Logro getLogro() {
        return logro;
    }

    public void setLogro(Logro logro) {
        this.logro = logro;
    }

    public String getEstadoLogro() {
        return estadoLogro;
    }

    public void setEstadoLogro(String estadoLogro) {
        this.estadoLogro = estadoLogro;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Integer getPlazoDias() {
        return plazoDias;
    }

    public void setPlazoDias(Integer plazoDias) {
        this.plazoDias = plazoDias;
    }

    public long getDiasRestantes() {
        if (this.plazoDias != null) {
            LocalDate fechaLimite = fechaCreacion.plusDays(plazoDias);
            return ChronoUnit.DAYS.between(LocalDate.now(), fechaLimite);
        }
        return -1; // Si no tiene plazo, no se calcula
    }
}
