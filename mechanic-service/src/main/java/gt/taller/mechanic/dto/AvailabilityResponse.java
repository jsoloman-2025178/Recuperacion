package gt.taller.mechanic.dto;

import java.time.LocalTime;

/**
 * Resultado de evaluar si un rango horario cabe en la jornada del mecanico.
 */
public record AvailabilityResponse(
        Long mechanicId,
        LocalTime startTime,
        int durationMinutes,
        boolean available
) {
}
