package de.entwicklertraining.openrouter4j.privateproviders;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to retrieve one of the organization's private providers:
 * GET https://openrouter.ai/api/v1/private-providers/{slug}
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint and the private endpoints entitlement; a slug without a
 * provider answers 404. The {@code slug} path segment is URL-encoded (the
 * keys surface convention) - the form encoding renders a space as {@code +},
 * which a path-decoding server reads as a literal plus, but a real slug only
 * ever contains lowercase letters, digits and single hyphens, so the
 * encoding is identity for one.
 *
 * <p>Traps: the provider's {@code name} and {@code slug} cannot be changed
 * later ({@code PATCH /private-providers/{slug}} edits only
 * {@code display_name} and {@code base_url}), and the data policy is fixed
 * by OpenRouter - a private provider serves only your organization, so its
 * endpoints are recorded as retaining no prompts, training on none and
 * publishing none, and pass every data-policy filter your account or
 * guardrails apply. The slug is what {@code POST /private-endpoints} and
 * BYOK keys use as {@code provider_slug}. Not yet live-verified (management
 * route; probing mutates account state).
 */
public final class OpenRouterPrivateProviderGetRequest
        extends OpenRouterRequest<OpenRouterPrivateProviderResponse<OpenRouterPrivateProviderGetRequest>> {

    private final OpenRouterClient client;
    private final String slug;

    private OpenRouterPrivateProviderGetRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.slug = builder.slug;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String slug() {
        return URLEncoder.encode(slug, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/private-providers/" + URLEncoder.encode(slug, StandardCharsets.UTF_8);
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
    public OpenRouterPrivateProviderResponse<OpenRouterPrivateProviderGetRequest> createResponse(
            String responseBody) {
        return new OpenRouterPrivateProviderResponse<>(
                responseBody == null || responseBody.isEmpty()
                        ? new JSONObject()
                        : new JSONObject(responseBody),
                this);
    }

    /**
     * Starting point for building an {@link OpenRouterPrivateProviderGetRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterPrivateProviderGetRequest> {

        private final OpenRouterClient client;
        private final String slug;

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

        @Override
        public OpenRouterPrivateProviderGetRequest build() {
            return new OpenRouterPrivateProviderGetRequest(this);
        }

        @Override
        public OpenRouterPrivateProviderResponse<OpenRouterPrivateProviderGetRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterPrivateProviderResponse<OpenRouterPrivateProviderGetRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterPrivateProviderResponse<OpenRouterPrivateProviderGetRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
