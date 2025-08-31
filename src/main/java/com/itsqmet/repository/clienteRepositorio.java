package com.itsqmet.repository;

import com.itsqmet.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface clienteRepositorio extends JpaRepository<Cliente, Long> {
   @Override
   Optional<Cliente> findById(Long aLong);

   Optional<Cliente> findByEmail(String email);
   List<Cliente> findByNombreCompletoContainingIgnoreCase(String nombreCompleto);
}