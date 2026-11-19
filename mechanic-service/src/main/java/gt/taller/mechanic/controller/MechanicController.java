package gt.taller.mechanic.controller;

import gt.taller.mechanic.dto.AvailabilityResponse;
import gt.taller.mechanic.dto.MechanicRequest;
import gt.taller.mechanic.dto.MechanicResponse;
import gt.taller.mechanic.entity.Mechanic;
import gt.taller.mechanic.service.MechanicService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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

import java.time.LocalTime;
import java.util.List;

/**
 * API REST del microservicio de mecanicos.
 * GET  -> ADMIN, OPERADOR y SERVICE (cuenta de appointment-service)
 * POST/PUT/PATCH/DELETE -> solo ADMIN
 */
@RestController
@RequestMapping("/api/mechanics")
@Validated
public class MechanicController {

    private final MechanicService mechanicService;

    public MechanicController(MechanicService mechanicService) {
        this.mechanicService = mechanicService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MechanicResponse create(@Valid @RequestBody MechanicRequest request) {
        return MechanicResponse.from(mechanicService.create(request));
    }

    @GetMapping
    public List<MechanicResponse> list(@RequestParam(required = false) Boolean active) {
        return mechanicService.findAll(active).stream()
                .map(MechanicResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public MechanicResponse getById(@PathVariable Long id) {
        return MechanicResponse.from(mechanicService.getById(id));
    }

    /**
     * Mismo calculo que exercitan MEC-03, MEC-04 y MEC-05.
     */
    @GetMapping("/{id}/availability")
    public AvailabilityResponse availability(@PathVariable Long id,
                                             @RequestParam LocalTime start,
                                             @RequestParam @Positive int durationMinutes) {
        return new AvailabilityResponse(
                id, start, durationMinutes,
                mechanicService.isAvailable(id, start, durationMinutes)
        );
    }

    @PutMapping("/{id}")
    public MechanicResponse update(@PathVariable Long id, @Valid @RequestBody MechanicRequest request) {
        return MechanicResponse.from(mechanicService.update(id, request));
    }

    @PatchMapping("/{id}/active")
    public MechanicResponse setActive(@PathVariable Long id, @RequestParam boolean active) {
        return MechanicResponse.from(mechanicService.setActive(id, active));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        mechanicService.delete(id);
    }
}
