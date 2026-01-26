package com.tallerwebi.dominio.model;

import com.tallerwebi.dominio.model.Usuario;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "amistad")
public class Amistad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "amigo_id")
    private Usuario amigo;

    @Column(nullable = false)
    private String estado;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_solicitud")
    private Date fechaSolicitud;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_aceptada")
    private Date fechaAceptada;

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

    public Usuario getAmigo() {
        return amigo;
    }

    public void setAmigo(Usuario amigo) {
        this.amigo = amigo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Date getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(Date fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public Date getFechaAceptada() {
        return fechaAceptada;
    }

    public void setFechaAceptada(Date fechaAceptada) {
        this.fechaAceptada = fechaAceptada;
    }
}
