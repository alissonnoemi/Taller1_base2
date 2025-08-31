package com.itsqmet.repository;

import com.itsqmet.entity.Citas;
import com.itsqmet.entity.Cliente;
import com.itsqmet.entity.Negocio;
import com.itsqmet.entity.Profesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface citasRepositorio extends JpaRepository<Citas, Long> {

    List<Citas> findByClienteOrderByFechaHoraInicioAsc(Cliente cliente);

    List<Citas> findByProfesionalOrderByFechaHoraInicioAsc(Profesional profesional);

    List<Citas> findByProfesional_NegocioOrderByFechaHoraInicioAsc(Negocio negocio);

    // Conflictos para nueva cita
    @Query("SELECT c FROM Citas c WHERE c.profesional = :profesional " +
            "AND c.fechaHoraInicio <= :finCitaNueva AND c.fechaHoraFin >= :inicioCitaNueva")
    List<Citas> findConflictingAppointments(@Param("profesional") Profesional profesional,
                                            @Param("inicioCitaNueva") LocalDateTime inicioCitaNueva,
                                            @Param("finCitaNueva") LocalDateTime finCitaNueva);

    // Conflictos para actualizar cita (excluye la propia)
    @Query("SELECT c FROM Citas c WHERE c.profesional = :profesional " +
            "AND c.idCita != :idCitaActual " +
            "AND c.fechaHoraInicio <= :finCitaNueva AND c.fechaHoraFin >= :inicioCitaNueva")
    List<Citas> findConflictingAppointmentsExcludingSelf(@Param("profesional") Profesional profesional,
                                                         @Param("inicioCitaNueva") LocalDateTime inicioCitaNueva,
                                                         @Param("finCitaNueva") LocalDateTime finCitaNueva,
                                                         @Param("idCitaActual") Long idCitaActual);
}
