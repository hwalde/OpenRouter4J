package de.entwicklertraining.openrouter4j.interns;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.util.regex.Pattern;
import org.json.JSONObject;

/**
 * A request to create an intern:
 * POST https://openrouter.ai/api/v1/interns
 *
 * <p>Body: {@code name} (required, 2-17 chars, pattern
 * {@code ^[a-z][a-z0-9]*(-[a-z0-9]+)*$}, unique per creator within the
 * workspace; validated loudly here) plus the optional {@code workspace_id},
 * {@code description} (max 2,000 chars), {@code instructions} (max 100,000
 * chars), {@code provision} (start booting immediately, default
 * {@code false}) and {@code vault_id} (a vault owned by another intern in
 * the workspace, attached as a borrowed vault). Every field is emitted only
 * when explicitly configured.
 *
 * <p>Traps: the operation is idempotent on retry - the server derives a
 * stable key from the request body, or you can set your own with
 * {@link Builder#idempotencyKey(String)}; the body is capped at 1 MiB (413
 * above); {@code workspace_id} must match the API key workspace when given;
 * the response answers 404 for keys outside the interns programme.
 */
public final class OpenRouterInternCreateRequest
        extends OpenRouterRequest<OpenRouterInternResponse<OpenRouterInternCreateRequest>> {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z][a-z0-9]*(-[a-z0-9]+)*$");

    private final OpenRouterClient client;
    private final String name;
    private final String workspaceId;
    private final String description;
    private final String instructions;
    private final Boolean provision;
    private final String vaultId;

    private OpenRouterInternCreateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.name = builder.name;
        this.workspaceId = builder.workspaceId;
        this.description = builder.description;
        this.instructions = builder.instructions;
        this.provision = builder.provision;
        this.vaultId = builder.vaultId;
        if (builder.idempotencyKey != null) {
            setHeader("Idempotency-Key", builder.idempotencyKey);
        }
    }

    @Override
    public String getRelativeUrl() {
        return "/interns";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * The JSON body: the configured settings, {@code name} always present.
     *
     * @return the JSON body string
     */
    @Override
    public String getBody() {
        JSONObject body = new JSONObject();
        body.put("name", name);
        if (workspaceId != null) {
            body.put("workspace_id", workspaceId);
        }
        if (description != null) {
            body.put("description", description);
        }
        if (instructions != null) {
            body.put("instructions", instructions);
        }
        if (provision != null) {
            body.put("provision", provision);
        }
        if (vaultId != null) {
            body.put("vault_id", vaultId);
        }
        return body.toString();
    }

    @Override
    public OpenRouterInternResponse<OpenRouterInternCreateRequest> createResponse(
            String responseBody) {
        return new OpenRouterInternResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterInternCreateRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterInternCreateRequest> {

        private final OpenRouterClient client;
        private final String name;
        private String idempotencyKey;
        private String workspaceId;
        private String description;
        private String instructions;
        private Boolean provision;
        private String vaultId;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param name the intern name (2-17 chars, pattern
         *             {@code ^[a-z][a-z0-9]*(-[a-z0-9]+)*$}, unique per
         *             creator within the workspace; validated loudly)
         */
        public Builder(OpenRouterClient client, String name) {
            super(client);
            this.client = client;
            if (name == null || name.length() < 2 || name.length() > 17) {
                throw new IllegalArgumentException(
                        "intern name must be 2 to 17 characters long");
            }
            if (!NAME_PATTERN.matcher(name).matches()) {
                throw new IllegalArgumentException(
                        "intern name must match ^[a-z][a-z0-9]*(-[a-z0-9]+)*$: " + name);
            }
            this.name = name;
        }

        /**
         * Sets the JSON field {@code workspace_id} - the workspace that will
         * own the intern. Defaults to the workspace the API key resolves to;
         * when given, it must match the API key workspace.
         *
         * @param workspaceId the workspace id (UUID)
         * @return this builder
         */
        public Builder workspaceId(String workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        /**
         * Sets the JSON field {@code description} - a free-form description
         * (at most 2,000 characters, validated loudly).
         *
         * @param description the description
         * @return this builder
         */
        public Builder description(String description) {
            if (description != null && description.length() > 2_000) {
                throw new IllegalArgumentException(
                        "description must be at most 2000 characters");
            }
            this.description = description;
            return this;
        }

        /**
         * Sets the JSON field {@code instructions} - the standing
         * instructions the intern boots with (at most 100,000 characters,
         * validated loudly).
         *
         * @param instructions the standing instructions
         * @return this builder
         */
        public Builder instructions(String instructions) {
            if (instructions != null && instructions.length() > 100_000) {
                throw new IllegalArgumentException(
                        "instructions must be at most 100000 characters");
            }
            this.instructions = instructions;
            return this;
        }

        /**
         * Sets the JSON field {@code provision} - start provisioning during
         * this create operation. Default {@code false}: the intern is
         * created queued and boots on the provision endpoint.
         *
         * @param provision true to boot immediately
         * @return this builder
         */
        public Builder provision(boolean provision) {
            this.provision = provision;
            return this;
        }

        /**
         * Sets the JSON field {@code vault_id} - a vault owned by another
         * intern in this workspace, attached as a borrowed vault.
         *
         * @param vaultId the vault id (UUID)
         * @return this builder
         */
        public Builder vaultId(String vaultId) {
            this.vaultId = vaultId;
            return this;
        }

        /**
         * Sets the {@code Idempotency-Key} header (1-255 characters,
         * validated loudly) that makes retries resume the same create
         * operation. Without the header, the server derives a stable key
         * from the request body.
         *
         * @param idempotencyKey the idempotency key
         * @return this builder
         */
        public Builder idempotencyKey(String idempotencyKey) {
            if (idempotencyKey == null || idempotencyKey.isEmpty()
                    || idempotencyKey.length() > 255) {
                throw new IllegalArgumentException(
                        "idempotency key must be 1 to 255 characters");
            }
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        @Override
        public OpenRouterInternCreateRequest build() {
            return new OpenRouterInternCreateRequest(this);
        }

        @Override
        public OpenRouterInternResponse<OpenRouterInternCreateRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterInternResponse<OpenRouterInternCreateRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterInternResponse<OpenRouterInternCreateRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
