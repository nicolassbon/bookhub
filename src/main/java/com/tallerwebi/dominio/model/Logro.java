package com.tallerwebi.dominio.model;

import javax.persistence.*;

@Entity
public class Logro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private Integer objetivoLibros; // Objetivo de libros
    private Boolean esPredefinido;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getObjetivoLibros() {
        return objetivoLibros;
    }

    public void setObjetivoLibros(Integer objetivoLibros) {
        this.objetivoLibros = objetivoLibros;
    }

    public Boolean getEsPredefinido() {
        return esPredefinido;
    }

    public void setEsPredefinido(Boolean esPredefinido) {
        this.esPredefinido = esPredefinido;
    }
}
