package de.entwicklertraining.openrouter4j.privateproviders;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to delete a private provider:
 * DELETE https://openrouter.ai/api/v1/private-providers/{slug}
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint and the private endpoints entitlement. The API answers
 * 200 with the deleted slug wrapped in {@code data}
 * ({@code {"data": {"slug": "..."}}}); {@link #createResponse(String)} also
 * tolerates an empty body, so a 204 from other tooling parses as an empty
 * response instead of failing.
 *
 * <p>Traps: the delete refuses with 409 while any private endpoint still
 * runs under the provider - delete or move those endpoints first (the
 * sibling surface is {@code POST /private-endpoints} and its by-id routes).
 * The {@code slug} path segment is URL-encoded (the keys surface
 * convention) - a real slug only ever contains lowercase letters, digits and
 * single hyphens, so the encoding is identity for one. Not yet
 * live-verified (management route; probing mutates account state).
 */
public final class OpenRouterPrivateProviderDeleteRequest
        extends OpenRouterRequest<OpenRouterPrivateProviderDeleteResponse<OpenRouterPrivateProviderDeleteRequest>> {

    private final OpenRouterClient client;
    private final String slug;

    private OpenRouterPrivateProviderDeleteRequest(Builder builder) {
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
    public OpenRouterPrivateProviderDeleteResponse<OpenRouterPrivateProviderDeleteRequest> createResponse(
            String responseBody) {
        return new OpenRouterPrivateProviderDeleteResponse<>(
                responseBody == null || responseBody.isEmpty()
                        ? new JSONObject()
                        : new JSONObject(responseBody),
                this);
    }

    /**
     * Starting point for building an {@link OpenRouterPrivateProviderDeleteRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterPrivateProviderDeleteRequest> {

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
        public OpenRouterPrivateProviderDeleteRequest build() {
            return new OpenRouterPrivateProviderDeleteRequest(this);
        }

        @Override
        public OpenRouterPrivateProviderDeleteResponse<OpenRouterPrivateProviderDeleteRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterPrivateProviderDeleteResponse<OpenRouterPrivateProviderDeleteRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterPrivateProviderDeleteResponse<OpenRouterPrivateProviderDeleteRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
