package de.entwicklertraining.openrouter4j.oauth;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the OAuth authorization-code request shapes, the PKCE helper (with
 * the RFC 7636 appendix-B test vector) and the response accessors against
 * recorded JSON shapes of POST /auth/keys/code and POST /auth/keys.
 */
class OpenRouterOAuthTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void createRequestUsesPostMethodOnAuthCodeUrl() {
        OpenRouterCreateAuthorizationCodeRequest request =
                new OpenRouterCreateAuthorizationCodeRequest.Builder(client())
                        .callbackUrl("https://myapp.com/auth/callback")
                        .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/auth/keys/code");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
    }

    @Test
    void createRequestEmitsOnlyTheSetFields() {
        JSONObject minimal = new JSONObject(new OpenRouterCreateAuthorizationCodeRequest.Builder(client())
                .callbackUrl("https://myapp.com/auth/callback")
                .build()
                .getBody());

        assertThat(minimal.toMap()).containsOnlyKeys("callback_url");

        JSONObject full = new JSONObject(new OpenRouterCreateAuthorizationCodeRequest.Builder(client())
                .callbackUrl("https://myapp.com/auth/callback")
                .codeChallenge("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM")
                .codeChallengeMethod("S256")
                .expiresAt("2027-12-31T23:59:59Z")
                .keyLabel("My Custom Key")
                .limit(100.0)
                .usageLimitType("monthly")
                .workspaceId("4b2f7d1e-8c3a-4e5f-9a6b-1c2d3e4f5a6b")
                .build()
                .getBody());

        assertThat(full.getString("callback_url")).isEqualTo("https://myapp.com/auth/callback");
        assertThat(full.getString("code_challenge")).isEqualTo("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM");
        assertThat(full.getString("code_challenge_method")).isEqualTo("S256");
        assertThat(full.getString("expires_at")).isEqualTo("2027-12-31T23:59:59Z");
        assertThat(full.getString("key_label")).isEqualTo("My Custom Key");
        assertThat(full.getDouble("limit")).isEqualTo(100.0);
        assertThat(full.getString("usage_limit_type")).isEqualTo("monthly");
        assertThat(full.getString("workspace_id")).isEqualTo("4b2f7d1e-8c3a-4e5f-9a6b-1c2d3e4f5a6b");
    }

    @Test
    void createRequestRejectsMissingCallbackUrl() {
        assertThatThrownBy(() -> new OpenRouterCreateAuthorizationCodeRequest.Builder(client()).build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createResponseAccessorsAreSurfaced() {
        OpenRouterCreateAuthorizationCodeRequest request =
                new OpenRouterCreateAuthorizationCodeRequest.Builder(client())
                        .callbackUrl("https://myapp.com/auth/callback")
                        .build();
        OpenRouterCreateAuthorizationCodeResponse response = request.createResponse("""
                {"data": {"id": "auth_code_xyz789", "app_id": 12345, "created_at": "2025-08-24T10:30:00Z"}}
                """);

        assertThat(response.id()).isEqualTo("auth_code_xyz789");
        assertThat(response.appId()).isEqualTo(12345L);
        assertThat(response.createdAt()).isEqualTo("2025-08-24T10:30:00Z");
        assertThat(response.data()).isNotNull();
    }

    @Test
    void createResponseAccessorsReturnNullWhenAbsentOrMalformed() {
        OpenRouterCreateAuthorizationCodeRequest request =
                new OpenRouterCreateAuthorizationCodeRequest.Builder(client())
                        .callbackUrl("https://myapp.com/auth/callback")
                        .build();

        OpenRouterCreateAuthorizationCodeResponse empty = request.createResponse("{}");
        assertThat(empty.id()).isNull();
        assertThat(empty.appId()).isNull();
        assertThat(empty.createdAt()).isNull();
        assertThat(empty.data()).isNull();

        OpenRouterCreateAuthorizationCodeResponse malformed =
                request.createResponse("{\"data\": \"not-an-object\"}");
        assertThat(malformed.id()).isNull();
        assertThat(malformed.appId()).isNull();
        assertThat(malformed.createdAt()).isNull();
    }

    @Test
    void exchangeRequestUsesPostMethodOnAuthKeysUrl() {
        OpenRouterAuthorizationCodeExchangeRequest request =
                new OpenRouterAuthorizationCodeExchangeRequest.Builder(client())
                        .code("auth_code_abc123def456")
                        .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/auth/keys");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
    }

    @Test
    void exchangeRequestEmitsOnlyTheSetFields() {
        JSONObject minimal = new JSONObject(new OpenRouterAuthorizationCodeExchangeRequest.Builder(client())
                .code("auth_code_abc123def456")
                .build()
                .getBody());

        assertThat(minimal.toMap()).containsOnlyKeys("code");

        JSONObject full = new JSONObject(new OpenRouterAuthorizationCodeExchangeRequest.Builder(client())
                .code("auth_code_abc123def456")
                .codeVerifier("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk")
                .codeChallengeMethod("S256")
                .build()
                .getBody());

        assertThat(full.getString("code")).isEqualTo("auth_code_abc123def456");
        assertThat(full.getString("code_verifier")).isEqualTo("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk");
        assertThat(full.getString("code_challenge_method")).isEqualTo("S256");
    }

    @Test
    void exchangeRequestRejectsMissingCode() {
        assertThatThrownBy(() -> new OpenRouterAuthorizationCodeExchangeRequest.Builder(client()).build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exchangeResponseAccessorsAreSurfaced() {
        OpenRouterAuthorizationCodeExchangeRequest request =
                new OpenRouterAuthorizationCodeExchangeRequest.Builder(client())
                        .code("auth_code_abc123def456")
                        .build();
        OpenRouterAuthorizationCodeExchangeResponse response = request.createResponse("""
                {"key": "sk-or-v1-example-key-not-real",
                 "user_id": "user_2yOPcMpKoQhcd4bVgSMlELRaIah"}
                """);

        assertThat(response.key())
                .isEqualTo("sk-or-v1-example-key-not-real");
        assertThat(response.userId()).isEqualTo("user_2yOPcMpKoQhcd4bVgSMlELRaIah");
    }

    @Test
    void exchangeResponseAccessorsReturnNullWhenAbsent() {
        OpenRouterAuthorizationCodeExchangeRequest request =
                new OpenRouterAuthorizationCodeExchangeRequest.Builder(client())
                        .code("auth_code_abc123def456")
                        .build();

        OpenRouterAuthorizationCodeExchangeResponse empty = request.createResponse("{}");
        assertThat(empty.key()).isNull();
        assertThat(empty.userId()).isNull();

        OpenRouterAuthorizationCodeExchangeResponse malformed = request.createResponse("{\"key\": 42}");
        assertThat(malformed.key()).isEqualTo("42");
        assertThat(malformed.userId()).isNull();
    }

    @Test
    void pkceHelperMatchesTheRfc7636AppendixBVector() {
        String verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";

        assertThat(OpenRouterPkce.codeChallengeS256(verifier))
                .isEqualTo("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM");
    }

    @Test
    void pkceHelperRejectsInvalidVerifiers() {
        assertThatThrownBy(() -> OpenRouterPkce.codeChallengeS256(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterPkce.codeChallengeS256("short"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterPkce.codeChallengeS256("a".repeat(129)))
                .isInstanceOf(IllegalArgumentException.class);
        // invalid alphabet characters
        assertThatThrownBy(() -> OpenRouterPkce.codeChallengeS256("a".repeat(42) + "+"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void pkceGeneratedVerifiersAreRandomValidAndChallengable() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            String verifier = OpenRouterPkce.generateCodeVerifier();
            assertThat(verifier.length()).isBetween(43, 128);
            assertThat(verifier).matches("[A-Za-z0-9._~-]+");
            assertThat(OpenRouterPkce.codeChallengeS256(verifier)).matches("[A-Za-z0-9_-]+");
            seen.add(verifier);
        }
        assertThat(seen).hasSize(20);
    }
}
