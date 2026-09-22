package de.entwicklertraining.openrouter4j.interns;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to read one intern's public lifecycle state and settings:
 * GET https://openrouter.ai/api/v1/interns/{internId}
 *
 * <p>Trap: every path answers 404 for keys outside the interns programme.
 */
public final class OpenRouterInternGetRequest
        extends OpenRouterRequest<OpenRouterInternResponse<OpenRouterInternGetRequest>> {

    private final OpenRouterClient client;
    private final String internId;

    private OpenRouterInternGetRequest(Builder builder) {
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
        return "/interns/" + URLEncoder.encode(internId, StandardCharsets.UTF_8);
    }

    @Override
    public String getHttpMethod() {
        return "GET";
    }

    /**
     * GET requests carry no body.
     *
     * @return always {@code null}
     */
    @Override
    public String getBody() {
        return null;
    }

    @Override
    public OpenRouterInternResponse<OpenRouterInternGetRequest> createResponse(
            String responseBody) {
        return new OpenRouterInternResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterInternGetRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterInternGetRequest> {

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
        public OpenRouterInternGetRequest build() {
            return new OpenRouterInternGetRequest(this);
        }

        @Override
        public OpenRouterInternResponse<OpenRouterInternGetRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterInternResponse<OpenRouterInternGetRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterInternResponse<OpenRouterInternGetRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
