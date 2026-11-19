-- ============================================================
-- Creacion de las bases de datos del proyecto.
-- Ejecutar con el usuario de laboratorio IN5AM.
-- IN5AM solo tiene permisos sobre esquemas que terminan en
-- _in5am, por eso los nombres llevan ese sufijo.
-- ============================================================

CREATE DATABASE IF NOT EXISTS db_taller_mec_in5am
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS db_taller_citas_in5am
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
