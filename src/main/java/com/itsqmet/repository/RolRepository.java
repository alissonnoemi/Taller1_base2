package com.itsqmet.repository;

import com.itsqmet.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolRepository extends JpaRepository <Rol, Long> {
    List<Rol> findByNombreContainingIgnoreCase(String nombre); //buscar por nombre
}
