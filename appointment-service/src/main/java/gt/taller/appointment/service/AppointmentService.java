package gt.taller.appointment.service;

import gt.taller.appointment.client.MechanicClient;
import gt.taller.appointment.dto.AppointmentRequest;
import gt.taller.appointment.dto.MechanicDto;
import gt.taller.appointment.entity.Appointment;
import gt.taller.appointment.entity.AppointmentStatus;
import gt.taller.appointment.exception.BusinessValidationException;
import gt.taller.appointment.exception.ResourceNotFoundException;
import gt.taller.appointment.exception.ScheduleConflictException;
import gt.taller.appointment.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Reglas de negocio de la agenda del taller.
 * Las pruebas unitarias APT-01 a APT-07 ejercitan exactamente esta clase,
 * sin levantar MySQL ni servidor HTTP: el repositorio y MechanicClient son mocks.
 */
@Service
@Transactional(readOnly = true)
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final MechanicClient mechanicClient;

    public AppointmentService(AppointmentRepository appointmentRepository, MechanicClient mechanicClient) {
        this.appointmentRepository = appointmentRepository;
        this.mechanicClient = mechanicClient;
    }

    /**
     * APT-01 a APT-05: validar mecanico, jornada y traslape antes de guardar.
     */
    @Transactional
    public Appointment create(AppointmentRequest request) {
        MechanicDto mechanic = requireActiveMechanic(request.mechanicId());

        LocalTime start = request.startTime();
        LocalTime end = calculateEnd(start, request.durationMinutes());
        validateWorkingHours(mechanic, start, end);

        // Las CANCELADA no cuentan: al cancelar se libera el cupo.
        checkConflict(request.mechanicId(), request.appointmentDate(), start, end, null);

        Appointment appointment = new Appointment();
        applyRequest(appointment, request);
        appointment.setStatus(AppointmentStatus.PROGRAMADA);

        return appointmentRepository.save(appointment);
    }

    /**
     * Actualizacion de una cita existente: se repiten todas las validaciones,
     * excluyendo la propia cita de la busqueda de traslapes.
     */
    @Transactional
    public Appointment update(Long id, AppointmentRequest request) {
        Appointment appointment = findById(id);

        if (appointment.getStatus() == AppointmentStatus.CANCELADA
                || appointment.getStatus() == AppointmentStatus.COMPLETADA) {
            throw new BusinessValidationException(
                    "No se puede modificar una cita " + appointment.getStatus());
        }

        MechanicDto mechanic = requireActiveMechanic(request.mechanicId());
        LocalTime start = request.startTime();
        LocalTime end = calculateEnd(start, request.durationMinutes());
        validateWorkingHours(mechanic, start, end);
        checkConflict(request.mechanicId(), request.appointmentDate(), start, end, id);

        applyRequest(appointment, request);
        return appointmentRepository.save(appointment);
    }

    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una cita con id " + id));
    }

    /** Agenda con filtros opcionales por fecha, mecanico y estado. */
    public List<Appointment> findAll(LocalDate date, Long mechanicId, AppointmentStatus status) {
        return appointmentRepository.search(date, mechanicId, status);
    }

    /**
     * APT-06 / APT-07: transiciones controladas del ciclo de vida.
     */
    @Transactional
    public Appointment updateStatus(Long id, AppointmentStatus target) {
        Appointment appointment = findById(id);
        validateTransition(appointment.getStatus(), target);
        appointment.setStatus(target);
        return appointmentRepository.save(appointment);
    }

    /** Cancelar libera el horario porque la consulta de traslapes excluye CANCELADA. */
    @Transactional
    public Appointment cancel(Long id) {
        return updateStatus(id, AppointmentStatus.CANCELADA);
    }

    @Transactional
    public void delete(Long id) {
        appointmentRepository.delete(findById(id));
    }

    // ------------------------------------------------------------------
    // Reglas de negocio
    // ------------------------------------------------------------------

    private MechanicDto requireActiveMechanic(Long mechanicId) {
        MechanicDto mechanic;
        try {
            mechanic = mechanicClient.getMechanicById(mechanicId);
        } catch (ResourceNotFoundException ex) {
            throw new BusinessValidationException("El mecanico " + mechanicId + " no existe");
        }

        if (mechanic == null) {
            throw new BusinessValidationException("El mecanico " + mechanicId + " no existe");
        }
        if (!Boolean.TRUE.equals(mechanic.active())) {
            throw new BusinessValidationException("El mecanico " + mechanicId + " esta inactivo");
        }
        return mechanic;
    }

    private LocalTime calculateEnd(LocalTime start, Integer durationMinutes) {
        if (start == null || durationMinutes == null || durationMinutes <= 0) {
            throw new BusinessValidationException("La duracion de la cita debe ser mayor a cero");
        }
        try {
            return start.plusMinutes(durationMinutes);
        } catch (DateTimeException ex) {
            throw new BusinessValidationException("El horario calculado para la cita no es valido");
        }
    }

    private void validateWorkingHours(MechanicDto mechanic, LocalTime start, LocalTime end) {
        if (start.isBefore(mechanic.workStartTime()) || end.isAfter(mechanic.workEndTime())) {
            throw new BusinessValidationException(
                    "La cita debe iniciar y terminar dentro de la jornada del mecanico ("
                            + mechanic.workStartTime() + " - " + mechanic.workEndTime() + ")");
        }
    }

    /**
     * Deteccion de solapamiento: inicioA < finB && finA > inicioB.
     * Al ser estricta, una cita contigua (10:00 justo despues de 09:00-10:00)
     * NO se considera traslape (APT-03).
     */
    private void checkConflict(Long mechanicId, LocalDate date, LocalTime start, LocalTime end,
                               Long excludedAppointmentId) {
        List<Appointment> existing = appointmentRepository
                .findByMechanicIdAndAppointmentDateAndStatusNot(
                        mechanicId, date, AppointmentStatus.CANCELADA);

        for (Appointment current : existing) {
            if (excludedAppointmentId != null && excludedAppointmentId.equals(current.getId())) {
                continue;
            }
            if (current.getStartTime() == null || current.getDurationMinutes() == null) {
                continue;
            }

            LocalTime currentStart = current.getStartTime();
            LocalTime currentEnd;
            try {
                currentEnd = currentStart.plusMinutes(current.getDurationMinutes());
            } catch (DateTimeException ex) {
                continue;
            }

            if (start.isBefore(currentEnd) && end.isAfter(currentStart)) {
                throw new ScheduleConflictException(
                        "El horario " + start + "-" + end + " se traslapa con la cita "
                                + current.getId() + " (" + currentStart + "-" + currentEnd + ")");
            }
        }
    }

    private void validateTransition(AppointmentStatus current, AppointmentStatus target) {
        if (target == null) {
            throw new BusinessValidationException("El estado destino es obligatorio");
        }
        if (current == target) {
            return;
        }

        boolean allowed = switch (current) {
            case PROGRAMADA -> target == AppointmentStatus.EN_TALLER
                    || target == AppointmentStatus.CANCELADA;
            case EN_TALLER -> target == AppointmentStatus.COMPLETADA
                    || target == AppointmentStatus.CANCELADA;
            case COMPLETADA, CANCELADA -> false;
        };

        if (!allowed) {
            throw new BusinessValidationException(
                    "Transicion de estado no permitida: " + current + " -> " + target);
        }
    }

    private void applyRequest(Appointment appointment, AppointmentRequest request) {
        appointment.setMechanicId(request.mechanicId());
        appointment.setCustomerName(request.customerName());
        appointment.setVehiclePlate(request.vehiclePlate());
        appointment.setVehicleModel(request.vehicleModel());
        appointment.setServiceType(request.serviceType());
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setStartTime(request.startTime());
        appointment.setDurationMinutes(request.durationMinutes());
        appointment.setObservations(request.observations());
    }
}
