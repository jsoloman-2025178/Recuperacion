package gt.taller.appointment.client;

import gt.taller.appointment.dto.MechanicDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * Implementacion real con RestClient (Spring Boot 3.x).
 * La autenticacion Basica se configura en HttpClientConfig.
 */
@Component
public class MechanicRestClient implements MechanicClient {

    private final RestClient restClient;

    public MechanicRestClient(@Qualifier("mechanicApiRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public MechanicDto getMechanicById(Long mechanicId) {
        try {
            return restClient.get()
                    .uri("/api/mechanics/{id}", mechanicId)
                    .retrieve()
                    .body(MechanicDto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            // El servicio de mecanicos responde 404: se traduce a error de negocio en AppointmentService.
            return null;
        }
    }
}
