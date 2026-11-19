package gt.taller.appointment.dto;

/**
 * Fila de la tabla de resultados de las pruebas unitarias (MEC-01..APT-07).
 * Se expone como JSON para poder consultarla desde Postman.
 */
public record TestResult(
        String id,
        String prueba,
        String microservicio,
        String escenarioEvaluado,
        String resultadoEsperado,
        String estado
) {
}
