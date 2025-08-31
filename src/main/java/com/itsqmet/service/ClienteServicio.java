package com.itsqmet.service;

import com.itsqmet.entity.Cliente;
import com.itsqmet.repository.clienteRepositorio;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteServicio {

    @Autowired
    private clienteRepositorio clienteRepositorio;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Transactional
    public Cliente guardarCliente(Cliente cliente) {
        String passwordEncriptado=passwordEncoder.encode(cliente.getPassword());
        //añado el encriptado al objeto
        cliente.setPassword(passwordEncriptado);
        if (cliente.getNombreCompleto() == null || cliente.getNombreCompleto().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente no puede estar vacío.");
        }

        return clienteRepositorio.save(cliente);
    }

    public Optional<Cliente> obtenerClientePorId(Long id) {
        return clienteRepositorio.findById(id);
    }

    public List<Cliente> obtenerTodosLosClientes() {
        return clienteRepositorio.findAll();
    }


    public void eliminarCliente(Long id) {
        if (!clienteRepositorio.existsById(id)) {
            throw new IllegalArgumentException("Cliente no encontrado con ID: " + id);
        }
        clienteRepositorio.deleteById(id);
    }
}