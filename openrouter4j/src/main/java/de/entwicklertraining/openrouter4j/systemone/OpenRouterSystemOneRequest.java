package de.entwicklertraining.openrouter4j.systemone;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.decisions.OpenRouterDecisionQuestion;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterTraceConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A request to submit a System One request to a System One model (TypeSafe
 * Jev family): POST https://openrouter.ai/api/v1/systemone
 *
 * <p>Related but distinct from the Decisions API ({@code client.decisions()},
 * POST /api/alpha/decisions): the question types ({@code noul}, {@code choice},
 * {@code score}) and the wire format are identical - the question factories
 * {@link OpenRouterDecisionQuestion} are reused unchanged - but System One is
 * its own operation on the ordinary {@code /api/v1} base (no alpha namespace,
 * no special client routing) for TypeSafe SDK compatibility, and it accepts
 * bare System One model ids.
 *
 * <p>Required: {@code model} (the System One model), {@code state} (the
 * content to evaluate) and at least one {@code questions} entry. The state
 * accepts three wire forms - a plain string, a JSON object or an array of
 * related context; the last form set wins, exactly one is emitted.
 *
 * <p>Traps: bare model ids such as {@code jev-1.13} or {@code jev-latest} are
 * mapped onto the {@code typesafe/} namespace server-side (both forms work);
 * ordinary chat model ids are rejected with 400, like on the Decisions
 * surface. The state forms are mutually exclusive - setting
 * {@link Builder#state(String)} clears a previously set object or array form.
 */
public final class OpenRouterSystemOneRequest
        extends OpenRouterRequest<OpenRouterSystemOneResponse> {

    private final OpenRouterClient client;
    private final String model;
    private final String stateText;
    private final JSONObject stateObject;
    private final JSONArray stateArray;
    private final Map<String, OpenRouterDecisionQuestion> questions;
    private final String user;
    private final String sessionId;
    private final OpenRouterTraceConfig trace;
    private final List<String> providerOrder;
    private final List<String> providerOnly;
    private final List<String> providerIgnore;
    private final Boolean requireParameters;
    private final Boolean allowFallbacks;

    private OpenRouterSystemOneRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.model = builder.model;
        this.stateText = builder.stateText;
        this.stateObject = builder.stateObject == null ? null : new JSONObject(builder.stateObject.toString());
        this.stateArray = builder.stateArray == null ? null : new JSONArray(builder.stateArray.toString());
        this.questions = builder.questions == null ? null : new LinkedHashMap<>(builder.questions);
        this.user = builder.user;
        this.sessionId = builder.sessionId;
        this.trace = builder.trace;
        this.providerOrder = builder.providerOrder == null ? null : List.copyOf(builder.providerOrder);
        this.providerOnly = builder.providerOnly == null ? null : List.copyOf(builder.providerOnly);
        this.providerIgnore = builder.providerIgnore == null ? null : List.copyOf(builder.providerIgnore);
        this.requireParameters = builder.requireParameters;
        this.allowFallbacks = builder.allowFallbacks;
    }

    /** @return the judging model id (e.g. {@code typesafe/jev-1.13}) */
    public String model() {
        return model;
    }

    /** @return the plain-string state form, or {@code null} when the state is an object or array */
    public String stateText() {
        return stateText;
    }

    /** @return the object state form, or {@code null} when the state is a string or array */
    public JSONObject stateObject() {
        return stateObject;
    }

    /** @return the array state form, or {@code null} when the state is a string or object */
    public JSONArray stateArray() {
        return stateArray;
    }

    /** @return the configured questions keyed as they will be emitted, empty when none were set */
    public Map<String, OpenRouterDecisionQuestion> questions() {
        return questions == null ? Map.of() : questions;
    }

    /**
     * The {@code user} end-user identifier, or {@code null} when unset (the
     * key is not sent). Used by OpenRouter for abuse monitoring and cost
     * isolation.
     *
     * @return the end-user identifier, or {@code null}
     */
    public String user() {
        return user;
    }

    /**
     * The {@code session_id} grouping key, or {@code null} when unset (the
     * key is not sent). Groups related requests for observability grouping in
     * Broadcast and private logging; never sent to the provider.
     *
     * @return the session identifier, or {@code null}
     */
    public String sessionId() {
        return sessionId;
    }

    /**
     * The {@code trace} observability configuration, or {@code null} when
     * unset (the key is not sent).
     *
     * @return the trace configuration, or {@code null}
     */
    public OpenRouterTraceConfig trace() {
        return trace;
    }

    @Override
    public String getRelativeUrl() {
        return "/systemone";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * JSON path: the request body - {@code model} (required), {@code state}
     * (required; exactly one of the string, object or array form),
     * {@code questions} (required, keyed by the caller-chosen keys),
     * {@code user}, {@code session_id} and {@code trace} (all omitted when
     * unset) and the {@code provider} object (omitted unless any provider
     * routing option is set).
     *
     * @return the JSON body of this request
     */
    @Override
    public String getBody() {
        JSONObject root = new JSONObject();
        root.put("model", model);
        if (stateText != null) {
            root.put("state", stateText);
        } else if (stateObject != null) {
            root.put("state", new JSONObject(stateObject.toString()));
        } else if (stateArray != null) {
            root.put("state", new JSONArray(stateArray.toString()));
        }
        JSONObject questionsObj = new JSONObject();
        for (Map.Entry<String, OpenRouterDecisionQuestion> entry : questions.entrySet()) {
            questionsObj.put(entry.getKey(), entry.getValue().toJson());
        }
        root.put("questions", questionsObj);
        if (user != null) {
            root.put("user", user);
        }
        if (sessionId != null) {
            root.put("session_id", sessionId);
        }
        if (trace != null) {
            root.put("trace", trace.toJson());
        }

        // The provider object is emitted whenever any provider routing option is set,
        // so an unset option never appears in the JSON.
        boolean hasOrder = providerOrder != null && !providerOrder.isEmpty();
        boolean hasOnly = providerOnly != null && !providerOnly.isEmpty();
        boolean hasIgnore = providerIgnore != null && !providerIgnore.isEmpty();
        if (hasOrder || hasOnly || hasIgnore
                || requireParameters != null || allowFallbacks != null) {
            JSONObject providerObj = new JSONObject();
            if (hasOrder) {
                providerObj.put("order", new JSONArray(providerOrder));
            }
            if (hasOnly) {
                providerObj.put("allow_fallbacks", false);
                providerObj.put("only", new JSONArray(providerOnly));
            }
            if (hasIgnore) {
                providerObj.put("ignore", new JSONArray(providerIgnore));
            }
            if (requireParameters != null) {
                providerObj.put("require_parameters", requireParameters);
            }
            if (allowFallbacks != null) {
                providerObj.put("allow_fallbacks", allowFallbacks);
            }
            root.put("provider", providerObj);
        }
        return root.toString();
    }

    @Override
    public OpenRouterSystemOneResponse createResponse(String responseBody) {
        return new OpenRouterSystemOneResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterSystemOneRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterSystemOneRequest> {

        private final OpenRouterClient client;
        private String model;
        private String stateText;
        private JSONObject stateObject;
        private JSONArray stateArray;
        private Map<String, OpenRouterDecisionQuestion> questions;
        private String user;
        private String sessionId;
        private OpenRouterTraceConfig trace;
        private List<String> providerOrder;
        private List<String> providerOnly;
        private List<String> providerIgnore;
        private Boolean requireParameters;
        private Boolean allowFallbacks;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         */
        public Builder(OpenRouterClient client) {
            super(client);
            this.client = client;
        }

        /**
         * Sets the required JSON field {@code model} - the System One model
         * (TypeSafe Jev family, e.g. {@code typesafe/jev-1.13}). Bare ids
         * such as {@code jev-1.13} / {@code jev-latest} are mapped onto the
         * {@code typesafe/} namespace server-side; ordinary chat model ids
         * are rejected with 400 (as on the Decisions surface).
         *
         * @param model the judging model id
         * @return this builder
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * Sets the JSON field {@code state} as a plain string - the content
         * to evaluate. Clears a previously set object or array form; exactly
         * one form is emitted.
         *
         * @param state the state text
         * @return this builder
         */
        public Builder state(String state) {
            this.stateText = state;
            this.stateObject = null;
            this.stateArray = null;
            return this;
        }

        /**
         * Sets the JSON field {@code state} as a JSON object - the content to
         * evaluate as structured key-value context. Clears a previously set
         * string or array form; exactly one form is emitted.
         *
         * @param state the state object
         * @return this builder
         */
        public Builder state(JSONObject state) {
            this.stateText = null;
            this.stateObject = state;
            this.stateArray = null;
            return this;
        }

        /**
         * Sets the JSON field {@code state} as an array - the content to
         * evaluate as related context entries. Clears a previously set string
         * or object form; exactly one form is emitted.
         *
         * @param state the state array
         * @return this builder
         */
        public Builder state(JSONArray state) {
            this.stateText = null;
            this.stateObject = null;
            this.stateArray = state;
            return this;
        }

        /**
         * Adds a question under a caller-chosen key. Repeated calls with the
         * same key replace the previous question (JSON objects are unordered,
         * the key is the identity).
         *
         * @param key the caller-chosen question key (echoed in the answers)
         * @param question the typed question (build with the
         *                 {@link OpenRouterDecisionQuestion} factories)
         * @return this builder
         */
        public Builder question(String key, OpenRouterDecisionQuestion question) {
            if (key == null || key.isEmpty()) {
                throw new IllegalArgumentException("question key must not be null or empty");
            }
            if (question == null) {
                throw new IllegalArgumentException("question must not be null");
            }
            ensureQuestions().put(key, question);
            return this;
        }

        /**
         * Adds a verbatim question object under a caller-chosen key - the
         * escape hatch for question shapes the typed factories do not cover.
         * The object must carry a {@code type} field.
         *
         * @param key the caller-chosen question key (echoed in the answers)
         * @param rawQuestion the verbatim question object
         * @return this builder
         */
        public Builder question(String key, JSONObject rawQuestion) {
            return question(key, OpenRouterDecisionQuestion.raw(rawQuestion));
        }

        /**
         * Sets the JSON field {@code user} - a unique identifier for the
         * end-user, used by OpenRouter for abuse monitoring. Omitted when
         * unset.
         *
         * @param user the end-user identifier
         * @return this builder
         */
        public Builder user(String user) {
            this.user = user;
            return this;
        }

        /**
         * Sets the JSON field {@code session_id} - a unique identifier for
         * grouping related requests. Used for observability grouping in
         * Broadcast and private logging; never sent to the provider. Maximum
         * 256 characters (API-side limit, not validated here). Omitted when
         * unset.
         *
         * @param sessionId the session grouping identifier
         * @return this builder
         */
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        /**
         * Sets the JSON field {@code trace} - observability metadata that
         * OpenRouter forwards to configured broadcast destinations. Build it
         * with {@link OpenRouterTraceConfig#builder()}. Omitted when unset.
         *
         * @param trace the trace configuration
         * @return this builder
         */
        public Builder trace(OpenRouterTraceConfig trace) {
            this.trace = trace;
            return this;
        }

        /**
         * Sets the JSON field {@code provider.order} - the preferred serving
         * providers in priority order. Only emitted when at least one
         * provider routing option is set.
         *
         * @param providers the provider slugs in priority order
         * @return this builder
         */
        public Builder providerOrder(String... providers) {
            this.providerOrder = toNonEmptyList(providers);
            return this;
        }

        /**
         * Sets the JSON field {@code provider.only} - the only providers
         * allowed to serve the request (emits {@code allow_fallbacks: false}
         * alongside). Only emitted when at least one provider routing option
         * is set.
         *
         * @param providers the provider slugs
         * @return this builder
         */
        public Builder providerOnly(String... providers) {
            this.providerOnly = toNonEmptyList(providers);
            return this;
        }

        /**
         * Sets the JSON field {@code provider.ignore} - providers excluded
         * from serving the request. Only emitted when at least one provider
         * routing option is set.
         *
         * @param providers the provider slugs to exclude
         * @return this builder
         */
        public Builder providerIgnore(String... providers) {
            this.providerIgnore = toNonEmptyList(providers);
            return this;
        }

        /**
         * Sets the JSON field {@code provider.require_parameters} - reject
         * the request when the serving provider cannot honour every
         * parameter. Only emitted when at least one provider routing option
         * is set.
         *
         * @param requireParameters the flag
         * @return this builder
         */
        public Builder requireParameters(Boolean requireParameters) {
            this.requireParameters = requireParameters;
            return this;
        }

        /**
         * Sets the JSON field {@code provider.allow_fallbacks} - whether
         * providers outside the preference lists may serve the request.
         * Only emitted when at least one provider routing option is set.
         *
         * @param allowFallbacks the flag
         * @return this builder
         */
        public Builder allowFallbacks(Boolean allowFallbacks) {
            this.allowFallbacks = allowFallbacks;
            return this;
        }

        private Map<String, OpenRouterDecisionQuestion> ensureQuestions() {
            if (questions == null) {
                questions = new LinkedHashMap<>();
            }
            return questions;
        }

        private List<String> toNonEmptyList(String... values) {
            List<String> list = new ArrayList<>();
            for (String v : values) {
                if (v != null && !v.isEmpty()) {
                    list.add(v);
                }
            }
            if (list.isEmpty()) {
                throw new IllegalArgumentException("at least one provider slug is required");
            }
            return list;
        }

        @Override
        public OpenRouterSystemOneRequest build() {
            if (model == null || model.isEmpty()) {
                throw new IllegalStateException("model is required for a System One request");
            }
            if (stateText == null && stateObject == null && stateArray == null) {
                throw new IllegalStateException(
                        "state is required for a System One request - use state(String), state(JSONObject) or state(JSONArray)");
            }
            if (questions == null || questions.isEmpty()) {
                throw new IllegalStateException(
                        "at least one question is required for a System One request - use question(key, ...)");
            }
            return new OpenRouterSystemOneRequest(this);
        }

        @Override
        public OpenRouterSystemOneResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterSystemOneResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterSystemOneResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
