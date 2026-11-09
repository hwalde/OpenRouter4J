package de.entwicklertraining.openrouter4j.privateendpoints;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A typed view of the capabilities and limits routing uses for a private
 * endpoint (schema {@code PrivateEndpointSettings}). A new endpoint copies
 * them from the public endpoint for the same model and provider, or from the
 * model; the owner changes them through the {@code settings} field of
 * create/update (request side:
 * {@link OpenRouterPrivateEndpointSettingsUpdate}). Rate limits are not part
 * of this object - a private endpoint is billed to its own upstream key, so
 * request limits stay with the provider.
 *
 * <p>The nested groups ({@link Completions}, {@link Inputs},
 * {@link Reasoning}, {@link ToolChoice}) are typed views of their own, each
 * with a {@code json()} hatch.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention; use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterPrivateEndpointSettings {

    private final JSONObject json;

    OpenRouterPrivateEndpointSettings(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw settings object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code context_length} - longest prompt plus completion, in
     * tokens, or {@code null} to keep the model default (and when the field
     * is absent or not a number).
     *
     * @return the context length, or {@code null}
     */
    public Integer contextLength() {
        Object value = json.opt("context_length");
        return value instanceof Number number ? number.intValue() : null;
    }

    /**
     * JSON path: {@code max_prompt_tokens} - longest prompt, in tokens, or
     * {@code null} for no cap below the context length (and when the field
     * is absent or not a number). Cannot exceed {@code context_length}, or
     * the model context length while {@code context_length} is {@code null}.
     *
     * @return the prompt cap, or {@code null}
     */
    public Integer maxPromptTokens() {
        Object value = json.opt("max_prompt_tokens");
        return value instanceof Number number ? number.intValue() : null;
    }

    /**
     * JSON path: {@code max_completion_tokens} - longest completion the
     * upstream returns, in tokens, or {@code null} to keep the model default
     * (and when the field is absent or not a number). This is the settings
     * copy of the cap; the managed and full endpoint views surface their own
     * top-level {@code max_completion_tokens}.
     *
     * @return the completion cap, or {@code null}
     */
    public Integer maxCompletionTokens() {
        Object value = json.opt("max_completion_tokens");
        return value instanceof Number number ? number.intValue() : null;
    }

    /**
     * JSON path: {@code quantization} - weight precision the deployment
     * runs at ({@code int4}, {@code int8}, {@code fp4}, {@code mxfp4},
     * {@code nvfp4}, {@code fp6}, {@code fp8}, {@code mxfp8}, {@code fp16},
     * {@code bf16}, {@code fp32}, {@code unknown}); {@code unknown} when not
     * stated. Unknown spellings pass through verbatim.
     *
     * @return the quantization, or {@code null} when absent or not a string
     */
    public String quantization() {
        Object value = json.opt("quantization");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code excluded_parameters} - request parameters the
     * upstream rejects even though the model supports them; requests using
     * one do not route here. Cannot name a tested parameter. Non-string
     * entries are dropped.
     *
     * @return the excluded parameter names, empty when absent or malformed
     */
    public List<String> excludedParameters() {
        return stringList(json.opt("excluded_parameters"));
    }

    /**
     * JSON path: {@code completions} - which completion APIs this endpoint
     * serves.
     *
     * @return the group view, or {@code null} when absent or not an object
     */
    public Completions completions() {
        JSONObject group = json.optJSONObject("completions");
        return group != null ? new Completions(group) : null;
    }

    /**
     * JSON path: {@code inputs} - which input forms this endpoint accepts.
     *
     * @return the group view, or {@code null} when absent or not an object
     */
    public Inputs inputs() {
        JSONObject group = json.optJSONObject("inputs");
        return group != null ? new Inputs(group) : null;
    }

    /**
     * JSON path: {@code reasoning} - whether requests with {@code reasoning}
     * route here and where the upstream puts reasoning tokens.
     *
     * @return the group view, or {@code null} when absent or not an object
     */
    public Reasoning reasoning() {
        JSONObject group = json.optJSONObject("reasoning");
        return group != null ? new Reasoning(group) : null;
    }

    /**
     * JSON path: {@code tool_choice} - which {@code tool_choice} modes the
     * upstream accepts. Declaring {@code tool_choice} as a supported
     * parameter requires {@code tools} to be tested or declared as well.
     *
     * @return the group view, or {@code null} when absent or not an object
     */
    public ToolChoice toolChoice() {
        JSONObject group = json.optJSONObject("tool_choice");
        return group != null ? new ToolChoice(group) : null;
    }

    private static List<String> stringList(Object value) {
        List<String> result = new ArrayList<>();
        if (value instanceof JSONArray array) {
            for (int i = 0; i < array.length(); i++) {
                Object entry = array.opt(i);
                if (entry instanceof String text) {
                    result.add(text);
                }
            }
        }
        return result;
    }

    /**
     * Typed view of the {@code completions} group (which completion APIs the
     * endpoint serves).
     */
    public static final class Completions {

        private final JSONObject json;

        Completions(JSONObject json) {
            this.json = json;
        }

        /** @return the raw group object behind this view */
        public JSONObject json() {
            return json;
        }

        /**
         * JSON path: {@code completions.chat} - serves
         * {@code /chat/completions}; {@code null} uses the provider default.
         *
         * @return the flag, or {@code null} when absent or not a boolean
         */
        public Boolean chat() {
            Object value = json.opt("chat");
            return value instanceof Boolean flag ? flag : null;
        }

        /**
         * JSON path: {@code completions.text} - serves {@code /completions};
         * {@code null} uses the provider default.
         *
         * @return the flag, or {@code null} when absent or not a boolean
         */
        public Boolean text() {
            Object value = json.opt("text");
            return value instanceof Boolean flag ? flag : null;
        }
    }

    /**
     * Typed view of the {@code inputs} group (which input forms the
     * endpoint accepts).
     */
    public static final class Inputs {

        private final JSONObject json;

        Inputs(JSONObject json) {
            this.json = json;
        }

        /** @return the raw group object behind this view */
        public JSONObject json() {
            return json;
        }

        /**
         * JSON path: {@code inputs.audio} - accepts audio input;
         * {@code null} keeps the provider default.
         *
         * @return the flag, or {@code null} when absent or not a boolean
         */
        public Boolean audio() {
            Object value = json.opt("audio");
            return value instanceof Boolean flag ? flag : null;
        }

        /**
         * JSON path: {@code inputs.base64_files} - accepts base64 file data;
         * {@code null} keeps the provider default.
         *
         * @return the flag, or {@code null} when absent or not a boolean
         */
        public Boolean base64Files() {
            Object value = json.opt("base64_files");
            return value instanceof Boolean flag ? flag : null;
        }

        /**
         * JSON path: {@code inputs.base64_images} - accepts base64 image
         * data; {@code null} keeps the provider default.
         *
         * @return the flag, or {@code null} when absent or not a boolean
         */
        public Boolean base64Images() {
            Object value = json.opt("base64_images");
            return value instanceof Boolean flag ? flag : null;
        }

        /**
         * JSON path: {@code inputs.file_urls} - accepts file URLs;
         * {@code null} keeps the provider default.
         *
         * @return the flag, or {@code null} when absent or not a boolean
         */
        public Boolean fileUrls() {
            Object value = json.opt("file_urls");
            return value instanceof Boolean flag ? flag : null;
        }

        /**
         * JSON path: {@code inputs.image_urls} - accepts image URLs;
         * {@code null} keeps the provider default.
         *
         * @return the flag, or {@code null} when absent or not a boolean
         */
        public Boolean imageUrls() {
            Object value = json.opt("image_urls");
            return value instanceof Boolean flag ? flag : null;
        }
    }

    /**
     * Typed view of the {@code reasoning} group.
     */
    public static final class Reasoning {

        private final JSONObject json;

        Reasoning(JSONObject json) {
            this.json = json;
        }

        /** @return the raw group object behind this view */
        public JSONObject json() {
            return json;
        }

        /**
         * JSON path: {@code reasoning.supported} - whether requests with
         * {@code reasoning} route to this endpoint.
         *
         * @return the flag, or {@code null} when absent or not a boolean
         */
        public Boolean supported() {
            Object value = json.opt("supported");
            return value instanceof Boolean flag ? flag : null;
        }

        /**
         * JSON path: {@code reasoning.return_mechanism} - where the upstream
         * puts reasoning tokens in its response: {@code reasoning} and
         * {@code reasoning-content} are response fields, {@code content-string}
         * is inline in the message, {@code none} returns no reasoning;
         * {@code null} uses the provider default. Unknown spellings pass
         * through verbatim.
         *
         * @return the mechanism, or {@code null} when absent or not a string
         */
        public String returnMechanism() {
            Object value = json.opt("return_mechanism");
            return value instanceof String text ? text : null;
        }
    }

    /**
     * Typed view of the {@code tool_choice} group: which {@code tool_choice}
     * modes the upstream accepts.
     */
    public static final class ToolChoice {

        private final JSONObject json;

        ToolChoice(JSONObject json) {
            this.json = json;
        }

        /** @return the raw group object behind this view */
        public JSONObject json() {
            return json;
        }

        /**
         * JSON path: {@code tool_choice.auto} - accepts
         * {@code tool_choice: "auto"}.
         *
         * @return the flag, or {@code null} when absent or not a boolean
         */
        public Boolean auto() {
            Object value = json.opt("auto");
            return value instanceof Boolean flag ? flag : null;
        }

        /**
         * JSON path: {@code tool_choice.function} - accepts a named function
         * in {@code tool_choice}.
         *
         * @return the flag, or {@code null} when absent or not a boolean
         */
        public Boolean function() {
            Object value = json.opt("function");
            return value instanceof Boolean flag ? flag : null;
        }

        /**
         * JSON path: {@code tool_choice.none} - accepts
         * {@code tool_choice: "none"}.
         *
         * @return the flag, or {@code null} when absent or not a boolean
         */
        public Boolean none() {
            Object value = json.opt("none");
            return value instanceof Boolean flag ? flag : null;
        }

        /**
         * JSON path: {@code tool_choice.required} - accepts
         * {@code tool_choice: "required"}.
         *
         * @return the flag, or {@code null} when absent or not a boolean
         */
        public Boolean required() {
            Object value = json.opt("required");
            return value instanceof Boolean flag ? flag : null;
        }
    }
}
