package de.entwicklertraining.openrouter4j;

/**
 * The image resolution tier ({@code detail}) a vision model should use when
 * processing an image content part - {@code OpenRouterChatCompletionRequest.Builder#addImageByUrl(String, OpenRouterImageDetail)}
 * and {@code OpenRouterChatCompletionRequest.Builder#addImageByBase64(java.nio.file.Path, OpenRouterImageDetail)}.
 * <p>
 * {@code AUTO} is the provider default; {@code LOW} and {@code HIGH} trade
 * fidelity against token cost; {@code ORIGINAL} is an OpenRouter extension not
 * present in the OpenAI Chat Completions spec and is downgraded to
 * {@code HIGH} on providers without an original-resolution tier.
 */
public enum OpenRouterImageDetail {

    /** Provider default resolution handling. */
    AUTO("auto"),

    /** Low-resolution tier: fewer tokens, less fidelity. */
    LOW("low"),

    /** High-resolution tier: more tokens, more fidelity. */
    HIGH("high"),

    /** Original resolution (OpenRouter extension; downgraded to {@code high}
     * on providers without an original-resolution tier). */
    ORIGINAL("original");

    private final String wireName;

    OpenRouterImageDetail(String wireName) {
        this.wireName = wireName;
    }

    /**
     * The value sent on the wire ({@code "auto"}, {@code "low"}, {@code "high"},
     * {@code "original"}).
     */
    public String wireName() {
        return wireName;
    }
}
