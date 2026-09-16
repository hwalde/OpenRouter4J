package de.entwicklertraining.openrouter4j.guardrails;

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
 * A request to unassign members from a guardrail:
 * POST https://openrouter.ai/api/v1/guardrails/{id}/assignments/members/remove
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 */
public final class OpenRouterGuardrailMembersUnassignRequest extends OpenRouterRequest<OpenRouterGuardrailMembersUnassignResponse> {

    private final OpenRouterClient client;
    private final String id;
    private final List<String> memberUserIds;

    private OpenRouterGuardrailMembersUnassignRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.id = builder.id;
        this.memberUserIds = builder.memberUserIds;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String id() {
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/guardrails/" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "/assignments/members/remove";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * Builds the JSON body with only the explicitly configured fields
     * (an unset option never appears in the JSON).
     *
     * @return the JSON body
     */
    @Override
    public String getBody() {
        JSONObject body = new JSONObject();
        body.put("member_user_ids", new JSONArray(memberUserIds));
        return body.toString();
    }

    @Override
    public OpenRouterGuardrailMembersUnassignResponse createResponse(String responseBody) {
        return new OpenRouterGuardrailMembersUnassignResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterGuardrailMembersUnassignRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterGuardrailMembersUnassignRequest> {

        private final OpenRouterClient client;
    private final String id;
    private List<String> memberUserIds;


    /**
     * Creates a builder bound to the given client.
     *
     * @param client the client used to send the request
     * @param id the id (UUID) of the guardrail
     */
    public Builder(OpenRouterClient client, String id) {
        super(client);
        this.client = client;
        this.id = id;
    }

    /**
 * Adds a member user id to the body field {@code member_user_ids} (required,
 * at least one) - the Clerk user id of a member to release from this guardrail.
 */
    public Builder memberUserIds(List<String> memberUserIds) {
        this.memberUserIds = memberUserIds;
        return this;
    }
    /**
     * Adds one member user id to the list.
     *
     * <p>Adds one member user id to the body field {@code member_user_ids}
(required, at least one) - the Clerk user id of a member to release from
this guardrail.
     *
     * @param addMemberUserId the member user id to add
     * @return this builder
     */
    public Builder addMemberUserId(String addMemberUserId) {
        if (this.memberUserIds == null) {
            this.memberUserIds = new ArrayList<>();
        }
        this.memberUserIds.add(addMemberUserId);
        return this;
    }

        @Override
        public OpenRouterGuardrailMembersUnassignRequest build() {
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("id is required");
        }
        if (memberUserIds == null || memberUserIds.isEmpty()) {
            throw new IllegalStateException("memberUserIds must contain at least one member user id");
        }
            return new OpenRouterGuardrailMembersUnassignRequest(this);
        }


    @Override
    public OpenRouterGuardrailMembersUnassignResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterGuardrailMembersUnassignResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterGuardrailMembersUnassignResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
