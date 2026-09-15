package de.entwicklertraining.openrouter4j.video;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of POST /videos and GET /videos/{jobId}: the async video
 * generation job with its status and, once completed, the download URLs and
 * usage.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}/empty
 * convention: a field that is absent (or a malformed body) yields {@code null}
 * (or an empty list) instead of an exception. Use {@link #getJson()} to
 * inspect the raw response.
 *
 * <p>The submission response is {@code 202} with status {@code pending};
 * track the job with {@link #awaitCompletion(OpenRouterClient)} or poll manually with
 * {@code client.videos().job(jobId)}.
 *
 * @param <T> the concrete request type this response belongs to (the
 *        submission or the polling request)
 */
public class OpenRouterVideoGenerationResponse<T extends OpenRouterRequest<?>>
        extends OpenRouterResponse<T> {

    /** Poll interval of the {@link #awaitCompletion(OpenRouterClient)} helper: 5 seconds. */
    public static final long DEFAULT_POLL_INTERVAL_MILLIS = 5_000L;

    /** Timeout of the {@link #awaitCompletion(OpenRouterClient)} helper: 15 minutes. */
    public static final long DEFAULT_TIMEOUT_MILLIS = 15 * 60 * 1_000L;

    OpenRouterVideoGenerationResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code id} - the job id (e.g. {@code job-abc123}).
     *
     * @return the value, or {@code null} when absent
     */
    public String id() {
        return optString("id");
    }

    /**
     * JSON path: {@code polling_url} - the relative polling URL of the job
     * (e.g. {@code /api/v1/videos/job-abc123}).
     *
     * @return the value, or {@code null} when absent
     */
    public String pollingUrl() {
        return optString("polling_url");
    }

    /**
     * JSON path: {@code status} - the raw status string: {@code pending},
     * {@code in_progress}, {@code completed}, {@code failed},
     * {@code cancelled} or {@code expired} (unknown values are passed
     * through verbatim).
     *
     * @return the value, or {@code null} when absent
     */
    public String status() {
        return optString("status");
    }

    /**
     * Convenience status check.
     *
     * @return {@code true} when {@code status} is {@code completed}
     */
    public boolean isCompleted() {
        return "completed".equals(status());
    }

    /**
     * Convenience status check for the terminal states.
     *
     * @return {@code true} when {@code status} is {@code completed},
     *         {@code failed}, {@code cancelled} or {@code expired}
     */
    public boolean isTerminal() {
        String s = status();
        return "completed".equals(s) || "failed".equals(s)
                || "cancelled".equals(s) || "expired".equals(s);
    }

    /**
     * JSON path: {@code generation_id} - the generation id associated with
     * the job (e.g. {@code gen-xyz789}); available once the job has been
     * processed (metadata becomes queryable via
     * {@code client.generation(...)} a few seconds later).
     *
     * @return the value, or {@code null} when absent
     */
    public String generationId() {
        return optString("generation_id");
    }

    /**
     * JSON path: {@code unsigned_urls} - the download URLs of the generated
     * video(s), empty when absent. The URLs are unsigned (no expiring
     * signature); fetch the raw bytes via
     * {@code client.videos().jobContent(jobId)}.
     *
     * @return the URLs, never {@code null}
     */
    public List<String> unsignedUrls() {
        List<String> result = new ArrayList<>();
        try {
            org.json.JSONArray arr = json.optJSONArray("unsigned_urls");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    String value = arr.optString(i, null);
                    if (value != null) {
                        result.add(value);
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    /**
     * JSON path: {@code error} - the error message of a failed job.
     *
     * @return the value, or {@code null} when absent
     */
    public String error() {
        return optString("error");
    }

    /**
     * @return the raw {@code usage} object, or {@code null} when absent
     */
    public JSONObject usage() {
        try {
            return json.optJSONObject("usage");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code usage.cost} - the cost of the video generation in
     * USD; available once the job has completed.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double cost() {
        try {
            JSONObject usage = usage();
            if (usage == null || !usage.has("cost") || usage.isNull("cost")) {
                return null;
            }
            Object value = usage.get("cost");
            return value instanceof Number number ? number.doubleValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code usage.is_byok} - whether the request was made using
     * a Bring Your Own Key configuration.
     *
     * @return the value, or {@code null} when absent
     */
    public Boolean isByok() {
        try {
            JSONObject usage = usage();
            if (usage == null || !usage.has("is_byok") || usage.isNull("is_byok")) {
                return null;
            }
            return usage.optBoolean("is_byok");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Polls {@code GET /videos/{jobId}} every
     * {@link #DEFAULT_POLL_INTERVAL_MILLIS} until the job reaches a terminal
     * state or {@link #DEFAULT_TIMEOUT_MILLIS} have elapsed, and returns the
     * final response (this object when the job already is terminal).
     * <p>
     * Trap: the helper blocks the calling thread and never fails on a failed
     * job - check {@link #status()} and {@link #error()} on the returned
     * response. When the timeout elapses first, the last polled response is
     * returned without exception.
     *
     * @param client the client used to send the polling requests
     * @return the final (or last polled) job response
     */
    public OpenRouterVideoGenerationResponse<T> awaitCompletion(OpenRouterClient client) {
        return awaitCompletion(client, DEFAULT_POLL_INTERVAL_MILLIS, DEFAULT_TIMEOUT_MILLIS);
    }

    /**
     * Polls {@code GET /videos/{jobId}} at the given interval until the job
     * reaches a terminal state (see {@link #isTerminal()}) or the timeout
     * elapses, and returns the final response (this object when the job
     * already is terminal).
     *
     * @param client the client used to send the polling requests
     * @param pollIntervalMillis the sleep between two polls in ms (at least 250 ms)
     * @param timeoutMillis the overall timeout in ms
     * @return the final (or last polled) job response
     */
    public OpenRouterVideoGenerationResponse<T> awaitCompletion(
            OpenRouterClient client, long pollIntervalMillis, long timeoutMillis) {
        OpenRouterVideoGenerationResponse<T> current = this;
        long deadline = System.currentTimeMillis() + Math.max(timeoutMillis, 0);
        long interval = Math.max(pollIntervalMillis, 250);
        while (!current.isTerminal() && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(Math.min(interval, Math.max(deadline - System.currentTimeMillis(), 1)));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return current;
            }
            current = current.poll(client);
        }
        return current;
    }

    private OpenRouterVideoGenerationResponse<T> poll(OpenRouterClient client) {
        if (client == null || id() == null) {
            return this;
        }
        try {
            @SuppressWarnings({"unchecked", "rawtypes"})
            OpenRouterVideoGenerationResponse<T> polled =
                    (OpenRouterVideoGenerationResponse) client.videos().job(id()).execute();
            return polled;
        } catch (Exception e) {
            return this;
        }
    }

    private String optString(String key) {
        try {
            return json.optString(key, null);
        } catch (Exception e) {
            return null;
        }
    }
}
