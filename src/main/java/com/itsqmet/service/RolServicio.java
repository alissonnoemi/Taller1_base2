package com.itsqmet.service;

import com.itsqmet.entity.Rol;
import com.itsqmet.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RolServicio {

    @Autowired
    private RolRepository rolRepositorio;

    public List<Rol> mostrarRol() {
        return rolRepositorio.findAll();
    }

    public Optional<Rol> obtenerRolPorId(Long id) {
        return rolRepositorio.findById(id);
    }
}
