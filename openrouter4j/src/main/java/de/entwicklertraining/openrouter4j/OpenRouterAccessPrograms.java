package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

/**
 * The {@code access_programs} object of a Responses request - OpenAI access
 * programs the request runs under (schema {@code ResponsesRequest}
 * field {@code access_programs}).
 *
 * <p>Only {@code cyber} is declared by the current schema, with the three
 * documented values {@code "standard"}, {@code "daybreak_blue"} and
 * {@code "daybreak_red"}; unknown spellings are passed through verbatim
 * (the schema carries {@code x-speakeasy-unknown-values: allow}), and
 * {@link #raw(JSONObject)} covers program keys the schema does not declare.
 *
 * <p>Trap: a program other than {@code "standard"} is supported only on BYOK
 * requests that run on your own OpenAI key enrolled in that program; any
 * other endpoint, including OpenRouter's own OpenAI credentials, rejects it
 * with a 400 error. Requests on OpenRouter's OpenAI credentials always run
 * {@code "standard"}. Omit the field unless your key is enrolled in a
 * Daybreak program.
 */
public final class OpenRouterAccessPrograms {

    private final JSONObject json;

    private OpenRouterAccessPrograms(JSONObject json) {
        this.json = json;
    }

    /**
     * The {@code cyber} access program - {@code "standard"} (default),
     * {@code "daybreak_blue"} or {@code "daybreak_red"}; unknown spellings
     * pass through verbatim (see the class javadoc for the BYOK trap).
     *
     * @param program the cyber access program
     * @return the access programs value emitting {@code {"cyber": program}}
     * @throws IllegalArgumentException when the program is null or empty
     */
    public static OpenRouterAccessPrograms cyber(String program) {
        if (program == null || program.isEmpty()) {
            throw new IllegalArgumentException("cyber access program must not be null or empty");
        }
        JSONObject json = new JSONObject();
        json.put("cyber", program);
        return new OpenRouterAccessPrograms(json);
    }

    /**
     * The verbatim escape hatch - an {@code access_programs} object sent as
     * given (e.g. a program key the schema does not declare). The object is
     * stored as a detached copy, so later mutations of the caller's instance
     * cannot change the request.
     *
     * @param json the raw {@code access_programs} object
     * @return the access programs value emitting {@code json} verbatim
     * @throws IllegalArgumentException when the object is null
     */
    public static OpenRouterAccessPrograms raw(JSONObject json) {
        if (json == null) {
            throw new IllegalArgumentException("access programs object must not be null");
        }
        return new OpenRouterAccessPrograms(new JSONObject(json.toString()));
    }

    /**
     * The configured {@code cyber} program, or {@code null} when this value
     * was built {@link #raw(JSONObject)} without a {@code cyber} key, or
     * with a non-string {@code cyber} value (wrong-typed values are not
     * coerced, they answer {@code null}).
     *
     * @return the cyber access program, or {@code null}
     */
    public String cyber() {
        try {
            if (!json.has("cyber") || json.isNull("cyber")) {
                return null;
            }
            Object value = json.get("cyber");
            return value instanceof String program ? program : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * A detached copy of the {@code access_programs} object as it will be
     * sent - mutating the result cannot change the request.
     *
     * @return the JSON object
     */
    public JSONObject toJson() {
        return new JSONObject(json.toString());
    }
}
