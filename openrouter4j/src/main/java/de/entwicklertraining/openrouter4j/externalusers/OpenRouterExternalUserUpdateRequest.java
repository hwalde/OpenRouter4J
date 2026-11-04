package de.entwicklertraining.openrouter4j.externalusers;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to change an external-user registration's state:
 * PATCH https://openrouter.ai/api/v1/external-users/{user} (management key
 * required).
 *
 * <p>Body {@code UpdateExternalUserRequest}: the required {@code is_active}
 * (always emitted - there is no unset state on this request). Registration
 * state only; <b>does not enforce inference access</b> - reactivating a
 * registration does not grant inference rights, deactivating does not block
 * inference. The {@code user} path segment is URL-encoded (the keys surface
 * convention); an id of {@code "."} or {@code ".."} cannot be addressed by
 * path and is rejected loudly.
 */
public final class OpenRouterExternalUserUpdateRequest
        extends OpenRouterRequest<OpenRouterExternalUserResponse<OpenRouterExternalUserUpdateRequest>> {

    private final OpenRouterClient client;
    private final String user;
    private final boolean isActive;

    private OpenRouterExternalUserUpdateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.user = builder.user;
        this.isActive = builder.isActive;
    }

    /** @return the URL-encoded path segment */
    public String user() {
        return OpenRouterExternalUserPath.encode(user);
    }

    /** @return {@code is_active} - the registration state being written */
    public Boolean isActive() {
        return isActive;
    }

    @Override
    public String getRelativeUrl() {
        return "/external-users/" + OpenRouterExternalUserPath.encode(user);
    }

    @Override
    public String getHttpMethod() {
        return "PATCH";
    }

    /**
     * JSON path: the request body - {@code is_active} (required, always
     * emitted as a JSON boolean).
     *
     * @return the JSON body of this request
     */
    @Override
    public String getBody() {
        JSONObject root = new JSONObject();
        root.put("is_active", isActive);
        return root.toString();
    }

    @Override
    public OpenRouterExternalUserResponse<OpenRouterExternalUserUpdateRequest> createResponse(
            String responseBody) {
        return new OpenRouterExternalUserResponse<>(
                responseBody == null || responseBody.isEmpty()
                        ? new JSONObject()
                        : new JSONObject(responseBody),
                this);
    }

    /**
     * Starting point for building an {@link OpenRouterExternalUserUpdateRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterExternalUserUpdateRequest> {

        private final OpenRouterClient client;
        private final String user;
        private final boolean isActive;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param user the tracking string (URL-encoded into the path)
         * @param isActive the registration state to write (reactivate with
         *        {@code true}); always emitted
         */
        public Builder(OpenRouterClient client, String user, boolean isActive) {
            super(client);
            this.client = client;
            OpenRouterExternalUserPath.encode(user);
            this.user = user;
            this.isActive = isActive;
        }

        @Override
        public OpenRouterExternalUserUpdateRequest build() {
            return new OpenRouterExternalUserUpdateRequest(this);
        }

        @Override
        public OpenRouterExternalUserResponse<OpenRouterExternalUserUpdateRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterExternalUserResponse<OpenRouterExternalUserUpdateRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterExternalUserResponse<OpenRouterExternalUserUpdateRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
