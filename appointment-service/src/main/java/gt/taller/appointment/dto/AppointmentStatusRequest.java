package gt.taller.appointment.dto;

import gt.taller.appointment.entity.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo del endpoint de cambio de estado.
 */
public record AppointmentStatusRequest(
        @NotNull(message = "El estado destino es obligatorio")
        AppointmentStatus status
) {
}
