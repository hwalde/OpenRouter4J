package de.entwicklertraining.openrouter4j.decisions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A single question of an evaluation request - the Decisions API
 * ({@code client.decisions()}, {@link OpenRouterDecisionsRequest}) and the
 * System One API ({@code client.systemOne()}, {@code OpenRouterSystemOneRequest})
 * share this question schema and both take the same factories. Sent in the
 * {@code questions} object under a caller-chosen key. Discriminated on the
 * {@code type} field:
 * <ul>
 *   <li>{@code noul} - a boolean evaluation; {@code instructions} required,
 *   optional {@code criteria} object with the {@code "true"} / {@code "false"}
 *   description strings ({@link #noul(String)} / {@link #noul(String, JSONObject)}).</li>
 *   <li>{@code choice} - a multi-class decision; {@code instructions} and the
 *   {@code criteria} object (each option mapped to its description) required
 *   ({@link #choice(String, JSONObject)}).</li>
 *   <li>{@code score} - an ordered score-step decision; {@code instructions}
 *   required plus the ordered {@code criteria} step strings
 *   ({@link #score(String, List)}).</li>
 * </ul>
 *
 * <p>Create instances through the typed factories or the verbatim escape
 * hatch {@link #raw(JSONObject)} for question types OpenRouter adds after
 * this library was released.
 *
 * <p>Trap: the Decisions endpoint lives in OpenRouter's alpha namespace
 * ({@code /api/alpha/decisions}) and may change or disappear without a major
 * version of the API; the System One endpoint is a first-class
 * {@code /api/v1} operation and carries no alpha status.
 */
public final class OpenRouterDecisionQuestion {

    private final JSONObject json;

    private OpenRouterDecisionQuestion(JSONObject json) {
        this.json = json;
    }

    /**
     * Creates a {@code noul} question without criteria
     * ({@code {"type":"noul","instructions":...}}) - a boolean evaluation
     * judged against the instructions alone.
     *
     * @param instructions the evaluation instructions
     * @return the question
     */
    public static OpenRouterDecisionQuestion noul(String instructions) {
        return noul(instructions, null);
    }

    /**
     * Creates a {@code noul} question with the optional criteria object
     * ({@code {"type":"noul","instructions":...,"criteria":{"true":...,"false":...}}}).
     *
     * @param instructions the evaluation instructions
     * @param criteria the {@code "true"} / {@code "false"} description
     *                 strings, or {@code null} to omit the object
     * @return the question
     */
    public static OpenRouterDecisionQuestion noul(String instructions, JSONObject criteria) {
        JSONObject json = new JSONObject();
        json.put("type", "noul");
        json.put("instructions", requireInstructions(instructions));
        if (criteria != null) {
            if (criteria.length() == 0) {
                throw new IllegalArgumentException("noul criteria must not be empty - omit it entirely or provide the \"true\"/\"false\" descriptions");
            }
            json.put("criteria", new JSONObject(criteria.toString()));
        }
        return new OpenRouterDecisionQuestion(json);
    }

    /**
     * Creates a {@code choice} question
     * ({@code {"type":"choice","instructions":...,"criteria":{...}}}) - the
     * decision picks one of the criteria keys.
     *
     * @param instructions the decision instructions
     * @param criteria an object mapping each option key to its description
     *                 (required, non-empty)
     * @return the question
     */
    public static OpenRouterDecisionQuestion choice(String instructions, JSONObject criteria) {
        Objects.requireNonNull(criteria, "choice criteria must not be null");
        if (criteria.length() == 0) {
            throw new IllegalArgumentException("choice criteria must have at least one option");
        }
        JSONObject json = new JSONObject();
        json.put("type", "choice");
        json.put("instructions", requireInstructions(instructions));
        json.put("criteria", new JSONObject(criteria.toString()));
        return new OpenRouterDecisionQuestion(json);
    }

    /**
     * Creates a {@code score} question
     * ({@code {"type":"score","instructions":...,"criteria":[...]}}) - the
     * decision returns a position on the ordered score steps.
     *
     * @param instructions the decision instructions
     * @param criteria the ordered score step descriptions (required, non-empty)
     * @return the question
     */
    public static OpenRouterDecisionQuestion score(String instructions, List<String> criteria) {
        Objects.requireNonNull(criteria, "score criteria must not be null");
        if (criteria.isEmpty()) {
            throw new IllegalArgumentException("score criteria must have at least one step");
        }
        JSONObject json = new JSONObject();
        json.put("type", "score");
        json.put("instructions", requireInstructions(instructions));
        JSONArray arr = new JSONArray();
        for (String step : criteria) {
            if (step == null || step.isEmpty()) {
                throw new IllegalArgumentException("score criteria steps must not be null or empty");
            }
            arr.put(step);
        }
        json.put("criteria", arr);
        return new OpenRouterDecisionQuestion(json);
    }

    /**
     * Creates a {@code score} question from the ordered step strings
     * (varargs convenience for {@link #score(String, List)}).
     *
     * @param instructions the decision instructions
     * @param criteria the ordered score step descriptions (required, non-empty)
     * @return the question
     */
    public static OpenRouterDecisionQuestion score(String instructions, String... criteria) {
        List<String> list = new ArrayList<>();
        for (String step : criteria) {
            list.add(step);
        }
        return score(instructions, list);
    }

    /**
     * Creates a question verbatim - the escape hatch for question types
     * OpenRouter adds after this library was released. The object must carry
     * a {@code type} field.
     *
     * @param json the verbatim question object
     * @return the question
     */
    public static OpenRouterDecisionQuestion raw(JSONObject json) {
        Objects.requireNonNull(json, "question json must not be null");
        if (json.optString("type", null) == null) {
            throw new IllegalArgumentException("a raw question must carry a \"type\" field");
        }
        return new OpenRouterDecisionQuestion(new JSONObject(json.toString()));
    }

    /**
     * @return the discriminator value ({@code noul}, {@code choice},
     *         {@code score} or the raw type of an escape-hatch question)
     */
    public String type() {
        return json.optString("type", null);
    }

    /**
     * @return the JSON object emitted as the {@code questions[key]} value
     */
    public JSONObject toJson() {
        return new JSONObject(json.toString());
    }

    private static String requireInstructions(String instructions) {
        if (instructions == null || instructions.isEmpty()) {
            throw new IllegalArgumentException("instructions are required for every decision question");
        }
        return instructions;
    }

    /**
     * Builds the {@code questions} object from an ordered key-to-question
     * mapping (the wire form is a single JSON object; insertion order is
     * preserved for readability, JSON objects are unordered on the wire).
     *
     * @param questions the ordered key-to-question mapping
     * @return the {@code questions} wire object
     */
    static JSONObject questionsObject(Map<String, OpenRouterDecisionQuestion> questions) {
        JSONObject result = new JSONObject();
        for (Map.Entry<String, OpenRouterDecisionQuestion> entry : new LinkedHashMap<>(questions).entrySet()) {
            result.put(entry.getKey(), entry.getValue().toJson());
        }
        return result;
    }
}
