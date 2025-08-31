package com.itsqmet.repository;

import com.itsqmet.entity.Admin;
import com.itsqmet.entity.Negocio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepositorio extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);
}
