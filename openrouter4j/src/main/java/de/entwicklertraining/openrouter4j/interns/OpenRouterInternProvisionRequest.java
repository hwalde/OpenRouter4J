package de.entwicklertraining.openrouter4j.interns;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to provision an intern - the first boot, or the resume after a
 * suspension:
 * POST https://openrouter.ai/api/v1/interns/{internId}/provision
 *
 * <p>Answers 202 with {@code provisioning}. A failed attempt is reported in
 * the intern's {@code status} {@code failed} with
 * {@code last_failure_message}.
 */
public final class OpenRouterInternProvisionRequest
        extends OpenRouterRequest<OpenRouterInternLifecycleResponse<OpenRouterInternProvisionRequest>> {

    private final OpenRouterClient client;
    private final String internId;

    private OpenRouterInternProvisionRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.internId = builder.internId;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String internId() {
        return URLEncoder.encode(internId, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/interns/"
                + URLEncoder.encode(internId, StandardCharsets.UTF_8)
                + "/provision";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * This action carries no body.
     *
     * @return always {@code null}
     */
    @Override
    public String getBody() {
        return null;
    }

    @Override
    public OpenRouterInternLifecycleResponse<OpenRouterInternProvisionRequest> createResponse(
            String responseBody) {
        return new OpenRouterInternLifecycleResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterInternProvisionRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterInternProvisionRequest> {

        private final OpenRouterClient client;
        private final String internId;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param internId the id (UUID) of the intern
         */
        public Builder(OpenRouterClient client, String internId) {
            super(client);
            this.client = client;
            this.internId = internId;
        }

        @Override
        public OpenRouterInternProvisionRequest build() {
            return new OpenRouterInternProvisionRequest(this);
        }

        @Override
        public OpenRouterInternLifecycleResponse<OpenRouterInternProvisionRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterInternLifecycleResponse<OpenRouterInternProvisionRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterInternLifecycleResponse<OpenRouterInternProvisionRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
