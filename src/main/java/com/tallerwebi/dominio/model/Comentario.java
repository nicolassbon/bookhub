package com.tallerwebi.dominio.model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Texto del comentario que el usuario hizo sobre la reseña
    private String texto;

    @ManyToOne
    @JoinColumn(name = "resenia_id", nullable = false)
    private Resenia resenia;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Usuario usuario;

    private LocalDate fechaComentario;

    public Comentario() {
        this.fechaComentario = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Resenia getResenia() {
        return resenia;
    }

    public void setResenia(Resenia resenia) {
        this.resenia = resenia;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDate getFechaComentario() {
        return fechaComentario;
    }

    public void setFechaComentario(LocalDate fechaComentario) {
        this.fechaComentario = fechaComentario;
    }
}
