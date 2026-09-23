package de.entwicklertraining.openrouter4j;

/**
 * The video {@code processing} mode a video-capable model should use on a
 * {@code video_url} content part -
 * {@code OpenRouterChatCompletionRequest.Builder#addVideoByUrl(String, OpenRouterVideoProcessing)}.
 * <p>
 * {@link #AGENTIC} lets the model actively seek and search through the video
 * instead of sampling frames at a fixed rate; {@link #STATIC} forces
 * fixed-rate frame sampling on providers that support it (currently Google
 * Gemini). Omitting the mode leaves the provider default. The same field
 * exists on the legacy {@code input_video} variant, which has no typed helper -
 * set it through {@code addContentPart(JSONObject)} there.
 */
public enum OpenRouterVideoProcessing {

    /** Lets the model actively seek and search through the video instead of
     * sampling frames at a fixed rate - the counterpart of {@link #STATIC}. */
    AGENTIC("agentic"),

    /** Forces fixed-rate frame sampling (Google Gemini only today). */
    STATIC("static");

    private final String wireName;

    OpenRouterVideoProcessing(String wireName) {
        this.wireName = wireName;
    }

    /**
     * The value sent on the wire ({@code "agentic"} or {@code "static"}).
     */
    public String wireName() {
        return wireName;
    }
}
