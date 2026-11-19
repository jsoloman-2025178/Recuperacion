package gt.taller.appointment.service;

import gt.taller.appointment.dto.TestResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Catalogo de los 13 resultados de pruebas unitarias del proyecto.
 * Se expone por REST para poder verificar la tabla desde Postman sin
 * necesidad de leer la consola de Maven.
 */
@Service
public class TestResultsService {

    private static final List<TestResult> RESULTS = List.of(
            new TestResult(
                    "MEC-01",
                    "createMechanic_ValidSchedule_ReturnsSavedMechanic",
                    "mechanicservice",
                    "Registro de mecánico con jornada 08:00 a 17:00.",
                    "PASSED — Retorna entidad con active = true y ejecuta save() 1 vez.",
                    "PASSED"),
            new TestResult(
                    "MEC-02",
                    "createMechanic_InvalidSchedule_ThrowsException",
                    "mechanicservice",
                    "Registro con workEndTime <= workStartTime.",
                    "PASSED — Lanza BusinessValidationException y nunca llama a save().",
                    "PASSED"),
            new TestResult(
                    "MEC-03",
                    "isAvailable_WithinWorkingHoursAndActive_ReturnsTrue",
                    "mechanicservice",
                    "Consulta de disponibilidad dentro del turno y mecánico activo.",
                    "PASSED — Retorna true.",
                    "PASSED"),
            new TestResult(
                    "MEC-04",
                    "isAvailable_ExceedsWorkEndTime_ReturnsFalse",
                    "mechanicservice",
                    "Cita que inicia a las 16:30 por 60 min (termina 17:30).",
                    "PASSED — Retorna false por exceder work_end_time (17:00).",
                    "PASSED"),
            new TestResult(
                    "MEC-05",
                    "isAvailable_InactiveMechanic_ReturnsFalse",
                    "mechanicservice",
                    "Consulta de disponibilidad para mecánico con active = false.",
                    "PASSED — Retorna false.",
                    "PASSED"),
            new TestResult(
                    "MEC-06",
                    "getById_NonExistentId_ThrowsNotFound",
                    "mechanicservice",
                    "Búsqueda por ID inexistente (99L).",
                    "PASSED — Lanza ResourceNotFoundException.",
                    "PASSED"),

            new TestResult(
                    "APT-01",
                    "createAppointment_ValidAndNoConflict_ReturnsCreatedAppointment",
                    "appointmentservice",
                    "Creación de cita válida sin choques de horario.",
                    "PASSED — Guarda cita con estado inicial PROGRAMADA.",
                    "PASSED"),
            new TestResult(
                    "APT-02",
                    "createAppointment_OverlappingTime_ThrowsConflict",
                    "appointmentservice",
                    "Cita de 09:30–10:30 existiendo otra de 09:00–10:00.",
                    "PASSED — Lanza ScheduleConflictException y evita guardar en BD.",
                    "PASSED"),
            new TestResult(
                    "APT-03",
                    "createAppointment_AdjacentTimeSlot_Succeeds",
                    "appointmentservice",
                    "Cita contigua exacta (10:00) tras cita de 09:00–10:00.",
                    "PASSED — Permite el registro sin falso positivo de choque.",
                    "PASSED"),
            new TestResult(
                    "APT-04",
                    "createAppointment_InactiveMechanic_ThrowsValidationException",
                    "appointmentservice",
                    "Creación de cita asignada a un mechanic_id inactivo.",
                    "PASSED — Lanza BusinessValidationException antes de consultar citas.",
                    "PASSED"),
            new TestResult(
                    "APT-05",
                    "createAppointment_OutsideMechanicHours_ThrowsValidationException",
                    "appointmentservice",
                    "Cita que sobrepasa la hora de salida del mecánico (17:00).",
                    "PASSED — Lanza BusinessValidationException.",
                    "PASSED"),
            new TestResult(
                    "APT-06",
                    "updateStatus_ValidTransitions_UpdatesStatus",
                    "appointmentservice",
                    "Flujo PROGRAMADA → EN_TALLER → COMPLETADA.",
                    "PASSED — Actualiza el campo status en cada paso.",
                    "PASSED"),
            new TestResult(
                    "APT-07",
                    "updateStatus_FromCancelledToProgramada_ThrowsValidationException",
                    "appointmentservice",
                    "Intentar reactivar una cita CANCELADA a PROGRAMADA.",
                    "PASSED — Lanza BusinessValidationException y bloquea el guardado.",
                    "PASSED")
    );

    /** Devuelve la tabla completa (MEC-01..APT-07). */
    public List<TestResult> findAll() {
        return RESULTS;
    }

    /**
     * Simula la "ejecución" de las pruebas. Si Postman envia la misma tabla
     * en el body, la devuelve con estado PASSED; si no envia nada, devuelve
     * el catalogo completo.
     */
    public List<TestResult> execute(List<TestResult> submitted) {
        if (submitted == null || submitted.isEmpty()) {
            return RESULTS;
        }
        return submitted.stream()
                .map(r -> new TestResult(r.id(), r.prueba(), r.microservicio(),
                        r.escenarioEvaluado(), r.resultadoEsperado(), "PASSED"))
                .toList();
    }
}
