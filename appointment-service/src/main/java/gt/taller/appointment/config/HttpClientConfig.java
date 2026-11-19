package gt.taller.appointment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Cliente HTTP sincrono con Basic Auth para consultar mechanic-service.
 * RestClient.Builder es provisto automaticamente por spring-boot-starter-web.
 */
@Configuration
public class HttpClientConfig {

    @Bean
    public RestClient mechanicApiRestClient(RestClient.Builder builder, MechanicClientProperties properties) {
        String basic = Base64.getEncoder().encodeToString(
                (properties.username() + ":" + properties.password())
                        .getBytes(StandardCharsets.UTF_8));

        return builder
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .build();
    }
}
