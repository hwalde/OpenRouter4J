package de.entwicklertraining.openrouter4j.externalusers;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the external-users request shapes and the response accessors against
 * recorded JSON shapes of the /external-users routes.
 */
class OpenRouterExternalUsersTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void fiveRoutesProduceTheDocumentedUrlsAndMethods() {
        assertThat(client().externalUsers().list().build().getRelativeUrl()).isEqualTo("/external-users");
        assertThat(client().externalUsers().list().build().getHttpMethod()).isEqualTo("GET");

        assertThat(client().externalUsers().register("employee_123").build().getRelativeUrl())
                .isEqualTo("/external-users");
        assertThat(client().externalUsers().register("employee_123").build().getHttpMethod())
                .isEqualTo("POST");

        assertThat(client().externalUsers().get("employee_123").build().getRelativeUrl())
                .isEqualTo("/external-users/employee_123");
        assertThat(client().externalUsers().get("employee_123").build().getHttpMethod())
                .isEqualTo("GET");

        assertThat(client().externalUsers().update("employee_123", true).build().getRelativeUrl())
                .isEqualTo("/external-users/employee_123");
        assertThat(client().externalUsers().update("employee_123", true).build().getHttpMethod())
                .isEqualTo("PATCH");

        assertThat(client().externalUsers().deactivate("employee_123").build().getRelativeUrl())
                .isEqualTo("/external-users/employee_123");
        assertThat(client().externalUsers().deactivate("employee_123").build().getHttpMethod())
                .isEqualTo("DELETE");
    }

    @Test
    void byUserRoutesUrlEncodeTheTrackingString() {
        assertThat(client().externalUsers().get("team member/42").build().getRelativeUrl())
                .isEqualTo("/external-users/team+member%2F42");
        assertThat(client().externalUsers().update("team member/42", false).build().getRelativeUrl())
                .isEqualTo("/external-users/team+member%2F42");
        assertThat(client().externalUsers().deactivate("team member/42").build().getRelativeUrl())
                .isEqualTo("/external-users/team+member%2F42");
    }

    @Test
    void registerEmitsTheUserBody() {
        JSONObject body = new JSONObject(client().externalUsers()
                .register("employee_123")
                .build()
                .getBody());

        assertThat(body.keySet()).containsExactlyInAnyOrder("user");
        assertThat(body.getString("user")).isEqualTo("employee_123");
    }

    @Test
    void updateEmitsIsActiveAsAJsonBoolean() {
        JSONObject body = new JSONObject(client().externalUsers()
                .update("employee_123", true)
                .build()
                .getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder("is_active");
        assertThat(body.get("is_active")).isEqualTo(Boolean.TRUE);

        JSONObject bodyOff = new JSONObject(client().externalUsers()
                .update("employee_123", false)
                .build()
                .getBody());
        // the false half matters: it is the only value that distinguishes
        // the option from an unset boolean
        assertThat(bodyOff.get("is_active")).isEqualTo(Boolean.FALSE);
    }

    @Test
    void listEmitsEveryTypedQueryParameter() {
        assertThat(client().externalUsers().list()
                .offset(10).limit(25).user("employee_123").includeInactive(true)
                .build().getRelativeUrl())
                .isEqualTo("/external-users?offset=10&limit=25&user=employee_123&include_inactive=true");

        assertThat(client().externalUsers().list().includeInactive(false).build().getRelativeUrl())
                .isEqualTo("/external-users?include_inactive=false");

        // values are URL-encoded like the path segments
        assertThat(client().externalUsers().list().user("team member/42").build().getRelativeUrl())
                .isEqualTo("/external-users?user=team+member%2F42");

        // unset leaves the URL shape unchanged
        assertThat(client().externalUsers().list().build().getRelativeUrl())
                .isEqualTo("/external-users");
        assertThat(client().externalUsers().list()
                .offset(1).limit(2).user("u").includeInactive(true)
                .offset(null).limit(null).user(null).includeInactive(null)
                .build().getRelativeUrl())
                .isEqualTo("/external-users");
    }

    @Test
    void listReadBackAccessors() {
        OpenRouterExternalUsersListRequest request = client().externalUsers().list()
                .offset(10).limit(25).user("employee_123").includeInactive(true)
                .build();

        assertThat(request.offset()).isEqualTo(10);
        assertThat(request.limit()).isEqualTo(25);
        assertThat(request.user()).isEqualTo("employee_123");
        assertThat(request.includeInactive()).isTrue();

        OpenRouterExternalUsersListRequest bare = client().externalUsers().list().build();
        assertThat(bare.offset()).isNull();
        assertThat(bare.limit()).isNull();
        assertThat(bare.user()).isNull();
        assertThat(bare.includeInactive()).isNull();
    }

    @Test
    void registerValidatesTheTrackingStringContract() {
        assertThatThrownBy(() -> client().externalUsers().register(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().externalUsers().register(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().externalUsers().register("a".repeat(513)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().externalUsers().register("a\u0000b"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().externalUsers().register(" leading"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().externalUsers().register("trailing "))
                .isInstanceOf(IllegalArgumentException.class);
        // the boundary values pass
        assertThat(client().externalUsers().register("a".repeat(512)).build().getBody())
                .contains("a".repeat(512));
    }

    @Test
    void byUserRoutesRejectPathUnaddressableIdsLoudly() {
        for (String id : List.of(".", "..")) {
            assertThatThrownBy(() -> client().externalUsers().get(id))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("list filter");
            assertThatThrownBy(() -> client().externalUsers().update(id, true))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> client().externalUsers().deactivate(id))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> client().externalUsers().get(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().externalUsers().get(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void listValidatesPaginationLoudly() {
        assertThatThrownBy(() -> client().externalUsers().list().limit(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().externalUsers().list().limit(101))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().externalUsers().list().offset(-1))
                .isInstanceOf(IllegalArgumentException.class);
        // the boundary values pass
        assertThat(client().externalUsers().list().limit(1).offset(0).build().getRelativeUrl())
                .isEqualTo("/external-users?offset=0&limit=1");
        assertThat(client().externalUsers().list().limit(100).build().getRelativeUrl())
                .isEqualTo("/external-users?limit=100");
    }

    @Test
    void singleRegistrationResponseAccessors() {
        OpenRouterExternalUserGetRequest request = client().externalUsers().get("employee_123").build();
        OpenRouterExternalUserResponse<OpenRouterExternalUserGetRequest> response =
                new OpenRouterExternalUserResponse<>(new JSONObject("""
                        {"data": {
                          "user": "employee_123",
                          "is_active": true,
                          "created_at": "2026-09-16T12:00:00Z",
                          "updated_at": "2026-09-16T12:00:00Z"
                        }}
                        """), request);

        assertThat(response.data().user()).isEqualTo("employee_123");
        assertThat(response.data().isActive()).isTrue();
        assertThat(response.data().createdAt()).isEqualTo("2026-09-16T12:00:00Z");
        assertThat(response.data().updatedAt()).isEqualTo("2026-09-16T12:00:00Z");
        assertThat(response.data().json().getString("user")).isEqualTo("employee_123");

        // is_active false is a real value, not the absent answer
        OpenRouterExternalUserResponse<OpenRouterExternalUserGetRequest> inactive =
                new OpenRouterExternalUserResponse<>(new JSONObject(
                        "{\"data\": {\"user\": \"u\", \"is_active\": false, "
                                + "\"created_at\": \"c\", \"updated_at\": \"u\"}}"), request);
        assertThat(inactive.data().isActive()).isFalse();
    }

    @Test
    void listResponseAccessors() {
        OpenRouterExternalUsersListRequest request = client().externalUsers().list().build();
        OpenRouterExternalUsersListResponse<OpenRouterExternalUsersListRequest> response =
                new OpenRouterExternalUsersListResponse<>(new JSONObject("""
                        {
                          "data": [
                            {"user": "a", "is_active": true,
                             "created_at": "2026-09-16T12:00:00Z", "updated_at": "2026-09-16T12:00:00Z"},
                            {"user": "b", "is_active": false,
                             "created_at": "2026-09-16T12:00:00Z", "updated_at": "2026-09-16T12:00:00Z"}
                          ],
                          "total_count": 17
                        }
                        """), request);

        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).user()).isEqualTo("a");
        assertThat(response.items().get(0).isActive()).isTrue();
        assertThat(response.items().get(1).user()).isEqualTo("b");
        assertThat(response.items().get(1).isActive()).isFalse();
        assertThat(response.totalCount()).isEqualTo(17L);
    }

    @Test
    void responseAccessorsSwallowMalformedValues() {
        OpenRouterExternalUserGetRequest request = client().externalUsers().get("u").build();
        OpenRouterExternalUserResponse<OpenRouterExternalUserGetRequest> malformed =
                new OpenRouterExternalUserResponse<>(new JSONObject("""
                        {"data": {
                          "user": 42,
                          "is_active": "not-a-boolean",
                          "created_at": 7,
                          "updated_at": null
                        }}
                        """), request);

        assertThat(malformed.data().user()).isNull();
        assertThat(malformed.data().isActive()).isNull();
        assertThat(malformed.data().createdAt()).isNull();
        assertThat(malformed.data().updatedAt()).isNull();

        // absent or non-object data answers null
        assertThat(new OpenRouterExternalUserResponse<>(new JSONObject("{}"), request).data()).isNull();
        assertThat(new OpenRouterExternalUserResponse<>(
                new JSONObject("{\"data\": \"not-an-object\"}"), request).data()).isNull();

        // the list view drops non-object rows while good ones survive, and
        // swallows a wrong-typed total_count
        OpenRouterExternalUsersListRequest listRequest = client().externalUsers().list().build();
        OpenRouterExternalUsersListResponse<OpenRouterExternalUsersListRequest> malformedList =
                new OpenRouterExternalUsersListResponse<>(new JSONObject("""
                        {"data": ["not-an-object", {"user": "good", "is_active": true,
                          "created_at": "c", "updated_at": "u"}], "total_count": "many"}
                        """), listRequest);
        assertThat(malformedList.items()).hasSize(1);
        assertThat(malformedList.items().get(0).user()).isEqualTo("good");
        assertThat(malformedList.totalCount()).isNull();

        // absent fields answer empty/null
        OpenRouterExternalUsersListResponse<OpenRouterExternalUsersListRequest> empty =
                new OpenRouterExternalUsersListResponse<>(new JSONObject("{}"), listRequest);
        assertThat(empty.items()).isEmpty();
        assertThat(empty.totalCount()).isNull();
    }

    @Test
    void deactivateResponseToleratesTheEmptyBody() {
        OpenRouterExternalUserDeactivateRequest request =
                client().externalUsers().deactivate("employee_123").build();
        OpenRouterExternalUserDeactivateResponse response = request.createResponse("");
        assertThat(response.json().isEmpty()).isTrue();
    }

    @Test
    void createAndUpdateReadBackAccessors() {
        assertThat(client().externalUsers().register("employee_123").build().user())
                .isEqualTo("employee_123");
        assertThat(client().externalUsers().update("employee_123", false).build().user())
                .isEqualTo("employee_123");
        assertThat(client().externalUsers().update("employee_123", false).build().isActive())
                .isFalse();
        assertThat(client().externalUsers().get("team member").build().user())
                .isEqualTo("team+member");
        assertThat(client().externalUsers().deactivate("team member").build().user())
                .isEqualTo("team+member");
    }

    @Test
    void everyRouteValidatesTheTrackingStringContract() {
        for (String bad : List.of(" leading", "trailing ", "a".repeat(513), "a\u0000b")) {
            assertThatThrownBy(() -> client().externalUsers().register(bad))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> client().externalUsers().get(bad))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> client().externalUsers().update(bad, true))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> client().externalUsers().deactivate(bad))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> client().externalUsers().list().user(bad))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        // the 512-character boundary passes on the by-user routes too
        assertThat(client().externalUsers().get("a".repeat(512)).build().getRelativeUrl())
                .isEqualTo("/external-users/" + "a".repeat(512));
    }

    @Test
    void trackingStringsMustBeWellFormedUnicode() {
        // an unpaired surrogate is not well-formed Unicode
        assertThatThrownBy(() -> client().externalUsers().register("\uD800"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("well-formed");
        assertThatThrownBy(() -> client().externalUsers().get("a\uDC00"))
                .isInstanceOf(IllegalArgumentException.class);
        // a valid surrogate pair counts as ONE character against the 512
        // limit (code points, not UTF-16 units)
        String emojis = "😀".repeat(512);
        assertThat(client().externalUsers().register(emojis).build().getBody())
                .contains(emojis);
        assertThatThrownBy(() -> client().externalUsers().register("😀".repeat(513)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dotIdsRemainReachableViaRegisterAndTheListFilter() {
        // the documented escape path for ids the by-user routes reject
        assertThat(client().externalUsers().register(".").build().getBody())
                .contains("\"user\":\".\"");
        assertThat(client().externalUsers().list().user("..").build().getRelativeUrl())
                .isEqualTo("/external-users?user=..");
    }

    @Test
    void getAndUpdateResponsesTolerateEmptyBodies() {
        OpenRouterExternalUserGetRequest get = client().externalUsers().get("u").build();
        assertThat(get.createResponse("").data()).isNull();
        assertThat(get.createResponse((String) null).data()).isNull();
        OpenRouterExternalUserUpdateRequest update =
                client().externalUsers().update("u", true).build();
        assertThat(update.createResponse("").data()).isNull();
        assertThat(update.createResponse((String) null).data()).isNull();
    }
}
