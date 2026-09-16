package de.entwicklertraining.openrouter4j.observability;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the observability destination management requests (URL shape, body
 * presence/absence, config escape hatch, path-segment URL encoding, loud
 * validation) and the response accessors against recorded JSON shapes of the
 * OpenRouter /observability/destinations endpoints.
 */
class OpenRouterObservabilityTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void listRequestEmitsTypedQueryParameters() {
        OpenRouterObservabilityDestinationsListRequest request =
                new OpenRouterObservabilityDestinationsListRequest.Builder(client())
                        .offset(5)
                        .limit(20)
                        .workspaceId("550e8400-e29b-41d4-a716-446655440000")
                        .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/observability/destinations?offset=5&limit=20"
                + "&workspace_id=550e8400-e29b-41d4-a716-446655440000");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
    }

    @Test
    void createRequestEmitsOnlyConfiguredFields() {
        OpenRouterObservabilityDestinationCreateRequest request =
                new OpenRouterObservabilityDestinationCreateRequest.Builder(client())
                        .type("langfuse")
                        .name("Production Langfuse")
                        .configOption("baseUrl", "https://us.cloud.langfuse.com")
                        .configOption("publicKey", "pk-l-example")
                        .configOption("secretKey", "sk-l-example-secret")
                        .enabled(true)
                        .regions(List.of("europe"))
                        .samplingRate(0.5)
                        .privacyMode(true)
                        .broadcastGenerationCost(false)
                        .apiKeyHashes(List.of("c56454edb818d6b14bc0d61c46025f1450b0f4012d12304ab40aacb519fcbc93"))
                        .build();

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder(
                "type", "name", "config", "enabled", "regions", "sampling_rate",
                "privacy_mode", "broadcast_generation_cost", "api_key_hashes");
        assertThat(body.get("type")).isEqualTo("langfuse");
        assertThat(body.get("name")).isEqualTo("Production Langfuse");
        JSONObject config = body.getJSONObject("config");
        assertThat(config.getString("baseUrl")).isEqualTo("https://us.cloud.langfuse.com");
        assertThat(config.getString("secretKey")).isEqualTo("sk-l-example-secret");
        assertThat(body.getBoolean("privacy_mode")).isTrue();
        assertThat(body.getJSONArray("regions").toList()).containsExactly("europe");
        assertThat(body.getDouble("sampling_rate")).isEqualTo(0.5);
        assertThat(body.getJSONArray("api_key_hashes").toList())
                .containsExactly("c56454edb818d6b14bc0d61c46025f1450b0f4012d12304ab40aacb519fcbc93");
        // unset fields never appear
        assertThat(body.has("workspace_id")).isFalse();
        assertThat(body.has("filter_rules")).isFalse();
        assertThat(body.has("broadcast_generation_identity")).isFalse();
    }

    @Test
    void createRequestAcceptsAWholeConfigObject() {
        OpenRouterObservabilityDestinationCreateRequest request =
                new OpenRouterObservabilityDestinationCreateRequest.Builder(client())
                        .type("webhook")
                        .name("Hook")
                        .config(new JSONObject().put("url", "https://example.invalid/hook"))
                        .build();

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getJSONObject("config").getString("url")).isEqualTo("https://example.invalid/hook");
    }

    @Test
    void createRequestRejectsMissingRequiredFields() {
        assertThatThrownBy(() -> new OpenRouterObservabilityDestinationCreateRequest.Builder(client())
                .name("D").config(new JSONObject()).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("type");
        assertThatThrownBy(() -> new OpenRouterObservabilityDestinationCreateRequest.Builder(client())
                .type("langfuse").config(new JSONObject()).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("name");
        assertThatThrownBy(() -> new OpenRouterObservabilityDestinationCreateRequest.Builder(client())
                .type("langfuse").name("D").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("config");
    }

    @Test
    void updateRequestEmitsOnlyConfiguredFields() {
        OpenRouterObservabilityDestinationUpdateRequest request =
                new OpenRouterObservabilityDestinationUpdateRequest.Builder(client(), "d-1")
                        .name("Updated Langfuse")
                        .enabled(false)
                        .regions(List.of("europe"))
                        .samplingRate(0.5)
                        .privacyMode(true)
                        .filterRules(new JSONObject().put("enabled", false))
                        .apiKeyHashes(List.of("c56454edb818d6b14bc0d61c46025f1450b0f4012d12304ab40aacb519fcbc93"))
                        .broadcastGenerationCost(true)
                        .broadcastGenerationIdentity(false)
                        .broadcastGenerationRequestContext(false)
                        .configOption("baseUrl", "https://eu.cloud.langfuse.com")
                        .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/observability/destinations/d-1");
        assertThat(request.getHttpMethod()).isEqualTo("PATCH");
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder(
                "name", "enabled", "regions", "sampling_rate", "privacy_mode",
                "filter_rules", "api_key_hashes", "broadcast_generation_cost",
                "broadcast_generation_identity", "broadcast_generation_request_context",
                "config");
        assertThat(body.getBoolean("enabled")).isFalse();
        assertThat(body.getJSONArray("regions").toList()).containsExactly("europe");
        assertThat(body.getDouble("sampling_rate")).isEqualTo(0.5);
        assertThat(body.getJSONObject("filter_rules").getBoolean("enabled")).isFalse();
        assertThat(body.getJSONObject("config").getString("baseUrl")).isEqualTo("https://eu.cloud.langfuse.com");
        // no generation artifact keys
        assertThat(body.has("None")).isFalse();
    }

    @Test
    void createRequestNeverEmitsArtifactKeys() {
        OpenRouterObservabilityDestinationCreateRequest request =
                new OpenRouterObservabilityDestinationCreateRequest.Builder(client())
                        .type("langfuse")
                        .name("D")
                        .config(new JSONObject().put("secretKey", "sk-lf-example"))
                        .build();

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder("type", "name", "config");
        assertThat(body.has("None")).isFalse();
    }

    @Test
    void getAndDeleteRequestsUrlEncodeTheId() {
        assertThat(client().observability().destinations().get("id with space").build().getRelativeUrl())
                .isEqualTo("/observability/destinations/id+with+space");
        OpenRouterObservabilityDestinationDeleteRequest delete =
                client().observability().destinations().delete("d/1").build();
        assertThat(delete.getRelativeUrl()).isEqualTo("/observability/destinations/d%2F1");
        assertThat(delete.getHttpMethod()).isEqualTo("DELETE");
        assertThat(delete.getBody()).isNull();
    }

    @Test
    void listResponseSurfacesTypedDestinations() {
        OpenRouterObservabilityDestinationsListResponse response =
                new OpenRouterObservabilityDestinationsListResponse(new JSONObject("""
                        {
                          "data": [
                            {
                              "id": "99999999-aaaa-bbbb-cccc-dddddddddddd",
                              "type": "langfuse",
                              "name": "Production Langfuse",
                              "enabled": true,
                              "regions": ["global"],
                              "sampling_rate": 1,
                              "privacy_mode": false,
                              "broadcast_generation_cost": false,
                              "broadcast_generation_identity": false,
                              "broadcast_generation_request_context": false,
                              "api_key_hashes": null,
                              "filter_rules": null,
                              "config": {"baseUrl": "https://us.cloud.langfuse.com", "publicKey": "pk-l"},
                              "workspace_id": "550e8400-e29b-41d4-a716-446655440000",
                              "created_at": "2025-08-24T10:30:00Z",
                              "updated_at": "2025-08-24T15:45:00Z"
                            }
                          ],
                          "total_count": 1
                        }
                        """), null);

        assertThat(response.totalCount()).isEqualTo(1);
        OpenRouterObservabilityDestination destination = response.items().get(0);
        assertThat(destination.id()).isEqualTo("99999999-aaaa-bbbb-cccc-dddddddddddd");
        assertThat(destination.type()).isEqualTo("langfuse");
        assertThat(destination.name()).isEqualTo("Production Langfuse");
        assertThat(destination.enabled()).isTrue();
        assertThat(destination.regions()).containsExactly("global");
        assertThat(destination.samplingRate()).isEqualTo(1.0);
        assertThat(destination.privacyMode()).isFalse();
        assertThat(destination.broadcastGenerationCost()).isFalse();
        assertThat(destination.apiKeyHashes()).isEmpty();
        assertThat(destination.config().getString("baseUrl")).isEqualTo("https://us.cloud.langfuse.com");
        assertThat(destination.filterRules()).isNull();
        assertThat(destination.workspaceId()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void responsesReturnNullAndEmptyWhenAbsent() {
        OpenRouterObservabilityDestinationGetResponse get =
                new OpenRouterObservabilityDestinationGetResponse(new JSONObject("{}"), null);
        assertThat(get.data()).isNull();

        OpenRouterObservabilityDestinationsListResponse list =
                new OpenRouterObservabilityDestinationsListResponse(new JSONObject("{}"), null);
        assertThat(list.items()).isEmpty();

        OpenRouterObservabilityDestinationDeleteResponse deleted =
                new OpenRouterObservabilityDestinationDeleteResponse(new JSONObject("{}"), null);
        assertThat(deleted.deleted()).isNull();
    }
}
