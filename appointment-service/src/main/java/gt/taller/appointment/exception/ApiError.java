package gt.taller.appointment.exception;

import java.time.Instant;
import java.util.Map;

/**
 * Cuerpo estandar de error de la API.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors,
        String path
) {
}
