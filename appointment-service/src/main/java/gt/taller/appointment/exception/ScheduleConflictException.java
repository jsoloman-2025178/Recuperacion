package gt.taller.appointment.exception;

/**
 * Existe otra cita activa del mismo mecanico cuyo rango se traslapa.
 * Se traduce a HTTP 409.
 */
public class ScheduleConflictException extends RuntimeException {

    public ScheduleConflictException(String message) {
        super(message);
    }
}
