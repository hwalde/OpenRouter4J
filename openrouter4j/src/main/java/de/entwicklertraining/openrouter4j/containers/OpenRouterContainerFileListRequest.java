package de.entwicklertraining.openrouter4j.containers;

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
 * Lists the files of one code-execution container:
 * GET https://openrouter.ai/api/v1/containers/{container_id}/files
 *
 * <p>The container id is the canonical id exactly as returned in a bash/shell
 * server-tool result (e.g. {@code sess_abc123}; a restarted session carries
 * its own {@code -r<nonce>}-suffixed id). The listing is ordered
 * lexicographically by container-relative path and is paginated with an
 * {@code after} cursor: pass the previous page's {@code last_id} and the
 * listing resumes strictly after that file (the cursor file itself is not
 * repeated). When {@code has_more} is {@code true} on the response, another
 * page can be fetched this way.
 *
 * <p>Query parameters are sent as the query string; parameters not covered by
 * a typed method can be sent verbatim via {@link Builder#queryParam(String, Object)}.
 */
public final class OpenRouterContainerFileListRequest
        extends OpenRouterRequest<OpenRouterContainerFileListResponse<OpenRouterContainerFileListRequest>> {

    private final OpenRouterClient client;
    private final String containerId;
    private final Map<String, String> queryParams;

    private OpenRouterContainerFileListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.containerId = builder.containerId;
        this.queryParams = Collections.unmodifiableMap(new LinkedHashMap<>(builder.queryParams));
    }

    /** @return the container id ({@code sess_...}) whose files are listed */
    public String containerId() {
        return containerId;
    }

    /**
     * @return the query parameters this request sends, in insertion order
     */
    public Map<String, String> queryParams() {
        return queryParams;
    }

    @Override
    public String getRelativeUrl() {
        return appendQuery("/containers/" + encode(containerId) + "/files");
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

    private static String encode(String segment) {
        if (segment == null) {
            return "";
        }
        // URLEncoder is form-encoding; path segments must keep "/" out anyway,
        // so encoding it is the correct behaviour for a container id.
        return URLEncoder.encode(segment, StandardCharsets.UTF_8);
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
    public OpenRouterContainerFileListResponse<OpenRouterContainerFileListRequest> createResponse(String responseBody) {
        return new OpenRouterContainerFileListResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterContainerFileListRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterContainerFileListRequest> {

        private final OpenRouterClient client;
        private final String containerId;
        private final Map<String, String> queryParams = new LinkedHashMap<>();

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param containerId the container id ({@code sess_...}) whose files to list
         */
        public Builder(OpenRouterClient client, String containerId) {
            super(client);
            this.client = client;
            this.containerId = containerId;
        }

        /**
         * Sets the query key {@code limit} - maximum number of files to
         * return per page. JSON/query field: {@code limit}; API default 100
         * (when unset the key is omitted and the default applies).
         * <p>
         * Trap: values outside 1..1000 are rejected loudly here instead of
         * being silently clamped (or rejected with a cryptic HTTP error) by
         * the API.
         *
         * @param limit maximum number of files per page, 1..1000
         * @return this builder
         * @throws IllegalArgumentException when limit is outside 1..1000
         */
        public Builder limit(Integer limit) {
            if (limit != null && (limit < 1 || limit > 1000)) {
                throw new IllegalArgumentException("limit must be between 1 and 1000, got: " + limit);
            }
            return queryParam("limit", limit);
        }

        /**
         * Sets the query key {@code after} - the forward pagination cursor.
         * Pass the previous page's {@code last_id} (see
         * {@link OpenRouterContainerFileListResponse#lastId()}) and the
         * listing resumes strictly after that file in lexicographic path
         * order - the cursor file itself is not repeated. JSON/query field:
         * {@code after}; when unset the key is omitted and the listing starts
         * at the first file.
         *
         * @param after the id of the file to resume after
         * @return this builder
         */
        public Builder after(String after) {
            return queryParam("after", after);
        }

        /**
         * Adds any documented query parameter verbatim, for parameters
         * without a typed method above and for any parameter OpenRouter adds
         * later. The value is sent URL-encoded; {@code null} values are
         * ignored.
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
        public OpenRouterContainerFileListRequest build() {
            if (containerId == null || containerId.isEmpty()) {
                throw new IllegalStateException("containerId is required for a container file list request");
            }
            return new OpenRouterContainerFileListRequest(this);
        }

        @Override
        public OpenRouterContainerFileListResponse<OpenRouterContainerFileListRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterContainerFileListResponse<OpenRouterContainerFileListRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterContainerFileListResponse<OpenRouterContainerFileListRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
