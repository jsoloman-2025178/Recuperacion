package gt.taller.mechanic.service;

import gt.taller.mechanic.dto.MechanicRequest;
import gt.taller.mechanic.entity.Mechanic;
import gt.taller.mechanic.exception.BusinessValidationException;
import gt.taller.mechanic.exception.ResourceNotFoundException;
import gt.taller.mechanic.repository.MechanicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.util.List;

/**
 * Reglas de negocio del catalogo de mecanicos.
 * Las pruebas unitarias MEC-01 a MEC-06 ejercitan exactamente esta clase.
 */
@Service
@Transactional(readOnly = true)
public class MechanicService {

    private final MechanicRepository mechanicRepository;

    public MechanicService(MechanicRepository mechanicRepository) {
        this.mechanicRepository = mechanicRepository;
    }

    /**
     * MEC-01 / MEC-02: registrar mecanico con jornada valida y active = true.
     */
    @Transactional
    public Mechanic create(MechanicRequest request) {
        validateSchedule(request.workStartTime(), request.workEndTime());

        Mechanic mechanic = new Mechanic();
        mechanic.setFullName(request.fullName());
        mechanic.setSpecialty(request.specialty());
        mechanic.setBayCode(request.bayCode());
        mechanic.setWorkStartTime(request.workStartTime());
        mechanic.setWorkEndTime(request.workEndTime());
        mechanic.setActive(true);

        return mechanicRepository.save(mechanic);
    }

    public List<Mechanic> findAll(Boolean active) {
        if (active == null) {
            return mechanicRepository.findAll();
        }
        return mechanicRepository.findByActive(active);
    }

    /**
     * MEC-06: lanzar ResourceNotFoundException cuando el id no existe.
     */
    public Mechanic getById(Long id) {
        return mechanicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un mecanico con id " + id));
    }

    @Transactional
    public Mechanic update(Long id, MechanicRequest request) {
        validateSchedule(request.workStartTime(), request.workEndTime());

        Mechanic mechanic = getById(id);
        mechanic.setFullName(request.fullName());
        mechanic.setSpecialty(request.specialty());
        mechanic.setBayCode(request.bayCode());
        mechanic.setWorkStartTime(request.workStartTime());
        mechanic.setWorkEndTime(request.workEndTime());

        return mechanicRepository.save(mechanic);
    }

    @Transactional
    public Mechanic setActive(Long id, boolean active) {
        Mechanic mechanic = getById(id);
        mechanic.setActive(active);
        return mechanicRepository.save(mechanic);
    }

    @Transactional
    public void delete(Long id) {
        mechanicRepository.delete(getById(id));
    }

    /**
     * MEC-03 / MEC-04 / MEC-05:
     * el mecanico debe existir, estar activo y el rango [inicio, inicio+duracion)
     * debe caber dentro de su jornada laboral.
     */
    public boolean isAvailable(Long id, LocalTime start, int durationMinutes) {
        Mechanic mechanic = getById(id);

        if (!Boolean.TRUE.equals(mechanic.getActive())) {
            return false;
        }
        if (start == null || durationMinutes <= 0) {
            return false;
        }
        try {
            LocalTime end = start.plusMinutes(durationMinutes);
            return !start.isBefore(mechanic.getWorkStartTime())
                    && !end.isAfter(mechanic.getWorkEndTime());
        } catch (DateTimeException ex) {
            // La suma se pasa de medianoche: no cabe en la jornada.
            return false;
        }
    }

    private void validateSchedule(LocalTime start, LocalTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new BusinessValidationException(
                    "La hora de fin de la jornada debe ser posterior a la hora de inicio");
        }
    }
}
