package de.entwicklertraining.openrouter4j;

import de.entwicklertraining.openrouter4j.batches.OpenRouterBatchEndpoint;
import de.entwicklertraining.openrouter4j.batches.OpenRouterBatchItem;
import de.entwicklertraining.openrouter4j.credits.OpenRouterCreditsRequest;
import de.entwicklertraining.openrouter4j.extractions.OpenRouterExtractionField;
import de.entwicklertraining.openrouter4j.extractions.OpenRouterExtractionsRequest;
import de.entwicklertraining.openrouter4j.models.OpenRouterModelRequest;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the client entry points of the read endpoints: each factory must
 * produce the correct request type with the expected relative URL, and the
 * model-id splitting of {@code model()} / {@code modelEndpoints()} must
 * validate its input.
 */
class OpenRouterClientEntryPointsTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void creditsEntryPointProducesTheCreditsRequest() {
        OpenRouterCreditsRequest request = client().credits().build();
        assertThat(request.getRelativeUrl()).isEqualTo("/credits");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
    }

    @Test
    void modelsEntryPointsProduceTheCatalogRequests() {
        assertThat(client().models().build().getRelativeUrl()).isEqualTo("/models");
        assertThat(client().modelsCount().build().getRelativeUrl()).isEqualTo("/models/count");
        assertThat(client().userModels().build().getRelativeUrl()).isEqualTo("/models/user");
    }

    @Test
    void modelEntryPointSplitsTheModelId() {
        assertThat(client().model("openai/gpt-4").build().getRelativeUrl())
                .isEqualTo("/model/openai/gpt-4");
        assertThat(client().modelEndpoints("openai/gpt-4").build().getRelativeUrl())
                .isEqualTo("/models/openai/gpt-4/endpoints");

        // only the first slash splits: slugs may not contain slashes themselves,
        // and everything after it is the slug
        OpenRouterModelRequest request = client().model("a/b/c").build();
        assertThat(request.author()).isEqualTo("a");
        assertThat(request.slug()).isEqualTo("b/c");
    }

    @Test
    void modelEntryPointRejectsMalformedModelIds() {
        assertThatThrownBy(() -> client().model(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().model("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().model("gpt-4")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().model("/gpt-4")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().model("openai/")).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> client().modelEndpoints(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().modelEndpoints("gpt-4")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generationEntryPointsProduceTheGenerationRequests() {
        assertThat(client().generation("gen-1").build().getRelativeUrl())
                .isEqualTo("/generation?id=gen-1");
        assertThat(client().generationContent("gen-1").build().getRelativeUrl())
                .isEqualTo("/generation/content?id=gen-1");
        assertThat(client().generationFeedback().generationId("gen-1").category("other")
                .build().getRelativeUrl())
                .isEqualTo("/generation/feedback");
        assertThat(client().generationFeedback().generationId("gen-1").category("other")
                .build().getHttpMethod()).isEqualTo("POST");
    }

    @Test
    void activityEntryPointsProduceTheUsageRequests() {
        assertThat(client().activity().build().getRelativeUrl()).isEqualTo("/activity");
        assertThat(client().activity().date("2026-09-14").build().getRelativeUrl())
                .isEqualTo("/activity?date=2026-09-14");
        assertThat(client().analyticsQuery().metrics("request_count").build().getRelativeUrl())
                .isEqualTo("/analytics/query");
        assertThat(client().analyticsQuery().metrics("request_count").build().getHttpMethod())
                .isEqualTo("POST");
    }

    @Test
    void embeddingsEntryPointsProduceTheEmbeddingsRequests() {
        assertThat(client().embeddings()
                .model("openai/text-embedding-3-small").input("hello")
                .build().getRelativeUrl()).isEqualTo("/embeddings");
        assertThat(client().embeddings()
                .model("openai/text-embedding-3-small").input("hello")
                .build().getHttpMethod()).isEqualTo("POST");
        assertThat(client().embeddingsModels().build().getRelativeUrl())
                .isEqualTo("/embeddings/models");
        assertThat(client().embeddingsModels().build().getHttpMethod()).isEqualTo("GET");
    }

    @Test
    void rerankEntryPointProducesTheRerankRequest() {
        assertThat(client().rerank()
                .model("cohere/rerank-v3.5").query("q").addDocument("doc")
                .build().getRelativeUrl()).isEqualTo("/rerank");
        assertThat(client().rerank()
                .model("cohere/rerank-v3.5").query("q").addDocument("doc")
                .build().getHttpMethod()).isEqualTo("POST");
    }

    @Test
    void oauthEntryPointsProduceTheAuthorizationCodeRequests() {
        assertThat(client().createAuthorizationCode()
                .callbackUrl("https://myapp.com/auth/callback")
                .build().getRelativeUrl()).isEqualTo("/auth/keys/code");
        assertThat(client().createAuthorizationCode()
                .callbackUrl("https://myapp.com/auth/callback")
                .build().getHttpMethod()).isEqualTo("POST");
        assertThat(client().exchangeAuthorizationCode()
                .code("auth_code_abc123def456")
                .build().getRelativeUrl()).isEqualTo("/auth/keys");
        assertThat(client().exchangeAuthorizationCode()
                .code("auth_code_abc123def456")
                .build().getHttpMethod()).isEqualTo("POST");
    }

    @Test
    void workloadIdentityEntryPointProducesTheTokenExchangeRequest() {
        assertThat(client().exchangeWorkloadIdentityToken()
                .subjectToken("header.payload.signature")
                .federationPolicyId("4b2f7d1e-8c3a-4e5f-9a6b-1c2d3e4f5a6b")
                .build().getRelativeUrl()).isEqualTo("/oauth/token");
        assertThat(client().exchangeWorkloadIdentityToken()
                .subjectToken("header.payload.signature")
                .federationPolicyId("4b2f7d1e-8c3a-4e5f-9a6b-1c2d3e4f5a6b")
                .build().getHttpMethod()).isEqualTo("POST");
    }

    @Test
    void routingDiscoveryEntryPointsProduceTheReadRequests() {
        assertThat(client().providers().build().getRelativeUrl()).isEqualTo("/providers");
        assertThat(client().providers().build().getHttpMethod()).isEqualTo("GET");
        assertThat(client().zdrEndpoints().build().getRelativeUrl()).isEqualTo("/endpoints/zdr");
        assertThat(client().zdrEndpoints().build().getHttpMethod()).isEqualTo("GET");
    }

    @Test
    void analyticsMetaEntryPointProducesTheMetaRequest() {
        assertThat(client().analyticsMeta().build().getRelativeUrl()).isEqualTo("/analytics/meta");
        assertThat(client().analyticsMeta().build().getHttpMethod()).isEqualTo("GET");
    }

    @Test
    void publicDataEntryPointsProduceTheDatasetRequests() {
        assertThat(client().benchmarks().source("openrouter").build().getRelativeUrl())
                .isEqualTo("/benchmarks?source=openrouter");
        assertThat(client().benchmarks().build().getHttpMethod()).isEqualTo("GET");
        assertThat(client().datasets().appRankings().limit(10).build().getRelativeUrl())
                .isEqualTo("/datasets/app-rankings?limit=10");
        assertThat(client().datasets().rankingsDaily().period("week").build().getRelativeUrl())
                .isEqualTo("/datasets/rankings-daily?period=week");
        assertThat(client().datasets().sessionCost().appSlug("hermes-agent").build().getRelativeUrl())
                .isEqualTo("/datasets/session-cost?app_slug=hermes-agent");
        assertThat(client().taskClassifications().window("7d").build().getRelativeUrl())
                .isEqualTo("/classifications/task?window=7d");
    }

    @Test
    void keysEntryPointsProduceTheManagementRequests() {
        assertThat(client().currentKey().build().getRelativeUrl()).isEqualTo("/key");
        assertThat(client().keys().list().build().getRelativeUrl()).isEqualTo("/keys");
        assertThat(client().keys().create().name("My Key").build().getRelativeUrl()).isEqualTo("/keys");
        assertThat(client().keys().create().name("My Key").build().getHttpMethod()).isEqualTo("POST");
        assertThat(client().keys().get("abc123").build().getRelativeUrl()).isEqualTo("/keys/abc123");
        assertThat(client().keys().update("abc123").disabled(true).build().getHttpMethod()).isEqualTo("PATCH");
        assertThat(client().keys().delete("abc123").build().getHttpMethod()).isEqualTo("DELETE");
    }

    @Test
    void batchesEntryPointsProduceTheBatchRequests() {
        assertThat(client().batches().list().build().getRelativeUrl()).isEqualTo("/batches");
        assertThat(client().batches().submit()
                .endpoint(OpenRouterBatchEndpoint.CHAT_COMPLETIONS)
                .model("openai/gpt-4o")
                .addRequest(OpenRouterBatchItem.of("req-1", new JSONObject().put("messages", "x")))
                .build().getHttpMethod()).isEqualTo("POST");
        assertThat(client().batches().get("batch_1").build().getRelativeUrl()).isEqualTo("/batches/batch_1");
        assertThat(client().batches().delete("batch_1").build().getHttpMethod()).isEqualTo("DELETE");
    }

    @Test
    void workspaceEntryPointsProduceTheManagementRequests() {
        assertThat(client().workspaces().list().build().getRelativeUrl()).isEqualTo("/workspaces");
        assertThat(client().workspaces().create().name("W").slug("w").build().getHttpMethod()).isEqualTo("POST");
        assertThat(client().workspaces().get("ws-1").build().getRelativeUrl()).isEqualTo("/workspaces/ws-1");
        assertThat(client().workspaces().update("ws-1").name("N").build().getHttpMethod()).isEqualTo("PATCH");
        assertThat(client().workspaces().delete("ws-1").build().getHttpMethod()).isEqualTo("DELETE");
        assertThat(client().workspaces().members("ws-1").build().getRelativeUrl()).isEqualTo("/workspaces/ws-1/members");
        assertThat(client().workspaces().addMembers("ws-1").addUserId("u1").build().getRelativeUrl()).isEqualTo("/workspaces/ws-1/members/add");
        assertThat(client().workspaces().removeMembers("ws-1").addUserId("u1").build().getRelativeUrl()).isEqualTo("/workspaces/ws-1/members/remove");
        assertThat(client().workspaces().budgets("ws-1").build().getRelativeUrl()).isEqualTo("/workspaces/ws-1/budgets");
        assertThat(client().workspaces().budget("ws-1", "monthly").build().getRelativeUrl()).isEqualTo("/workspaces/ws-1/budgets/monthly");
        assertThat(client().workspaces().upsertBudget("ws-1", "monthly").limitUsd(10.0).build().getHttpMethod()).isEqualTo("PUT");
        assertThat(client().workspaces().deleteBudget("ws-1", "monthly").build().getHttpMethod()).isEqualTo("DELETE");
        assertThat(client().organization().members().build().getRelativeUrl()).isEqualTo("/organization/members");
    }

    @Test
    void guardrailsEntryPointsProduceTheManagementRequests() {
        assertThat(client().guardrails().list().build().getRelativeUrl()).isEqualTo("/guardrails");
        assertThat(client().guardrails().create().name("G").build().getHttpMethod()).isEqualTo("POST");
        assertThat(client().guardrails().get("g1").build().getRelativeUrl()).isEqualTo("/guardrails/g1");
        assertThat(client().guardrails().update("g1").name("N").build().getHttpMethod()).isEqualTo("PATCH");
        assertThat(client().guardrails().delete("g1").build().getHttpMethod()).isEqualTo("DELETE");
        assertThat(client().guardrails().keyAssignments("g1").build().getRelativeUrl()).isEqualTo("/guardrails/g1/assignments/keys");
        assertThat(client().guardrails().memberAssignments("g1").build().getRelativeUrl()).isEqualTo("/guardrails/g1/assignments/members");
        assertThat(client().guardrails().allKeyAssignments().build().getRelativeUrl()).isEqualTo("/guardrails/assignments/keys");
        assertThat(client().guardrails().allMemberAssignments().build().getRelativeUrl()).isEqualTo("/guardrails/assignments/members");
        assertThat(client().guardrails().assignKeys("g1").addKeyHash("h1").build().getHttpMethod()).isEqualTo("POST");
        assertThat(client().guardrails().unassignKeys("g1").addKeyHash("h1").build().getRelativeUrl()).isEqualTo("/guardrails/g1/assignments/keys/remove");
        assertThat(client().guardrails().assignMembers("g1").addMemberUserId("u1").build().getRelativeUrl()).isEqualTo("/guardrails/g1/assignments/members");
        assertThat(client().guardrails().unassignMembers("g1").addMemberUserId("u1").build().getRelativeUrl()).isEqualTo("/guardrails/g1/assignments/members/remove");
    }

    @Test
    void guardrailPoliciesEntryPointsProduceTheManagementRequests() {
        var budgetConfig = de.entwicklertraining.openrouter4j.guardrailpolicies.OpenRouterGuardrailBudgetPolicyConfig
                .builder().limitUsd(1.0).resetInterval("daily").includeByokInBudgets(false).build();
        var policy = de.entwicklertraining.openrouter4j.guardrailpolicies.OpenRouterGuardrailPolicyRequest
                .budget("N", budgetConfig).build();

        assertThat(client().guardrailPolicies().listAccountPolicies().build().getRelativeUrl())
                .isEqualTo("/guardrails/policies/account");
        assertThat(client().guardrailPolicies().createAccountPolicy().policy(policy).build().getHttpMethod())
                .isEqualTo("POST");
        assertThat(client().guardrailPolicies().updateAccountPolicy("p1").policy(policy).build().getRelativeUrl())
                .isEqualTo("/guardrails/policies/account/p1");
        assertThat(client().guardrailPolicies().deleteAccountPolicy("p1").build().getHttpMethod())
                .isEqualTo("DELETE");

        assertThat(client().guardrailPolicies().listBudgetPolicies().workspaceId("ws-1").build().getRelativeUrl())
                .isEqualTo("/guardrails/policies/budget?workspace_id=ws-1");
        assertThat(client().guardrailPolicies().createBudgetPolicy().name("N").config(budgetConfig)
                .workspaceId("ws-1").build().getHttpMethod()).isEqualTo("POST");
        assertThat(client().guardrailPolicies().getBudgetPolicy("p1").workspaceId("ws-1").build().getRelativeUrl())
                .startsWith("/guardrails/policies/budget/p1");
        assertThat(client().guardrailPolicies().updateBudgetPolicy("p1").name("N").config(budgetConfig)
                .workspaceId("ws-1").build().getHttpMethod()).isEqualTo("PUT");
        assertThat(client().guardrailPolicies().deleteBudgetPolicy("p1").workspaceId("ws-1").build().getRelativeUrl())
                .startsWith("/guardrails/policies/budget/p1");

        assertThat(client().guardrailPolicies().listModelAvailabilityPolicies().workspaceId("ws-1").build()
                .getRelativeUrl()).startsWith("/guardrails/policies/model-availability");
        assertThat(client().guardrailPolicies().createModelAvailabilityPolicy().name("N")
                .config(de.entwicklertraining.openrouter4j.guardrailpolicies.OpenRouterGuardrailModelAvailabilityPolicyConfig
                        .builder().build())
                .workspaceId("ws-1").build().getHttpMethod()).isEqualTo("POST");
        assertThat(client().guardrailPolicies().getModelAvailabilityPolicy("p1").workspaceId("ws-1").build()
                .getRelativeUrl()).startsWith("/guardrails/policies/model-availability/p1");
        assertThat(client().guardrailPolicies().updateModelAvailabilityPolicy("p1").name("N")
                .config(de.entwicklertraining.openrouter4j.guardrailpolicies.OpenRouterGuardrailModelAvailabilityPolicyConfig
                        .builder().build())
                .workspaceId("ws-1").build()
                .getHttpMethod()).isEqualTo("PUT");
        assertThat(client().guardrailPolicies().deleteModelAvailabilityPolicy("p1").workspaceId("ws-1").build()
                .getRelativeUrl()).startsWith("/guardrails/policies/model-availability/p1");

        assertThat(client().guardrailPolicies().listModerationPolicies().workspaceId("ws-1").build()
                .getRelativeUrl()).startsWith("/guardrails/policies/moderation");
        assertThat(client().guardrailPolicies().createModerationPolicy().name("N")
                .config(de.entwicklertraining.openrouter4j.guardrailpolicies.OpenRouterGuardrailModerationPolicyConfig
                        .builder().build())
                .workspaceId("ws-1").build().getHttpMethod()).isEqualTo("POST");
        assertThat(client().guardrailPolicies().getModerationPolicy("p1").workspaceId("ws-1").build()
                .getRelativeUrl()).startsWith("/guardrails/policies/moderation/p1");
        assertThat(client().guardrailPolicies().updateModerationPolicy("p1").name("N")
                .config(de.entwicklertraining.openrouter4j.guardrailpolicies.OpenRouterGuardrailModerationPolicyConfig
                        .builder().build())
                .workspaceId("ws-1").build()
                .getHttpMethod()).isEqualTo("PUT");
        assertThat(client().guardrailPolicies().deleteModerationPolicy("p1").workspaceId("ws-1").build()
                .getRelativeUrl()).startsWith("/guardrails/policies/moderation/p1");

        assertThat(client().guardrailPolicies().apiKeyPolicyAssignment("h").workspaceId("ws-1")
                .policyType("budget").build().getRelativeUrl())
                .startsWith("/guardrails/policies/assignments/api-keys/h");
        var expectedAssignment = de.entwicklertraining.openrouter4j.guardrailpolicies.OpenRouterGuardrailPolicyAssignmentState
                .of(java.util.List.of(), java.util.List.of(), false);
        assertThat(client().guardrailPolicies().changeApiKeyPolicyAssignment("h").workspaceId("ws-1")
                .policyType("budget").policyId("p1").assigned(true)
                .expectedAssignment(expectedAssignment).build().getHttpMethod())
                .isEqualTo("PUT");
        assertThat(client().guardrailPolicies().memberPolicyAssignment("u1").workspaceId("ws-1")
                .policyType("budget").build().getRelativeUrl())
                .startsWith("/guardrails/policies/assignments/members/u1");
        assertThat(client().guardrailPolicies().changeMemberPolicyAssignment("u1").workspaceId("ws-1")
                .policyType("budget").policyId("p1").assigned(false)
                .expectedAssignment(expectedAssignment).build().getHttpMethod())
                .isEqualTo("PUT");
    }

    @Test
    void byokEntryPointsProduceTheManagementRequests() {
        assertThat(client().byok().list().build().getRelativeUrl()).isEqualTo("/byok");
        assertThat(client().byok().create().provider("openai").key("sk-secret").build().getHttpMethod()).isEqualTo("POST");
        assertThat(client().byok().get("b1").build().getRelativeUrl()).isEqualTo("/byok/b1");
        assertThat(client().byok().update("b1").name("N").build().getHttpMethod()).isEqualTo("PATCH");
        assertThat(client().byok().delete("b1").build().getHttpMethod()).isEqualTo("DELETE");
    }

    @Test
    void privateProvidersEntryPointsProduceTheManagementRequests() {
        assertThat(client().privateProviders().list().build().getRelativeUrl()).isEqualTo("/private-providers");
        assertThat(client().privateProviders().list().build().getHttpMethod()).isEqualTo("GET");
        assertThat(client().privateProviders().create().name("Acme").build().getRelativeUrl()).isEqualTo("/private-providers");
        assertThat(client().privateProviders().create().name("Acme").build().getHttpMethod()).isEqualTo("POST");
        assertThat(client().privateProviders().get("acme").build().getRelativeUrl()).isEqualTo("/private-providers/acme");
        assertThat(client().privateProviders().get("acme").build().getHttpMethod()).isEqualTo("GET");
        assertThat(client().privateProviders().update("acme").displayName("Acme").build().getHttpMethod()).isEqualTo("PATCH");
        assertThat(client().privateProviders().delete("acme").build().getHttpMethod()).isEqualTo("DELETE");
    }

    @Test
    void vaultEntryPointsProduceTheVaultRequests() {
        assertThat(client().vault().list().build().getRelativeUrl()).isEqualTo("/vault/secrets");
        assertThat(client().vault().store("token").value("v").hosts("h.com").build().getHttpMethod()).isEqualTo("PUT");
        assertThat(client().vault().store("token").value("v").hosts("h.com").build().getRelativeUrl()).isEqualTo("/vault/secrets/token");
        assertThat(client().vault().delete("token").build().getHttpMethod()).isEqualTo("DELETE");
        assertThat(client().vault().internSecrets("i1").build().getRelativeUrl()).isEqualTo("/vault/interns/i1/secrets");
        assertThat(client().vault().effectiveInternSecrets("i1").build().getRelativeUrl()).isEqualTo("/vault/interns/i1/effective-secrets");
        assertThat(client().vault().storeInternSecret("i1", "token").value("v").hosts("h.com").build().getRelativeUrl()).isEqualTo("/vault/interns/i1/secrets/token");
        assertThat(client().vault().deleteInternSecret("i1", "token").build().getHttpMethod()).isEqualTo("DELETE");
        assertThat(client().vault().copySecretsToIntern("i1").names("a").build().getRelativeUrl()).isEqualTo("/vault/interns/i1/secrets/copy");
    }

    @Test
    void externalUsersEntryPointsProduceTheExternalUserRequests() {
        assertThat(client().externalUsers().list().build().getRelativeUrl()).isEqualTo("/external-users");
        assertThat(client().externalUsers().register("employee_123").build().getHttpMethod()).isEqualTo("POST");
        assertThat(client().externalUsers().register("employee_123").build().getRelativeUrl()).isEqualTo("/external-users");
        assertThat(client().externalUsers().get("employee_123").build().getHttpMethod()).isEqualTo("GET");
        assertThat(client().externalUsers().get("employee_123").build().getRelativeUrl()).isEqualTo("/external-users/employee_123");
        assertThat(client().externalUsers().update("employee_123", true).build().getHttpMethod()).isEqualTo("PATCH");
        assertThat(client().externalUsers().deactivate("employee_123").build().getHttpMethod()).isEqualTo("DELETE");
    }

    @Test
    void serverToolsEntryPointsProduceTheServerToolCatalogRequests() {
        assertThat(client().serverTools().list().build().getHttpMethod()).isEqualTo("GET");
        assertThat(client().serverTools().list().build().getRelativeUrl()).isEqualTo("/tools");
        assertThat(client().serverTools().get("openrouter:web_search").build().getHttpMethod()).isEqualTo("GET");
        assertThat(client().serverTools().get("openrouter:web_search").build().getRelativeUrl()).isEqualTo("/tools/openrouter%3Aweb_search");
    }

    @Test
    void internsEntryPointsProduceTheInternRequests() {
        assertThat(client().interns().list().build().getRelativeUrl()).isEqualTo("/interns");
        assertThat(client().interns().create("ok-name").build().getHttpMethod()).isEqualTo("POST");
        assertThat(client().interns().get("i1").build().getRelativeUrl()).isEqualTo("/interns/i1");
        assertThat(client().interns().update("i1").name("ok-name").build().getHttpMethod()).isEqualTo("PATCH");
        assertThat(client().interns().delete("i1").build().getHttpMethod()).isEqualTo("DELETE");
        assertThat(client().interns().provision("i1").build().getRelativeUrl()).isEqualTo("/interns/i1/provision");
        assertThat(client().interns().suspend("i1").build().getRelativeUrl()).isEqualTo("/interns/i1/suspend");
        assertThat(client().interns().invoke("i1", "hi").build().getRelativeUrl()).isEqualTo("/interns/i1/invoke");
        assertThat(client().interns().daemonAccess("i1").build().getRelativeUrl()).isEqualTo("/interns/i1/daemon-access");
        assertThat(client().interns().connections("i1").build().getRelativeUrl()).isEqualTo("/vault/interns/i1/connections");
        assertThatThrownBy(() -> client().interns().chat("i1").addUserMessage("x").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only streams");
        assertThat(client().interns().chat("i1").addUserMessage("x")
                .stream(new de.entwicklertraining.openrouter4j.interns.OpenRouterInternChatAccumulator(null))
                .build().getRelativeUrl())
                .isEqualTo("/interns/i1/chat/completions");
    }

    @Test
    void privateEndpointsEntryPointsProduceThePrivateEndpointRequests() {
        assertThat(client().privateEndpoints().list().build().getRelativeUrl()).isEqualTo("/private-endpoints");
        assertThat(client().privateEndpoints().list().build().getHttpMethod()).isEqualTo("GET");
        assertThat(client().privateEndpoints().create()
                .modelPermaslug("m").providerSlug("p").upstreamModelId("u")
                .build().getHttpMethod()).isEqualTo("POST");
        assertThat(client().privateEndpoints().get("ep1").build().getRelativeUrl())
                .isEqualTo("/private-endpoints/ep1");
        assertThat(client().privateEndpoints().update("ep1").build().getHttpMethod()).isEqualTo("PATCH");
        assertThat(client().privateEndpoints().delete("ep1").draftOnly(true).build().getRelativeUrl())
                .isEqualTo("/private-endpoints/ep1?draft_only=true");
        assertThat(client().privateEndpoints().activate("ep1").build().getRelativeUrl())
                .isEqualTo("/private-endpoints/ep1/activate");
        assertThat(client().privateEndpoints().disable("ep1").build().getRelativeUrl())
                .isEqualTo("/private-endpoints/ep1/disable");
        assertThat(client().privateEndpoints().enable("ep1").build().getRelativeUrl())
                .isEqualTo("/private-endpoints/ep1/enable");
        assertThat(client().privateEndpoints().validate("ep1", "ws1").build().getRelativeUrl())
                .isEqualTo("/private-endpoints/ep1/validate");
    }

    @Test
    void observabilityEntryPointsProduceTheManagementRequests() {
        assertThat(client().observability().destinations().list().build().getRelativeUrl()).isEqualTo("/observability/destinations");
        assertThat(client().observability().destinations().create().type("langfuse").name("D").config(new JSONObject().put("baseUrl", "https://example.invalid")).build().getHttpMethod()).isEqualTo("POST");
        assertThat(client().observability().destinations().get("d1").build().getRelativeUrl()).isEqualTo("/observability/destinations/d1");
        assertThat(client().observability().destinations().update("d1").name("N").build().getHttpMethod()).isEqualTo("PATCH");
        assertThat(client().observability().destinations().delete("d1").build().getHttpMethod()).isEqualTo("DELETE");
    }

    @Test
    void filesAndContainersEntryPointsProduceTheRequests() {
        assertThat(client().files().upload().fileByBytes(new byte[] {1}, "a.pdf").build().getHttpMethod()).isEqualTo("POST");
        assertThat(client().files().list().build().getRelativeUrl()).isEqualTo("/files");
        assertThat(client().files().get("f1").build().getRelativeUrl()).isEqualTo("/files/f1");
        assertThat(client().files().delete("f1").build().getHttpMethod()).isEqualTo("DELETE");
        assertThat(client().files().content("f1").build().isBinaryResponse()).isTrue();
        assertThat(client().containers().listFiles("sess_a").build().getRelativeUrl()).isEqualTo("/containers/sess_a/files");
        assertThat(client().containers().file("sess_a", "cfile_b").build().getRelativeUrl()).isEqualTo("/containers/sess_a/files/cfile_b");
        assertThat(client().containers().fileContent("sess_a", "cfile_b").build().isBinaryResponse()).isTrue();
        assertThat(client().containers().promoteFile("sess_a", "cfile_b").build().getHttpMethod()).isEqualTo("POST");
    }

    @Test
    void extractionsEntryPointProducesTheExtractionsRequest() {
        OpenRouterExtractionsRequest request = client().extractions()
                .model("cloudflare/clairic")
                .state("Invoice INV-2291")
                .field("invoice_number", OpenRouterExtractionField.text().build())
                .build();
        assertThat(request.getRelativeUrl()).isEqualTo("/extractions");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
    }
}
