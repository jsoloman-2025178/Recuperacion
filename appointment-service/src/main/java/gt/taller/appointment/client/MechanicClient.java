package gt.taller.appointment.client;

import gt.taller.appointment.dto.MechanicDto;

/**
 * Contrato del cliente HTTP sincrono hacia mechanic-service.
 * Se mocks en APT-01 a APT-05: las pruebas no levantan servidor HTTP.
 */
public interface MechanicClient {

    /**
     * @return datos del mecanico, o null si el servicio responde 404.
     */
    MechanicDto getMechanicById(Long mechanicId);
}
