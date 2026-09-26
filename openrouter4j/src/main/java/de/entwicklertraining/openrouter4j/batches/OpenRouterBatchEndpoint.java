package de.entwicklertraining.openrouter4j.batches;

/**
 * The API shape every request body of one batch follows, sent as the required
 * top-level {@code endpoint} field of
 * {@code POST https://openrouter.ai/api/v1/batches}.
 *
 * <p>All requests of one batch use the same shape; to mix shapes, submit
 * separate batches. The wire value is the OpenRouter path of the
 * corresponding synchronous API.
 */
public enum OpenRouterBatchEndpoint {

    /** OpenAI-style chat completions ({@code POST /v1/chat/completions}). */
    CHAT_COMPLETIONS("/v1/chat/completions"),

    /** OpenAI Responses API ({@code POST /v1/responses}). */
    RESPONSES("/v1/responses"),

    /** Anthropic Messages API ({@code POST /v1/messages}). */
    MESSAGES("/v1/messages"),

    /** Embeddings ({@code POST /v1/embeddings}). */
    EMBEDDINGS("/v1/embeddings");

    private final String wireName;

    OpenRouterBatchEndpoint(String wireName) {
        this.wireName = wireName;
    }

    /**
     * The value sent on the wire ({@code "/v1/chat/completions"},
     * {@code "/v1/responses"}, {@code "/v1/messages"},
     * {@code "/v1/embeddings"}).
     */
    public String wireName() {
        return wireName;
    }
}
