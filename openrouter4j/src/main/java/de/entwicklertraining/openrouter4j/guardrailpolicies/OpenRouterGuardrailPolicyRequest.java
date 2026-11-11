package de.entwicklertraining.openrouter4j.guardrailpolicies;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * One workspace-wide (account-level) guardrail policy request value - the
 * discriminated {@code AccountGuardrailPolicyRequest} union of the spec,
 * carried verbatim as the body of the account policy create and update
 * requests. The three typed factories build the three policy types (spec
 * {@code policy_type}: {@code "budget"}, {@code "model_availability"},
 * {@code "moderation"}); {@link #raw(JSONObject)} covers shapes outside the
 * union verbatim.
 * <p>
 * Required on the wire: {@code name} (1-255 characters), {@code policy_type}
 * and {@code config}. The optional envelope fields describe which workspaces
 * the policy covers: {@code workspace_ids} (the workspaces when it is not the
 * default), {@code is_default} (cover every workspace except
 * {@code opted_out_workspace_ids}; on update {@code false} on the current
 * default unsets it), {@code opted_out_workspace_ids},
 * {@code released_floor_workspace_ids} (workspaces whose own workspace-wide
 * policy of this type is dropped so they inherit this default) and
 * {@code keep_previous_default_workspaces} (keep the workspaces that
 * inherited the replaced default on that policy - cannot be combined with
 * {@code released_floor_workspace_ids}, validated loudly at
 * {@link Builder#build()}), plus {@code floor_reassignment}
 * ({@code policy_id} + {@code workspace_ids}, min 1 - move workspaces off
 * this policy onto another workspace-wide policy in the same write).
 * <p>
 * Semantics (docs): a workspace-wide policy applies to every member and API
 * key of an assigned workspace and cannot be loosened by a member/key policy;
 * a member/key holds at most one policy of each type per workspace; when
 * levels combine, the stricter rule wins (filters union, {@code block} beats
 * {@code redact}, every budget checked separately - budgets are per-subject,
 * never a shared pool).
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/guardrails/policies">Guardrail policies</a>
 */
public final class OpenRouterGuardrailPolicyRequest {

    private final String name;
    private final String policyType;
    private final JSONObject config;
    private final List<String> workspaceIds;
    private final List<String> optedOutWorkspaceIds;
    private final List<String> releasedFloorWorkspaceIds;
    private final JSONObject floorReassignment;
    private final Boolean isDefault;
    private final Boolean keepPreviousDefaultWorkspaces;
    private final JSONObject rawJson;

    private OpenRouterGuardrailPolicyRequest(Builder builder) {
        this.name = builder.name;
        this.policyType = builder.policyType;
        this.config = builder.config;
        this.workspaceIds = copyOrNull(builder.workspaceIds);
        this.optedOutWorkspaceIds = copyOrNull(builder.optedOutWorkspaceIds);
        this.releasedFloorWorkspaceIds = copyOrNull(builder.releasedFloorWorkspaceIds);
        this.floorReassignment = builder.floorReassignment == null
                ? null : new JSONObject(builder.floorReassignment.toString());
        this.isDefault = builder.isDefault;
        this.keepPreviousDefaultWorkspaces = builder.keepPreviousDefaultWorkspaces;
        this.rawJson = null;
    }

    private OpenRouterGuardrailPolicyRequest(JSONObject rawJson) {
        this.name = null;
        this.policyType = null;
        this.config = null;
        this.workspaceIds = null;
        this.optedOutWorkspaceIds = null;
        this.releasedFloorWorkspaceIds = null;
        this.floorReassignment = null;
        this.isDefault = null;
        this.keepPreviousDefaultWorkspaces = null;
        this.rawJson = rawJson;
    }

    private static List<String> copyOrNull(List<String> list) {
        return list == null ? null : List.copyOf(list);
    }

    /**
     * A budget policy request ({@code policy_type: "budget"}): a per-subject
     * USD spending limit.
     *
     * @param name the policy name (1-255 characters)
     * @param config the budget authoring config
     * @return the builder with {@code name}, {@code policy_type} and
     *         {@code config} pre-set
     */
    public static Builder budget(String name, OpenRouterGuardrailBudgetPolicyConfig config) {
        return new Builder("budget", name, config == null ? null : config.toJson());
    }

    /**
     * A model-availability policy request
     * ({@code policy_type: "model_availability"}): which models, providers
     * and data regions requests may use.
     *
     * @param name the policy name (1-255 characters)
     * @param config the model-availability authoring config
     * @return the builder with {@code name}, {@code policy_type} and
     *         {@code config} pre-set
     */
    public static Builder modelAvailability(String name, OpenRouterGuardrailModelAvailabilityPolicyConfig config) {
        return new Builder("model_availability", name, config == null ? null : config.toJson());
    }

    /**
     * A moderation policy request ({@code policy_type: "moderation"}):
     * content/PII filters and injection protection.
     *
     * @param name the policy name (1-255 characters)
     * @param config the moderation authoring config
     * @return the builder with {@code name}, {@code policy_type} and
     *         {@code config} pre-set
     */
    public static Builder moderation(String name, OpenRouterGuardrailModerationPolicyConfig config) {
        return new Builder("moderation", name, config == null ? null : config.toJson());
    }

    /**
     * Verbatim escape hatch for request shapes outside the typed union. The
     * object is stored as a detached copy and must carry the required
     * {@code name}, {@code policy_type} and {@code config} keys; the envelope
     * setters are not available on the raw form - the caller owns the whole
     * body.
     *
     * @param json the full {@code AccountGuardrailPolicyRequest} object
     * @return the verbatim request value
     */
    public static OpenRouterGuardrailPolicyRequest raw(JSONObject json) {
        if (json == null) {
            throw new IllegalArgumentException("json must not be null");
        }
        if (json.isNull("name") || json.isNull("policy_type") || json.isNull("config")) {
            throw new IllegalArgumentException(
                    "raw policy request requires the \"name\", \"policy_type\" and \"config\" keys");
        }
        return new OpenRouterGuardrailPolicyRequest(new JSONObject(json.toString()));
    }

    /**
     * @return the policy name, or {@code null} on the raw form
     */
    public String name() {
        return name;
    }

    /**
     * @return the {@code policy_type} discriminator, or {@code null} on the
     *         raw form
     */
    public String policyType() {
        return policyType;
    }

    /**
     * @return a detached copy of the {@code config} object, or {@code null}
     *         on the raw form
     */
    public JSONObject config() {
        return config == null ? null : new JSONObject(config.toString());
    }

    /**
     * Returns the JSON object emitted as the request body. The result is a
     * detached copy per call.
     */
    public JSONObject toJson() {
        return new JSONObject((rawJson != null ? rawJson : buildJson()).toString());
    }

    private JSONObject buildJson() {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("policy_type", policyType);
        json.put("config", config);
        if (workspaceIds != null) {
            json.put("workspace_ids", new JSONArray(workspaceIds));
        }
        if (optedOutWorkspaceIds != null) {
            json.put("opted_out_workspace_ids", new JSONArray(optedOutWorkspaceIds));
        }
        if (releasedFloorWorkspaceIds != null) {
            json.put("released_floor_workspace_ids", new JSONArray(releasedFloorWorkspaceIds));
        }
        if (floorReassignment != null) {
            json.put("floor_reassignment", floorReassignment);
        }
        if (isDefault != null) {
            json.put("is_default", isDefault.booleanValue());
        }
        if (keepPreviousDefaultWorkspaces != null) {
            json.put("keep_previous_default_workspaces", keepPreviousDefaultWorkspaces.booleanValue());
        }
        return json;
    }

    /** Builder for {@link OpenRouterGuardrailPolicyRequest}. */
    public static final class Builder {

        private final String policyType;
        private String name;
        private JSONObject config;
        private List<String> workspaceIds;
        private List<String> optedOutWorkspaceIds;
        private List<String> releasedFloorWorkspaceIds;
        private JSONObject floorReassignment;
        private Boolean isDefault;
        private Boolean keepPreviousDefaultWorkspaces;

        private Builder(String policyType, String name, JSONObject config) {
            requireName(name);
            if (config == null) {
                throw new IllegalArgumentException("config must not be null");
            }
            this.policyType = policyType;
            this.name = name;
            this.config = config;
        }

        private static void requireName(String name) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("name must not be null or empty");
            }
            int length = name.codePointCount(0, name.length());
            if (length > 255) {
                throw new IllegalArgumentException("name must be at most 255 characters, was: " + length);
            }
        }

        /**
         * Sets {@code workspace_ids}: the workspaces the policy covers when
         * it is not the default. A set list replaces the previous one;
         * {@code null} unsets.
         */
        public Builder workspaceIds(List<String> workspaceIds) {
            this.workspaceIds = workspaceIds;
            return this;
        }

        /**
         * Sets {@code opted_out_workspace_ids}: the workspaces left out of a
         * default policy. {@code null} unsets.
         */
        public Builder optedOutWorkspaceIds(List<String> optedOutWorkspaceIds) {
            this.optedOutWorkspaceIds = optedOutWorkspaceIds;
            return this;
        }

        /**
         * Sets {@code released_floor_workspace_ids}: the workspaces whose own
         * workspace-wide policy of this type is dropped so they inherit this
         * default. {@code null} unsets. Cannot be combined with
         * {@link #keepPreviousDefaultWorkspaces(Boolean)} (validated loudly
         * at {@link #build()}).
         */
        public Builder releasedFloorWorkspaceIds(List<String> releasedFloorWorkspaceIds) {
            this.releasedFloorWorkspaceIds = releasedFloorWorkspaceIds;
            return this;
        }

        /**
         * Sets {@code floor_reassignment}: move the given workspaces off this
         * policy onto the policy named by {@code policyId} in the same write
         * (at least one workspace). {@code null} unsets.
         *
         * @param policyId the policy to move the workspaces onto
         * @param workspaceIds the workspaces to move (min 1)
         */
        public Builder floorReassignment(String policyId, List<String> workspaceIds) {
            if (policyId == null || policyId.isEmpty()) {
                throw new IllegalArgumentException("floorReassignment policyId must not be null or empty");
            }
            if (workspaceIds == null || workspaceIds.isEmpty()) {
                throw new IllegalArgumentException("floorReassignment needs at least one workspace id");
            }
            this.floorReassignment = new JSONObject()
                    .put("policy_id", policyId)
                    .put("workspace_ids", new JSONArray(workspaceIds));
            return this;
        }

        /**
         * Sets {@code is_default}: cover every workspace in the account
         * except {@code opted_out_workspace_ids}. On update, {@code false} on
         * the current default unsets it. {@code false} is a set option and is
         * emitted; {@code null} unsets.
         */
        public Builder isDefault(Boolean isDefault) {
            this.isDefault = isDefault;
            return this;
        }

        /**
         * Sets {@code keep_previous_default_workspaces}: keep the workspaces
         * that inherited the replaced default on that policy. Cannot be
         * combined with {@link #releasedFloorWorkspaceIds(List)} (validated
         * loudly at {@link #build()}). {@code null} unsets.
         */
        public Builder keepPreviousDefaultWorkspaces(Boolean keepPreviousDefaultWorkspaces) {
            this.keepPreviousDefaultWorkspaces = keepPreviousDefaultWorkspaces;
            return this;
        }

        /**
         * Builds the request value.
         *
         * @throws IllegalStateException when
         *         {@code keep_previous_default_workspaces} is combined with
         *         {@code released_floor_workspace_ids}
         */
        public OpenRouterGuardrailPolicyRequest build() {
            if (Boolean.TRUE.equals(keepPreviousDefaultWorkspaces) && releasedFloorWorkspaceIds != null) {
                throw new IllegalStateException(
                        "keep_previous_default_workspaces cannot be combined with released_floor_workspace_ids");
            }
            return new OpenRouterGuardrailPolicyRequest(this);
        }
    }
}
