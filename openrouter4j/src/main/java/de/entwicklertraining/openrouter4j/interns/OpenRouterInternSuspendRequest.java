package de.entwicklertraining.openrouter4j.interns;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to suspend an intern - stop the runtime, keep the disk and the
 * configuration:
 * POST https://openrouter.ai/api/v1/interns/{internId}/suspend
 *
 * <p>Answers 200 with {@code suspended}. Resume with the provision endpoint.
 * Trap: closing an active chat stream cancels the run; suspending while a
 * turn is running has the same effect on it.
 */
public final class OpenRouterInternSuspendRequest
        extends OpenRouterRequest<OpenRouterInternLifecycleResponse<OpenRouterInternSuspendRequest>> {

    private final OpenRouterClient client;
    private final String internId;

    private OpenRouterInternSuspendRequest(Builder builder) {
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
                + "/suspend";
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
    public OpenRouterInternLifecycleResponse<OpenRouterInternSuspendRequest> createResponse(
            String responseBody) {
        return new OpenRouterInternLifecycleResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterInternSuspendRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterInternSuspendRequest> {

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
        public OpenRouterInternSuspendRequest build() {
            return new OpenRouterInternSuspendRequest(this);
        }

        @Override
        public OpenRouterInternLifecycleResponse<OpenRouterInternSuspendRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterInternLifecycleResponse<OpenRouterInternSuspendRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterInternLifecycleResponse<OpenRouterInternSuspendRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
