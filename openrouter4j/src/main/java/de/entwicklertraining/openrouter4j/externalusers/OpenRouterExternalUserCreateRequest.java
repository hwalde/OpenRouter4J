package de.entwicklertraining.openrouter4j.externalusers;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to register an external user explicitly:
 * POST https://openrouter.ai/api/v1/external-users
 *
 * <p>The body carries the required {@code user} tracking string (1-512
 * characters, well-formed Unicode, no NUL, no surrounding whitespace,
 * validated loudly). Registration is normally implicit - sending
 * {@code user} on inference requests or {@code external.user} on API keys
 * (for organization keys outside HIPAA workspaces; the stored value has
 * surrounding whitespace trimmed) registers it with {@code is_active:
 * true}; this route exists for callers who want the registration before
 * first use. The answer is 201 with the registration.
 *
 * <p>Traps: registering a tracking id that already exists - active or
 * deactivated - answers HTTP 409 (the soft-deactivated form is brought
 * back with {@code update(user, true)}, not by re-registering). An id of
 * {@code .} or {@code ..} can be registered here (it is a valid tracking
 * string) but can never be addressed by path afterwards - look it up with
 * the {@code user} filter on {@link OpenRouterExternalUsersListRequest}.
 * Every route of this family requires a management key.
 */
public final class OpenRouterExternalUserCreateRequest
        extends OpenRouterRequest<OpenRouterExternalUserResponse<OpenRouterExternalUserCreateRequest>> {

    private final OpenRouterClient client;
    private final String user;

    private OpenRouterExternalUserCreateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.user = builder.user;
    }

    /**
     * @return the tracking string being registered
     */
    public String user() {
        return user;
    }

    @Override
    public String getRelativeUrl() {
        return "/external-users";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    @Override
    public String getBody() {
        JSONObject body = new JSONObject();
        body.put("user", user);
        return body.toString();
    }

    @Override
    public OpenRouterExternalUserResponse<OpenRouterExternalUserCreateRequest> createResponse(
            String responseBody) {
        return new OpenRouterExternalUserResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building an {@link OpenRouterExternalUserCreateRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterExternalUserCreateRequest> {

        private final OpenRouterClient client;
        private final String user;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param user the tracking string to register (validated loudly)
         */
        public Builder(OpenRouterClient client, String user) {
            super(client);
            this.client = client;
            this.user = OpenRouterExternalUserValidation.requireValidUser(user);
        }

        @Override
        public OpenRouterExternalUserCreateRequest build() {
            return new OpenRouterExternalUserCreateRequest(this);
        }

        @Override
        public OpenRouterExternalUserResponse<OpenRouterExternalUserCreateRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterExternalUserResponse<OpenRouterExternalUserCreateRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterExternalUserResponse<OpenRouterExternalUserCreateRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
