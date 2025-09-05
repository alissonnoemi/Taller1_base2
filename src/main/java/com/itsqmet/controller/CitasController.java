package com.itsqmet.controller;

import com.itsqmet.entity.Citas;
import com.itsqmet.entity.Cliente;
import com.itsqmet.entity.Profesional;
import com.itsqmet.entity.Servicio;
import com.itsqmet.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
public class CitasController {

    @Autowired
    private CitasServicio citasServicio;
    @Autowired
    private ClienteServicio clienteServicio;
    @Autowired
    private ProfesionalServicio profesionalServicio;
    @Autowired
    private ServicioServicio servicioServicio;
    @Autowired
    private NegocioServicio negocioServicio;

    // 🔹 Mostrar lista de todas las citas (para administrador)
    @GetMapping("/listaCita")
    public String mostrarListaCitas(Model model) {
        model.addAttribute("citas", citasServicio.obtenerTodosLosCitas());
        return "pages/listaCita";
    }

    // 🔹 Formulario para agendar cita
    @GetMapping("/agendar")
    public String mostrarFormularioAgendar(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Cliente cliente = clienteServicio.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con email: " + email));

        Citas cita = new Citas();
        cita.setCliente(cliente);
        cita.setDuracionServicioHoras(0L);
        cita.setEstadoCita(Citas.EstadoCita.PENDIENTE); // 🔹 Inicializamos como PENDIENTE

        model.addAttribute("cita", cita);
        model.addAttribute("clienteNombre", cliente.getNombreCompleto());
        model.addAttribute("negocios", negocioServicio.obtenerTodosLosNegocios());
        model.addAttribute("profesionales", Collections.emptyList());
        model.addAttribute("servicios", Collections.emptyList());
        return "pages/cita";
    }

    // 🔹 Guardar nueva cita
    @PostMapping("/agendar")
    public String agendarCita(@Valid @ModelAttribute("cita") Citas cita,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Cliente cliente = clienteServicio.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con email: " + email));
        cita.setCliente(cliente);

        // 🔹 Aseguramos que el estado se mantenga como PENDIENTE
        cita.setEstadoCita(Citas.EstadoCita.PENDIENTE);

        Runnable addCommonModelAttributes = () -> {
            model.addAttribute("clienteNombre", cliente.getNombreCompleto());
            model.addAttribute("negocios", negocioServicio.obtenerTodosLosNegocios());
            if (cita.getNegocio() != null && cita.getNegocio().getIdNegocio() != null) {
                model.addAttribute("profesionales", profesionalServicio.obtenerProfesionalesPorNegocio(cita.getNegocio().getIdNegocio()));
                model.addAttribute("servicios", servicioServicio.obtenerServiciosPorNegocio(cita.getNegocio().getIdNegocio()));
            } else {
                model.addAttribute("profesionales", Collections.emptyList());
                model.addAttribute("servicios", Collections.emptyList());
            }
        };

        if (result.hasErrors()) {
            addCommonModelAttributes.run();
            return "pages/cita";
        }

        try {
            citasServicio.agendarNuevaCita(cita);
            redirectAttributes.addFlashAttribute("mensajeTipo", "success");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "Cita agendada exitosamente!");
            return "redirect:/mis-citas";
        } catch (IllegalArgumentException e) {
            model.addAttribute("mensajeTipo", "error");
            model.addAttribute("mensajeCuerpo", e.getMessage());
            addCommonModelAttributes.run();
            return "pages/cita";
        } catch (Exception e) {
            model.addAttribute("mensajeTipo", "error");
            model.addAttribute("mensajeCuerpo", "Error al agendar la cita: " + e.getMessage());
            addCommonModelAttributes.run();
            return "pages/cita";
        }
    }

    // 🔹 Ver solo las citas del cliente autenticado
    @GetMapping("/mis-citas")
    public String verMisCitas(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Cliente cliente = clienteServicio.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con email: " + email));

        model.addAttribute("citas", citasServicio.obtenerCitasPorCliente(cliente.getId()));
        model.addAttribute("clienteNombre", cliente.getNombreCompleto());
        return "pages/misCitas";
    }

    // 🔹 Editar cita
    @GetMapping("/editarCita/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model) {
        Optional<Citas> citaOpt = citasServicio.buscarCitaPorId(id);
        if (citaOpt.isPresent()) {
            Citas cita = citaOpt.get();
            Cliente cliente = cita.getCliente(); // 🔹 para mostrar el nombre

            model.addAttribute("cita", cita);
            model.addAttribute("clienteNombre", cliente.getNombreCompleto());
            model.addAttribute("negocios", negocioServicio.obtenerTodosLosNegocios());
            model.addAttribute("profesionales", Collections.emptyList());
            model.addAttribute("servicios", Collections.emptyList());
            return "pages/cita";
        } else {
            model.addAttribute("mensajeTipo", "error");
            model.addAttribute("mensajeCuerpo", "Cita no encontrada para edición.");
            return "redirect:/listaCita";
        }
    }

    @PostMapping("/editarCita/{id}")
    public String actualizarCita(@PathVariable("id") Long id,
                                 @Valid @ModelAttribute("cita") Citas cita,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        Runnable addCommonModelAttributes = () -> {
            model.addAttribute("clienteNombre", cita.getCliente() != null ? cita.getCliente().getNombreCompleto() : "");
            model.addAttribute("negocios", negocioServicio.obtenerTodosLosNegocios());
            if (cita.getNegocio() != null && cita.getNegocio().getIdNegocio() != null) {
                model.addAttribute("profesionales", profesionalServicio.obtenerProfesionalesPorNegocio(cita.getNegocio().getIdNegocio()));
                model.addAttribute("servicios", servicioServicio.obtenerServiciosPorNegocio(cita.getNegocio().getIdNegocio()));
            } else {
                model.addAttribute("profesionales", Collections.emptyList());
                model.addAttribute("servicios", Collections.emptyList());
            }
        };

        if (result.hasErrors()) {
            addCommonModelAttributes.run();
            return "pages/cita";
        }
        try {
            cita.setIdCita(id);
            citasServicio.actualizarCita(cita.getIdCita(), cita);
            redirectAttributes.addFlashAttribute("mensajeTipo", "success");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "Cita actualizada exitosamente!");
            return "redirect:/mis-citas";
        } catch (IllegalArgumentException e) {
            model.addAttribute("mensajeTipo", "error");
            model.addAttribute("mensajeCuerpo", e.getMessage());
            addCommonModelAttributes.run();
            return "pages/cita";
        } catch (Exception e) {
            model.addAttribute("mensajeTipo", "error");
            model.addAttribute("mensajeCuerpo", "Error al actualizar la cita: " + e.getMessage());
            addCommonModelAttributes.run();
            return "pages/cita";
        }
    }

    // 🔹 Eliminar cita
    @GetMapping("/eliminarCita/{id}")
    public String eliminarCita(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            citasServicio.eliminarCita(id);
            redirectAttributes.addFlashAttribute("mensajeTipo", "success");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "Cita eliminada exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeTipo", "error");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "Error al eliminar la cita: " + e.getMessage());
        }
        return "redirect:/mis-citas";
    }

    // 🔹 Endpoints REST (para AJAX)
    @GetMapping("/api/profesionales/porNegocio/{negocioId}")
    @ResponseBody
    public List<Profesional> getProfesionalesPorNegocio(@PathVariable("negocioId") Long negocioId) {
        return profesionalServicio.obtenerProfesionalesPorNegocio(negocioId);
    }

    @GetMapping("/api/servicios/porNegocio/{negocioId}")
    @ResponseBody
    public List<Servicio> getServiciosPorNegocio(@PathVariable("negocioId") Long negocioId) {
        return servicioServicio.obtenerServiciosPorNegocio(negocioId);
    }
}
