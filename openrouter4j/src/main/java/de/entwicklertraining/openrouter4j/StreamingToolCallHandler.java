package de.entwicklertraining.openrouter4j;

import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;
import org.json.JSONObject;

/**
 * Extended streaming handler that receives lifecycle events during a streaming
 * chat completion with tool calling. All new methods have default no-op
 * implementations so callers only override what they need.
 *
 * <p>When both streaming and tools are active, the library automatically
 * orchestrates a multi-turn loop: each turn is streamed, tool calls are
 * detected and executed between turns, and the final text answer is
 * streamed to {@link #onData(String)}.
 *
 * <p>A regular {@link StreamingResponseHandler} can also be used — it will
 * simply not receive the extra lifecycle events.
 */
public interface StreamingToolCallHandler extends StreamingResponseHandler<String> {

    /**
     * Called when a tool call has been detected in the stream, before the
     * tool callback is executed. Useful for logging or UI updates.
     *
     * @param toolName   the name of the tool being called
     * @param toolCallId the unique ID assigned by the model to this call
     * @param arguments  the parsed arguments for the tool
     */
    default void onToolCallDetected(String toolName, String toolCallId, JSONObject arguments) {}

    /**
     * Called after a tool callback has been executed successfully.
     *
     * @param toolName   the name of the tool that was called
     * @param toolCallId the unique ID assigned by the model to this call
     * @param result     the result returned by the tool callback
     */
    default void onToolExecuted(String toolName, String toolCallId, OpenRouterToolResult result) {}

    /**
     * Called when one turn of the conversation (one API call) has completed
     * and the next turn is about to start.
     *
     * @param turnNumber the 1-based turn number that just completed
     */
    default void onTurnComplete(int turnNumber) {}

    /**
     * Called when the entire streaming tool-call loop has finished and the
     * final text response has been fully streamed.
     */
    default void onFinalComplete() {}
}
