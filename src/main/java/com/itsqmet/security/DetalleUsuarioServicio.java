package com.itsqmet.security;

import com.itsqmet.entity.Admin;
import com.itsqmet.entity.Cliente;
import com.itsqmet.entity.Negocio;
import com.itsqmet.repository.AdminRepositorio;
import com.itsqmet.repository.clienteRepositorio;
import com.itsqmet.repository.negocioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Service
public class DetalleUsuarioServicio implements UserDetailsService {

    @Autowired
    private clienteRepositorio clienteRepositorio;

    @Autowired
    private negocioRepositorio negocioRepositorio;

    @Autowired
    private AdminRepositorio adminRepositorio;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1️⃣ Admin primero
        Optional<Admin> adminOptional = adminRepositorio.findByEmail(email);
        if (adminOptional.isPresent()) {
            Admin admin = adminOptional.get();
            Collection<? extends GrantedAuthority> authorities =
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
            return User.builder()
                    .username(admin.getEmail())
                    .password(admin.getPassword())
                    .authorities(authorities)
                    .build();
        }

        // 2️⃣ Negocio
        Optional<Negocio> negocioOptional = negocioRepositorio.findByEmail(email);
        if (negocioOptional.isPresent()) {
            Negocio negocio = negocioOptional.get();
            Collection<? extends GrantedAuthority> authorities =
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_NEGOCIO"));
            return User.builder()
                    .username(negocio.getEmail())
                    .password(negocio.getPassword())
                    .authorities(authorities)
                    .build();
        }

        // 3️⃣ Cliente
        Optional<Cliente> clienteOptional = clienteRepositorio.findByEmail(email);
        if (clienteOptional.isPresent()) {
            Cliente cliente = clienteOptional.get();
            Collection<? extends GrantedAuthority> authorities =
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENTE"));
            return User.builder()
                    .username(cliente.getEmail())
                    .password(cliente.getPassword())
                    .authorities(authorities)
                    .build();
        }

        throw new UsernameNotFoundException("No se encontró usuario con el email: " + email);
    }
}
