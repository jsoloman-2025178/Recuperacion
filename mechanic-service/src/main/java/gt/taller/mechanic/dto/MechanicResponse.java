package gt.taller.mechanic.dto;

import gt.taller.mechanic.entity.Mechanic;

import java.time.LocalTime;

/**
 * DTO de salida para el catalogo de mecanicos.
 * Es la misma forma que consume appointment-service (MechanicDto).
 */
public record MechanicResponse(
        Long id,
        String fullName,
        String specialty,
        String bayCode,
        LocalTime workStartTime,
        LocalTime workEndTime,
        Boolean active
) {
    public static MechanicResponse from(Mechanic mechanic) {
        return new MechanicResponse(
                mechanic.getId(),
                mechanic.getFullName(),
                mechanic.getSpecialty(),
                mechanic.getBayCode(),
                mechanic.getWorkStartTime(),
                mechanic.getWorkEndTime(),
                mechanic.getActive()
        );
    }
}
