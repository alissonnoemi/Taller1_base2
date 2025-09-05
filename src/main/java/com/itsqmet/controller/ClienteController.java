package com.itsqmet.controller;

import com.itsqmet.entity.*;
import com.itsqmet.service.ClienteServicio;
import com.itsqmet.service.ProfesionalServicio;
import com.itsqmet.service.RolServicio;
import com.itsqmet.service.ServicioServicio;
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
    @GetMapping("/listaClientes")
    public String listaClientes(Model model) {
        model.addAttribute("clientes", clienteServicio.obtenerTodosLosClientes());
        return "pages/listaClientes";
    }

    @GetMapping("/registroCliente")
    public String mostrarFormularioRegistroCliente(Model model) {
        List<Rol> roles = rolServicio.mostrarRol();
        System.out.println("Roles encontrados: " + roles); // Verifica que no esté vacío
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

            clienteServicio.guardarCliente(cliente);
            redirectAttributes.addFlashAttribute("mensajeTipo", "success");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "Cliente registrado exitosamente! Ya puedes iniciar sesión.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeTipo", "error");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", e.getMessage());
            return "pages/registroCliente";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeTipo", "error");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "Error al registrar el cliente: " + e.getMessage());
            return "pages/registroCliente";
        }
    }

    @GetMapping("/editarCliente/{id}")
    public String mostrarFormularioEditarCliente(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Cliente> clienteOptional = clienteServicio.obtenerClientePorId(id);
        if (clienteOptional.isPresent()) {
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

            cliente.setId(id);
            clienteServicio.guardarCliente(cliente); // El servicio detectará el ID y actualizará
            redirectAttributes.addFlashAttribute("mensajeTipo", "success");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "Cliente actualizado exitosamente!");
            return "redirect:/listaClientes"; // Redirige a la lista de clientes después de una actualización exitosa
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeTipo", "error");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", e.getMessage());
            return "pages/registroCliente"; // Vuelve al formulario con el mensaje de error
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeTipo", "error");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "Error al actualizar el cliente: " + e.getMessage());
            return "pages/registroCliente"; // Vuelve al formulario con el mensaje de error
        }
    }

    @PostMapping("/eliminarCliente/{id}")
    public String eliminarCliente(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            clienteServicio.eliminarCliente(id);
            redirectAttributes.addFlashAttribute("mensajeTipo", "success");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "Cliente eliminado exitosamente!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeTipo", "error");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeTipo", "error");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "Error al eliminar el cliente.");
        }
        return "redirect:/listaClientes";
    }

    @GetMapping("/inicioClientes")
    public String mostrarLogin(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "pages/inicioClientes";
    }


}