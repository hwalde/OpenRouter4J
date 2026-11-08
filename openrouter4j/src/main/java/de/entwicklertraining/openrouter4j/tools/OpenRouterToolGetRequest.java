package de.entwicklertraining.openrouter4j.tools;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to read one server tool of the catalog:
 * GET https://openrouter.ai/api/v1/tools/{name}
 *
 * <p>Accepts the canonical {@code openrouter:*} name or any accepted
 * {@code tools[].type} alias. The {@code name} path segment is URL-encoded
 * (form encoding: a space renders as {@code +}, which a path-decoding
 * server reads as a literal plus); null, blank and {@code "."} / {@code ".."}
 * names are rejected loudly - a normalizing hop would resolve those against
 * the {@code /tools/} prefix instead of addressing a tool.
 *
 * <p><b>Alias trap (live-verified):</b> a lookup by an alias answers HTTP
 * 308 with the canonical tool URL in the {@code Location} header instead of
 * a body, and the transport neither follows redirects nor surfaces response
 * headers (the api-base response-headers gap). The call therefore fails with
 * api-base's generic {@code ApiClientException} ("Unexpected HTTP status
 * 308") instead of returning the tool: resolve the canonical {@code id}
 * from {@link OpenRouterServerToolCatalogEntry#aliases()} of the
 * {@link OpenRouterToolsListRequest} response and call {@code get(id)} with
 * it - the canonical name answers 200 with the tool. An unknown name
 * answers 404 ({@code HTTP_404_NotFoundException}).
 */
public final class OpenRouterToolGetRequest
        extends OpenRouterRequest<OpenRouterToolGetResponse<OpenRouterToolGetRequest>> {

    private final OpenRouterClient client;
    private final String name;

    private OpenRouterToolGetRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.name = builder.name;
    }

    /**
     * @return the tool name (unencoded); the path segment is URL-encoded in
     *         {@link #getRelativeUrl()}
     */
    public String name() {
        return name;
    }

    @Override
    public String getRelativeUrl() {
        return "/tools/" + OpenRouterToolPath.encode(name);
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
    public OpenRouterToolGetResponse<OpenRouterToolGetRequest> createResponse(String responseBody) {
        return new OpenRouterToolGetResponse<>(
                responseBody == null || responseBody.isEmpty()
                        ? new JSONObject()
                        : new JSONObject(responseBody),
                this);
    }

    /**
     * Starting point for building an {@link OpenRouterToolGetRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterToolGetRequest> {

        private final OpenRouterClient client;
        private final String name;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param name the canonical {@code openrouter:*} name or an accepted
         *        alias (validated loudly: non-null, non-blank, no
         *        {@code "."} / {@code ".."})
         */
        public Builder(OpenRouterClient client, String name) {
            super(client);
            OpenRouterToolPath.requireValid(name);
            this.client = client;
            this.name = name;
        }

        @Override
        public OpenRouterToolGetRequest build() {
            return new OpenRouterToolGetRequest(this);
        }

        @Override
        public OpenRouterToolGetResponse<OpenRouterToolGetRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterToolGetResponse<OpenRouterToolGetRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterToolGetResponse<OpenRouterToolGetRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}