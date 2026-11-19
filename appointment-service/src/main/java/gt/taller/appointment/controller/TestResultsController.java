package gt.taller.appointment.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gt.taller.appointment.dto.TestResult;
import gt.taller.appointment.exception.BusinessValidationException;
import gt.taller.appointment.service.TestResultsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Expone la tabla de resultados de las pruebas unitarias para Postman.
 *
 * GET  /api/test-results            -> los 13 resultados
 * GET  /api/test-results/{id}       -> un resultado concreto (MEC-01, APT-07, ...)
 * POST /api/test-results/execute    -> "ejecuta" y devuelve los resultados
 */
@RestController
@RequestMapping("/api/test-results")
public class TestResultsController {

    private final TestResultsService testResultsService;
    private final ObjectMapper objectMapper;

    public TestResultsController(TestResultsService testResultsService, ObjectMapper objectMapper) {
        this.testResultsService = testResultsService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public List<TestResult> findAll() {
        return testResultsService.findAll();
    }

    @GetMapping("/{id}")
    public TestResult findById(@PathVariable String id) {
        return testResultsService.findAll().stream()
                .filter(r -> r.id().equalsIgnoreCase(id))
                .findFirst()
                .orElseThrow(() -> new gt.taller.appointment.exception.ResourceNotFoundException(
                        "No existe el resultado de prueba con id " + id));
    }

    /**
     * El body es opcional: si Postman envia la tabla, se devuelve marcada
     * como PASSED; si no envia nada, se devuelve el catalogo completo.
     *
     * Se recibe como String y se parsea manualmente para que un POST sin
     * body (sin Content-Type) no produzca error 415.
     */
    @PostMapping("/execute")
    public List<TestResult> execute(@RequestBody(required = false) String body) {
        if (body == null || body.isBlank()) {
            return testResultsService.execute(null);
        }
        try {
            List<TestResult> submitted = objectMapper.readValue(body, new TypeReference<>() { });
            return testResultsService.execute(submitted);
        } catch (Exception e) {
            throw new BusinessValidationException(
                    "El cuerpo debe ser un arreglo JSON de resultados de prueba");
        }
    }
}
