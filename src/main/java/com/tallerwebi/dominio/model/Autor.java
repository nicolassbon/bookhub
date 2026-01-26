package com.tallerwebi.dominio.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "autor")
public class Autor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;

//    @ManyToMany(mappedBy = "autoresFavoritos")
//    private Set<Usuario> usuarios = new HashSet<>();

//    @ManyToMany
//    @JoinTable(
//            name = "autor_genero",
//            joinColumns = @JoinColumn(name = "autor_id"),
//            inverseJoinColumns = @JoinColumn(name = "genero_id")
//    )
//    private Set<Genero> generos = new HashSet<>();

    public Autor() {}

    public Autor(String nombre) {
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

}
