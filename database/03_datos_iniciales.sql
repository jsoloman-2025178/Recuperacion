-- ============================================================
-- Datos de prueba para probar la API con MySQL levantado
-- (los tests unitarios NO necesitan estos datos ni MySQL)
-- ============================================================

USE db_taller_mec_in5am;

INSERT INTO mechanics (id, full_name, specialty, bay_code, work_start_time, work_end_time, active)
VALUES (1, 'Carlos Méndez', 'FRENOS', 'BAHIA-01', '08:00:00', '17:00:00', TRUE),
       (2, 'Luis Pérez', 'MOTOR', 'BAHIA-02', '08:00:00', '17:00:00', TRUE),
       (3, 'Ana Morales', 'ELECTRICIDAD', 'BAHIA-03', '09:00:00', '18:00:00', FALSE);

ALTER TABLE mechanics AUTO_INCREMENT = 10;

USE db_taller_citas_in5am;

INSERT INTO appointments (id, mechanic_id, customer_name, vehicle_plate, vehicle_model,
                          service_type, appointment_date, start_time, duration_minutes,
                          status, observations, created_at)
VALUES (1, 1, 'Ana López', 'P-458JKL', 'Toyota Corolla 2019', 'Revisión de frenos',
        '2026-11-25', '09:00:00', 60, 'PROGRAMADA', 'Ruido al frenar', NOW());

ALTER TABLE appointments AUTO_INCREMENT = 10;
