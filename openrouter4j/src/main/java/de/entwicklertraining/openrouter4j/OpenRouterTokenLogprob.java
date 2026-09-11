package de.entwicklertraining.openrouter4j;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * One token of a log-probability list ({@code ChatTokenLogprob} in the API
 * reference): the token string, its log probability, the UTF-8 bytes of the
 * token (when the provider reports them) and the top alternative tokens at
 * that position.
 * <p>
 * Instances are produced by {@code OpenRouterChatCompletionResponse#contentLogprobs()}
 * and {@code OpenRouterChatCompletionResponse#refusalLogprobs()} when the request
 * opted in via {@code OpenRouterChatCompletionRequest.Builder#logprobs(Boolean)}
 * (and {@code topLogprobs(n)}).
 *
 * @param token the token text
 * @param logprob the log probability of the token, or {@code null} when absent
 * @param bytes the UTF-8 byte values of the token, or {@code null} when the
 *              provider did not report them (byte values may exceed 127 and
 *              must not be read as chars)
 * @param topLogprobs the top alternative tokens at this position, empty when
 *                    the request did not ask for them ({@code topLogprobs(n)})
 */
public record OpenRouterTokenLogprob(
        String token,
        Double logprob,
        List<Integer> bytes,
        List<OpenRouterTokenLogprob> topLogprobs
) {

    /**
     * Builds an instance from the API's {@code ChatTokenLogprob} JSON object
     * ({@code token}, {@code logprob}, {@code bytes}, {@code top_logprobs}).
     *
     * @param json the token-logprob JSON object
     * @return the typed instance
     */
    public static OpenRouterTokenLogprob fromJson(JSONObject json) {
        String token = json.optString("token", null);
        Double logprob = json.isNull("logprob") || !json.has("logprob")
                ? null : json.optDouble("logprob");
        List<Integer> bytes = null;
        if (json.has("bytes") && !json.isNull("bytes")) {
            JSONArray bytesArr = json.optJSONArray("bytes");
            if (bytesArr != null) {
                bytes = new ArrayList<>();
                for (int i = 0; i < bytesArr.length(); i++) {
                    bytes.add(bytesArr.optInt(i));
                }
            }
        }
        List<OpenRouterTokenLogprob> top = new ArrayList<>();
        if (json.has("top_logprobs") && !json.isNull("top_logprobs")) {
            JSONArray topArr = json.optJSONArray("top_logprobs");
            if (topArr != null) {
                for (int i = 0; i < topArr.length(); i++) {
                    JSONObject entry = topArr.optJSONObject(i);
                    if (entry != null) {
                        top.add(fromJson(entry));
                    }
                }
            }
        }
        return new OpenRouterTokenLogprob(token, logprob, bytes == null ? null : List.copyOf(bytes), List.copyOf(top));
    }
}
