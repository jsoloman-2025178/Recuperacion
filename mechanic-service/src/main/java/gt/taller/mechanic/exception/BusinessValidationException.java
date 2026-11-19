package gt.taller.mechanic.exception;

/**
 * Regla de negocio violada (horario laboral invalido, rango fuera de jornada, etc.).
 * Se traduce a HTTP 400 en el manejo global de errores.
 */
public class BusinessValidationException extends RuntimeException {

    public BusinessValidationException(String message) {
        super(message);
    }
}
