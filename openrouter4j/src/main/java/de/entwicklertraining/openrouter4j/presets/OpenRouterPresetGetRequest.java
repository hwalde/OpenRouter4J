package de.entwicklertraining.openrouter4j.presets;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A request to get one preset by slug, including its currently designated
 * version:
 * GET https://openrouter.ai/api/v1/presets/{slug}
 *
 * <p>Useful to check a slug's existence before sending an inference request:
 * an unknown preset slug in the {@code preset} body field is silently ignored
 * on inference, while this read returns 404.
 */
public final class OpenRouterPresetGetRequest extends OpenRouterRequest<OpenRouterPresetGetResponse> {

    private final OpenRouterClient client;
    private final String slug;

    private OpenRouterPresetGetRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.slug = builder.slug;
    }

    /** @return the URL-encoded slug path segment */
    public String slug() {
        return URLEncoder.encode(slug, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/presets/" + URLEncoder.encode(slug, StandardCharsets.UTF_8);
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
    public OpenRouterPresetGetResponse createResponse(String responseBody) {
        return new OpenRouterPresetGetResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterPresetGetRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterPresetGetRequest> {

        private final OpenRouterClient client;
        private final String slug;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param slug the preset slug
         */
        public Builder(OpenRouterClient client, String slug) {
            super(client);
            this.client = client;
            this.slug = slug;
        }

        @Override
        public OpenRouterPresetGetRequest build() {
            if (slug == null || slug.isEmpty()) {
                throw new IllegalStateException("slug is required to get a preset");
            }
            return new OpenRouterPresetGetRequest(this);
        }

        @Override
        public OpenRouterPresetGetResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterPresetGetResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterPresetGetResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
