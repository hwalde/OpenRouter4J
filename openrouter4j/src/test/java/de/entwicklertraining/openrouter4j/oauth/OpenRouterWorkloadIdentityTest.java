package de.entwicklertraining.openrouter4j.oauth;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the workload identity token-exchange request shape (RFC 8693,
 * urlencoded form body), the fixed form constants and the response accessors
 * against recorded JSON shapes of POST /oauth/token.
 */
class OpenRouterWorkloadIdentityTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void requestUsesPostMethodOnOauthTokenUrlWithFormContentType() {
        OpenRouterWorkloadIdentityExchangeRequest request =
                new OpenRouterWorkloadIdentityExchangeRequest.Builder(client())
                        .subjectToken("header.payload.signature")
                        .federationPolicyId("4b2f7d1e-8c3a-4e5f-9a6b-1c2d3e4f5a6b")
                        .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/oauth/token");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
        assertThat(request.getContentType()).isEqualTo("application/x-www-form-urlencoded");
    }

    @Test
    void bodyIsUrlencodedFormWithFixedConstantsAndOnlySetOptionals() {
        String minimal = new OpenRouterWorkloadIdentityExchangeRequest.Builder(client())
                .subjectToken("header.payload.signature")
                .federationPolicyId("4b2f7d1e-8c3a-4e5f-9a6b-1c2d3e4f5a6b")
                .build()
                .getBody();

        Map<String, String> form = parseForm(minimal);
        assertThat(form).containsOnlyKeys(
                "grant_type", "subject_token", "subject_token_type", "federation_policy_id");
        assertThat(form.get("grant_type"))
                .isEqualTo("urn:ietf:params:oauth:grant-type:token-exchange");
        assertThat(form.get("subject_token_type")).isEqualTo("urn:ietf:params:oauth:token-type:jwt");
        assertThat(form.get("subject_token")).isEqualTo("header.payload.signature");
        assertThat(form.get("federation_policy_id"))
                .isEqualTo("4b2f7d1e-8c3a-4e5f-9a6b-1c2d3e4f5a6b");

        String full = new OpenRouterWorkloadIdentityExchangeRequest.Builder(client())
                .subjectToken("header.payload.signature")
                .federationPolicyId("4b2f7d1e-8c3a-4e5f-9a6b-1c2d3e4f5a6b")
                .scope("inference")
                .requestedTokenType("urn:ietf:params:oauth:token-type:access_token")
                .build()
                .getBody();

        Map<String, String> fullForm = parseForm(full);
        assertThat(fullForm).containsOnlyKeys(
                "grant_type", "subject_token", "subject_token_type", "federation_policy_id",
                "scope", "requested_token_type");
        assertThat(fullForm.get("scope")).isEqualTo("inference");
        assertThat(fullForm.get("requested_token_type"))
                .isEqualTo("urn:ietf:params:oauth:token-type:access_token");
    }

    @Test
    void formValuesAreUrlEncoded() {
        String body = new OpenRouterWorkloadIdentityExchangeRequest.Builder(client())
                .subjectToken("a b&c=d")
                .federationPolicyId("4b2f7d1e-8c3a-4e5f-9a6b-1c2d3e4f5a6b")
                .build()
                .getBody();

        // the encoded subject token contains no raw space, ampersand or equals sign
        assertThat(body).doesNotContain("a b&c=d");
        assertThat(parseForm(body).get("subject_token")).isEqualTo("a b&c=d");
    }

    @Test
    void buildRejectsMissingSubjectTokenAndPolicy() {
        assertThatThrownBy(() -> new OpenRouterWorkloadIdentityExchangeRequest.Builder(client()).build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterWorkloadIdentityExchangeRequest.Builder(client())
                .subjectToken("header.payload.signature")
                .build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterWorkloadIdentityExchangeRequest.Builder(client())
                .federationPolicyId("4b2f7d1e-8c3a-4e5f-9a6b-1c2d3e4f5a6b")
                .build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void accessorsAreSurfaced() {
        OpenRouterWorkloadIdentityExchangeRequest request =
                new OpenRouterWorkloadIdentityExchangeRequest.Builder(client())
                        .subjectToken("header.payload.signature")
                        .federationPolicyId("4b2f7d1e-8c3a-4e5f-9a6b-1c2d3e4f5a6b")
                        .build();
        OpenRouterWorkloadIdentityExchangeResponse response = request.createResponse("""
                {
                  "access_token": "<short-lived openrouter access token jwt>",
                  "expires_in": 900,
                  "issued_token_type": "urn:ietf:params:oauth:token-type:access_token",
                  "scope": "inference",
                  "token_type": "Bearer"
                }
                """);

        assertThat(response.accessToken()).isEqualTo("<short-lived openrouter access token jwt>");
        assertThat(response.expiresIn()).isEqualTo(900L);
        assertThat(response.issuedTokenType())
                .isEqualTo("urn:ietf:params:oauth:token-type:access_token");
        assertThat(response.scope()).isEqualTo("inference");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void accessorsReturnNullWhenAbsentOrMalformed() {
        OpenRouterWorkloadIdentityExchangeRequest request =
                new OpenRouterWorkloadIdentityExchangeRequest.Builder(client())
                        .subjectToken("header.payload.signature")
                        .federationPolicyId("4b2f7d1e-8c3a-4e5f-9a6b-1c2d3e4f5a6b")
                        .build();

        OpenRouterWorkloadIdentityExchangeResponse empty = request.createResponse("{}");
        assertThat(empty.accessToken()).isNull();
        assertThat(empty.expiresIn()).isNull();
        assertThat(empty.issuedTokenType()).isNull();
        assertThat(empty.scope()).isNull();
        assertThat(empty.tokenType()).isNull();

        OpenRouterWorkloadIdentityExchangeResponse malformed =
                request.createResponse("{\"access_token\": 42, \"expires_in\": \"soon\"}");
        assertThat(malformed.accessToken()).isEqualTo("42");
        assertThat(malformed.expiresIn()).isNull();
    }

    @Test
    void requestAccessorsReflectTheBuilderInput() {
        OpenRouterWorkloadIdentityExchangeRequest request =
                new OpenRouterWorkloadIdentityExchangeRequest.Builder(client())
                        .subjectToken("header.payload.signature")
                        .federationPolicyId("4b2f7d1e-8c3a-4e5f-9a6b-1c2d3e4f5a6b")
                        .scope("inference")
                        .requestedTokenType("urn:ietf:params:oauth:token-type:access_token")
                        .build();

        assertThat(request.subjectToken()).isEqualTo("header.payload.signature");
        assertThat(request.federationPolicyId()).isEqualTo("4b2f7d1e-8c3a-4e5f-9a6b-1c2d3e4f5a6b");
        assertThat(request.scope()).isEqualTo("inference");
        assertThat(request.requestedTokenType()).isEqualTo("urn:ietf:params:oauth:token-type:access_token");
    }

    @Test
    void requestAccessorsReturnNullWhenUnset() {
        OpenRouterWorkloadIdentityExchangeRequest request =
                new OpenRouterWorkloadIdentityExchangeRequest.Builder(client())
                        .subjectToken("header.payload.signature")
                        .federationPolicyId("4b2f7d1e-8c3a-4e5f-9a6b-1c2d3e4f5a6b")
                        .build();

        assertThat(request.scope()).isNull();
        assertThat(request.requestedTokenType()).isNull();
    }

    private Map<String, String> parseForm(String body) {
        Map<String, String> form = new LinkedHashMap<>();
        for (String pair : body.split("&")) {
            int equals = pair.indexOf('=');
            form.put(
                    URLDecoder.decode(pair.substring(0, equals), StandardCharsets.UTF_8),
                    URLDecoder.decode(pair.substring(equals + 1), StandardCharsets.UTF_8));
        }
        return form;
    }
}
