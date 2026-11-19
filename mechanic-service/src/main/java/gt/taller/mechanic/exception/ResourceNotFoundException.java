package gt.taller.mechanic.exception;

/**
 * No existe el recurso solicitado. Se traduce a HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
