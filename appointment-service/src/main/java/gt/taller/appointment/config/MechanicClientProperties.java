package gt.taller.appointment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades services.mechanic.* del application.yml.
 */
@ConfigurationProperties(prefix = "services.mechanic")
public record MechanicClientProperties(String baseUrl, String username, String password) {

    public MechanicClientProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:8081";
        }
        if (username == null || username.isBlank()) {
            username = "citas-service";
        }
        if (password == null || password.isBlank()) {
            password = "citas123";
        }
    }
}
