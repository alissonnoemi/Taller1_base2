package com.itsqmet.service;

import com.itsqmet.entity.Rol;
import com.itsqmet.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolServicio {
    @Autowired
    private RolRepository rolRepository;
    //Mostrar los roles
    public List <Rol> mostrarRol(){
        return rolRepository.findAll();
    }
}
