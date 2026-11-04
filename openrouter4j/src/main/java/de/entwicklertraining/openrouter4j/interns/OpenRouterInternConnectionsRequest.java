package de.entwicklertraining.openrouter4j.interns;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to list the connections in scope for one intern:
 * GET https://openrouter.ai/api/v1/vault/interns/{internId}/connections
 * (operationId {@code listInternConnections}).
 *
 * <p>The answer carries the connection keys (the key of their card on the
 * dashboard's Connections tab: {@code github}, {@code slack},
 * {@code google}, a catalog MCP server such as {@code linear} or
 * {@code notion}, or {@code mcp:<host>} for a custom MCP server), the
 * vault's routing table for the intern ({@code routing}) and its plain-text
 * form ({@code routing_text}). A connection is listed when it is stored,
 * enabled, and not turned off or hidden for this intern, as the
 * Connections tab lists it - whether its last test passed is <b>not</b>
 * part of the rule. {@code ori features add} checks a feature's
 * {@code ori.requires.connections} against this list.
 *
 * <p>Traps: {@code routing} is for the intern's <b>own</b> API key only -
 * {@code null} for any other caller and when the vault could not build the
 * table in time (the keys still list in that case); {@code routing_text} is
 * {@code null} only for a caller other than the intern - when the table
 * could not be built it carries the notice that it could not be read.
 * Never credentials - the table carries metadata about them. Error paths:
 * 404 for an intern the key cannot see (a member key without an admin role
 * sees only interns its member created or interns in workspaces they
 * administer) and outside the Intern API programme (every vault route,
 * including this read, requires access to it); 403 when the key has no
 * usable workspace scope or the request arrived on a regional hostname such
 * as {@code eu.openrouter.ai} (refused - this library supports regional
 * base URLs, so a regional client cannot use the route at all); 408 when
 * the route deadline passes; 502 when the vault could not be reached or
 * answered unexpectedly; 503 when vault writes are disabled for the caller
 * or the vault is not configured; 504 when the vault did not answer in
 * time. The scope is selected by the API key - workspace routes act on the
 * key's active workspace and intern routes act on one intern inside that
 * workspace, there is no default workspace and no fallback to another
 * scope. An intern's own API key is confined to that intern: it can always
 * read the intern's secrets and effective secrets, and every other intern
 * and every workspace route answers 404.
 */
public final class OpenRouterInternConnectionsRequest
        extends OpenRouterRequest<OpenRouterInternConnectionsResponse> {

    private final OpenRouterClient client;
    private final String internId;

    private OpenRouterInternConnectionsRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.internId = builder.internId;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String internId() {
        return URLEncoder.encode(internId, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/vault/interns/" + URLEncoder.encode(internId, StandardCharsets.UTF_8)
                + "/connections";
    }

    @Override
    public String getHttpMethod() {
        return "GET";
    }

    /**
     * GET requests carry no body.
     *
     * @return always {@code null}
     */
    @Override
    public String getBody() {
        return null;
    }

    @Override
    public OpenRouterInternConnectionsResponse createResponse(String responseBody) {
        return new OpenRouterInternConnectionsResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building an {@link OpenRouterInternConnectionsRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterInternConnectionsRequest> {

        private final OpenRouterClient client;
        private final String internId;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param internId the id (UUID) of the intern
         */
        public Builder(OpenRouterClient client, String internId) {
            super(client);
            this.client = client;
            this.internId = internId;
        }

        @Override
        public OpenRouterInternConnectionsRequest build() {
            return new OpenRouterInternConnectionsRequest(this);
        }

        @Override
        public OpenRouterInternConnectionsResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterInternConnectionsResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterInternConnectionsResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
