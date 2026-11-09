package de.entwicklertraining.openrouter4j.privateproviders;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the private-providers request shapes and the response accessors
 * against recorded JSON shapes of the /private-providers routes. The routes
 * are management routes that mutate account state, so nothing here goes to
 * the live API.
 */
class OpenRouterPrivateProvidersTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void fiveRoutesProduceTheDocumentedUrlsAndMethods() {
        assertThat(client().privateProviders().list().build().getRelativeUrl())
                .isEqualTo("/private-providers");
        assertThat(client().privateProviders().list().build().getHttpMethod())
                .isEqualTo("GET");

        assertThat(client().privateProviders().create().name("Acme").build().getRelativeUrl())
                .isEqualTo("/private-providers");
        assertThat(client().privateProviders().create().name("Acme").build().getHttpMethod())
                .isEqualTo("POST");

        assertThat(client().privateProviders().get("acme").build().getRelativeUrl())
                .isEqualTo("/private-providers/acme");
        assertThat(client().privateProviders().get("acme").build().getHttpMethod())
                .isEqualTo("GET");

        assertThat(client().privateProviders().update("acme").displayName("Acme").build().getRelativeUrl())
                .isEqualTo("/private-providers/acme");
        assertThat(client().privateProviders().update("acme").displayName("Acme").build().getHttpMethod())
                .isEqualTo("PATCH");

        assertThat(client().privateProviders().delete("acme").build().getRelativeUrl())
                .isEqualTo("/private-providers/acme");
        assertThat(client().privateProviders().delete("acme").build().getHttpMethod())
                .isEqualTo("DELETE");
    }

    @Test
    void bySlugRoutesUrlEncodeTheSlug() {
        assertThat(client().privateProviders().get("team member/42").build().getRelativeUrl())
                .isEqualTo("/private-providers/team+member%2F42");
        assertThat(client().privateProviders().update("team member/42").displayName("T").build().getRelativeUrl())
                .isEqualTo("/private-providers/team+member%2F42");
        assertThat(client().privateProviders().delete("team member/42").build().getRelativeUrl())
                .isEqualTo("/private-providers/team+member%2F42");
    }

    @Test
    void createEmitsOnlyTheConfiguredFields() {
        JSONObject body = new JSONObject(client().privateProviders()
                .create()
                .name("Acme")
                .build()
                .getBody());

        assertThat(body.keySet()).containsExactlyInAnyOrder("name");
        assertThat(body.getString("name")).isEqualTo("Acme");

        JSONObject full = new JSONObject(client().privateProviders()
                .create()
                .name("Acme Inference")
                .slug("acme-inference")
                .displayName("Acme")
                .baseUrl("https://inference.example.com/v1")
                .build()
                .getBody());

        assertThat(full.keySet()).containsExactlyInAnyOrder("name", "slug", "display_name", "base_url");
        assertThat(full.getString("name")).isEqualTo("Acme Inference");
        assertThat(full.getString("slug")).isEqualTo("acme-inference");
        assertThat(full.getString("display_name")).isEqualTo("Acme");
        assertThat(full.getString("base_url")).isEqualTo("https://inference.example.com/v1");

        // null unsets an option again - the key disappears from the body
        JSONObject unset = new JSONObject(client().privateProviders()
                .create()
                .name("Acme")
                .slug("acme")
                .displayName("Acme")
                .baseUrl("https://inference.example.com/v1")
                .slug(null)
                .displayName(null)
                .baseUrl(null)
                .build()
                .getBody());
        assertThat(unset.keySet()).containsExactlyInAnyOrder("name");
    }

    @Test
    void updateEmitsOnlyTheConfiguredFields() {
        JSONObject body = new JSONObject(client().privateProviders()
                .update("acme")
                .displayName("Acme")
                .build()
                .getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder("display_name");
        assertThat(body.getString("display_name")).isEqualTo("Acme");

        JSONObject full = new JSONObject(client().privateProviders()
                .update("acme")
                .displayName("Acme")
                .baseUrl("https://inference.example.com/v1")
                .build()
                .getBody());
        assertThat(full.keySet()).containsExactlyInAnyOrder("display_name", "base_url");
        assertThat(full.getString("base_url")).isEqualTo("https://inference.example.com/v1");

        // null unsets an option again - the key disappears from the body
        JSONObject unset = new JSONObject(client().privateProviders()
                .update("acme")
                .displayName("Acme")
                .baseUrl("https://inference.example.com/v1")
                .displayName(null)
                .build()
                .getBody());
        assertThat(unset.keySet()).containsExactlyInAnyOrder("base_url");
    }

    @Test
    void createValidatesTheNameContract() {
        assertThatThrownBy(() -> client().privateProviders().create().name(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name is required");
        assertThatThrownBy(() -> client().privateProviders().create().name(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2-64");
        assertThatThrownBy(() -> client().privateProviders().create().name("A"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2-64");
        assertThatThrownBy(() -> client().privateProviders().create().name("a".repeat(65)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2-64");
        // the documented character set: letters, numbers, spaces, dots,
        // underscores and hyphens, starting with a letter or number
        assertThatThrownBy(() -> client().privateProviders().create().name(".Acme"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("^[A-Za-z0-9][A-Za-z0-9 ._-]*$");
        assertThatThrownBy(() -> client().privateProviders().create().name(" leading"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().privateProviders().create().name("Acme!"))
                .isInstanceOf(IllegalArgumentException.class);
        // the boundary values pass
        assertThat(client().privateProviders().create().name("Ab").build().getBody())
                .contains("\"name\":\"Ab\"");
        assertThat(client().privateProviders().create().name("A b.c_d-e".repeat(7) + "f")
                .build().getBody()).isNotNull();
    }

    @Test
    void createValidatesTheSlugContract() {
        // null is a legal "unset" - the API derives the slug from the name
        assertThat(new JSONObject(client().privateProviders().create().name("Acme").slug(null)
                .build().getBody()).keySet()).containsExactlyInAnyOrder("name");
        assertThatThrownBy(() -> client().privateProviders().create().slug(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2-64");
        assertThatThrownBy(() -> client().privateProviders().create().slug("a"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2-64");
        assertThatThrownBy(() -> client().privateProviders().create().slug("a".repeat(65)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2-64");
        assertThatThrownBy(() -> client().privateProviders().create().slug("Acme"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("^[a-z0-9]+(?:-[a-z0-9]+)*$");
        assertThatThrownBy(() -> client().privateProviders().create().slug("-acme"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().privateProviders().create().slug("acme-"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().privateProviders().create().slug("acme--inference"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().privateProviders().create().slug("acme_inference"))
                .isInstanceOf(IllegalArgumentException.class);
        // the boundary values pass
        assertThat(client().privateProviders().create().name("Acme").slug("ab").build().getBody())
                .contains("\"slug\":\"ab\"");
        assertThat(client().privateProviders().create().name("Acme").slug("a" + "b".repeat(63))
                .build().getBody()).isNotNull();
    }

    @Test
    void displayNameAndBaseUrlValidateTheirDocumentedRanges() {
        assertThatThrownBy(() -> client().privateProviders().create().displayName(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1-80");
        assertThatThrownBy(() -> client().privateProviders().create().displayName("a".repeat(81)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1-80");
        assertThatThrownBy(() -> client().privateProviders().create().baseUrl(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("base_url");
        assertThatThrownBy(() -> client().privateProviders().update("acme").displayName("a".repeat(81)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().privateProviders().update("acme").baseUrl(""))
                .isInstanceOf(IllegalArgumentException.class);
        // the boundary values pass
        assertThat(new JSONObject(client().privateProviders().create().name("Acme")
                .displayName("A").baseUrl("x").build().getBody()).keySet())
                .containsExactlyInAnyOrder("name", "display_name", "base_url");
        assertThat(new JSONObject(client().privateProviders().create().name("Acme")
                .displayName("a".repeat(80)).build().getBody()).keySet())
                .containsExactlyInAnyOrder("name", "display_name");
    }

    @Test
    void createRequiresNameAndPatchRequiresAnEditableField() {
        assertThatThrownBy(() -> client().privateProviders().create().build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("name is required");
        assertThatThrownBy(() -> client().privateProviders().update("acme").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no editable field");
        // displayName-only and baseUrl-only are both valid PATCH bodies
        assertThat(client().privateProviders().update("acme").displayName("A").build().getBody())
                .isNotNull();
        assertThat(client().privateProviders().update("acme").baseUrl("https://x.example").build().getBody())
                .isNotNull();
    }

    @Test
    void bySlugRoutesRejectMissingSlugsLoudly() {
        assertThatThrownBy(() -> client().privateProviders().get(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("slug is required");
        assertThatThrownBy(() -> client().privateProviders().get(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().privateProviders().update(null).displayName("A"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().privateProviders().update(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().privateProviders().delete(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().privateProviders().delete(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void readBackAccessors() {
        OpenRouterPrivateProviderCreateRequest create = client().privateProviders().create()
                .name("Acme Inference")
                .slug("acme-inference")
                .displayName("Acme")
                .baseUrl("https://inference.example.com/v1")
                .build();
        assertThat(create.name()).isEqualTo("Acme Inference");
        assertThat(create.slug()).isEqualTo("acme-inference");
        assertThat(create.displayName()).isEqualTo("Acme");
        assertThat(create.baseUrl()).isEqualTo("https://inference.example.com/v1");

        OpenRouterPrivateProviderCreateRequest bare = client().privateProviders().create()
                .name("Acme").build();
        assertThat(bare.slug()).isNull();
        assertThat(bare.displayName()).isNull();
        assertThat(bare.baseUrl()).isNull();

        OpenRouterPrivateProviderUpdateRequest update = client().privateProviders().update("acme")
                .displayName("Acme")
                .build();
        assertThat(update.slug()).isEqualTo("acme");
        assertThat(update.displayName()).isEqualTo("Acme");
        assertThat(update.baseUrl()).isNull();

        // the by-slug accessors hand back the URL-encoded path segment
        assertThat(client().privateProviders().get("team member").build().slug())
                .isEqualTo("team+member");
        assertThat(client().privateProviders().delete("team member").build().slug())
                .isEqualTo("team+member");
    }

    @Test
    void providerViewExposesEveryField() {
        OpenRouterPrivateProvider provider = new OpenRouterPrivateProvider(new JSONObject("""
                {
                  "name": "Acme Inference",
                  "slug": "acme-inference",
                  "display_name": "Acme",
                  "base_url": "https://inference.example.com/v1",
                  "created_at": "2026-11-01T12:00:00.000Z"
                }
                """));

        assertThat(provider.name()).isEqualTo("Acme Inference");
        assertThat(provider.slug()).isEqualTo("acme-inference");
        assertThat(provider.displayName()).isEqualTo("Acme");
        assertThat(provider.baseUrl()).isEqualTo("https://inference.example.com/v1");
        assertThat(provider.createdAt()).isEqualTo("2026-11-01T12:00:00.000Z");
        assertThat(provider.json().getString("slug")).isEqualTo("acme-inference");

        // base_url is nullable in the schema: null means the provider carries
        // no URL of its own
        OpenRouterPrivateProvider withoutUrl = new OpenRouterPrivateProvider(new JSONObject("""
                {"name": "Acme", "slug": "acme", "display_name": "Acme",
                 "base_url": null, "created_at": "2026-11-01T12:00:00.000Z"}
                """));
        assertThat(withoutUrl.baseUrl()).isNull();
        assertThat(withoutUrl.name()).isEqualTo("Acme");

        OpenRouterPrivateProvider withoutUrlKey = new OpenRouterPrivateProvider(new JSONObject("""
                {"name": "Acme", "slug": "acme", "display_name": "Acme",
                 "created_at": "2026-11-01T12:00:00.000Z"}
                """));
        assertThat(withoutUrlKey.baseUrl()).isNull();
    }

    @Test
    void providerViewSwallowsWrongTypedScalars() {
        OpenRouterPrivateProvider malformed = new OpenRouterPrivateProvider(new JSONObject("""
                {
                  "name": 42,
                  "slug": true,
                  "display_name": [],
                  "base_url": 7,
                  "created_at": {}
                }
                """));

        assertThat(malformed.name()).isNull();
        assertThat(malformed.slug()).isNull();
        assertThat(malformed.displayName()).isNull();
        assertThat(malformed.baseUrl()).isNull();
        assertThat(malformed.createdAt()).isNull();
    }

    @Test
    void listResponseAccessors() {
        OpenRouterPrivateProvidersListRequest request = client().privateProviders().list().build();
        OpenRouterPrivateProvidersListResponse<OpenRouterPrivateProvidersListRequest> response =
                new OpenRouterPrivateProvidersListResponse<>(new JSONObject("""
                        {
                          "data": [
                            {"name": "Acme", "slug": "acme", "display_name": "Acme",
                             "base_url": "https://inference.example.com/v1",
                             "created_at": "2026-11-01T12:00:00.000Z"},
                            {"name": "Beta", "slug": "beta", "display_name": "Beta",
                             "base_url": null, "created_at": "2026-11-02T12:00:00.000Z"}
                          ]
                        }
                        """), request);

        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).slug()).isEqualTo("acme");
        assertThat(response.items().get(1).slug()).isEqualTo("beta");
        assertThat(response.items().get(1).baseUrl()).isNull();
    }

    @Test
    void listResponseSwallowsMalformedValues() {
        OpenRouterPrivateProvidersListRequest request = client().privateProviders().list().build();

        // non-object rows are dropped while good ones survive
        OpenRouterPrivateProvidersListResponse<OpenRouterPrivateProvidersListRequest> mixed =
                new OpenRouterPrivateProvidersListResponse<>(new JSONObject("""
                        {"data": ["not-an-object", {"name": "Acme", "slug": "acme",
                          "display_name": "Acme", "base_url": null,
                          "created_at": "2026-11-01T12:00:00.000Z"}]}
                        """), request);
        assertThat(mixed.items()).hasSize(1);
        assertThat(mixed.items().get(0).slug()).isEqualTo("acme");

        // a non-array data answers empty, and so does an empty object
        assertThat(new OpenRouterPrivateProvidersListResponse<>(
                new JSONObject("{\"data\": \"not-an-array\"}"), request).items()).isEmpty();
        assertThat(new OpenRouterPrivateProvidersListResponse<>(
                new JSONObject("{}"), request).items()).isEmpty();
    }

    @Test
    void singleProviderResponseAccessors() {
        OpenRouterPrivateProviderGetRequest request = client().privateProviders().get("acme").build();
        OpenRouterPrivateProviderResponse<OpenRouterPrivateProviderGetRequest> response =
                new OpenRouterPrivateProviderResponse<>(new JSONObject("""
                        {"data": {
                          "name": "Acme",
                          "slug": "acme",
                          "display_name": "Acme",
                          "base_url": null,
                          "created_at": "2026-11-01T12:00:00.000Z"
                        }}
                        """), request);

        assertThat(response.data().name()).isEqualTo("Acme");
        assertThat(response.data().slug()).isEqualTo("acme");
        assertThat(response.data().createdAt()).isEqualTo("2026-11-01T12:00:00.000Z");
        assertThat(response.data().baseUrl()).isNull();
        assertThat(response.data().json().getString("name")).isEqualTo("Acme");

        // absent or non-object data answers null
        assertThat(new OpenRouterPrivateProviderResponse<>(new JSONObject("{}"), request).data())
                .isNull();
        assertThat(new OpenRouterPrivateProviderResponse<>(
                new JSONObject("{\"data\": \"not-an-object\"}"), request).data()).isNull();
    }

    @Test
    void deleteResponseExposesTheDeletedSlug() {
        OpenRouterPrivateProviderDeleteRequest request =
                client().privateProviders().delete("acme").build();
        OpenRouterPrivateProviderDeleteResponse<OpenRouterPrivateProviderDeleteRequest> response =
                new OpenRouterPrivateProviderDeleteResponse<>(new JSONObject("""
                        {"data": {"slug": "acme"}}
                        """), request);

        assertThat(response.slug()).isEqualTo("acme");

        // wrong-typed and absent values answer null
        assertThat(new OpenRouterPrivateProviderDeleteResponse<>(
                new JSONObject("{\"data\": {\"slug\": 42}}"), request).slug()).isNull();
        assertThat(new OpenRouterPrivateProviderDeleteResponse<>(
                new JSONObject("{\"data\": \"not-an-object\"}"), request).slug()).isNull();
        assertThat(new OpenRouterPrivateProviderDeleteResponse<>(
                new JSONObject("{}"), request).slug()).isNull();
    }

    @Test
    void everyResponseToleratesEmptyAndNullBodies() {
        OpenRouterPrivateProvidersListResponse<OpenRouterPrivateProvidersListRequest> emptyList =
                client().privateProviders().list().build().createResponse("");
        assertThat(emptyList.items()).isEmpty();
        assertThat(client().privateProviders().list().build().createResponse((String) null).items())
                .isEmpty();

        OpenRouterPrivateProviderCreateRequest create =
                client().privateProviders().create().name("Acme").build();
        assertThat(create.createResponse("").data()).isNull();
        assertThat(create.createResponse((String) null).data()).isNull();

        OpenRouterPrivateProviderGetRequest get = client().privateProviders().get("acme").build();
        assertThat(get.createResponse("").data()).isNull();
        assertThat(get.createResponse((String) null).data()).isNull();

        OpenRouterPrivateProviderUpdateRequest update =
                client().privateProviders().update("acme").displayName("Acme").build();
        assertThat(update.createResponse("").data()).isNull();
        assertThat(update.createResponse((String) null).data()).isNull();

        // the delete route documents 200 with data.slug, but an empty body
        // (e.g. a 204 from other tooling) must not fail either
        OpenRouterPrivateProviderDeleteRequest delete =
                client().privateProviders().delete("acme").build();
        assertThat(delete.createResponse("").slug()).isNull();
        assertThat(delete.createResponse((String) null).slug()).isNull();
    }

    @Test
    void getBodyAndMethodContractOfEveryRoute() {
        assertThat(client().privateProviders().list().build().getBody()).isNull();
        assertThat(client().privateProviders().get("acme").build().getBody()).isNull();
        assertThat(client().privateProviders().delete("acme").build().getBody()).isNull();
        assertThat(new JSONObject(client().privateProviders().create().name("Acme")
                .build().getBody())).isNotNull();
        assertThat(new JSONObject(client().privateProviders().update("acme").displayName("A")
                .build().getBody())).isNotNull();
    }
}
