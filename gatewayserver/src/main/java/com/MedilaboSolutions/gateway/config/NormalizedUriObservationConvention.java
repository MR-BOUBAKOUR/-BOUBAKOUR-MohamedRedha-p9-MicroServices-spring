package com.MedilaboSolutions.gateway.config;

import io.micrometer.common.KeyValues;
import org.springframework.http.server.reactive.observation.DefaultServerRequestObservationConvention;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;
import org.springframework.stereotype.Component;


// Custom observation convention to normalize URI paths in metrics.
// This helps group similar endpoints (e.g. /v1/patients/123) under a generic pattern
// (/v1/patients/{id}) to avoid high cardinality in the observability stack
@Component
public class NormalizedUriObservationConvention extends DefaultServerRequestObservationConvention {

    @Override
    public KeyValues getLowCardinalityKeyValues(ServerRequestObservationContext context) {
        KeyValues keyValues = super.getLowCardinalityKeyValues(context);

        // Get the original URI
        String uri = context.getCarrier().getPath().value();
        // Normalizing it -> /{id}
        String normalizedUri = normalizeUri(uri);
        // using the "normalized" one for the metrics
        return keyValues.and("uri", normalizedUri);
    }

    private String normalizeUri(String uri) {
        return uri
                .replaceAll("/v1/patients/\\d+", "/v1/patients/{id}")
                .replaceAll("/v1/notes/\\d+", "/v1/notes/{id}")
                .replaceAll("/v1/assessments/\\d+", "/v1/assessments/{id}");
    }
}