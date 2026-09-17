package de.entwicklertraining.openrouter4j.files;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A request to list the files of the workspace:
 * GET https://openrouter.ai/api/v1/files
 *
 * <p>Filters are sent as query parameters; every typed filter below mirrors a
 * documented query key of the endpoint. Filters not covered by a typed method
 * can be sent verbatim via {@link Builder#queryParam(String, Object)}.
 *
 * <p>Traps: {@code order} accepts {@code asc} and {@code desc} in the API
 * schema, but the OpenRouter storage only supports {@code asc} - the builder
 * does not hard-validate the value because the spec allows unknown values.
 * {@code before_id} is the Anthropic-style reverse cursor and is not
 * supported by the OpenRouter storage at all.
 */
public final class OpenRouterFileListRequest
        extends OpenRouterRequest<OpenRouterFileListResponse<OpenRouterFileListRequest>> {

    private final OpenRouterClient client;
    private final Map<String, String> queryParams;

    private OpenRouterFileListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.queryParams = Collections.unmodifiableMap(new LinkedHashMap<>(builder.queryParams));
    }

    /**
     * @return the query parameters this request sends, in insertion order
     */
    public Map<String, String> queryParams() {
        return queryParams;
    }

    @Override
    public String getRelativeUrl() {
        return appendQuery("/files");
    }

    private String appendQuery(String path) {
        if (queryParams.isEmpty()) {
            return path;
        }
        StringBuilder sb = new StringBuilder(path);
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            sb.append(sb.indexOf("?") < 0 ? '?' : '&');
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            sb.append('=');
            sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return sb.toString();
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
    public OpenRouterFileListResponse<OpenRouterFileListRequest> createResponse(String responseBody) {
        return new OpenRouterFileListResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterFileListRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterFileListRequest> {

        private final OpenRouterClient client;
        private final Map<String, String> queryParams = new LinkedHashMap<>();

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
         * Sets the query key {@code limit} - maximum number of files to
         * return (API range 1-1000). Rejected loudly outside that range.
         *
         * @param limit maximum number of files
         * @return this builder
         */
        public Builder limit(Integer limit) {
            if (limit != null && (limit < 1 || limit > 1000)) {
                throw new IllegalArgumentException(
                        "limit must be between 1 and 1000, got: " + limit);
            }
            return queryParam("limit", limit);
        }

        /**
         * Sets the query key {@code cursor} - the OpenRouter-style pagination
         * cursor from a previous response (see
         * {@link OpenRouterFileListResponse#cursor()}).
         *
         * @param cursor the pagination cursor
         * @return this builder
         */
        public Builder cursor(String cursor) {
            return queryParam("cursor", cursor);
        }

        /**
         * Sets the query key {@code workspace_id} - lists only the files of
         * this workspace.
         *
         * @param workspaceId the workspace id
         * @return this builder
         */
        public Builder workspaceId(String workspaceId) {
            return queryParam("workspace_id", workspaceId);
        }

        /**
         * Sets the query key {@code provider} - filters by storage provider.
         * The API documents {@code openai} and {@code anthropic}, but the
         * value is free-form: unknown values are not rejected by the builder
         * and are passed through verbatim.
         *
         * @param provider the storage provider
         * @return this builder
         */
        public Builder provider(String provider) {
            return queryParam("provider", provider);
        }

        /**
         * Sets the query key {@code after} - the OpenAI-style forward cursor:
         * lists files created after this file id.
         *
         * @param after the file id to page forward from
         * @return this builder
         */
        public Builder after(String after) {
            return queryParam("after", after);
        }

        /**
         * Sets the query key {@code after_id} - the Anthropic-style forward
         * cursor: lists files after this file id.
         *
         * @param afterId the file id to page forward from
         * @return this builder
         */
        public Builder afterId(String afterId) {
            return queryParam("after_id", afterId);
        }

        /**
         * Sets the query key {@code before_id} - the Anthropic-style reverse
         * cursor. Trap: not supported by the OpenRouter storage - sending it
         * has no effect or is rejected server-side.
         *
         * @param beforeId the file id to page backward from
         * @return this builder
         */
        public Builder beforeId(String beforeId) {
            return queryParam("before_id", beforeId);
        }

        /**
         * Sets the query key {@code order} - the sort order ({@code asc} or
         * {@code desc}). Trap: the API schema allows {@code desc}, but the
         * OpenRouter storage only supports {@code asc}; unknown values are
         * not rejected by the builder and are passed through verbatim.
         *
         * @param order the sort order
         * @return this builder
         */
        public Builder order(String order) {
            return queryParam("order", order);
        }

        /**
         * Adds any documented query parameter verbatim, for filters without a
         * typed method above and for any parameter OpenRouter adds later. The
         * value is sent URL-encoded; {@code null} values are ignored.
         *
         * @param name the query parameter name
         * @param value the query parameter value
         * @return this builder
         */
        public Builder queryParam(String name, Object value) {
            if (name != null && !name.isEmpty() && value != null) {
                queryParams.put(name, String.valueOf(value));
            }
            return this;
        }

        @Override
        public OpenRouterFileListRequest build() {
            return new OpenRouterFileListRequest(this);
        }

        @Override
        public OpenRouterFileListResponse<OpenRouterFileListRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterFileListResponse<OpenRouterFileListRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterFileListResponse<OpenRouterFileListRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
