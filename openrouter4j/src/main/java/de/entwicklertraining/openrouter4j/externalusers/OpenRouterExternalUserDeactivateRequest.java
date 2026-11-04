package de.entwicklertraining.openrouter4j.externalusers;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to deactivate an external-user registration:
 * DELETE https://openrouter.ai/api/v1/external-users/{user} (management key
 * required).
 *
 * <p>Soft-deactivates the registration while retaining its tracking ID -
 * repeat deactivation succeeds. This does <b>not</b> block inference and it
 * does not delete the registration: reactivate through
 * {@link OpenRouterExternalUserUpdateRequest}. The API answers 204 with no
 * body. The {@code user} path segment is URL-encoded (the keys surface
 * convention); an id of {@code "."} or {@code ".."} cannot be addressed by
 * path and is rejected loudly, and an id with internal spaces cannot be
 * reliably addressed either (the form encoding renders a space as
 * {@code +}, which a path-decoding server reads as a literal plus) -
 * look those up with the {@code user} filter of the list route.
 */
public final class OpenRouterExternalUserDeactivateRequest
        extends OpenRouterRequest<OpenRouterExternalUserDeactivateResponse> {

    private final OpenRouterClient client;
    private final String user;

    private OpenRouterExternalUserDeactivateRequest(Builder builder) {
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
        return "DELETE";
    }

    /**
     * DELETE requests carry no body.
     *
     * @return always {@code null}
     */
    @Override
    public String getBody() {
        return null;
    }

    @Override
    public OpenRouterExternalUserDeactivateResponse createResponse(String responseBody) {
        return new OpenRouterExternalUserDeactivateResponse(
                responseBody == null || responseBody.isEmpty()
                        ? new JSONObject()
                        : new JSONObject(responseBody),
                this);
    }

    /**
     * Starting point for building an {@link OpenRouterExternalUserDeactivateRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterExternalUserDeactivateRequest> {

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
        public OpenRouterExternalUserDeactivateRequest build() {
            return new OpenRouterExternalUserDeactivateRequest(this);
        }

        @Override
        public OpenRouterExternalUserDeactivateResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterExternalUserDeactivateResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterExternalUserDeactivateResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
