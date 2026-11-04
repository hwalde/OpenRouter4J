package de.entwicklertraining.openrouter4j.externalusers;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to retrieve one external-user registration:
 * GET https://openrouter.ai/api/v1/external-users/{user} (management key
 * required).
 *
 * <p>Retrieves an active or inactive registration by its URL-encoded,
 * caller-supplied tracking ID. The {@code user} path segment is URL-encoded
 * (the keys surface convention); an id of {@code "."} or {@code ".."} cannot
 * be addressed by path and is rejected loudly - look those up with the
 * {@code user} filter of {@link OpenRouterExternalUsersListRequest}.
 */
public final class OpenRouterExternalUserGetRequest
        extends OpenRouterRequest<OpenRouterExternalUserResponse<OpenRouterExternalUserGetRequest>> {

    private final OpenRouterClient client;
    private final String user;

    private OpenRouterExternalUserGetRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.user = builder.user;
    }

    /** @return the URL-encoded path segment */
    public String user() {
        return OpenRouterExternalUserPath.encode(user);
    }

    @Override
    public String getRelativeUrl() {
        return "/external-users/" + OpenRouterExternalUserPath.encode(user);
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
    public OpenRouterExternalUserResponse<OpenRouterExternalUserGetRequest> createResponse(
            String responseBody) {
        return new OpenRouterExternalUserResponse<>(
                responseBody == null || responseBody.isEmpty()
                        ? new JSONObject()
                        : new JSONObject(responseBody),
                this);
    }

    /**
     * Starting point for building an {@link OpenRouterExternalUserGetRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterExternalUserGetRequest> {

        private final OpenRouterClient client;
        private final String user;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param user the tracking string (URL-encoded into the path)
         */
        public Builder(OpenRouterClient client, String user) {
            super(client);
            this.client = client;
            OpenRouterExternalUserPath.encode(user);
            this.user = user;
        }

        @Override
        public OpenRouterExternalUserGetRequest build() {
            return new OpenRouterExternalUserGetRequest(this);
        }

        @Override
        public OpenRouterExternalUserResponse<OpenRouterExternalUserGetRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterExternalUserResponse<OpenRouterExternalUserGetRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterExternalUserResponse<OpenRouterExternalUserGetRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
