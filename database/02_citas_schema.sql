-- ============================================================
-- Microservicio: appointment-service
-- Base de datos separada (aislamiento de datos por microservicio)
-- mechanic_id es una referencia LOGICA al mechanic-service:
-- NO se crea llave foranea fisica entre esquemas distintos.
-- ============================================================
CREATE DATABASE IF NOT EXISTS db_taller_citas_in5am
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE db_taller_citas_in5am;

CREATE TABLE IF NOT EXISTS appointments (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    mechanic_id      BIGINT       NOT NULL,
    customer_name    VARCHAR(120) NOT NULL,
    vehicle_plate    VARCHAR(20)  NOT NULL,
    vehicle_model    VARCHAR(120) NOT NULL,
    service_type     VARCHAR(120) NOT NULL,
    appointment_date DATE         NOT NULL,
    start_time       TIME         NOT NULL,
    duration_minutes INT          NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    observations     VARCHAR(500) NULL,
    created_at       DATETIME     NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_apt_mechanic_date (mechanic_id, appointment_date),
    INDEX idx_apt_date (appointment_date),
    INDEX idx_apt_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
