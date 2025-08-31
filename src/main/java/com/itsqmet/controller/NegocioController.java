package com.itsqmet.controller;

import com.itsqmet.dto.NegocioRegistroDTO;
import com.itsqmet.entity.Negocio;
import com.itsqmet.entity.Profesional;
import com.itsqmet.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class NegocioController {

    @Autowired
    private NegocioServicio negocioServicio;
    @Autowired
    private CitasServicio citasServicio;
    @Autowired
    private ProfesionalServicio profesionalServicio;
    @Autowired
    private ServicioServicio servicioServicio;
    @Autowired
    private RolServicio rolServicio;
    @GetMapping("/registroNegocio")
    public String mostrarFormularioNuevoNegocio(Model model) {
        if (!model.containsAttribute("negocioDto")) {
            model.addAttribute("negocioDto", new NegocioRegistroDTO());
        }
        model.addAttribute("roles", rolServicio.mostrarRol());
        return "pages/registroNegocio";
    }
    //Listar todos los negocios
    @GetMapping("/negocios")
    public String listarNegocios(@RequestParam(name = "buscarNegocio", required = false, defaultValue = "") String buscarNegocio,
                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                 Model model) {
        model.addAttribute("buscarNegocio", buscarNegocio);
        model.addAttribute("negocios", negocioServicio.buscarNegocioPorNombre(buscarNegocio));
        return "pages/listaNegocio";
    }

    @PostMapping("/guardarNegocio")
    public String guardarNegocio(@Valid @ModelAttribute("negocioDto") NegocioRegistroDTO negocioDto,
                                 BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("negocioDto", negocioDto);
            model.addAttribute("mensajeTipo", "error");
            model.addAttribute("mensajeCuerpo", "Por favor, corrige los errores en el formulario.");
            return "pages/registroNegocio";
        }
        String rucValidationMessage = negocioServicio.validarRucParaRegistro(negocioDto.getRuc());
        if (rucValidationMessage != null) {
            result.addError(new FieldError("negocioDto", "ruc", negocioDto.getRuc(), false, null, null, rucValidationMessage));
            model.addAttribute("mensajeTipo", "error");
            model.addAttribute("mensajeCuerpo", rucValidationMessage);
            model.addAttribute("negocioDto", negocioDto);
            return "pages/registroNegocio";
        }
        // Validar RUC solo si es de otro negocio
        if (negocioServicio.isRucAlreadyRegistered(negocioDto.getRuc())) {
            Optional<Negocio> existente = negocioServicio.obtenerNegocioPorRuc(negocioDto.getRuc());
            if (existente.isEmpty() || !existente.get().getIdNegocio().equals(negocioDto.getIdNegocio())) {
                result.addError(new FieldError("negocioDto", "ruc", negocioDto.getRuc(), false, null, null, "Este RUC ya está registrado en nuestra plataforma."));
                model.addAttribute("mensajeTipo", "error");
                model.addAttribute("mensajeCuerpo", "Este RUC ya está registrado en nuestra plataforma.");
                model.addAttribute("negocioDto", negocioDto);
                return "pages/registroNegocio";
            }
        }
        // Validar email solo si es de otro negocio
        if (negocioServicio.isEmailAlreadyRegistered(negocioDto.getEmail())) {
            Optional<Negocio> existente = negocioServicio.obtenerNegocioPorEmail(negocioDto.getEmail());
            if (existente.isEmpty() || !existente.get().getIdNegocio().equals(negocioDto.getIdNegocio())) {
                result.addError(new FieldError("negocioDto", "emailProfesional", negocioDto.getEmail(), false, null, null, "Este email profesional ya está en uso."));
                model.addAttribute("mensajeTipo", "error");
                model.addAttribute("mensajeCuerpo", "Este email profesional ya está en uso.");
                model.addAttribute("negocioDto", negocioDto);
                return "pages/registroNegocio";
            }
        }
        //VALIDACIÓN CONDICIONAL DE CAMPOS DE PAGO
        boolean requiresPaymentInfo = "basico".equals(negocioDto.getPlan()) || "pro".equals(negocioDto.getPlan());

        if (requiresPaymentInfo) {
            if (negocioDto.getCardNumber() == null || negocioDto.getCardNumber().isBlank()) {
                result.addError(new FieldError("negocioDto", "cardNumber", "El número de tarjeta es obligatorio para este plan."));
            }
            if (negocioDto.getExpiryDate() == null || negocioDto.getExpiryDate().isBlank()) {
                result.addError(new FieldError("negocioDto", "expiryDate", "La fecha de vencimiento es obligatoria para este plan."));
            }
            if (negocioDto.getCvv() == null || negocioDto.getCvv().isBlank()) {
                result.addError(new FieldError("negocioDto", "cvv", "El CVV es obligatorio para este plan."));
            }
            if (negocioDto.getBillingAddress() == null || negocioDto.getBillingAddress().isBlank()) {
                result.addError(new FieldError("negocioDto", "billingAddress", "La dirección de facturación es obligatoria para este plan."));
            }

            if (result.hasErrors()) {
                model.addAttribute("negocioDto", negocioDto);
                model.addAttribute("mensajeTipo", "error");
                model.addAttribute("mensajeCuerpo", "Por favor, completa los datos de pago para el plan seleccionado.");
                return "pages/registroNegocio";
            }
        }
        try {
            Negocio negocio = new Negocio();
            if (negocioDto.getIdNegocio() != null) {
                negocio.setIdNegocio(negocioDto.getIdNegocio());
            }
            // Mapeo de DTO a Entidad
            negocio.setNombreCompleto(negocioDto.getNombreCompleto());
            negocio.setEmail(negocioDto.getEmail());
            negocio.setPassword(negocioDto.getPassword());
            negocio.setNombreNegocio(negocioDto.getNombreNegocio());
            negocio.setTipoNegocio(negocioDto.getTipoNegocio());
            negocio.setDireccion(negocioDto.getDireccion());
            negocio.setTelefono(negocioDto.getTelefono());
            negocio.setRuc(negocioDto.getRuc());
            negocio.setPlan(negocioDto.getPlan());
            if (requiresPaymentInfo) {
                System.out.println("Procesando datos de pago para futura facturación (simulado):");
                System.out.println("  Tarjeta: " + negocioDto.getCardNumber());
                System.out.println("  Fecha: " + negocioDto.getExpiryDate());
                System.out.println("  CVV: " + negocioDto.getCvv());
                System.out.println("  Dirección Fact.: " + negocioDto.getBillingAddress());
            }
            negocioServicio.guardarNegocio(negocio);
            redirectAttributes.addFlashAttribute("mensajeTipo", "success");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "¡Felicidades! Has elegido el Plan " + negocio.getPlan().toUpperCase() + ". Disfruta de tus 14 días de prueba gratuita. La facturación comenzará después.");
            return "redirect:/negocios";
        } catch (Exception e) {
            result.reject("global.registrationError", "Hubo un error al intentar registrar tu negocio. Por favor, inténtalo de nuevo.");
            model.addAttribute("negocioDto", negocioDto);
            model.addAttribute("mensajeTipo", "error");
            model.addAttribute("mensajeCuerpo", "Error al registrar el negocio: " + e.getMessage());
            return "pages/registroNegocio";
        }
    }
    //Mostrar formulario para editar
    @GetMapping("/editarNegocio/{id}")
    public String mostrarFormularioEditarNegocio(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Negocio> negocioOptional = negocioServicio.obtenerNegocioPorId(id);
        if (negocioOptional.isPresent()) {
            Negocio negocio = negocioOptional.get();
            NegocioRegistroDTO negocioDto = new NegocioRegistroDTO();
            negocioDto.setIdNegocio(negocio.getIdNegocio());
            negocioDto.setNombreCompleto(negocio.getNombreCompleto());
            negocioDto.setEmail(negocio.getEmail());
            negocioDto.setNombreNegocio(negocio.getNombreNegocio());
            negocioDto.setTipoNegocio(negocio.getTipoNegocio());
            negocioDto.setDireccion(negocio.getDireccion());
            negocioDto.setTelefono(negocio.getTelefono());
            negocioDto.setRuc(negocio.getRuc());
            negocioDto.setPlan(negocio.getPlan());
            model.addAttribute("negocioDto", negocioDto);
            return "pages/registroNegocio";
        } else {
            redirectAttributes.addFlashAttribute("mensajeTipo", "error");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "Negocio no encontrado.");
            return "redirect:/negocios";
        }
    }

    // Eliminar Negocio y Ver Historial citas por negocio
    @PostMapping("/eliminarNegocio/{id}")
    public String eliminarNegocio(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            negocioServicio.eliminarNegocio(id);
            redirectAttributes.addFlashAttribute("mensajeTipo", "success");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "Negocio eliminado exitosamente!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeTipo", "error");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeTipo", "error");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "Error al eliminar el negocio.");
        }
        return "redirect:/negocios";
    }

    @GetMapping("/historialCitasNegocio/{idNegocio}")
    public String verHistorialCitasNegocio(@PathVariable("idNegocio") Long idNegocio, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<Negocio> negocioOptional = negocioServicio.obtenerNegocioPorId(idNegocio);
            if (negocioOptional.isPresent()) {
                Negocio negocio = negocioOptional.get();
                model.addAttribute("negocio", negocio);
                model.addAttribute("citas", citasServicio.obtenerCitasPorNegocio(idNegocio));
                model.addAttribute("profesionales", profesionalServicio.obtenerProfesionalesPorNegocio(idNegocio));
                model.addAttribute("servicios", servicioServicio.obtenerServiciosPorNegocio(idNegocio));
                return "/pages/historialCitasNegocio";
            } else {
                redirectAttributes.addFlashAttribute("mensajeTipo", "error");
                redirectAttributes.addFlashAttribute("mensajeCuerpo", "Negocio no encontrado.");
                return "redirect:/negocios";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeTipo", "error");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "Error al obtener el historial de citas: " + e.getMessage());
            return "redirect:/negocios";
        }
    }
}