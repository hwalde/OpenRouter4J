package de.entwicklertraining.openrouter4j.generation;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of GET /generation/content: the stored prompt and completion of one
 * generation, or the failure error when the generation failed.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}/empty
 * convention. {@code data.input} is either a prompt string or an array of
 * messages; {@link #inputPrompt()} and {@link #inputMessages()} cover the two
 * forms and return {@code null}/empty when the other form is present.
 */
public final class OpenRouterGenerationContentResponse extends OpenRouterResponse<OpenRouterGenerationContentRequest> {

    OpenRouterGenerationContentResponse(JSONObject json, OpenRouterGenerationContentRequest request) {
        super(json, request);
    }

    /**
     * @return the raw {@code data} object of the response, or {@code null} when absent
     */
    public JSONObject data() {
        try {
            return json.optJSONObject("data");
        } catch (Exception e) {
            return null;
        }
    }

    private JSONObject input() {
        JSONObject data = data();
        return data != null ? data.optJSONObject("input") : null;
    }

    private JSONObject output() {
        JSONObject data = data();
        return data != null ? data.optJSONObject("output") : null;
    }

    /**
     * JSON path: {@code data.input.prompt} - the stored prompt when the input
     * was a single string.
     *
     * @return the value, or {@code null} when absent (input may be a message array instead)
     */
    public String inputPrompt() {
        JSONObject input = input();
        if (input == null || !input.has("prompt") || input.isNull("prompt")) {
            return null;
        }
        return input.optString("prompt", null);
    }

    /**
     * JSON path: {@code data.input.messages[]} - the stored messages when the
     * input was a message array.
     *
     * @return the raw message objects, empty when absent
     */
    public List<JSONObject> inputMessages() {
        List<JSONObject> result = new ArrayList<>();
        try {
            JSONObject input = input();
            JSONArray messages = input != null ? input.optJSONArray("messages") : null;
            if (messages != null) {
                for (int i = 0; i < messages.length(); i++) {
                    JSONObject message = messages.optJSONObject(i);
                    if (message != null) {
                        result.add(message);
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    /**
     * JSON path: {@code data.output.completion} - the stored completion text.
     *
     * @return the value, or {@code null} when absent
     */
    public String outputCompletion() {
        JSONObject output = output();
        return output != null ? output.optString("completion", null) : null;
    }

    /**
     * JSON path: {@code data.output.reasoning} - the stored reasoning text,
     * when the model produced any.
     *
     * @return the value, or {@code null} when absent
     */
    public String outputReasoning() {
        JSONObject output = output();
        return output != null ? output.optString("reasoning", null) : null;
    }

    /**
     * @return the raw {@code data.error} object of a failed generation, or
     *         {@code null} when the generation succeeded
     */
    public JSONObject error() {
        JSONObject data = data();
        return data != null ? data.optJSONObject("error") : null;
    }

    /**
     * JSON path: {@code data.error.status} - the HTTP status returned to the client.
     *
     * @return the value, or {@code null} when absent
     */
    public Integer errorStatus() {
        return intOf("status");
    }

    /**
     * JSON path: {@code data.error.message} - the error message returned to the client.
     *
     * @return the value, or {@code null} when absent
     */
    public String errorMessage() {
        JSONObject error = error();
        if (error == null || error.isNull("message")) {
            return null;
        }
        return error.optString("message", null);
    }

    /**
     * JSON path: {@code data.error.provider_name} - the provider whose error
     * was returned to the client, when known.
     *
     * @return the value, or {@code null} when absent
     */
    public String errorProviderName() {
        JSONObject error = error();
        if (error == null || error.isNull("provider_name")) {
            return null;
        }
        return error.optString("provider_name", null);
    }

    /**
     * JSON path: {@code data.error.raw} - the raw upstream error body, when stored.
     *
     * @return the value, or {@code null} when absent
     */
    public String errorRaw() {
        JSONObject error = error();
        if (error == null || error.isNull("raw")) {
            return null;
        }
        return error.optString("raw", null);
    }

    private Integer intOf(String key) {
        JSONObject error = error();
        if (error == null || !error.has(key) || error.isNull(key)) {
            return null;
        }
        return error.optInt(key);
    }
}
