package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.oauth.OpenRouterWorkloadIdentityExchangeRequest;
import de.entwicklertraining.openrouter4j.oauth.OpenRouterWorkloadIdentityExchangeResponse;

/**
 * Demonstrates the workload identity federation token exchange (RFC 8693,
 * POST /oauth/token): a JWT from your identity provider is exchanged for a
 * short-lived OpenRouter access token - the documented way for CI and
 * service accounts to call OpenRouter without embedding a long-lived key.
 *
 * <p>The subject token travels as a credential: this example reads it from
 * an environment variable instead of accepting it as an argument (which
 * would leak into process listings) and prints only the token metadata,
 * never the token itself. The federation policy UUID comes from OpenRouter
 * Settings -&gt; Workload identity.
 */
public class OpenRouterWorkloadIdentityExample {

    public static void main(String[] args) {
        String subjectToken = System.getenv("WORKLOAD_IDENTITY_TOKEN");
        if (subjectToken == null || subjectToken.isBlank()) {
            System.out.println("Set WORKLOAD_IDENTITY_TOKEN to the JWT of your identity provider.");
            return;
        }
        String federationPolicyId = System.getenv("OPENROUTER_FEDERATION_POLICY_ID");
        if (federationPolicyId == null || federationPolicyId.isBlank()) {
            System.out.println("Set OPENROUTER_FEDERATION_POLICY_ID to the UUID from");
            System.out.println("OpenRouter Settings -> Workload identity.");
            return;
        }

        OpenRouterClient client = new OpenRouterClient();

        OpenRouterWorkloadIdentityExchangeResponse response = client.exchangeWorkloadIdentityToken()
                .subjectToken(subjectToken)
                .federationPolicyId(federationPolicyId)
                .scope(OpenRouterWorkloadIdentityExchangeRequest.SCOPE_INFERENCE)
                .execute();

        System.out.println("Token type:  " + response.tokenType());
        System.out.println("Scope:       " + response.scope());
        System.out.println("Expires in:  " + response.expiresIn() + " seconds");
        System.out.println("Issued type: " + response.issuedTokenType());
        System.out.println("(the access token itself is intentionally not printed - treat it as a secret)");
    }
}
