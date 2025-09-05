package com.itsqmet.service;

import com.itsqmet.entity.*;
import com.itsqmet.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class CitasServicio {
    @Autowired
    private citasRepositorio citasRepositorio;
    @Autowired
    private clienteRepositorio pacienteRepositorio;
    @Autowired
    private servicioRepositorio servicioRepositorio;
    @Autowired
    private profesionalRepositorio profesionalRepositorio;
    @Autowired
    private negocioRepositorio negocioRepositorio;

    @Transactional
    public Citas agendarNuevaCita(Citas nuevaCita) throws Exception {
        // Validación de IDs y recuperación de entidades
        Cliente cliente = pacienteRepositorio.findById(nuevaCita.getCliente().getId())
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado con ID: " + nuevaCita.getCliente().getId()));
        Servicio servicio = servicioRepositorio.findById(nuevaCita.getServicio().getIdServicio())
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con ID: " + nuevaCita.getServicio().getIdServicio()));
        Profesional profesional = profesionalRepositorio.findById(nuevaCita.getProfesional().getIdProfesional())
                .orElseThrow(() -> new IllegalArgumentException("Profesional no encontrado con ID: " + nuevaCita.getProfesional().getIdProfesional()));

        nuevaCita.setCliente(cliente);
        nuevaCita.setServicio(servicio);
        nuevaCita.setProfesional(profesional);

        if (nuevaCita.getFechaHoraInicio() == null) {
            throw new IllegalArgumentException("La fecha y hora de inicio de la cita es obligatoria.");
        }

        // Si la duración no se especifica o es inválida, usa la del servicio
        if (nuevaCita.getDuracionServicioHoras() == null || nuevaCita.getDuracionServicioHoras() <= 0) {
            nuevaCita.setDuracionServicioHoras(servicio.getDuracionHoras());
        }
        nuevaCita.setFechaHoraFin(nuevaCita.getFechaHoraInicio().plusMinutes(nuevaCita.getDuracionServicioHoras()));

        List<Citas> citasConflictivas = citasRepositorio.findConflictingAppointments(
                profesional, nuevaCita.getFechaHoraInicio(), nuevaCita.getFechaHoraFin());

        if (!citasConflictivas.isEmpty()) {
            throw new IllegalArgumentException("Ya existe una cita agendada para el profesional '" +
                    profesional.getNombreCompleto() + "' en el horario seleccionado (" +
                    nuevaCita.getFechaHoraInicio().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) + " - " +
                    nuevaCita.getFechaHoraFin().format(DateTimeFormatter.ofPattern("HH:mm")) + ").");
        }
        if (nuevaCita.getEstadoCita() == null) {
            nuevaCita.setEstadoCita(Citas.EstadoCita.PENDIENTE);
        }

        return citasRepositorio.save(nuevaCita);
    }

    public List<Citas> obtenerTodosLosCitas() {
        return citasRepositorio.findAll();
    }

    public Optional<Citas> buscarCitaPorId(Long id) {
        return citasRepositorio.findById(id);
    }

    @Transactional
    public Citas actualizarCita(Long id, Citas citaActualizada) throws Exception {
        Citas citaExistente = citasRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada con ID: " + id));

        // Actualizar campos directos
        if (citaActualizada.getFechaHoraInicio() != null) {
            citaExistente.setFechaHoraInicio(citaActualizada.getFechaHoraInicio());
        }

        // Actualizar el Cliente si el ID ha cambiado
        if (citaActualizada.getCliente() != null && citaActualizada.getCliente().getId() != null &&
                (citaExistente.getCliente() == null || !citaExistente.getCliente().getId().equals(citaActualizada.getCliente().getId()))) {
            Cliente nuevoCliente = pacienteRepositorio.findById(citaActualizada.getCliente().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Nuevo Paciente no encontrado con ID: " + citaActualizada.getCliente().getId()));
            citaExistente.setCliente(nuevoCliente);
        }

        // Actualizar el Servicio y recalcular la duración si es necesario
        Servicio servicioActualSeleccionado = citaExistente.getServicio(); // Por defecto, el servicio actual
        if (citaActualizada.getServicio() != null && citaActualizada.getServicio().getIdServicio() != null) {
            // Si el servicio en la cita actualizada es diferente al existente
            if (citaExistente.getServicio() == null || !citaExistente.getServicio().getIdServicio().equals(citaActualizada.getServicio().getIdServicio())) {
                Servicio nuevoServicio = servicioRepositorio.findById(citaActualizada.getServicio().getIdServicio())
                        .orElseThrow(() -> new IllegalArgumentException("Nuevo Servicio no encontrado con ID: " + citaActualizada.getServicio().getIdServicio()));
                citaExistente.setServicio(nuevoServicio);
                servicioActualSeleccionado = nuevoServicio; // Ahora el servicio para la duración es el nuevo
            }
        }

        // Determinar la duración final para citaExistente
        if (citaActualizada.getDuracionServicioHoras() != null && citaActualizada.getDuracionServicioHoras() > 0) {
            // Si el usuario envió una duración válida, la usamos
            citaExistente.setDuracionServicioHoras(citaActualizada.getDuracionServicioHoras());
        } else if (servicioActualSeleccionado != null && servicioActualSeleccionado.getDuracionHoras() != null && servicioActualSeleccionado.getDuracionHoras() > 0) {
            // Si no se envió duración o es inválida, pero tenemos un servicio (nuevo o existente) con duración, la usamos
            citaExistente.setDuracionServicioHoras(servicioActualSeleccionado.getDuracionHoras());
        } else {
            // Si no hay duración válida ni servicio con duración, puedes manejarlo aquí (ej. lanzar error, usar un valor por defecto)
            // Por ahora, si no se puede determinar, se mantendrá el valor que ya tenía citaExistente o null.
            // Para ser más seguro, podrías lanzar: throw new IllegalArgumentException("No se pudo determinar la duración del servicio.");
        }


        // Recalcular la fecha de fin con la duración final (ahora que la duración está establecida)
        if (citaExistente.getFechaHoraInicio() != null && citaExistente.getDuracionServicioHoras() != null) {
            citaExistente.setFechaHoraFin(citaExistente.getFechaHoraInicio().plusMinutes(citaExistente.getDuracionServicioHoras()));
        }


        // Actualizar el Profesional si el ID ha cambiado
        if (citaActualizada.getProfesional() != null && citaActualizada.getProfesional().getIdProfesional() != null &&
                (citaExistente.getProfesional() == null || !citaExistente.getProfesional().getIdProfesional().equals(citaActualizada.getProfesional().getIdProfesional()))) {
            Profesional nuevoProfesional = profesionalRepositorio.findById(citaActualizada.getProfesional().getIdProfesional())
                    .orElseThrow(() -> new IllegalArgumentException("Nuevo Profesional no encontrado con ID: " + citaActualizada.getProfesional().getIdProfesional()));
            citaExistente.setProfesional(nuevoProfesional);
        }

        // Actualizar el Estado de la Cita
        if (citaActualizada.getEstadoCita() != null) {
            citaExistente.setEstadoCita(citaActualizada.getEstadoCita());
        }


        // Lógica de validación de superposición para actualización
        List<Citas> citasConflictivas = citasRepositorio.findConflictingAppointmentsExcludingSelf(
                citaExistente.getProfesional(), citaExistente.getFechaHoraInicio(), citaExistente.getFechaHoraFin(), citaExistente.getIdCita());

        if (!citasConflictivas.isEmpty()) {
            throw new IllegalArgumentException("Ya existe una cita agendada para el profesional '" +
                    citaExistente.getProfesional().getNombreCompleto() + "' en el horario seleccionado (" +
                    citaExistente.getFechaHoraInicio().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) + " - " +
                    citaExistente.getFechaHoraFin().format(DateTimeFormatter.ofPattern("HH:mm")) + ").");
        }

        // Finalmente, guardar la cita con todos los cambios
        return citasRepositorio.save(citaExistente);
    }

    public void eliminarCita(Long id) {
        if (!citasRepositorio.existsById(id)) {
            throw new IllegalArgumentException("Cita no encontrada con ID: " + id);
        }
        citasRepositorio.deleteById(id);
    }

    @Transactional
    public List<Citas> obtenerCitasPorNegocio(Long negocioId) {
        Negocio negocio = negocioRepositorio.findById(negocioId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado con ID: " + negocioId));
        return citasRepositorio.findByProfesional_NegocioOrderByFechaHoraInicioAsc(negocio);
    }

    @Transactional
    public List<Citas> obtenerCitasPorProfesional(Long profesionalId) {
        Profesional profesional = profesionalRepositorio.findById(profesionalId)
                .orElseThrow(() -> new IllegalArgumentException("Profesional no encontrado con ID: " + profesionalId));
        return citasRepositorio.findByProfesionalOrderByFechaHoraInicioAsc(profesional);
    }
}