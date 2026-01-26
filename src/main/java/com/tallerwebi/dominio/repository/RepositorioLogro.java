package com.tallerwebi.dominio.repository;

import com.tallerwebi.dominio.model.Logro;

import java.util.List;

public interface RepositorioLogro {
    void guardar(Logro logro);
    Logro buscarPorId(Long id);
    List<Logro> obtenerLogrosPredefinidos();
    void borrar(Logro logro);
}
