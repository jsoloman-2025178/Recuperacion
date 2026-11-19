package gt.taller.appointment.repository;

import gt.taller.appointment.entity.Appointment;
import gt.taller.appointment.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Consulta usada por la deteccion de traslapes:
     * todas las citas del mecanico en esa fecha salvo las CANCELADA (cupo liberado).
     */
    List<Appointment> findByMechanicIdAndAppointmentDateAndStatusNot(
            Long mechanicId, LocalDate appointmentDate, AppointmentStatus status);

    /**
     * Agenda con filtros opcionales por fecha, mecanico y estado.
     */
    @Query("""
            select a from Appointment a
            where (:date is null or a.appointmentDate = :date)
              and (:mechanicId is null or a.mechanicId = :mechanicId)
              and (:status is null or a.status = :status)
            order by a.appointmentDate, a.startTime, a.id
            """)
    List<Appointment> search(@Param("date") LocalDate date,
                             @Param("mechanicId") Long mechanicId,
                             @Param("status") AppointmentStatus status);
}
