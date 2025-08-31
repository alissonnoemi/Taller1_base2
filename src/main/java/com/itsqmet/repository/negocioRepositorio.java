package com.itsqmet.repository;

import com.itsqmet.entity.Negocio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface negocioRepositorio extends JpaRepository <Negocio, Long> {
    List <Negocio> findByTipoNegocioContainingIgnoreCase(String nombreNegocio);
    Optional <Negocio> findByEmail(String email);
    Optional<Negocio> findByRuc(String ruc);

}

