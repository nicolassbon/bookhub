package com.tallerwebi.dominio.model;

import javax.persistence.*;
import java.util.Date;

@Entity
public class UsuarioPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private Plan plan;

    private Double precio;
    private Date fecha_plan_adquirido;
    private Date fecha_plan_venc;

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Date getFecha_plan_adquirido() {
        return fecha_plan_adquirido;
    }

    public void setFecha_plan_adquirido(Date fecha_plan_adquirido) {
        this.fecha_plan_adquirido = fecha_plan_adquirido;
    }

    public Date getFecha_plan_venc() {
        return fecha_plan_venc;
    }

    public void setFecha_plan_venc(Date fecha_plan_venc) {
        this.fecha_plan_venc = fecha_plan_venc;
    }
}
