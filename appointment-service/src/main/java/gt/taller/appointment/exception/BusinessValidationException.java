package gt.taller.appointment.exception;

/**
 * Regla de negocio violada: mecanico inactivo/inexistente, cita fuera de jornada
 * o transicion de estado no permitida. Se traduce a HTTP 400.
 */
public class BusinessValidationException extends RuntimeException {

    public BusinessValidationException(String message) {
        super(message);
    }
}
