package com.tallerwebi.dominio.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "genero")
public class Genero {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
//
//    @ManyToMany(mappedBy = "generosFavoritos")
//    private Set<Usuario> usuarios = new HashSet<>();
//
////    @ManyToMany(mappedBy = "generos")
////    private Set<Autor> autores = new HashSet<>();
//
//    @ManyToMany
//    @JoinTable(
//            name = "libro_genero",
//            joinColumns = @JoinColumn(name = "genero_id"),
//            inverseJoinColumns = @JoinColumn(name = "libro_id")
//    )
//    private Set<Libro> libros = new HashSet<>();

    public Genero() {}

    public Genero(String nombre) {
        this.nombre = nombre;
    }

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

//    public Set<Usuario> getUsuarios() {
//        return usuarios;
//    }
//
//    public void setUsuarios(Set<Usuario> usuarios) {
//        this.usuarios = usuarios;
//    }

//    public Set<Autor> getAutores() {
//        return autores;
//    }
//
//    public void setAutores(Set<Autor> autores) {
//        this.autores = autores;
//    }

//    public Set<Libro> getLibros() {
//        return libros;
//    }
//
//    public void setLibros(Set<Libro> libros) {
//        this.libros = libros;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Genero)) return false;
        Genero genero = (Genero) o;
        return Objects.equals(id, genero.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}