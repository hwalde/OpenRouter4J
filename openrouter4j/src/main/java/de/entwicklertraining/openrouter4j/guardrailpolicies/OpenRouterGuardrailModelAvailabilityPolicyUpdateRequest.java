package de.entwicklertraining.openrouter4j.guardrailpolicies;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
/**
 * A request to update one model-availability guardrail policy:
 * PUT https://openrouter.ai/api/v1/guardrails/policies/model-availability/{id}
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 *
 * <p>Trap: {@code api_key_hashes} and {@code member_ids} are complete new
 * assignment lists and must be sent together, or both omitted to keep the
 * stored assignments - the library rejects the half-pair loudly at
 * {@link Builder#build()}.
 */
public final class OpenRouterGuardrailModelAvailabilityPolicyUpdateRequest extends OpenRouterRequest<OpenRouterGuardrailPolicyResponse<OpenRouterGuardrailModelAvailabilityPolicyUpdateRequest>> {

    private final OpenRouterClient client;
    private final String id;
    private final String name;
    private final OpenRouterGuardrailModelAvailabilityPolicyConfig config;
    private final String workspaceId;
    private final List<String> apiKeyHashes;
    private final List<String> memberIds;
    private final Boolean isWorkspaceDefault;
    private final List<String> optedOutApiKeyHashes;
    private final List<String> optedOutMemberIds;

    private OpenRouterGuardrailModelAvailabilityPolicyUpdateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.id = builder.id;
        this.name = builder.name;
        this.config = builder.config;
        this.workspaceId = builder.workspaceId;
        this.apiKeyHashes = builder.apiKeyHashes == null ? null : List.copyOf(builder.apiKeyHashes);
        this.memberIds = builder.memberIds == null ? null : List.copyOf(builder.memberIds);
        this.isWorkspaceDefault = builder.isWorkspaceDefault;
        this.optedOutApiKeyHashes = builder.optedOutApiKeyHashes == null ? null : List.copyOf(builder.optedOutApiKeyHashes);
        this.optedOutMemberIds = builder.optedOutMemberIds == null ? null : List.copyOf(builder.optedOutMemberIds);
    }

    /**
     * @return the URL-encoded path segment
     */
    public String id() {
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    /** @return the policy name as sent */
    public String name() {
        return name;
    }

    @Override
    public String getRelativeUrl() {
        return "/guardrails/policies/model-availability/" +
                URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    @Override
    public String getHttpMethod() {
        return "PUT";
    }

    @Override
    public String getBody() {
        JSONObject body = new JSONObject();
        body.put("name", name);
        body.put("config", config.toJson());
        body.put("workspace_id", workspaceId);
        if (apiKeyHashes != null) {
            body.put("api_key_hashes", new JSONArray(apiKeyHashes));
        }
        if (memberIds != null) {
            body.put("member_ids", new JSONArray(memberIds));
        }
        if (isWorkspaceDefault != null) {
            body.put("is_workspace_default", isWorkspaceDefault.booleanValue());
        }
        if (optedOutApiKeyHashes != null) {
            body.put("opted_out_api_key_hashes", new JSONArray(optedOutApiKeyHashes));
        }
        if (optedOutMemberIds != null) {
            body.put("opted_out_member_ids", new JSONArray(optedOutMemberIds));
        }
        return body.toString();
    }

    @Override
    public OpenRouterGuardrailPolicyResponse<OpenRouterGuardrailModelAvailabilityPolicyUpdateRequest> createResponse(String responseBody) {
        return new OpenRouterGuardrailPolicyResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterGuardrailModelAvailabilityPolicyUpdateRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterGuardrailModelAvailabilityPolicyUpdateRequest> {

        private final OpenRouterClient client;
        private final String id;
        private String name;
        private OpenRouterGuardrailModelAvailabilityPolicyConfig config;
        private String workspaceId;
        private List<String> apiKeyHashes;
        private List<String> memberIds;
        private Boolean isWorkspaceDefault;
        private List<String> optedOutApiKeyHashes;
        private List<String> optedOutMemberIds;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param id the id (UUID) of the policy
         */
        public Builder(OpenRouterClient client, String id) {
            super(client);
            this.client = client;
            this.id = id;
        }

        /**
         * Sets the body field {@code name} (required): the policy name
         * (1-255 characters).
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the body field {@code config} (required): the authoring
         * config that replaces the stored one - the PUT is a full replace,
         * send the full config every time.
         */
        public Builder config(OpenRouterGuardrailModelAvailabilityPolicyConfig config) {
            this.config = config;
            return this;
        }

        /**
         * Sets the body field {@code workspace_id} (required): the
         * workspace the policy belongs to.
         */
        public Builder workspaceId(String workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        /**
         * Sets the body field {@code api_key_hashes}: the complete new
         * list of assigned API keys. Send together with {@code member_ids},
         * or omit both to keep the stored assignments (validated loudly at
         * {@link #build()}).
         */
        public Builder apiKeyHashes(List<String> apiKeyHashes) {
            this.apiKeyHashes = apiKeyHashes;
            return this;
        }

        /**
         * Sets the body field {@code member_ids}: the complete new list of
         * assigned members. Send together with {@code api_key_hashes}, or
         * omit both to keep the stored assignments (validated loudly at
         * {@link #build()}).
         */
        public Builder memberIds(List<String> memberIds) {
            this.memberIds = memberIds;
            return this;
        }

        /**
         * Sets the body field {@code is_workspace_default}: omit to keep
         * the stored default status; {@code false} unsets the policy as
         * the workspace default (a set option and emitted).
         */
        public Builder isWorkspaceDefault(Boolean isWorkspaceDefault) {
            this.isWorkspaceDefault = isWorkspaceDefault;
            return this;
        }

        /**
         * Sets the body field {@code opted_out_api_key_hashes}: API keys
         * that should not inherit the policy when it is the workspace
         * default.
         */
        public Builder optedOutApiKeyHashes(List<String> optedOutApiKeyHashes) {
            this.optedOutApiKeyHashes = optedOutApiKeyHashes;
            return this;
        }

        /**
         * Sets the body field {@code opted_out_member_ids}: members that
         * should not inherit the policy when it is the workspace default.
         */
        public Builder optedOutMemberIds(List<String> optedOutMemberIds) {
            this.optedOutMemberIds = optedOutMemberIds;
            return this;
        }

        @Override
        public OpenRouterGuardrailModelAvailabilityPolicyUpdateRequest build() {
            if (id == null || id.isEmpty()) {
                throw new IllegalStateException("id is required");
            }
            if (".".equals(id) || "..".equals(id)) {
                throw new IllegalStateException("id of \".\" or \"..\" cannot be addressed by path");
            }
            if (name == null || name.isEmpty()) {
                throw new IllegalStateException("name is required");
            }
            if (name.codePointCount(0, name.length()) > 255) {
                throw new IllegalStateException("name must be at most 255 characters");
            }
            if (config == null) {
                throw new IllegalStateException("config is required (the PUT replaces name and settings)");
            }
            if (workspaceId == null || workspaceId.isEmpty()) {
                throw new IllegalStateException("workspace_id is required");
            }
            if ((apiKeyHashes == null) != (memberIds == null)) {
                throw new IllegalStateException(
                        "api_key_hashes and member_ids must be sent together, or both omitted");
            }
            return new OpenRouterGuardrailModelAvailabilityPolicyUpdateRequest(this);
        }

        @Override
        public OpenRouterGuardrailPolicyResponse<OpenRouterGuardrailModelAvailabilityPolicyUpdateRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterGuardrailPolicyResponse<OpenRouterGuardrailModelAvailabilityPolicyUpdateRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterGuardrailPolicyResponse<OpenRouterGuardrailModelAvailabilityPolicyUpdateRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
