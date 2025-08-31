package com.itsqmet.controller;

import com.itsqmet.entity.Citas;
import com.itsqmet.entity.Profesional;
import com.itsqmet.entity.Servicio;
import com.itsqmet.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class CitasController {

    @Autowired private CitasServicio citasServicio;
    @Autowired private ClienteServicio clienteServicio;
    @Autowired private ProfesionalServicio profesionalServicio;
    @Autowired private ServicioServicio servicioServicio;
    @Autowired private NegocioServicio negocioServicio;

    // ===================== VIEWS =====================
    @GetMapping("/listaCita")
    public String mostrarListaCitas(Model model) {
        model.addAttribute("citas", citasServicio.obtenerTodosLosCitas());
        return "pages/listaCita"; // Admin view
    }

    @GetMapping("/agendar")
    public String mostrarFormularioAgendar(Model model) {
        Citas cita = new Citas();
        cita.setDuracionServicioHoras(0L);
        model.addAttribute("cita", cita);
        model.addAttribute("clientes", clienteServicio.obtenerTodosLosClientes());
        model.addAttribute("negocios", negocioServicio.obtenerTodosLosNegocios());
        model.addAttribute("profesionales", Collections.emptyList());
        model.addAttribute("servicios", Collections.emptyList());
        return "pages/cita";
    }

    @GetMapping("/editarCita/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model) {
        Optional<Citas> citaOpt = citasServicio.buscarCitaPorId(id);
        if (citaOpt.isPresent()) {
            Citas cita = citaOpt.get();
            model.addAttribute("cita", cita);
            model.addAttribute("clientes", clienteServicio.obtenerTodosLosClientes());
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

    // ===================== CRUD =====================
    @PostMapping({"/agendar", "/editarCita/{id}"})
    public String guardarOCambiarCita(@PathVariable(required = false) Long id,
                                      @Valid @ModelAttribute("cita") Citas cita,
                                      BindingResult result,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        Runnable addCommonAttributes = () -> {
            model.addAttribute("clientes", clienteServicio.obtenerTodosLosClientes());
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
            addCommonAttributes.run();
            return "pages/cita";
        }

        try {
            if (id != null) cita.setIdCita(id);
            citasServicio.guardarOCambiarCita(cita);
            redirectAttributes.addFlashAttribute("mensajeTipo", "success");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", id != null ? "Cita actualizada exitosamente!" : "Cita agendada exitosamente!");
            return "redirect:/listaCita";
        } catch (Exception e) {
            model.addAttribute("mensajeTipo", "error");
            model.addAttribute("mensajeCuerpo", e.getMessage());
            addCommonAttributes.run();
            return "pages/cita";
        }
    }

    @GetMapping("/eliminarCita/{id}")
    public String eliminarCita(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            citasServicio.eliminarCita(id);
            redirectAttributes.addFlashAttribute("mensajeTipo", "success");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", "Cita eliminada exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeTipo", "error");
            redirectAttributes.addFlashAttribute("mensajeCuerpo", e.getMessage());
        }
        return "redirect:/listaCita";
    }
    @GetMapping("/pages/profesionales/porNegocio/{negocioId}")
    @ResponseBody
    public List<Profesional> getProfesionalesPorNegocio(@PathVariable Long negocioId) {
        return profesionalServicio.obtenerProfesionalesPorNegocio(negocioId);
    }

    @GetMapping("/api/servicios/porNegocio/{negocioId}")
    @ResponseBody
    public List<Servicio> getServiciosPorNegocio(@PathVariable Long negocioId) {
        return servicioServicio.obtenerServiciosPorNegocio(negocioId);
    }

    // Devuelve todas las citas para admin
    @GetMapping("/pages/admin")
    @ResponseBody
    public List<Citas> getCitasAdmin() {
        return citasServicio.obtenerTodosLosCitas();
    }

    // Devuelve solo citas de un cliente
    @GetMapping("/pages/cliente/{clienteId}")
    @ResponseBody
    public List<Citas> getCitasCliente(@PathVariable Long clienteId) {
        return citasServicio.obtenerCitasPorCliente(clienteId);
    }

    // Cancelar cita (cliente)
    @PutMapping("/pages/{citaId}/cancelar")
    @ResponseBody
    public ResponseEntity<?> cancelarCita(@PathVariable Long citaId) {
        citasServicio.cancelarCita(citaId); // solo cambia estado a CANCELADA
        return ResponseEntity.ok().build();
    }

    // Actualizar fechas (drag & drop) – solo admin
    @PutMapping("/pages/{citaId}/actualizar-fecha")
    public ResponseEntity<?> actualizarFecha(@PathVariable Long citaId, @RequestBody Map<String,String> fechas) {
        try {
            citasServicio.actualizarFechas(
                    citaId,
                    fechas.get("fechaHoraInicio"),
                    fechas.get("fechaHoraFin")
            );
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar la cita: " + e.getMessage());
        }
    }
}
