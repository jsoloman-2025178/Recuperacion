package gt.taller.appointment.exception;

/**
 * No existe la cita solicitada. Se traduce a HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
