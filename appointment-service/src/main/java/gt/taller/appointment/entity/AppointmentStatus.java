package gt.taller.appointment.entity;

/**
 * Ciclo de vida controlado de la cita.
 *
 * PROGRAMADA -> EN_TALLER -> COMPLETADA
 *      \            \
 *       ------> CANCELADA   (libera el cupo horario)
 */
public enum AppointmentStatus {
    PROGRAMADA,
    EN_TALLER,
    COMPLETADA,
    CANCELADA
}
