package de.entwicklertraining.openrouter4j.privateproviders;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to update a private provider's display name or base URL:
 * PATCH https://openrouter.ai/api/v1/private-providers/{slug}
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint and the private endpoints entitlement. Only explicitly
 * configured fields are sent; every field is optional on the wire.
 *
 * <p>Traps: the {@code name} and {@code slug} cannot change, and the data
 * policy is fixed by OpenRouter, so a body with no editable field is
 * rejected (400) - {@code build()} enforces this loudly. The data policy
 * itself is not caller-declared: a private provider serves only your
 * organization, so its endpoints are recorded as retaining no prompts,
 * training on none and publishing none, and pass every data-policy filter
 * your account or guardrails apply. {@code base_url} must be the HTTPS base
 * URL of an OpenAI-compatible API; private, internal, metadata and
 * OpenRouter hosts are rejected (422), and it is used by private endpoints
 * under this provider that set no URL of their own. The {@code slug} path
 * segment is URL-encoded (the keys surface convention) - a real slug only
 * ever contains lowercase letters, digits and single hyphens, so the
 * encoding is identity for one. Not yet live-verified (management route;
 * probing mutates account state).
 */
public final class OpenRouterPrivateProviderUpdateRequest
        extends OpenRouterRequest<OpenRouterPrivateProviderResponse<OpenRouterPrivateProviderUpdateRequest>> {

    private final OpenRouterClient client;
    private final String slug;
    private final String displayName;
    private final String baseUrl;

    private OpenRouterPrivateProviderUpdateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.slug = builder.slug;
        this.displayName = builder.displayName;
        this.baseUrl = builder.baseUrl;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String slug() {
        return URLEncoder.encode(slug, StandardCharsets.UTF_8);
    }

    /**
     * @return the new display name, or {@code null} when unset (unchanged)
     */
    public String displayName() {
        return displayName;
    }

    /**
     * @return the new base URL, or {@code null} when unset (unchanged)
     */
    public String baseUrl() {
        return baseUrl;
    }

    @Override
    public String getRelativeUrl() {
        return "/private-providers/" + URLEncoder.encode(slug, StandardCharsets.UTF_8);
    }

    @Override
    public String getHttpMethod() {
        return "PATCH";
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
        if (displayName != null) {
            body.put("display_name", displayName);
        }
        if (baseUrl != null) {
            body.put("base_url", baseUrl);
        }
        return body.toString();
    }

    @Override
    public OpenRouterPrivateProviderResponse<OpenRouterPrivateProviderUpdateRequest> createResponse(
            String responseBody) {
        return new OpenRouterPrivateProviderResponse<>(
                responseBody == null || responseBody.isEmpty()
                        ? new JSONObject()
                        : new JSONObject(responseBody),
                this);
    }

    /**
     * Starting point for building an {@link OpenRouterPrivateProviderUpdateRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterPrivateProviderUpdateRequest> {

        private final OpenRouterClient client;
        private final String slug;
        private String displayName;
        private String baseUrl;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param slug the provider slug (URL-encoded into the path; required)
         */
        public Builder(OpenRouterClient client, String slug) {
            super(client);
            this.client = client;
            this.slug = OpenRouterPrivateProviderValidation.requireSlugSegment(slug);
        }

        /**
         * Sets the body field {@code display_name} (optional) - the new
         * human-readable provider name shown in the UI, 1-80 characters
         * (validated loudly). {@code null} unsets a previously set value and
         * leaves the stored name unchanged.
         *
         * <p>Trap: {@code name} and {@code slug} are immutable - the API
         * offers no way to change them.
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
         * Sets the body field {@code base_url} (optional) - the new HTTPS
         * base URL of an OpenAI-compatible API, used by private endpoints
         * under this provider that set no URL of their own; without it, each
         * endpoint must set its own. {@code null} unsets a previously set
         * value and leaves the stored URL unchanged. The minimum length of 1
         * is validated loudly.
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
        public OpenRouterPrivateProviderUpdateRequest build() {
            if (displayName == null && baseUrl == null) {
                throw new IllegalStateException(
                        "PATCH /private-providers/{slug} needs at least one editable field "
                                + "(displayName or baseUrl) - the name and slug cannot change and "
                                + "the data policy is fixed by OpenRouter, so the API rejects a body "
                                + "with no editable field (400)");
            }
            return new OpenRouterPrivateProviderUpdateRequest(this);
        }

        @Override
        public OpenRouterPrivateProviderResponse<OpenRouterPrivateProviderUpdateRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterPrivateProviderResponse<OpenRouterPrivateProviderUpdateRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterPrivateProviderResponse<OpenRouterPrivateProviderUpdateRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
