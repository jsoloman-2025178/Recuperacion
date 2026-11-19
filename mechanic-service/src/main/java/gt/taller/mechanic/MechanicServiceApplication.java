package gt.taller.mechanic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio de mecanicos y disponibilidad.
 * Puerto por defecto: 8081. Base de datos: db_taller_mec_in5am.
 */
@SpringBootApplication
public class MechanicServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MechanicServiceApplication.class, args);
    }
}
