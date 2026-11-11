package de.entwicklertraining.openrouter4j.guardrailpolicies;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Request-JSON level tests for the guardrail policies surface: the URL and
 * body shapes of the 23 routes, the discriminated account request value, the
 * three authoring configs, the assignment contract and the response views.
 */
class OpenRouterGuardrailPoliciesTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    private OpenRouterGuardrailBudgetPolicyConfig budgetConfig() {
        return OpenRouterGuardrailBudgetPolicyConfig.builder()
                .limitUsd(200.0)
                .resetInterval("monthly")
                .includeByokInBudgets(false)
                .build();
    }

    // ------------------------------------------------------------------
    // URL and method shapes
    // ------------------------------------------------------------------

    @Test
    void listRoutesCarryTheTypedQueryParameters() {
        assertThat(client().guardrailPolicies().listAccountPolicies().build().getRelativeUrl())
                .isEqualTo("/guardrails/policies/account");
        assertThat(client().guardrailPolicies().listAccountPolicies().build().getHttpMethod())
                .isEqualTo("GET");

        OpenRouterGuardrailBudgetPoliciesListRequest list = client().guardrailPolicies()
                .listBudgetPolicies()
                .offset(10)
                .limit(50)
                .workspaceId("ws-1")
                .build();
        assertThat(list.getRelativeUrl())
                .isEqualTo("/guardrails/policies/budget?offset=10&limit=50&workspace_id=ws-1");

        // both pagination parameters omitted: the full list in one page
        assertThat(client().guardrailPolicies().listModerationPolicies()
                .workspaceId("ws-1").build().getRelativeUrl())
                .isEqualTo("/guardrails/policies/moderation?workspace_id=ws-1");

        assertThat(client().guardrailPolicies().listModelAvailabilityPolicies()
                .workspaceId("ws with space").build().getRelativeUrl())
                .contains("workspace_id=ws+with+space");
    }

    @Test
    void listRoutesValidateTheTypedQueryParameters() {
        assertThatThrownBy(() -> client().guardrailPolicies().listBudgetPolicies().build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("workspace_id");
        assertThatThrownBy(() -> client().guardrailPolicies().listBudgetPolicies()
                .workspaceId("ws-1").limit(0).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("limit");
        assertThatThrownBy(() -> client().guardrailPolicies().listBudgetPolicies()
                .workspaceId("ws-1").limit(101).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("limit");
        assertThatThrownBy(() -> client().guardrailPolicies().listBudgetPolicies()
                .workspaceId("ws-1").offset(-1).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("offset");
        // the same validation in the model-availability and moderation copies
        assertThatThrownBy(() -> client().guardrailPolicies().listModelAvailabilityPolicies()
                .workspaceId("ws-1").limit(101).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("limit");
        assertThatThrownBy(() -> client().guardrailPolicies().listModerationPolicies()
                .workspaceId("ws-1").offset(-1).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("offset");

        // the documented boundary values are accepted
        assertThat(client().guardrailPolicies().listBudgetPolicies()
                .workspaceId("ws-1").offset(0).limit(1).build().getRelativeUrl())
                .isEqualTo("/guardrails/policies/budget?offset=0&limit=1&workspace_id=ws-1");
        assertThat(client().guardrailPolicies().listBudgetPolicies()
                .workspaceId("ws-1").limit(100).build().getRelativeUrl())
                .contains("limit=100");
    }

    @Test
    void byIdRoutesUrlEncodeThePathSegmentAndRejectDotSegments() {
        assertThat(client().guardrailPolicies().getBudgetPolicy("p1").workspaceId("ws-1").build()
                .getRelativeUrl()).isEqualTo("/guardrails/policies/budget/p1?workspace_id=ws-1");
        assertThat(client().guardrailPolicies().deleteBudgetPolicy("p1").workspaceId("ws-1").build()
                .getHttpMethod()).isEqualTo("DELETE");
        assertThat(client().guardrailPolicies().deleteAccountPolicy("p1").build().getRelativeUrl())
                .isEqualTo("/guardrails/policies/account/p1");
        assertThat(client().guardrailPolicies().getModelAvailabilityPolicy("p1").workspaceId("ws-1").build()
                .getRelativeUrl()).isEqualTo("/guardrails/policies/model-availability/p1?workspace_id=ws-1");
        assertThat(client().guardrailPolicies().deleteModelAvailabilityPolicy("p1").workspaceId("ws-1").build()
                .getRelativeUrl()).isEqualTo("/guardrails/policies/model-availability/p1?workspace_id=ws-1");
        assertThat(client().guardrailPolicies().getModerationPolicy("p1").workspaceId("ws-1").build()
                .getRelativeUrl()).isEqualTo("/guardrails/policies/moderation/p1?workspace_id=ws-1");
        assertThat(client().guardrailPolicies().deleteModerationPolicy("p1").workspaceId("ws-1").build()
                .getRelativeUrl()).isEqualTo("/guardrails/policies/moderation/p1?workspace_id=ws-1");
        assertThat(client().guardrailPolicies().updateAccountPolicy("p1")
                .policy(OpenRouterGuardrailPolicyRequest.budget("N", budgetConfig()).build()).build()
                .getRelativeUrl())
                .isEqualTo("/guardrails/policies/account/p1");
        assertThat(client().guardrailPolicies().updateAccountPolicy("p1")
                .policy(OpenRouterGuardrailPolicyRequest.budget("N", budgetConfig()).build()).build()
                .getHttpMethod())
                .isEqualTo("PUT");

        assertThat(client().guardrailPolicies().getBudgetPolicy("id/with+slash").workspaceId("ws-1").build()
                .getRelativeUrl()).contains("id%2Fwith%2Bslash");

        assertThatThrownBy(() -> client().guardrailPolicies().getBudgetPolicy("..").workspaceId("ws-1").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be addressed by path");
        assertThatThrownBy(() -> client().guardrailPolicies().deleteModerationPolicy(".").workspaceId("ws-1").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be addressed by path");
        assertThatThrownBy(() -> client().guardrailPolicies().getBudgetPolicy(null).workspaceId("ws-1").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("id");
    }

    @Test
    void assignmentRoutesCarryTheSubjectSegment() {
        assertThat(client().guardrailPolicies().apiKeyPolicyAssignment("key hash")
                .workspaceId("ws-1").policyType("budget").build().getRelativeUrl())
                .isEqualTo("/guardrails/policies/assignments/api-keys/key+hash?workspace_id=ws-1&policy_type=budget");
        assertThat(client().guardrailPolicies().apiKeyPolicyAssignment("h").workspaceId("ws-1")
                .policyType("budget").build().getHttpMethod()).isEqualTo("GET");
        assertThat(client().guardrailPolicies().changeApiKeyPolicyAssignment("h").workspaceId("ws-1")
                .policyType("budget").policyId("p1").assigned(true)
                .expectedAssignment(OpenRouterGuardrailPolicyAssignmentState.of(List.of(), List.of(), false))
                .build().getHttpMethod()).isEqualTo("PUT");
        assertThat(client().guardrailPolicies().memberPolicyAssignment("user_2abc")
                .workspaceId("ws-1").policyType("moderation").build().getRelativeUrl())
                .isEqualTo("/guardrails/policies/assignments/members/user_2abc?workspace_id=ws-1&policy_type=moderation");
        assertThat(client().guardrailPolicies().changeMemberPolicyAssignment("user_2abc").workspaceId("ws-1")
                .policyType("moderation").policyId("p1").assigned(true)
                .expectedAssignment(OpenRouterGuardrailPolicyAssignmentState.of(List.of(), List.of(), false))
                .build()
                .getRelativeUrl())
                .isEqualTo("/guardrails/policies/assignments/members/user_2abc");

        assertThatThrownBy(() -> client().guardrailPolicies().apiKeyPolicyAssignment("h")
                .policyType("budget").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("workspace_id");
        assertThatThrownBy(() -> client().guardrailPolicies().apiKeyPolicyAssignment("h")
                .workspaceId("ws-1").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("policy_type");
        assertThatThrownBy(() -> client().guardrailPolicies().memberPolicyAssignment("..").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be addressed by path");
    }

    // ------------------------------------------------------------------
    // Account policy request value (discriminated union)
    // ------------------------------------------------------------------

    @Test
    void accountRequestFactoriesEmitTheDiscriminatorAndConfig() {
        JSONObject json = OpenRouterGuardrailPolicyRequest
                .budget("Engineering monthly budget", budgetConfig())
                .build().toJson();
        assertThat(json.getString("name")).isEqualTo("Engineering monthly budget");
        assertThat(json.getString("policy_type")).isEqualTo("budget");
        assertThat(json.getJSONObject("config").getDouble("limit_usd")).isEqualTo(200.0);
        assertThat(json.getJSONObject("config").getString("reset_interval")).isEqualTo("monthly");
        assertThat(json.getJSONObject("config").getBoolean("include_byok_in_budgets")).isFalse();
        assertThat(json.has("workspace_ids")).isFalse();

        assertThat(OpenRouterGuardrailPolicyRequest
                .modelAvailability("Approved models",
                        OpenRouterGuardrailModelAvailabilityPolicyConfig.builder().build())
                .build().toJson().getString("policy_type")).isEqualTo("model_availability");
        assertThat(OpenRouterGuardrailPolicyRequest
                .moderation("Block secrets",
                        OpenRouterGuardrailModerationPolicyConfig.builder().build())
                .build().toJson().getString("policy_type")).isEqualTo("moderation");
    }

    @Test
    void accountRequestEnvelopeFieldsAreEmittedOnlyWhenSet() {
        JSONObject json = OpenRouterGuardrailPolicyRequest
                .budget("N", budgetConfig())
                .workspaceIds(List.of("ws-1", "ws-2"))
                .optedOutWorkspaceIds(List.of("ws-3"))
                .releasedFloorWorkspaceIds(List.of("ws-4"))
                .isDefault(false)
                .build().toJson();

        assertThat(json.getJSONArray("workspace_ids").length()).isEqualTo(2);
        assertThat(json.getJSONArray("opted_out_workspace_ids").getString(0)).isEqualTo("ws-3");
        assertThat(json.getJSONArray("released_floor_workspace_ids").getString(0)).isEqualTo("ws-4");
        assertThat(json.getBoolean("is_default")).isFalse();

        JSONObject full = OpenRouterGuardrailPolicyRequest
                .budget("N", budgetConfig())
                .floorReassignment("other-policy", List.of("ws-9"))
                .keepPreviousDefaultWorkspaces(true)
                .build().toJson();
        assertThat(full.getJSONObject("floor_reassignment").getString("policy_id")).isEqualTo("other-policy");
        assertThat(full.getJSONObject("floor_reassignment").getJSONArray("workspace_ids").getString(0))
                .isEqualTo("ws-9");
        assertThat(full.getBoolean("keep_previous_default_workspaces")).isTrue();
    }

    @Test
    void accountRequestValidatesNameAndTheEnvelopeRules() {
        assertThatThrownBy(() -> OpenRouterGuardrailPolicyRequest.budget(null, budgetConfig()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
        assertThatThrownBy(() -> OpenRouterGuardrailPolicyRequest.budget("x".repeat(256), budgetConfig()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("255");
        assertThatThrownBy(() -> OpenRouterGuardrailPolicyRequest.budget("N", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("config");
        assertThatThrownBy(() -> OpenRouterGuardrailPolicyRequest
                .budget("N", budgetConfig())
                .keepPreviousDefaultWorkspaces(true)
                .releasedFloorWorkspaceIds(List.of("ws-1"))
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be combined");
        assertThatThrownBy(() -> OpenRouterGuardrailPolicyRequest
                .budget("N", budgetConfig()).floorReassignment("p", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void accountRequestRawHatchPassesVerbatimAndRequiresTheCoreKeys() {
        JSONObject source = new JSONObject()
                .put("name", "N")
                .put("policy_type", "budget")
                .put("config", new JSONObject().put("limit_usd", 5))
                .put("is_default", true);
        OpenRouterGuardrailPolicyRequest request = OpenRouterGuardrailPolicyRequest.raw(source);
        source.put("name", "changed");

        JSONObject json = request.toJson();
        assertThat(json.getString("name")).isEqualTo("N");
        assertThat(json.getBoolean("is_default")).isTrue();
        assertThat(request.name()).isNull();
        assertThat(request.policyType()).isNull();

        assertThatThrownBy(() -> OpenRouterGuardrailPolicyRequest.raw(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterGuardrailPolicyRequest.raw(new JSONObject().put("name", "N")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("policy_type");
    }

    // ------------------------------------------------------------------
    // Authoring configs
    // ------------------------------------------------------------------

    @Test
    void budgetConfigRequiresAllThreeFieldsAndEmitsNullsVerbatim() {
        assertThatThrownBy(() -> OpenRouterGuardrailBudgetPolicyConfig.builder()
                .limitUsd(10.0).resetInterval("daily").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("include_byok_in_budgets");
        assertThatThrownBy(() -> OpenRouterGuardrailBudgetPolicyConfig.builder()
                .resetInterval("daily").includeByokInBudgets(true).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("limit_usd");
        assertThatThrownBy(() -> OpenRouterGuardrailBudgetPolicyConfig.builder()
                .limitUsd(10.0).includeByokInBudgets(true).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("reset_interval");
        assertThatThrownBy(() -> OpenRouterGuardrailBudgetPolicyConfig.builder().limitUsd(0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("greater than 0");

        JSONObject json = OpenRouterGuardrailBudgetPolicyConfig.builder()
                .limitUsd(null)
                .resetInterval(null)
                .includeByokInBudgets(true)
                .build().toJson();
        assertThat(json.keySet()).containsExactlyInAnyOrder("limit_usd", "reset_interval", "include_byok_in_budgets");
        assertThat(json.isNull("limit_usd")).isTrue();
        assertThat(json.isNull("reset_interval")).isTrue();
        assertThat(json.getBoolean("include_byok_in_budgets")).isTrue();

        JSONObject unknownInterval = OpenRouterGuardrailBudgetPolicyConfig.builder()
                .limitUsd(1.0).resetInterval("fortnightly").includeByokInBudgets(false).build().toJson();
        assertThat(unknownInterval.getString("reset_interval")).isEqualTo("fortnightly");
    }

    @Test
    void modelAvailabilityConfigAllTypedSettersLandOnTheirWireKeys() {
        JSONObject json = OpenRouterGuardrailModelAvailabilityPolicyConfig.builder()
                .allowedModels(List.of("m1"))
                .ignoredModels(List.of("m2"))
                .allowedProviders(List.of("p1"))
                .ignoredProviders(List.of("p2"))
                .allowedDataRegions(List.of("europe"))
                .requireEuDataRegion(true)
                .enforceZdr(true)
                .enforceZdrAnthropic(true)
                .enforceZdrGoogle(true)
                .enforceZdrOpenai(true)
                .enforceZdrXai(true)
                .enforceZdrOther(true)
                .allowSafetyRetentionGoogle(true)
                .allowSafetyRetentionOther(true)
                .enforceFirstPartyHarnessModels(true)
                .enableFreeModelTraining(true)
                .enableFreeModelPublication(true)
                .enablePaidModelTraining(true)
                .platformBannedAuthors(List.of("a1"))
                .platformBannedModels(List.of("mm1"))
                .platformBannedProviders(List.of("pp1"))
                .build().toJson();

        assertThat(json.keySet()).containsExactlyInAnyOrder(
                "allowed_models", "ignored_models", "allowed_providers", "ignored_providers",
                "allowed_data_regions", "require_eu_data_region",
                "enforce_zdr", "enforce_zdr_anthropic", "enforce_zdr_google", "enforce_zdr_openai",
                "enforce_zdr_xai", "enforce_zdr_other",
                "allow_safety_retention_google", "allow_safety_retention_other",
                "enforce_first_party_harness_models",
                "enable_free_model_training", "enable_free_model_publication", "enable_paid_model_training",
                "platform_banned_authors", "platform_banned_models", "platform_banned_providers");
        assertThat(json.getJSONArray("allowed_models").getString(0)).isEqualTo("m1");
        assertThat(json.getJSONArray("ignored_models").getString(0)).isEqualTo("m2");
        assertThat(json.getJSONArray("allowed_providers").getString(0)).isEqualTo("p1");
        assertThat(json.getJSONArray("allowed_data_regions").getString(0)).isEqualTo("europe");
        assertThat(json.getBoolean("require_eu_data_region")).isTrue();
        assertThat(json.getBoolean("enforce_zdr_anthropic")).isTrue();
        assertThat(json.getBoolean("enforce_zdr_google")).isTrue();
        assertThat(json.getBoolean("enforce_zdr_openai")).isTrue();
        assertThat(json.getBoolean("enforce_zdr_xai")).isTrue();
        assertThat(json.getBoolean("enforce_zdr_other")).isTrue();
        assertThat(json.getBoolean("allow_safety_retention_other")).isTrue();
        assertThat(json.getBoolean("enforce_first_party_harness_models")).isTrue();
        assertThat(json.getBoolean("enable_free_model_training")).isTrue();
        assertThat(json.getBoolean("enable_free_model_publication")).isTrue();
        assertThat(json.getBoolean("enable_paid_model_training")).isTrue();
        assertThat(json.getJSONArray("platform_banned_authors").getString(0)).isEqualTo("a1");
        assertThat(json.getJSONArray("platform_banned_models").getString(0)).isEqualTo("mm1");
        assertThat(json.getJSONArray("platform_banned_providers").getString(0)).isEqualTo("pp1");

        // a second set-then-null leg beyond allowed_models
        JSONObject unset = OpenRouterGuardrailModelAvailabilityPolicyConfig.builder()
                .enforceZdr(true)
                .enforceZdr(null)
                .allowedDataRegions(List.of("europe"))
                .allowedDataRegions(null)
                .build().toJson();
        assertThat(unset.keySet()).isEmpty();

        assertThatThrownBy(() -> OpenRouterGuardrailModelAvailabilityPolicyConfig.builder()
                .allowedDataRegions(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one");
    }

    @Test
    void modelAvailabilityConfigEmitsOnlySetFields() {
        JSONObject empty = OpenRouterGuardrailModelAvailabilityPolicyConfig.builder().build().toJson();
        assertThat(empty.keySet()).isEmpty();

        JSONObject json = OpenRouterGuardrailModelAvailabilityPolicyConfig.builder()
                .allowedModels(List.of("openai/gpt-4o"))
                .ignoredProviders(List.of("acme"))
                .enforceZdr(true)
                .allowSafetyRetentionGoogle(false)
                .requireEuDataRegion(true)
                .allowedModels(null)
                .build().toJson();
        assertThat(json.has("allowed_models")).isFalse();
        assertThat(json.keySet()).containsExactlyInAnyOrder(
                "ignored_providers", "enforce_zdr", "allow_safety_retention_google", "require_eu_data_region");
        assertThat(json.getJSONArray("ignored_providers").getString(0)).isEqualTo("acme");
        assertThat(json.getBoolean("enforce_zdr")).isTrue();
        assertThat(json.getBoolean("allow_safety_retention_google")).isFalse();
    }

    @Test
    void moderationConfigEmitsTypedEntriesAndRawArmor() {
        JSONObject json = OpenRouterGuardrailModerationPolicyConfig.builder()
                .addContentFilterBuiltin("secrets", "block")
                .addContentFilterBuiltin("email", "redact", "Email", "user_only")
                .addContentFilter("pass(word)?", "flag", "Password")
                .addPiiFilter("email_address", "redact", 0.5)
                .contentFilterPatterns(List.of("regex-1"))
                .contentFilterBlocksRequests(true)
                .modelArmorInjectionProtectionMode("block")
                .armor(new JSONObject().put("v", 1))
                .build().toJson();

        assertThat(json.getJSONArray("content_filter_builtins").length()).isEqualTo(2);
        JSONObject builtin = json.getJSONArray("content_filter_builtins").getJSONObject(0);
        assertThat(builtin.keySet()).containsExactlyInAnyOrder("slug", "action");
        JSONObject fullBuiltin = json.getJSONArray("content_filter_builtins").getJSONObject(1);
        assertThat(fullBuiltin.getString("scan_scope")).isEqualTo("user_only");
        assertThat(fullBuiltin.getString("label")).isEqualTo("Email");

        JSONObject filter = json.getJSONArray("content_filters").getJSONObject(0);
        assertThat(filter.getString("pattern")).isEqualTo("pass(word)?");
        assertThat(filter.getString("action")).isEqualTo("flag");
        assertThat(filter.getString("label")).isEqualTo("Password");

        JSONObject pii = json.getJSONArray("pii_filters").getJSONObject(0);
        assertThat(pii.getString("entity_type")).isEqualTo("email_address");
        assertThat(pii.getDouble("score_threshold")).isEqualTo(0.5);

        assertThat(json.getJSONArray("content_filter_patterns").getString(0)).isEqualTo("regex-1");
        assertThat(json.getBoolean("content_filter_blocks_requests")).isTrue();
        assertThat(json.getString("model_armor_injection_protection_mode")).isEqualTo("block");
        assertThat(json.getJSONObject("armor").getInt("v")).isEqualTo(1);
        assertThat(json.has("platform_moderated_providers")).isFalse();
    }

    @Test
    void moderationEntryRawHatchesPassEntriesVerbatim() {
        JSONObject json = OpenRouterGuardrailModerationPolicyConfig.builder()
                .addContentFilterBuiltinRaw(new JSONObject().put("slug", "future-slug").put("action", "flag")
                        .put("unknown_entry_key", 1))
                .addContentFilterRaw(new JSONObject().put("pattern", "raw-pattern").put("action", "block")
                        .put("label", "raw"))
                .addContentFilter("simple-pattern", "flag")
                .addPiiFilterRaw(new JSONObject().put("entity_type", "custom").put("action", "block"))
                .enableModelArmorInjectionProtection(true)
                .modelArmorInjectionConfidenceLevel("high")
                .lakeraInjectionProtectionMode("flag")
                .broadcastModelArmorFlaggedRequests(true)
                .platformImageModeratedProviders(List.of("provider-1"))
                .build().toJson();

        assertThat(json.getJSONArray("content_filter_builtins").getJSONObject(0).getInt("unknown_entry_key"))
                .isEqualTo(1);
        assertThat(json.getJSONArray("content_filters").getJSONObject(0).getString("pattern"))
                .isEqualTo("raw-pattern");
        assertThat(json.getJSONArray("content_filters").getJSONObject(0).getString("label"))
                .isEqualTo("raw");
        assertThat(json.getJSONArray("content_filters").getJSONObject(1).getString("pattern"))
                .isEqualTo("simple-pattern");
        assertThat(json.getJSONArray("content_filters").getJSONObject(1).has("label")).isFalse();
        assertThat(json.getJSONArray("pii_filters").getJSONObject(0).getString("entity_type"))
                .isEqualTo("custom");
        assertThat(json.getBoolean("enable_model_armor_injection_protection")).isTrue();
        assertThat(json.getString("model_armor_injection_confidence_level")).isEqualTo("high");
        assertThat(json.getString("lakera_injection_protection_mode")).isEqualTo("flag");
        assertThat(json.getBoolean("broadcast_model_armor_flagged_requests")).isTrue();
        assertThat(json.getJSONArray("platform_image_moderated_providers").getString(0))
                .isEqualTo("provider-1");

        assertThatThrownBy(() -> OpenRouterGuardrailModerationPolicyConfig.builder()
                .addContentFilter("", "block"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pattern");
        assertThatThrownBy(() -> OpenRouterGuardrailModerationPolicyConfig.builder()
                .addContentFilterBuiltin("secrets", "block", "x".repeat(101), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("label");
        assertThatThrownBy(() -> OpenRouterGuardrailModerationPolicyConfig.builder()
                .addPiiFilter("email_address", "redact", 1.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("scoreThreshold");
        assertThatThrownBy(() -> OpenRouterGuardrailModerationPolicyConfig.builder()
                .addContentFilterRaw(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ------------------------------------------------------------------
    // Typed create / update bodies
    // ------------------------------------------------------------------

    @Test
    void typedCreateEmitsRequiredAndOptionalBodyFields() {
        OpenRouterGuardrailBudgetPolicyCreateRequest minimal = client().guardrailPolicies()
                .createBudgetPolicy()
                .name("Budget A")
                .config(budgetConfig())
                .workspaceId("ws-1")
                .build();
        JSONObject body = new JSONObject(minimal.getBody());
        assertThat(body.getString("name")).isEqualTo("Budget A");
        assertThat(body.getString("policy_type")).isEqualTo("budget");
        assertThat(body.getString("workspace_id")).isEqualTo("ws-1");
        assertThat(body.getJSONObject("config").getDouble("limit_usd")).isEqualTo(200.0);
        assertThat(body.has("api_key_hashes")).isFalse();
        assertThat(body.has("member_ids")).isFalse();
        assertThat(body.has("is_workspace_default")).isFalse();

        JSONObject full = new JSONObject(client().guardrailPolicies()
                .createModerationPolicy()
                .name("Moderation A")
                .config(OpenRouterGuardrailModerationPolicyConfig.builder().build())
                .workspaceId("ws-1")
                .addApiKeyHash("hash-1")
                .memberIds(List.of("user_a", "user_b"))
                .isWorkspaceDefault(false)
                .optedOutApiKeyHashes(List.of("hash-2"))
                .optedOutMemberIds(List.of("user_c"))
                .build().getBody());
        assertThat(full.getString("policy_type")).isEqualTo("moderation");
        assertThat(full.getJSONArray("api_key_hashes").getString(0)).isEqualTo("hash-1");
        assertThat(full.getJSONArray("member_ids").length()).isEqualTo(2);
        assertThat(full.getBoolean("is_workspace_default")).isFalse();
        assertThat(full.getJSONArray("opted_out_api_key_hashes").getString(0)).isEqualTo("hash-2");
        assertThat(full.getJSONArray("opted_out_member_ids").getString(0)).isEqualTo("user_c");
    }

    @Test
    void typedCreateValidatesTheRequiredFields() {
        assertThatThrownBy(() -> client().guardrailPolicies().createBudgetPolicy()
                .config(budgetConfig()).workspaceId("ws-1").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("name");
        assertThatThrownBy(() -> client().guardrailPolicies().createBudgetPolicy()
                .name("N").workspaceId("ws-1").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("config");
        assertThatThrownBy(() -> client().guardrailPolicies().createBudgetPolicy()
                .name("N").config(budgetConfig()).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("workspace_id");
        assertThatThrownBy(() -> client().guardrailPolicies().createBudgetPolicy()
                .name("x".repeat(256)).config(budgetConfig()).workspaceId("ws-1").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("255");
    }

    @Test
    void typedUpdateIsAFullReplaceAndEnforcesTheAssignmentPair() {
        OpenRouterGuardrailBudgetPolicyUpdateRequest minimal = client().guardrailPolicies()
                .updateBudgetPolicy("p1")
                .name("Budget A renamed")
                .config(budgetConfig())
                .workspaceId("ws-1")
                .build();
        JSONObject body = new JSONObject(minimal.getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder("name", "config", "workspace_id");

        JSONObject full = new JSONObject(client().guardrailPolicies()
                .updateBudgetPolicy("p1")
                .name("Budget A renamed")
                .config(budgetConfig())
                .workspaceId("ws-1")
                .apiKeyHashes(List.of("h1"))
                .memberIds(List.of("u1"))
                .isWorkspaceDefault(false)
                .optedOutApiKeyHashes(List.of("h2"))
                .optedOutMemberIds(List.of("u2"))
                .build().getBody());
        assertThat(full.keySet()).containsExactlyInAnyOrder("name", "config", "workspace_id",
                "api_key_hashes", "member_ids", "is_workspace_default",
                "opted_out_api_key_hashes", "opted_out_member_ids");
        assertThat(full.getBoolean("is_workspace_default")).isFalse();

        // the PUT is a full replace: name, config and workspace_id are all required
        assertThatThrownBy(() -> client().guardrailPolicies().updateBudgetPolicy("p1").name("N").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("config");
        assertThatThrownBy(() -> client().guardrailPolicies().updateBudgetPolicy("p1")
                .name("N").config(budgetConfig()).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("workspace_id");

        assertThatThrownBy(() -> client().guardrailPolicies().updateBudgetPolicy("p1")
                .name("N").config(budgetConfig()).workspaceId("ws-1")
                .apiKeyHashes(List.of("h1")).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sent together");
        assertThatThrownBy(() -> client().guardrailPolicies().updateModelAvailabilityPolicy("p1")
                .name("N").config(OpenRouterGuardrailModelAvailabilityPolicyConfig.builder().build())
                .workspaceId("ws-1").memberIds(List.of("u1")).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sent together");
        assertThatThrownBy(() -> client().guardrailPolicies().updateModerationPolicy("p1")
                .name("N").config(OpenRouterGuardrailModerationPolicyConfig.builder().build())
                .workspaceId("ws-1").apiKeyHashes(List.of("h1")).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sent together");
        assertThatThrownBy(() -> client().guardrailPolicies().updateModelAvailabilityPolicy("p1").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("name");
        assertThatThrownBy(() -> client().guardrailPolicies().updateModerationPolicy("..").name("N").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be addressed by path");
    }

    @Test
    void modelAvailabilityAndModerationUpdateBodiesCarryEveryField() {
        JSONObject maFull = new JSONObject(client().guardrailPolicies()
                .updateModelAvailabilityPolicy("p1")
                .name("MA renamed")
                .config(OpenRouterGuardrailModelAvailabilityPolicyConfig.builder()
                        .allowedModels(List.of("openai/gpt-4o")).build())
                .workspaceId("ws-1")
                .apiKeyHashes(List.of("h1"))
                .memberIds(List.of("u1"))
                .isWorkspaceDefault(false)
                .optedOutApiKeyHashes(List.of("h2"))
                .optedOutMemberIds(List.of("u2"))
                .build().getBody());
        assertThat(maFull.keySet()).containsExactlyInAnyOrder("name", "config", "workspace_id",
                "api_key_hashes", "member_ids", "is_workspace_default",
                "opted_out_api_key_hashes", "opted_out_member_ids");
        assertThat(maFull.getJSONObject("config").getJSONArray("allowed_models").getString(0))
                .isEqualTo("openai/gpt-4o");

        JSONObject modMinimal = new JSONObject(client().guardrailPolicies()
                .updateModerationPolicy("p1")
                .name("Mod renamed")
                .config(OpenRouterGuardrailModerationPolicyConfig.builder().build())
                .workspaceId("ws-1")
                .build().getBody());
        assertThat(modMinimal.keySet()).containsExactlyInAnyOrder("name", "config", "workspace_id");
    }

    @Test
    void modelAvailabilityCreateBodyIsEmittedPerClass() {
        JSONObject minimal = new JSONObject(client().guardrailPolicies()
                .createModelAvailabilityPolicy()
                .name("MA")
                .config(OpenRouterGuardrailModelAvailabilityPolicyConfig.builder()
                        .allowedModels(List.of("openai/gpt-4o")).build())
                .workspaceId("ws-1")
                .build().getBody());
        assertThat(minimal.keySet()).containsExactlyInAnyOrder("name", "policy_type", "config", "workspace_id");
        assertThat(minimal.getString("policy_type")).isEqualTo("model_availability");
        assertThat(minimal.getJSONObject("config").getJSONArray("allowed_models").getString(0))
                .isEqualTo("openai/gpt-4o");
        assertThat(minimal.has("is_workspace_default")).isFalse();

        JSONObject full = new JSONObject(client().guardrailPolicies()
                .createModelAvailabilityPolicy()
                .name("MA")
                .config(OpenRouterGuardrailModelAvailabilityPolicyConfig.builder().build())
                .workspaceId("ws-1")
                .apiKeyHashes(List.of("h1"))
                .memberIds(List.of("u1"))
                .isWorkspaceDefault(true)
                .optedOutApiKeyHashes(List.of("h2"))
                .optedOutMemberIds(List.of("u2"))
                .build().getBody());
        assertThat(full.keySet()).containsExactlyInAnyOrder("name", "policy_type", "config", "workspace_id",
                "api_key_hashes", "member_ids", "is_workspace_default",
                "opted_out_api_key_hashes", "opted_out_member_ids");
    }

    // ------------------------------------------------------------------
    // Account create / update bodies
    // ------------------------------------------------------------------

    @Test
    void accountCreateAndUpdateCarryTheUnionBodyVerbatim() {
        OpenRouterGuardrailPolicyRequest policy = OpenRouterGuardrailPolicyRequest
                .budget("Account budget", budgetConfig())
                .workspaceIds(List.of("ws-1"))
                .build();

        OpenRouterGuardrailAccountPolicyCreateRequest create = client().guardrailPolicies()
                .createAccountPolicy().policy(policy).build();
        assertThat(create.getRelativeUrl()).isEqualTo("/guardrails/policies/account");
        assertThat(create.getHttpMethod()).isEqualTo("POST");
        JSONObject body = new JSONObject(create.getBody());
        assertThat(body.getString("policy_type")).isEqualTo("budget");
        assertThat(body.getJSONArray("workspace_ids").getString(0)).isEqualTo("ws-1");

        OpenRouterGuardrailAccountPolicyUpdateRequest update = client().guardrailPolicies()
                .updateAccountPolicy("p1").policy(policy).build();
        assertThat(new JSONObject(update.getBody()).getString("name")).isEqualTo("Account budget");

        assertThatThrownBy(() -> client().guardrailPolicies().createAccountPolicy().build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("policy");
    }

    // ------------------------------------------------------------------
    // Assignment bodies
    // ------------------------------------------------------------------

    @Test
    void assignmentChangeCarriesTheExpectedAssignmentContract() {
        OpenRouterGuardrailPolicyAssignmentState expected =
                OpenRouterGuardrailPolicyAssignmentState.of(List.of("policy-1"), List.of(), false);

        OpenRouterGuardrailApiKeyPolicyAssignmentChangeRequest request = client().guardrailPolicies()
                .changeApiKeyPolicyAssignment("key-hash-1")
                .workspaceId("ws-1")
                .policyType("rate_limit")
                .policyId("policy-1")
                .assigned(true)
                .expectedAssignment(expected)
                .build();

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder(
                "workspace_id", "policy_type", "policy_id", "assigned", "expected_assignment");
        assertThat(body.getString("policy_type")).isEqualTo("rate_limit");
        assertThat(body.getBoolean("assigned")).isTrue();
        JSONObject state = body.getJSONObject("expected_assignment");
        assertThat(state.getJSONArray("policy_ids").getString(0)).isEqualTo("policy-1");
        assertThat(state.getJSONArray("workspace_default_policy_ids").length()).isEqualTo(0);
        assertThat(state.getBoolean("opted_out")).isFalse();

        JSONObject inherited = new JSONObject(client().guardrailPolicies()
                .changeApiKeyPolicyAssignment("key-hash-1")
                .workspaceId("ws-1")
                .policyType("budget")
                .policyId("policy-1")
                .assigned(true)
                .expectedAssignment(OpenRouterGuardrailPolicyAssignmentState.of(
                        List.of(), List.of("inherited-policy"), true))
                .build().getBody()).getJSONObject("expected_assignment");
        assertThat(inherited.getJSONArray("workspace_default_policy_ids").getString(0))
                .isEqualTo("inherited-policy");
        assertThat(inherited.getBoolean("opted_out")).isTrue();

        assertThatThrownBy(() -> client().guardrailPolicies().changeMemberPolicyAssignment("u1")
                .workspaceId("ws-1").policyType("budget").policyId("p1").assigned(true).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expected_assignment");
        assertThatThrownBy(() -> client().guardrailPolicies().changeMemberPolicyAssignment("u1")
                .workspaceId("ws-1").policyType("budget").policyId("p1").expectedAssignment(expected).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("assigned");
        assertThatThrownBy(() -> client().guardrailPolicies().changeApiKeyPolicyAssignment("h")
                .workspaceId("ws-1").policyType("budget").assigned(true).expectedAssignment(expected).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("policy_id");
    }

    // ------------------------------------------------------------------
    // Response views
    // ------------------------------------------------------------------

    @Test
    void summaryAndDetailViewsExposeEveryField() {
        OpenRouterGuardrailPolicySummary summary = new OpenRouterGuardrailPolicySummary(new JSONObject("""
                {
                  "id": "p1", "name": "Budget A", "policy_type": "budget",
                  "config": {"limit_usd": 50},
                  "updated_at": "2025-08-24T10:30:00Z",
                  "member_count": 3, "api_key_count": 2,
                  "is_workspace_default": true, "is_legacy_mapped": false,
                  "legacy_guardrail_ids": ["g1"], "replaced_legacy_guardrail_ids": ["g2"]
                }
                """));
        assertThat(summary.id()).isEqualTo("p1");
        assertThat(summary.name()).isEqualTo("Budget A");
        assertThat(summary.policyType()).isEqualTo("budget");
        assertThat(summary.config().getDouble("limit_usd")).isEqualTo(50.0);
        assertThat(summary.updatedAt()).isEqualTo("2025-08-24T10:30:00Z");
        assertThat(summary.memberCount()).isEqualTo(3);
        assertThat(summary.apiKeyCount()).isEqualTo(2);
        assertThat(summary.isWorkspaceDefault()).isTrue();
        assertThat(summary.isLegacyMapped()).isFalse();
        assertThat(summary.legacyGuardrailIds()).containsExactly("g1");
        assertThat(summary.replacedLegacyGuardrailIds()).containsExactly("g2");

        OpenRouterGuardrailPolicyDetail detail = new OpenRouterGuardrailPolicyDetail(new JSONObject("""
                {
                  "id": "p1", "name": "Budget A", "policy_type": "budget", "config": {},
                  "updated_at": null,
                  "member_ids": ["u1"], "api_key_hashes": ["h1"],
                  "opted_out_member_ids": ["u2"], "opted_out_api_key_hashes": ["h2"],
                  "overridden_member_ids": ["u3"], "overridden_api_key_hashes": ["h3"],
                  "is_workspace_default": false, "reference": "legacy_mapping",
                  "replaces_legacy_default": true, "is_account_policy": false
                }
                """));
        assertThat(detail.updatedAt()).isNull();
        assertThat(detail.memberIds()).containsExactly("u1");
        assertThat(detail.apiKeyHashes()).containsExactly("h1");
        assertThat(detail.optedOutMemberIds()).containsExactly("u2");
        assertThat(detail.optedOutApiKeyHashes()).containsExactly("h2");
        assertThat(detail.overriddenMemberIds()).containsExactly("u3");
        assertThat(detail.overriddenApiKeyHashes()).containsExactly("h3");
        assertThat(detail.isWorkspaceDefault()).isFalse();
        assertThat(detail.reference()).isEqualTo("legacy_mapping");
        assertThat(detail.replacesLegacyDefault()).isTrue();
        assertThat(detail.isAccountPolicy()).isFalse();
    }

    @Test
    void accountPolicyAndAssignmentStateViewsExposeEveryField() {
        OpenRouterGuardrailAccountPolicy policy = new OpenRouterGuardrailAccountPolicy(new JSONObject("""
                {
                  "id": "p1", "name": "Account budget", "policy_type": "budget", "config": {},
                  "created_at": "2025-08-24T10:30:00Z", "updated_at": null,
                  "is_default": true, "workspace_count": 2,
                  "workspace_ids": ["ws-1"], "opted_out_workspace_ids": ["ws-2"],
                  "is_synced": false, "is_workspace_default_synced": true
                }
                """));
        assertThat(policy.createdAt()).isEqualTo("2025-08-24T10:30:00Z");
        assertThat(policy.updatedAt()).isNull();
        assertThat(policy.isDefault()).isTrue();
        assertThat(policy.workspaceCount()).isEqualTo(2);
        assertThat(policy.workspaceIds()).containsExactly("ws-1");
        assertThat(policy.optedOutWorkspaceIds()).containsExactly("ws-2");
        assertThat(policy.isSynced()).isFalse();
        assertThat(policy.isWorkspaceDefaultSynced()).isTrue();

        OpenRouterGuardrailPolicyAssignmentState state = new OpenRouterGuardrailPolicyAssignmentState(new JSONObject("""
                {"policy_ids": ["p1"], "workspace_default_policy_ids": ["p2"], "opted_out": true}
                """));
        assertThat(state.policyIds()).containsExactly("p1");
        assertThat(state.workspaceDefaultPolicyIds()).containsExactly("p2");
        assertThat(state.optedOut()).isTrue();

        OpenRouterGuardrailPolicyAssignmentState empty = new OpenRouterGuardrailPolicyAssignmentState(null);
        assertThat(empty.policyIds()).isEmpty();
        assertThat(empty.workspaceDefaultPolicyIds()).isEmpty();
        assertThat(empty.optedOut()).isNull();
    }

    @Test
    void viewsSwallowMalformedValues() {
        OpenRouterGuardrailPolicySummary malformed = new OpenRouterGuardrailPolicySummary(new JSONObject("""
                {"id": 42, "name": false, "policy_type": {}, "config": "none",
                 "member_count": "three", "is_workspace_default": "yes",
                 "legacy_guardrail_ids": [1, "g1", {}]}
                """));
        assertThat(malformed.id()).isNull();
        assertThat(malformed.name()).isNull();
        assertThat(malformed.policyType()).isNull();
        assertThat(malformed.config()).isNull();
        assertThat(malformed.memberCount()).isNull();
        assertThat(malformed.isWorkspaceDefault()).isNull();
        assertThat(malformed.legacyGuardrailIds()).containsExactly("g1");

        OpenRouterGuardrailPolicy nullRow = new OpenRouterGuardrailPolicy(null);
        assertThat(nullRow.id()).isNull();
        assertThat(nullRow.config()).isNull();
        assertThat(nullRow.json().keySet()).isEmpty();
    }

    @Test
    void responseWrappersTolerateEmptyAndMalformedBodies() {
        OpenRouterGuardrailBudgetPolicyDeleteRequest deleteRequest = client().guardrailPolicies()
                .deleteBudgetPolicy("p1").workspaceId("ws-1").build();
        assertThat(deleteRequest.createResponse("{\"data\": true}").deleted()).isTrue();
        assertThat(deleteRequest.createResponse("{}").deleted()).isNull();
        assertThat(deleteRequest.createResponse("{\"data\": \"yes\"}").deleted()).isNull();

        OpenRouterGuardrailBudgetPolicyGetRequest getRequest = client().guardrailPolicies()
                .getBudgetPolicy("p1").workspaceId("ws-1").build();
        assertThat(getRequest.createResponse("{\"data\": {\"id\": \"p1\"}}").data().id()).isEqualTo("p1");
        assertThat(getRequest.createResponse("{}").data()).isNull();
        assertThat(getRequest.createResponse("{\"data\": 7}").data()).isNull();

        OpenRouterGuardrailBudgetPoliciesListRequest listRequest = client().guardrailPolicies()
                .listBudgetPolicies().workspaceId("ws-1").build();
        var list = listRequest.createResponse("""
                {"data": [{"id": "p1"}, "junk", {"id": "p2"}], "total_count": 5}
                """);
        assertThat(list.items()).hasSize(2);
        assertThat(list.totalCount()).isEqualTo(5);
        assertThat(listRequest.createResponse("{}").items()).isEmpty();
        assertThat(listRequest.createResponse("{}").totalCount()).isNull();

        OpenRouterGuardrailApiKeyPolicyAssignmentGetRequest assignmentGet = client().guardrailPolicies()
                .apiKeyPolicyAssignment("h").workspaceId("ws-1").policyType("budget").build();
        assertThat(assignmentGet.createResponse("{\"data\": {\"policy_ids\": []}}").data()).isNotNull();
        assertThat(assignmentGet.createResponse("{}").data()).isNull();

        OpenRouterGuardrailApiKeyPolicyAssignmentChangeRequest change = client().guardrailPolicies()
                .changeApiKeyPolicyAssignment("h").workspaceId("ws-1").policyType("budget")
                .policyId("p1").assigned(true)
                .expectedAssignment(OpenRouterGuardrailPolicyAssignmentState.of(List.of(), List.of(), false))
                .build();
        assertThat(change.createResponse("{\"data\": false}").changed()).isFalse();
        assertThat(change.createResponse("{}").changed()).isNull();

        OpenRouterGuardrailAccountPolicyCreateRequest create = client().guardrailPolicies()
                .createAccountPolicy()
                .policy(OpenRouterGuardrailPolicyRequest.budget("N", budgetConfig()).build())
                .build();
        assertThat(create.createResponse("{\"data\": {\"id\": \"p1\", \"workspace_count\": 1}}")
                .data().workspaceCount()).isEqualTo(1);
        assertThat(create.createResponse("{}").data()).isNull();

        // the account list wrapper: junk rows dropped, total_count read back
        var accountList = client().guardrailPolicies().listAccountPolicies().build()
                .createResponse("{\"data\": [{\"id\": \"p1\"}, \"junk\"], \"total_count\": 3}");
        assertThat(accountList.items()).hasSize(1);
        assertThat(accountList.items().get(0).id()).isEqualTo("p1");
        assertThat(accountList.totalCount()).isEqualTo(3);
        assertThat(client().guardrailPolicies().listAccountPolicies().build()
                .createResponse("{\"total_count\": \"many\"}").totalCount()).isNull();

        // the typed policy wrapper (create/update answers)
        var policyResponse = client().guardrailPolicies()
                .createBudgetPolicy().name("N").config(budgetConfig()).workspaceId("ws-1").build()
                .createResponse("{\"data\": {\"id\": \"p1\", \"policy_type\": \"budget\"}}");
        assertThat(policyResponse.data().id()).isEqualTo("p1");
        assertThat(policyResponse.data().policyType()).isEqualTo("budget");
        assertThat(client().guardrailPolicies()
                .createBudgetPolicy().name("N").config(budgetConfig()).workspaceId("ws-1").build()
                .createResponse("{}").data()).isNull();
    }
}
