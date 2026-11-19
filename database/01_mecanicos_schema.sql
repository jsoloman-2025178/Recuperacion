-- ============================================================
-- Microservicio: mechanic-service
-- Base de datos separada (aislamiento de datos por microservicio)
-- ============================================================
CREATE DATABASE IF NOT EXISTS db_taller_mec_in5am
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE db_taller_mec_in5am;

CREATE TABLE IF NOT EXISTS mechanics (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    full_name       VARCHAR(120) NOT NULL,
    specialty       VARCHAR(80)  NOT NULL,
    bay_code        VARCHAR(30)  NOT NULL,
    work_start_time TIME         NOT NULL,
    work_end_time   TIME         NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
