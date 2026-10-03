package com.example.multas.infrastructure.pago;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.multas.domain.ResultadoPago;
import com.example.multas.domain.port.PasarelaPagoPort;
import com.example.multas.model.Multa;

@Component
@ConditionalOnProperty(
        prefix = "app.pagos",
        name = "proveedor",
        havingValue = "wompi"
)
public class WompiAdapter implements PasarelaPagoPort {

    private final RestTemplate restTemplate;

    private final String urlPasarela;

    public WompiAdapter(
            RestTemplate restTemplate,
            @Value(
                    "${app.pagos.wompi.url:" +
                    "http://localhost:9002/wompi/transactions}"
            )
            String urlPasarela
    ) {
        this.restTemplate = restTemplate;
        this.urlPasarela = urlPasarela;
    }

    @Override
    public ResultadoPago procesar(Multa multa) {

        try {

            long montoEnCentavos =
                    multa.getMonto()
                            .movePointRight(2)
                            .longValueExact();

            WompiRequest request =
                    new WompiRequest(
                            "multa-" + multa.getId(),
                            montoEnCentavos
                    );

            WompiResponse response =
                    restTemplate.postForObject(
                            urlPasarela,
                            request,
                            WompiResponse.class
                    );

            boolean exitoso =
                    response != null
                    && "APPROVED".equalsIgnoreCase(
                            response.status()
                    );

            String referencia =
                    response != null
                            ? response.reference()
                            : null;

            String mensaje =
                    exitoso
                            ? "Pago aprobado por Wompi"
                            : "Pago rechazado por Wompi";

            return new ResultadoPago(
                    "WOMPI",
                    exitoso,
                    referencia,
                    mensaje
            );

        } catch (RestClientException ex) {

            return new ResultadoPago(
                    "WOMPI",
                    false,
                    null,
                    "Wompi no disponible: " +
                    ex.getMessage()
            );
        }
    }

    private record WompiRequest(
            String reference,
            long amountInCents
    ) {
    }

    private record WompiResponse(
            String reference,
            String status
    ) {
    }
}
