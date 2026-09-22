package de.entwicklertraining.openrouter4j.vault;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import java.util.List;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Request-JSON and response-fixture level tests for the vault endpoints.
 */
class OpenRouterVaultTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void listRequestEmitsTypedQueryParametersOnlyWhenSet() {
        OpenRouterVaultSecretsListRequest without = client().vault().list().build();
        assertThat(without.getRelativeUrl()).isEqualTo("/vault/secrets");
        assertThat(without.getHttpMethod()).isEqualTo("GET");
        assertThat(without.getBody()).isNull();

        OpenRouterVaultSecretsListRequest with = client().vault().list()
                .limit(50)
                .offset(100)
                .build();
        assertThat(with.getRelativeUrl()).isEqualTo("/vault/secrets?limit=50&offset=100");
    }

    @Test
    void listRequestRejectsOutOfRangePagination() {
        assertThatThrownBy(() -> client().vault().list().limit(0).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("limit");
        assertThatThrownBy(() -> client().vault().list().limit(101).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("limit");
        assertThatThrownBy(() -> client().vault().list().offset(-1).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("offset");
        assertThatThrownBy(() -> client().vault().list().offset(10_001).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("offset");
    }

    @Test
    void storeRequestEmitsValueAndHosts() {
        OpenRouterVaultSecretStoreRequest request = client().vault().store("github_token")
                .value("ghp_exampleTokenValue")
                .hosts("API.Example.com.", "api.github.com")
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/vault/secrets/github_token");
        assertThat(request.getHttpMethod()).isEqualTo("PUT");
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder("value", "hosts");
        assertThat(body.getString("value")).isEqualTo("ghp_exampleTokenValue");
        assertThat(body.getJSONArray("hosts").toList())
                .containsExactly("API.Example.com.", "api.github.com");
    }

    @Test
    void storeRequestKeepsTheHostListVerbatim() {
        // normalization (lowercasing, trailing dot) is server-side; the
        // library passes the entries through verbatim
        OpenRouterVaultSecretStoreRequest request = client().vault().store("token")
                .value("v")
                .hosts(List.of("api.example.com"))
                .build();
        assertThat(new JSONObject(request.getBody()).getJSONArray("hosts").toList())
                .containsExactly("api.example.com");
    }

    @Test
    void storeRequestRejectsInvalidNamesValuesAndHosts() {
        assertThatThrownBy(() -> client().vault().store("Github_Token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lowercase");
        assertThatThrownBy(() -> client().vault().store("a__b"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lowercase");
        assertThatThrownBy(() -> client().vault().store("token_"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lowercase");
        assertThatThrownBy(() -> client().vault().store("a".repeat(256)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("255");

        assertThatThrownBy(() -> client().vault().store("token").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("value");
        assertThatThrownBy(() -> client().vault().store("token").value("v").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hosts");
        assertThatThrownBy(() -> client().vault().store("token").value("").hosts("h.com").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("value");
        assertThatThrownBy(() -> client().vault().store("token")
                .value("v").value("x".repeat(65_537)).hosts("h.com").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("65536");
        assertThatThrownBy(() -> client().vault().store("token").value("v").build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> client().vault().store("token").value("v").hosts(new java.util.ArrayList<>()).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("host");
        assertThatThrownBy(() -> client().vault().store("token")
                .value("v").hosts(java.util.Collections.nCopies(101, "h.com")).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100");
        assertThatThrownBy(() -> client().vault().store("token")
                .value("v").hosts(" ", "h.com").build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void storeRequestToStringDoesNotContainTheValue() {
        OpenRouterVaultSecretStoreRequest request = client().vault().store("github_token")
                .value("super-secret-value-42")
                .hosts("api.github.com")
                .build();
        assertThat(request.toString()).doesNotContain("super-secret-value-42");
    }

    @Test
    void deleteRequestBuildsUrlEncodedDelete() {
        OpenRouterVaultSecretDeleteRequest delete = client().vault().delete("gh/token")
                .build();
        assertThat(delete.getRelativeUrl()).isEqualTo("/vault/secrets/gh%2Ftoken");
        assertThat(delete.getHttpMethod()).isEqualTo("DELETE");
        assertThat(delete.getBody()).isNull();
        assertThat(delete.name()).isEqualTo("gh%2Ftoken");
    }

    @Test
    void deleteResponseToleratesTheEmpty204Body() {
        OpenRouterVaultSecretDeleteRequest request = client().vault().delete("token").build();
        OpenRouterVaultSecretDeleteResponse<OpenRouterVaultSecretDeleteRequest> response =
                request.createResponse("");
        assertThat(response.deleted()).isNull();
        assertThat(request.createResponse("{\"deleted\":true}").deleted()).isTrue();
    }

    @Test
    void internListRequestBuildsScopedUrlWithPagination() {
        OpenRouterVaultInternSecretsListRequest request = client().vault()
                .internSecrets("550e8400-e29b-41d4-a716-446655440000")
                .limit(10)
                .offset(20)
                .build();
        assertThat(request.getRelativeUrl())
                .isEqualTo("/vault/interns/550e8400-e29b-41d4-a716-446655440000/secrets"
                        + "?limit=10&offset=20");
        assertThat(request.getHttpMethod()).isEqualTo("GET");

        OpenRouterVaultInternSecretsListRequest bare = client().vault()
                .internSecrets("i1").build();
        assertThat(bare.getRelativeUrl()).isEqualTo("/vault/interns/i1/secrets");
    }

    @Test
    void internStoreRequestBuildsScopedUrlAndBody() {
        OpenRouterVaultInternSecretStoreRequest request = client().vault()
                .storeInternSecret("i1", "github_token")
                .value("v")
                .hosts("api.github.com")
                .build();
        assertThat(request.getRelativeUrl()).isEqualTo("/vault/interns/i1/secrets/github_token");
        assertThat(request.getHttpMethod()).isEqualTo("PUT");
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder("value", "hosts");
        assertThat(request.toString()).doesNotContain("v");
    }

    @Test
    void internDeleteRequestBuildsScopedUrl() {
        OpenRouterVaultInternSecretDeleteRequest request = client().vault()
                .deleteInternSecret("i 1", "gh/token")
                .build();
        assertThat(request.getRelativeUrl()).isEqualTo("/vault/interns/i+1/secrets/gh%2Ftoken");
        assertThat(request.getHttpMethod()).isEqualTo("DELETE");
        assertThat(request.getBody()).isNull();
    }

    @Test
    void copyRequestEmitsDistinctNamesAndRejectsDuplicates() {
        OpenRouterVaultSecretCopyRequest request = client().vault()
                .copySecretsToIntern("i1")
                .names("github_token", "slack_token")
                .build();
        assertThat(request.getRelativeUrl()).isEqualTo("/vault/interns/i1/secrets/copy");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.keySet()).containsExactly("names");
        assertThat(body.getJSONArray("names").toList())
                .containsExactly("github_token", "slack_token");
    }

    @Test
    void copyRequestValidatesNames() {
        assertThatThrownBy(() -> client().vault().copySecretsToIntern("i1").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("names");
        assertThatThrownBy(() -> client().vault().copySecretsToIntern("i1")
                .names("Bad_Name").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lowercase");
        assertThatThrownBy(() -> client().vault().copySecretsToIntern("i1")
                .names("a", "a").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("distinct");
        assertThatThrownBy(() -> client().vault().copySecretsToIntern("i1")
                .names(java.util.Collections.nCopies(101, "a")).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100");
    }

    @Test
    void listResponseSurfacesTypedSecretMetadata() {
        OpenRouterVaultSecretsListRequest request = client().vault().list().build();
        OpenRouterVaultSecretsListResponse<OpenRouterVaultSecretsListRequest> response =
                request.createResponse("""
                {
                  "data": [
                    {"name": "github_token",
                     "hosts": ["api.github.com"],
                     "fingerprint": "sha256:9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08",
                     "created_at": "2026-09-15T17:44:00.000Z"},
                    {"name": "legacy_token", "hosts": null,
                     "fingerprint": null, "created_at": "2026-08-01T09:30:00.000Z"}
                  ],
                  "has_more": true
                }
                """);

        assertThat(response.secrets()).hasSize(2);
        assertThat(response.secrets().get(0).name()).isEqualTo("github_token");
        assertThat(response.secrets().get(0).hosts()).containsExactly("api.github.com");
        assertThat(response.secrets().get(0).fingerprint())
                .isEqualTo("sha256:9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08");
        assertThat(response.secrets().get(0).createdAt()).isEqualTo("2026-09-15T17:44:00.000Z");
        assertThat(response.secrets().get(1).hosts()).isNull();
        assertThat(response.secrets().get(1).fingerprint()).isNull();
        assertThat(response.hasMore()).isTrue();
    }

    @Test
    void mutationResponseSurfacesTheStoredSecret() {
        OpenRouterVaultSecretStoreRequest request = client().vault().store("github_token")
                .value("v")
                .hosts("api.github.com")
                .build();
        OpenRouterVaultSecretMutationResponse<OpenRouterVaultSecretStoreRequest> response =
                request.createResponse("""
                {"data": {"name": "github_token", "hosts": ["api.github.com"],
                 "fingerprint": "sha256:9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08",
                 "created_at": "2026-09-15T17:44:00.000Z"}}
                """);

        assertThat(response.secret()).isNotNull();
        assertThat(response.secret().name()).isEqualTo("github_token");
        assertThat(response.secret().hosts()).containsExactly("api.github.com");
    }

    @Test
    void copyResponseSurfacesTheCopiedSecrets() {
        OpenRouterVaultSecretCopyRequest request = client().vault()
                .copySecretsToIntern("i1").names("github_token").build();
        OpenRouterVaultSecretCopyResponse response = request.createResponse("""
                {"data": [{"name": "github_token", "hosts": ["api.github.com"],
                 "fingerprint": "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                 "created_at": "2026-09-15T17:44:00.000Z"}]}
                """);
        assertThat(response.secrets()).hasSize(1);
        assertThat(response.secrets().get(0).name()).isEqualTo("github_token");
    }

    @Test
    void responseAccessorsSwallowMalformedBody() {
        OpenRouterVaultSecretsListRequest listRequest = client().vault().list().build();
        OpenRouterVaultSecretsListResponse<OpenRouterVaultSecretsListRequest> empty =
                listRequest.createResponse("{}");
        assertThat(empty.secrets()).isEmpty();
        assertThat(empty.hasMore()).isNull();

        OpenRouterVaultSecretCopyRequest copyRequest = client().vault()
                .copySecretsToIntern("i1").names("a").build();
        assertThat(copyRequest.createResponse("{}").secrets()).isEmpty();
    }
}
