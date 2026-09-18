package de.entwicklertraining.openrouter4j.presets;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A request to get one specific version of a preset:
 * GET https://openrouter.ai/api/v1/presets/{slug}/versions/{version}
 */
public final class OpenRouterPresetVersionGetRequest extends OpenRouterRequest<OpenRouterPresetVersionGetResponse> {

    private final OpenRouterClient client;
    private final String slug;
    private final String version;

    private OpenRouterPresetVersionGetRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.slug = builder.slug;
        this.version = builder.version;
    }

    /** @return the URL-encoded slug path segment */
    public String slug() {
        return URLEncoder.encode(slug, StandardCharsets.UTF_8);
    }

    /** @return the URL-encoded version path segment */
    public String version() {
        return URLEncoder.encode(version, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/presets/" + URLEncoder.encode(slug, StandardCharsets.UTF_8)
                + "/versions/" + URLEncoder.encode(version, StandardCharsets.UTF_8);
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
    public OpenRouterPresetVersionGetResponse createResponse(String responseBody) {
        return new OpenRouterPresetVersionGetResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterPresetVersionGetRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterPresetVersionGetRequest> {

        private final OpenRouterClient client;
        private final String slug;
        private final String version;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param slug the preset slug
         * @param version the version number (or {@code latest})
         */
        public Builder(OpenRouterClient client, String slug, String version) {
            super(client);
            this.client = client;
            this.slug = slug;
            this.version = version;
        }

        @Override
        public OpenRouterPresetVersionGetRequest build() {
            if (slug == null || slug.isEmpty()) {
                throw new IllegalStateException("slug is required to get a preset version");
            }
            if (version == null || version.isEmpty()) {
                throw new IllegalStateException("version is required to get a preset version");
            }
            return new OpenRouterPresetVersionGetRequest(this);
        }

        @Override
        public OpenRouterPresetVersionGetResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterPresetVersionGetResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterPresetVersionGetResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
