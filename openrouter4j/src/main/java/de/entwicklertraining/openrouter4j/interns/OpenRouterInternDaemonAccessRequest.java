package de.entwicklertraining.openrouter4j.interns;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to read one intern's daemon access - the origin and bearer token
 * that attach {@code ori tui --host} to one visible, running intern:
 * GET https://openrouter.ai/api/v1/interns/{internId}/daemon-access
 * (schema {@code InternDaemonAccess}).
 *
 * <p>Documented traps: the response carries a <b>credential</b> - the token
 * is a bearer token for the intern's daemon, the response is sent with
 * {@code Cache-Control: no-store}, and each reveal is logged by caller and
 * intern (see {@link OpenRouterInternDaemonAccessResponse#token()});
 * {@code 403} when the key owner no longer has access <b>or the request
 * used a regional hostname such as {@code eu.openrouter.ai}</b> (refused -
 * this library supports regional base URLs, so a regional client cannot use
 * this endpoint at all); {@code 409} when the intern cannot be attached to,
 * with {@code error.metadata.reason} being {@code intern_not_running},
 * {@code intern_unreachable} (no usable address yet) or
 * {@code intern_needs_restart} (provisioned before daemon access was
 * available); {@code 408} route deadline 10 s; every {@code /interns} path
 * answers {@code 404} for keys outside the interns programme.
 */
public final class OpenRouterInternDaemonAccessRequest
        extends OpenRouterRequest<OpenRouterInternDaemonAccessResponse> {

    private final OpenRouterClient client;
    private final String internId;

    private OpenRouterInternDaemonAccessRequest(Builder builder) {
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
        return "/interns/"
                + URLEncoder.encode(internId, StandardCharsets.UTF_8)
                + "/daemon-access";
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
    public OpenRouterInternDaemonAccessResponse createResponse(String responseBody) {
        return new OpenRouterInternDaemonAccessResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterInternDaemonAccessRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterInternDaemonAccessRequest> {

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
        public OpenRouterInternDaemonAccessRequest build() {
            return new OpenRouterInternDaemonAccessRequest(this);
        }

        @Override
        public OpenRouterInternDaemonAccessResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterInternDaemonAccessResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterInternDaemonAccessResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
