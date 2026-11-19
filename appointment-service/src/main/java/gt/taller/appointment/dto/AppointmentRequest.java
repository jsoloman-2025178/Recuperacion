package gt.taller.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO de entrada para crear o actualizar una cita.
 * El orden de los componentes coincide con el constructor usado por APT-01 a APT-05.
 */
public record AppointmentRequest(
        @NotNull(message = "El id del mecanico es obligatorio")
        Long mechanicId,

        @NotBlank(message = "El nombre del cliente es obligatorio")
        String customerName,

        @NotBlank(message = "La placa del vehiculo es obligatoria")
        String vehiclePlate,

        @NotBlank(message = "El modelo del vehiculo es obligatorio")
        String vehicleModel,

        @NotBlank(message = "El tipo de servicio es obligatorio")
        String serviceType,

        @NotNull(message = "La fecha de la cita es obligatoria")
        LocalDate appointmentDate,

        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime startTime,

        @NotNull(message = "La duracion es obligatoria")
        @Positive(message = "La duracion debe ser mayor a cero")
        Integer durationMinutes,

        String observations
) {
}
