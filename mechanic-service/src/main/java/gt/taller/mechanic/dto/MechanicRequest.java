package gt.taller.mechanic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

/**
 * DTO de entrada para registrar o actualizar un mecanico.
 */
public record MechanicRequest(
        @NotBlank(message = "El nombre completo es obligatorio")
        String fullName,

        @NotBlank(message = "La especialidad es obligatoria")
        String specialty,

        @NotBlank(message = "El codigo de bahia es obligatorio")
        String bayCode,

        @NotNull(message = "La hora de inicio laboral es obligatoria")
        LocalTime workStartTime,

        @NotNull(message = "La hora de fin laboral es obligatoria")
        LocalTime workEndTime
) {
}
