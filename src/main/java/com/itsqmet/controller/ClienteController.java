package com.itsqmet.controller;

import com.itsqmet.entity.Admin;
import com.itsqmet.entity.Cliente;
import com.itsqmet.entity.Rol;
import com.itsqmet.service.ClienteServicio;
import com.itsqmet.service.ProfesionalServicio;
import com.itsqmet.service.RolServicio;
import com.itsqmet.service.ServicioServicio;
import com.itsqmet.repository.AdminRepositorio;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class ClienteController {

    @Autowired
    private ClienteServicio clienteServicio;

    @Autowired
    private ProfesionalServicio profesionalServicio;

    @Autowired
    private ServicioServicio servicioServicio;

    @Autowired
    private RolServicio rolServicio;

    @Autowired
    private AdminRepositorio adminRepositorio;

    // ---------------- LISTAR CLIENTES ----------------
    @GetMapping("/listaClientes")
    public String listaClientes(Model model) {
        model.addAttribute("clientes", clienteServicio.obtenerTodosLosClientes());
        return "pages/listaClientes";
    }

    // ---------------- REGISTRO CLIENTE ----------------
    @GetMapping("/registroCliente")
    public String mostrarFormularioRegistroCliente(Model model) {
        List<Rol> roles = rolServicio.mostrarRol();
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("roles", roles);
        return "pages/registroCliente";
    }

    @PostMapping("/registroCliente")
    public String registrarCliente(@Valid @ModelAttribute("cliente") Cliente cliente,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "pages/registroCliente";
        }

        try {
            // 🔹 Resolver objeto Rol completo
            if (cliente.getRol() != null && cliente.getRol().getId() != null) {
                Rol rol = rolServicio.obtenerRolPorId(cliente.getRol().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Rol inválido"));
                cliente.setRol(rol);
            }

            // 🔹 Guardar cliente
            clienteServicio.guardarCliente(cliente);

            // 🔹 Crear Admin si el rol es ADMIN
            if (cliente.getRol() != null && "ADMIN".equalsIgnoreCase(cliente.getRol().getNombre())) {
                Admin admin = new Admin();
                admin.setEmail(cliente.getEmail());
                admin.setPassword(cliente.getPassword()); // ya encriptada
                admin.setRol(cliente.getRol());
                adminRepositorio.save(admin);
            }

            redirectAttributes.addFlashAttribute("mensajeTipo", "success");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "Cliente registrado exitosamente!");
            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeTipo", "error");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "Error al registrar el cliente: " + e.getMessage());
            return "pages/registroCliente";
        }
    }

    // ---------------- EDITAR CLIENTE ----------------
    @GetMapping("/editarCliente/{id}")
    public String mostrarFormularioEditarCliente(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Cliente> clienteOptional = clienteServicio.obtenerClientePorId(id);
        if (clienteOptional.isPresent()) {
            List<Rol> roles = rolServicio.mostrarRol();
            model.addAttribute("roles", roles);
            model.addAttribute("cliente", clienteOptional.get());
            return "pages/registroCliente";
        } else {
            redirectAttributes.addFlashAttribute("mensajeTipo", "error");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "Cliente no encontrado para editar.");
            return "redirect:/listaClientes";
        }
    }

    @PostMapping("/editarCliente/{id}")
    public String actualizarCliente(@PathVariable("id") Long id,
                                    @Valid @ModelAttribute("cliente") Cliente cliente,
                                    BindingResult result,
                                    RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "pages/registroCliente";
        }

        try {
            // 🔹 Resolver rol completo antes de actualizar
            if (cliente.getRol() != null && cliente.getRol().getId() != null) {
                Rol rol = rolServicio.obtenerRolPorId(cliente.getRol().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Rol inválido"));
                cliente.setRol(rol);
            }

            cliente.setId(id);
            clienteServicio.guardarCliente(cliente); // actualizar cliente

            redirectAttributes.addFlashAttribute("mensajeTipo", "success");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "Cliente actualizado exitosamente!");
            return "redirect:/listaClientes";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeTipo", "error");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "Error al actualizar el cliente: " + e.getMessage());
            return "pages/registroCliente";
        }
    }

    @PostMapping("/eliminarCliente/{id}")
    public String eliminarCliente(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            clienteServicio.eliminarCliente(id);
            redirectAttributes.addFlashAttribute("mensajeTipo", "success");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "Cliente eliminado exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeTipo", "error");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "Error al eliminar el cliente: " + e.getMessage());
        }
        return "redirect:/listaClientes";
    }

    @GetMapping("/inicioClientes")
    public String mostrarLogin(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "pages/inicioClientes";
    }
}
