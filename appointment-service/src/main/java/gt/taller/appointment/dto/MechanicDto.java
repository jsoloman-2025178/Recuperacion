package gt.taller.appointment.dto;

import java.time.LocalTime;

/**
 * Representacion local del mecanico devuelta por mechanic-service.
 * Mantiene el aislamiento entre bases de datos: aqui solo llega un DTO por HTTP.
 */
public record MechanicDto(
        Long id,
        String fullName,
        String specialty,
        String bayCode,
        LocalTime workStartTime,
        LocalTime workEndTime,
        Boolean active
) {
}
