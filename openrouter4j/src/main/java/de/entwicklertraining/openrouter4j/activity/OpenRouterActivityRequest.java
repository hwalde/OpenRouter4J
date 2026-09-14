package de.entwicklertraining.openrouter4j.activity;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to read the usage activity of the authenticated account:
 * GET https://openrouter.ai/api/v1/activity
 *
 * <p>Returns per-day, per-model, per-endpoint usage rows (requests, tokens,
 * cost). OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint.
 */
public final class OpenRouterActivityRequest extends OpenRouterRequest<OpenRouterActivityResponse> {

    private final OpenRouterClient client;
    private final String date;
    private final String apiKeyHash;
    private final String userId;
    private final String groupBy;
    private final String workspaceId;

    private OpenRouterActivityRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.date = builder.date;
        this.apiKeyHash = builder.apiKeyHash;
        this.userId = builder.userId;
        this.groupBy = builder.groupBy;
        this.workspaceId = builder.workspaceId;
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/activity");
        append(sb, "date", date);
        append(sb, "api_key_hash", apiKeyHash);
        append(sb, "user_id", userId);
        append(sb, "group_by", groupBy);
        append(sb, "workspace_id", workspaceId);
        return sb.toString();
    }

    private static void append(StringBuilder sb, String name, String value) {
        if (value != null && !value.isEmpty()) {
            sb.append(sb.indexOf("?") < 0 ? '?' : '&');
            sb.append(name).append('=')
                    .append(java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8));
        }
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
    public OpenRouterActivityResponse createResponse(String responseBody) {
        return new OpenRouterActivityResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterActivityRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterActivityRequest> {

        private final OpenRouterClient client;
        private String date;
        private String apiKeyHash;
        private String userId;
        private String groupBy;
        private String workspaceId;

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
         * Sets the query key {@code date} - restrict the activity to one day
         * (YYYY-MM-DD).
         *
         * @param date the day
         * @return this builder
         */
        public Builder date(String date) {
            this.date = date;
            return this;
        }

        /**
         * Sets the query key {@code api_key_hash} - restrict the activity to
         * one API key (the key hash as returned by the key management API).
         *
         * @param apiKeyHash the key hash
         * @return this builder
         */
        public Builder apiKeyHash(String apiKeyHash) {
            this.apiKeyHash = apiKeyHash;
            return this;
        }

        /**
         * Sets the query key {@code user_id} - restrict the activity to one
         * end user (the {@code user} identifier sent with requests).
         *
         * @param userId the end-user identifier
         * @return this builder
         */
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Sets the query key {@code group_by} - split the response by a
         * grouping (e.g. {@code workspace}); with {@code workspace} the rows
         * carry a {@code workspace_id} (see {@link OpenRouterActivityItem#workspaceId()}).
         *
         * @param groupBy the grouping key
         * @return this builder
         */
        public Builder groupBy(String groupBy) {
            this.groupBy = groupBy;
            return this;
        }

        /**
         * Sets the query key {@code workspace_id} - restrict the activity to
         * one workspace.
         *
         * @param workspaceId the workspace id
         * @return this builder
         */
        public Builder workspaceId(String workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        @Override
        public OpenRouterActivityRequest build() {
            return new OpenRouterActivityRequest(this);
        }

        @Override
        public OpenRouterActivityResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterActivityResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterActivityResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
