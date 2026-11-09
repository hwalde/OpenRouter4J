package de.entwicklertraining.openrouter4j.privateproviders;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to create a private, bring-your-own-key provider:
 * POST https://openrouter.ai/api/v1/private-providers
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint and the private endpoints entitlement. The provider is
 * visible only to your organization; the answer is 201 with the provider
 * wrapped in {@code data}. Use the created {@code slug} as
 * {@code provider_slug} when creating private endpoints
 * ({@code POST /private-endpoints}) and BYOK keys, and in
 * {@code provider.order}, {@code only} and {@code ignore}.
 *
 * <p>Traps: {@code name} and {@code slug} are permanent - neither can be
 * changed later ({@code PATCH /private-providers/{slug}} edits only
 * {@code display_name} and {@code base_url}). Neither the name nor its slug
 * may match a public provider name or slug (case-insensitive); the server
 * rejects the collision while other customers may use the same name, and
 * the create route documents 400 (invalid parameters) and 422 (semantic
 * validation failure) for rejected input. The slug is unique within the
 * account and defaults to the name lowercased with every run of other
 * characters replaced by a hyphen ({@code Acme Inference} becomes
 * {@code acme-inference}). The data policy is fixed by OpenRouter: a private
 * provider serves only your organization, so its endpoints are recorded as
 * retaining no prompts, training on none and publishing none, and pass every
 * data-policy filter your account or guardrails apply - there is no
 * caller-declared policy. {@code base_url} must be the HTTPS base URL of an
 * OpenAI-compatible API; private, internal, metadata and OpenRouter hosts
 * are rejected (422), and it is used by private endpoints under this
 * provider that set no URL of their own - without it each endpoint must set
 * its own. Not yet live-verified (management route; probing mutates account
 * state).
 */
public final class OpenRouterPrivateProviderCreateRequest
        extends OpenRouterRequest<OpenRouterPrivateProviderResponse<OpenRouterPrivateProviderCreateRequest>> {

    private final OpenRouterClient client;
    private final String name;
    private final String slug;
    private final String displayName;
    private final String baseUrl;

    private OpenRouterPrivateProviderCreateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.name = builder.name;
        this.slug = builder.slug;
        this.displayName = builder.displayName;
        this.baseUrl = builder.baseUrl;
    }

    /**
     * @return the provider name being created
     */
    public String name() {
        return name;
    }

    /**
     * @return the slug being created, or {@code null} when unset (the API
     *         derives it from the name)
     */
    public String slug() {
        return slug;
    }

    /**
     * @return the display name, or {@code null} when unset (the API defaults
     *         it to {@code name})
     */
    public String displayName() {
        return displayName;
    }

    /**
     * @return the base URL, or {@code null} when unset (every private
     *         endpoint under this provider must then set its own URL)
     */
    public String baseUrl() {
        return baseUrl;
    }

    @Override
    public String getRelativeUrl() {
        return "/private-providers";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * Builds the JSON body with only the explicitly configured fields
     * (an unset option never appears in the JSON).
     *
     * @return the JSON body
     */
    @Override
    public String getBody() {
        JSONObject body = new JSONObject();
        body.put("name", name);
        if (slug != null) {
            body.put("slug", slug);
        }
        if (displayName != null) {
            body.put("display_name", displayName);
        }
        if (baseUrl != null) {
            body.put("base_url", baseUrl);
        }
        return body.toString();
    }

    @Override
    public OpenRouterPrivateProviderResponse<OpenRouterPrivateProviderCreateRequest> createResponse(
            String responseBody) {
        return new OpenRouterPrivateProviderResponse<>(
                responseBody == null || responseBody.isEmpty()
                        ? new JSONObject()
                        : new JSONObject(responseBody),
                this);
    }

    /**
     * Starting point for building an {@link OpenRouterPrivateProviderCreateRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterPrivateProviderCreateRequest> {

        private final OpenRouterClient client;
        private String name;
        private String slug;
        private String displayName;
        private String baseUrl;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         */
        public Builder(OpenRouterClient client) {
            super(client);
            this.client = client;
        }

        /**
         * Sets the body field {@code name} (required) - the provider name,
         * shown in your activity, logs and Broadcast output. 2-64 letters,
         * numbers, spaces, dots, underscores or hyphens, starting with a
         * letter or number (schema pattern
         * {@code ^[A-Za-z0-9][A-Za-z0-9 ._-]*$}, validated loudly).
         *
         * <p>Trap: the name cannot be changed later, and neither the name nor
         * its slug may match a public provider name or slug
         * (case-insensitive) - the server rejects the collision, while other
         * customers may use the same name. Passing {@code null} is invalid
         * for a required field.
         *
         * @param name the provider name (validated loudly)
         * @return this builder
         */
        public Builder name(String name) {
            this.name = OpenRouterPrivateProviderValidation.requireValidName(name);
            return this;
        }

        /**
         * Sets the body field {@code slug} (optional) - 2-64 lowercase
         * letters and numbers, separated by single hyphens (schema pattern
         * {@code ^[a-z0-9]+(?:-[a-z0-9]+)*$}, validated loudly). Defaults to
         * the name lowercased with every run of other characters replaced by
         * a hyphen ({@code Acme Inference} becomes {@code acme-inference});
         * {@code null} unsets the option and leaves the derivation to the
         * API.
         *
         * <p>Trap: the slug cannot be changed later and is unique within the
         * account. Use it as {@code provider_slug} when creating private
         * endpoints ({@code POST /private-endpoints}) and BYOK keys, and in
         * {@code provider.order}, {@code only} and {@code ignore}. Like the
         * name, it may not match a public provider slug (case-insensitive).
         *
         * @param slug the provider slug, or {@code null} to let the API
         *        derive it (validated loudly)
         * @return this builder
         */
        public Builder slug(String slug) {
            this.slug = slug != null ? OpenRouterPrivateProviderValidation.requireValidSlug(slug) : null;
            return this;
        }

        /**
         * Sets the body field {@code display_name} (optional) - the
         * human-readable provider name shown in the UI, 1-80 characters
         * (validated loudly). Defaults to {@code name};
         * {@code null} unsets a previously set value.
         *
         * @param displayName the display name (validated loudly)
         * @return this builder
         */
        public Builder displayName(String displayName) {
            this.displayName = displayName != null
                    ? OpenRouterPrivateProviderValidation.requireValidDisplayName(displayName)
                    : null;
            return this;
        }

        /**
         * Sets the body field {@code base_url} (optional) - the HTTPS base
         * URL of an OpenAI-compatible API, used by private endpoints under
         * this provider that set no URL of their own; without it, each
         * endpoint must set its own. {@code null} unsets a previously set
         * value. The minimum length of 1 is validated loudly.
         *
         * <p>Trap: private, internal, metadata and OpenRouter hosts are
         * rejected by the server - the library only checks the documented
         * minimum length.
         *
         * @param baseUrl the base URL (validated loudly)
         * @return this builder
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl != null
                    ? OpenRouterPrivateProviderValidation.requireValidBaseUrl(baseUrl)
                    : null;
            return this;
        }

        @Override
        public OpenRouterPrivateProviderCreateRequest build() {
            if (name == null) {
                throw new IllegalStateException("name is required for private provider creation");
            }
            return new OpenRouterPrivateProviderCreateRequest(this);
        }

        @Override
        public OpenRouterPrivateProviderResponse<OpenRouterPrivateProviderCreateRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterPrivateProviderResponse<OpenRouterPrivateProviderCreateRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterPrivateProviderResponse<OpenRouterPrivateProviderCreateRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
