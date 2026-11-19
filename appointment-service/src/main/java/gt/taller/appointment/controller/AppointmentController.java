package gt.taller.appointment.controller;

import gt.taller.appointment.dto.AppointmentRequest;
import gt.taller.appointment.dto.AppointmentStatusRequest;
import gt.taller.appointment.entity.Appointment;
import gt.taller.appointment.entity.AppointmentStatus;
import gt.taller.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * API REST de la agenda de citas.
 * Requiere rol ADMIN u OPERADOR. No expone interfaz grafica.
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Appointment create(@Valid @RequestBody AppointmentRequest request) {
        return appointmentService.create(request);
    }

    @GetMapping
    public List<Appointment> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long mechanicId,
            @RequestParam(required = false) AppointmentStatus status) {
        return appointmentService.findAll(date, mechanicId, status);
    }

    @GetMapping("/{id}")
    public Appointment getById(@PathVariable Long id) {
        return appointmentService.findById(id);
    }

    @PutMapping("/{id}")
    public Appointment update(@PathVariable Long id, @Valid @RequestBody AppointmentRequest request) {
        return appointmentService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public Appointment updateStatus(@PathVariable Long id,
                                    @Valid @RequestBody AppointmentStatusRequest request) {
        return appointmentService.updateStatus(id, request.status());
    }

    @PostMapping("/{id}/cancel")
    public Appointment cancel(@PathVariable Long id) {
        return appointmentService.cancel(id);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        appointmentService.delete(id);
    }
}
