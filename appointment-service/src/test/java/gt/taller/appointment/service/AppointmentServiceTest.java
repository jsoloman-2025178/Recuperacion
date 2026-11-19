package gt.taller.appointment.service;

import gt.taller.appointment.client.MechanicClient;
import gt.taller.appointment.dto.AppointmentRequest;
import gt.taller.appointment.dto.MechanicDto;
import gt.taller.appointment.entity.Appointment;
import gt.taller.appointment.entity.AppointmentStatus;
import gt.taller.appointment.exception.BusinessValidationException;
import gt.taller.appointment.exception.ScheduleConflictException;
import gt.taller.appointment.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private MechanicClient mechanicClient;

    @InjectMocks
    private AppointmentService appointmentService;

    private MechanicDto activeMechanicDto;
    private LocalDate targetDate;

    @BeforeEach
    void setUp() {
        targetDate = LocalDate.of(2026, 11, 25);
        activeMechanicDto = new MechanicDto(
                1L, "Carlos Méndez", "FRENOS", "BAHIA-01",
                LocalTime.of(8, 0), LocalTime.of(17, 0), true
        );
    }

    @Test
    @DisplayName("APT-01: Debe agendar cita exitosamente cuando el mecánico está activo y no hay traslape")
    void createAppointment_ValidAndNoConflict_ReturnsCreatedAppointment() {
        AppointmentRequest request = new AppointmentRequest(
                1L, "Ana López", "P-458JKL", "Toyota Corolla 2019",
                "Revisión de frenos", targetDate, LocalTime.of(9, 0), 60, "Ruido al frenar"
        );

        when(mechanicClient.getMechanicById(1L)).thenReturn(activeMechanicDto);
        when(appointmentRepository.findByMechanicIdAndAppointmentDateAndStatusNot(
                1L, targetDate, AppointmentStatus.CANCELADA
        )).thenReturn(List.of());

        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment saved = inv.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        Appointment result = appointmentService.create(request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(AppointmentStatus.PROGRAMADA, result.getStatus());
        assertEquals("P-458JKL", result.getVehiclePlate());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    @DisplayName("APT-02: Debe lanzar ScheduleConflictException si el nuevo horario choca con una cita existente")
    void createAppointment_OverlappingTime_ThrowsConflict() {
        // Cita existente: 09:00 a 10:00
        Appointment existing = new Appointment();
        existing.setId(5L);
        existing.setMechanicId(1L);
        existing.setAppointmentDate(targetDate);
        existing.setStartTime(LocalTime.of(9, 0));
        existing.setDurationMinutes(60);
        existing.setStatus(AppointmentStatus.PROGRAMADA);

        // Nueva cita solicitada: 09:30 a 10:30 (se cruza)
        AppointmentRequest overlappingRequest = new AppointmentRequest(
                1L, "Mario Ruiz", "P-912ABC", "Mazda 3 2020",
                "Cambio de aceite", targetDate, LocalTime.of(9, 30), 60, null
        );

        when(mechanicClient.getMechanicById(1L)).thenReturn(activeMechanicDto);
        when(appointmentRepository.findByMechanicIdAndAppointmentDateAndStatusNot(
                1L, targetDate, AppointmentStatus.CANCELADA
        )).thenReturn(List.of(existing));

        assertThrows(ScheduleConflictException.class, () -> appointmentService.create(overlappingRequest));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("APT-03: Debe permitir agendar justo cuando termina la cita anterior (frontera exacta)")
    void createAppointment_AdjacentTimeSlot_Succeeds() {
        // Cita existente: 09:00 a 10:00
        Appointment existing = new Appointment();
        existing.setStartTime(LocalTime.of(9, 0));
        existing.setDurationMinutes(60);
        existing.setStatus(AppointmentStatus.PROGRAMADA);

        // Nueva cita inicia exactamente a las 10:00 (no choca)
        AppointmentRequest adjacentRequest = new AppointmentRequest(
                1L, "Sofía Castro", "P-331XYZ", "Honda Civic 2021",
                "Escáner OBD2", targetDate, LocalTime.of(10, 0), 45, null
        );

        when(mechanicClient.getMechanicById(1L)).thenReturn(activeMechanicDto);
        when(appointmentRepository.findByMechanicIdAndAppointmentDateAndStatusNot(
                1L, targetDate, AppointmentStatus.CANCELADA
        )).thenReturn(List.of(existing));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = appointmentService.create(adjacentRequest);

        assertNotNull(result);
        assertEquals(LocalTime.of(10, 0), result.getStartTime());
    }

    @Test
    @DisplayName("APT-04: Debe rechazar la cita si el mecánico está inactivo")
    void createAppointment_InactiveMechanic_ThrowsValidationException() {
        MechanicDto inactiveMechanic = new MechanicDto(
                1L, "Carlos Méndez", "FRENOS", "BAHIA-01",
                LocalTime.of(8, 0), LocalTime.of(17, 0), false
        );

        AppointmentRequest request = new AppointmentRequest(
                1L, "Ana López", "P-458JKL", "Toyota Corolla 2019",
                "Frenos", targetDate, LocalTime.of(11, 0), 60, null
        );

        when(mechanicClient.getMechanicById(1L)).thenReturn(inactiveMechanic);

        assertThrows(BusinessValidationException.class, () -> appointmentService.create(request));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("APT-05: Debe rechazar la cita si excede la hora de salida del mecánico")
    void createAppointment_OutsideMechanicHours_ThrowsValidationException() {
        AppointmentRequest lateRequest = new AppointmentRequest(
                1L, "Jorge Díaz", "P-777MNO", "Hyundai Tucson 2022",
                "Alineación", targetDate, LocalTime.of(16, 30), 60, null
        );

        when(mechanicClient.getMechanicById(1L)).thenReturn(activeMechanicDto);

        assertThrows(BusinessValidationException.class, () -> appointmentService.create(lateRequest));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("APT-06: Debe permitir transición válida PROGRAMADA -> EN_TALLER -> COMPLETADA")
    void updateStatus_ValidTransitions_UpdatesStatus() {
        Appointment appointment = new Appointment();
        appointment.setId(10L);
        appointment.setStatus(AppointmentStatus.PROGRAMADA);

        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment inWorkshop = appointmentService.updateStatus(10L, AppointmentStatus.EN_TALLER);
        assertEquals(AppointmentStatus.EN_TALLER, inWorkshop.getStatus());

        Appointment completed = appointmentService.updateStatus(10L, AppointmentStatus.COMPLETADA);
        assertEquals(AppointmentStatus.COMPLETADA, completed.getStatus());
    }

    @Test
    @DisplayName("APT-07: Debe bloquear cambio de estado si la cita ya está CANCELADA")
    void updateStatus_FromCancelledToProgramada_ThrowsValidationException() {
        Appointment cancelled = new Appointment();
        cancelled.setId(10L);
        cancelled.setStatus(AppointmentStatus.CANCELADA);

        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(cancelled));

        assertThrows(BusinessValidationException.class,
                () -> appointmentService.updateStatus(10L, AppointmentStatus.PROGRAMADA));
        verify(appointmentRepository, never()).save(any());
    }
}