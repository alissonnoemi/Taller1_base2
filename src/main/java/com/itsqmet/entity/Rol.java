package com.itsqmet.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.List;

@Entity
public class Rol {
    @Id
    @GeneratedValue (strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    @OneToMany(mappedBy = "rol")
    private List<Cliente> clientes;
    @OneToMany (mappedBy = "rol")
    private List<Negocio> negocios;
    @OneToMany (mappedBy = "rol")
    private  List<Admin> admin;

    public Rol() {
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
}
