package gt.taller.appointment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Punto de entrada del microservicio de turnos y citas.
 * Puerto por defecto: 8082. Base de datos: db_taller_citas_in5am.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class AppointmentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppointmentServiceApplication.class, args);
    }
}
