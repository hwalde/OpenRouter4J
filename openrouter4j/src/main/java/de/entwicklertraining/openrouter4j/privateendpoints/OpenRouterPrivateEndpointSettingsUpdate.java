package de.entwicklertraining.openrouter4j.privateendpoints;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The request-side {@code settings} object of
 * POST /private-endpoints and PATCH /private-endpoints/{id} (schema
 * {@code UpdatePrivateEndpointSettingsRequest}) - capabilities and limits to
 * set over the ones copied from the public endpoint for this model and
 * provider (or from the model). Omitted fields keep the copied (create) or
 * stored (PATCH) values; the response side is
 * {@link OpenRouterPrivateEndpointSettings}.
 *
 * <p><b>Three wire states per field</b> (the byok {@code declaredZdrSet}
 * pattern): leaving a setter untouched omits the key; a value is sent; an
 * explicit {@code null} is sent as JSON {@code null}. For the nullable
 * fields the schema documents the null leg - it clears a limit or restores
 * the provider default ({@code context_length}, {@code max_prompt_tokens},
 * {@code max_completion_tokens} clear to the model default, the
 * {@code completions}/{@code inputs} flags and
 * {@code reasoning.return_mechanism} revert to the provider default). The
 * fields the schema types as non-nullable ({@code quantization},
 * {@code excluded_parameters}, {@code reasoning.supported} and the four
 * {@code tool_choice} flags) have no null leg - they are emitted only when
 * set.
 *
 * <p>Traps (server-side, checked before anything is written):
 * {@code max_prompt_tokens} may not exceed the context length (400
 * {@code settings_invalid}); both completion APIs may not be off (400
 * {@code settings_invalid}); {@code excluded_parameters} may not name a
 * tested parameter (400 {@code settings_invalid}). The two rules that can be
 * decided inside one object are validated loudly by
 * {@link Builder#build()}; the tested-parameter rule depends on server state
 * and is left to the API.
 */
public final class OpenRouterPrivateEndpointSettingsUpdate {

    private final JSONObject json;

    private OpenRouterPrivateEndpointSettingsUpdate(JSONObject json) {
        this.json = json;
    }

    /**
     * Starting point for building the {@code settings} request value.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return the settings object as it will be sent (fields in the
     *         "explicit JSON {@code null}" state appear as {@code null});
     *         a detached copy, so mutating it cannot change this value
     */
    public JSONObject json() {
        return new JSONObject(json.toString());
    }

    /**
     * Builder for the {@code settings} request value.
     */
    public static final class Builder {

        private Integer contextLength;
        private boolean contextLengthSet;
        private Integer maxPromptTokens;
        private boolean maxPromptTokensSet;
        private Integer maxCompletionTokens;
        private boolean maxCompletionTokensSet;
        private String quantization;
        private List<String> excludedParameters;
        private Boolean completionsChat;
        private boolean completionsChatSet;
        private Boolean completionsText;
        private boolean completionsTextSet;
        private Boolean inputsAudio;
        private boolean inputsAudioSet;
        private Boolean inputsBase64Files;
        private boolean inputsBase64FilesSet;
        private Boolean inputsBase64Images;
        private boolean inputsBase64ImagesSet;
        private Boolean inputsFileUrls;
        private boolean inputsFileUrlsSet;
        private Boolean inputsImageUrls;
        private boolean inputsImageUrlsSet;
        private Boolean reasoningSupported;
        private String reasoningReturnMechanism;
        private boolean reasoningReturnMechanismSet;
        private Boolean toolChoiceAuto;
        private Boolean toolChoiceFunction;
        private Boolean toolChoiceNone;
        private Boolean toolChoiceRequired;

        private Builder() {
        }

        /**
         * Sets the JSON field {@code context_length} - longest prompt plus
         * completion, in tokens (minimum 1, validated loudly). Tri-state:
         * unset keeps the copied (create) or stored (PATCH) value; a value is
         * sent; {@code null} is sent as explicit JSON {@code null} and keeps
         * the model default.
         *
         * @param contextLength the context length, or {@code null} to clear
         * @return this builder
         */
        public Builder contextLength(Integer contextLength) {
            if (contextLength != null && contextLength < 1) {
                throw new IllegalArgumentException("context_length must be at least 1");
            }
            this.contextLength = contextLength;
            this.contextLengthSet = true;
            return this;
        }

        /**
         * Sets the JSON field {@code max_prompt_tokens} - longest prompt, in
         * tokens (minimum 1, validated loudly). Tri-state: unset keeps the
         * copied/stored value; a value is sent; {@code null} clears the cap
         * below the context length. Trap: the value may not exceed
         * {@code context_length}, or the model context length while
         * {@code context_length} is {@code null} (400
         * {@code settings_invalid}); when both limits are named in one
         * object the rule is enforced loudly by {@link #build()}, otherwise
         * the API checks it against the stored context length.
         *
         * @param maxPromptTokens the prompt cap, or {@code null} to clear
         * @return this builder
         */
        public Builder maxPromptTokens(Integer maxPromptTokens) {
            if (maxPromptTokens != null && maxPromptTokens < 1) {
                throw new IllegalArgumentException("max_prompt_tokens must be at least 1");
            }
            this.maxPromptTokens = maxPromptTokens;
            this.maxPromptTokensSet = true;
            return this;
        }

        /**
         * Sets the JSON field {@code max_completion_tokens} - longest
         * completion the upstream returns, in tokens (minimum 1, validated
         * loudly). Tri-state: unset keeps the copied/stored value; a value
         * is sent; {@code null} keeps the model default.
         *
         * @param maxCompletionTokens the completion cap, or {@code null} to
         *        clear
         * @return this builder
         */
        public Builder maxCompletionTokens(Integer maxCompletionTokens) {
            if (maxCompletionTokens != null && maxCompletionTokens < 1) {
                throw new IllegalArgumentException("max_completion_tokens must be at least 1");
            }
            this.maxCompletionTokens = maxCompletionTokens;
            this.maxCompletionTokensSet = true;
            return this;
        }

        /**
         * Sets the JSON field {@code quantization} - weight precision the
         * deployment runs at ({@code int4}, {@code int8}, {@code fp4},
         * {@code mxfp4}, {@code nvfp4}, {@code fp6}, {@code fp8},
         * {@code mxfp8}, {@code fp16}, {@code bf16}, {@code fp32},
         * {@code unknown}); unknown spellings pass through verbatim - the
         * API rejects undocumented ones. Non-nullable in the schema: there
         * is no clear leg; send {@code unknown} for "not stated". Set-only:
         * unset omits the key.
         *
         * @param quantization the quantization spelling
         * @return this builder
         */
        public Builder quantization(String quantization) {
            this.quantization = quantization;
            return this;
        }

        /**
         * Sets the JSON field {@code excluded_parameters} - request
         * parameters the upstream rejects even though the model supports
         * them; requests using one do not route here. Replaces the stored
         * list (send an empty list to clear). Trap: it may not name a tested
         * parameter (400 {@code settings_invalid}) - which ones are tested is
         * server state, so the API checks this. Non-nullable in the schema:
         * set-only, unset omits the key. Unknown parameter names pass through
         * verbatim.
         *
         * @param excludedParameters the excluded parameter names
         * @return this builder
         */
        public Builder excludedParameters(List<String> excludedParameters) {
            this.excludedParameters = excludedParameters;
            return this;
        }

        /**
         * Sets the JSON field {@code completions.chat} - serves
         * {@code /chat/completions}. Tri-state: unset keeps the
         * copied/stored value; a value is sent; {@code null} uses the
         * provider default. Trap: both completion APIs may not be off at the
         * same time (400 {@code settings_invalid}); when both are named
         * {@code false} in one object the rule is enforced loudly by
         * {@link #build()}.
         *
         * @param chat the flag, or {@code null} for the provider default
         * @return this builder
         */
        public Builder completionsChat(Boolean chat) {
            this.completionsChat = chat;
            this.completionsChatSet = true;
            return this;
        }

        /**
         * Sets the JSON field {@code completions.text} - serves
         * {@code /completions}. Tri-state like
         * {@link #completionsChat(Boolean)}; the same both-off rule applies.
         *
         * @param text the flag, or {@code null} for the provider default
         * @return this builder
         */
        public Builder completionsText(Boolean text) {
            this.completionsText = text;
            this.completionsTextSet = true;
            return this;
        }

        /**
         * Sets the JSON field {@code inputs.audio} - accepts audio input.
         * Tri-state: unset keeps the copied/stored value; a value is sent;
         * {@code null} keeps the provider default.
         *
         * @param audio the flag, or {@code null} for the provider default
         * @return this builder
         */
        public Builder inputsAudio(Boolean audio) {
            this.inputsAudio = audio;
            this.inputsAudioSet = true;
            return this;
        }

        /**
         * Sets the JSON field {@code inputs.base64_files} - accepts base64
         * file data. Tri-state like {@link #inputsAudio(Boolean)}.
         *
         * @param base64Files the flag, or {@code null} for the provider
         *        default
         * @return this builder
         */
        public Builder inputsBase64Files(Boolean base64Files) {
            this.inputsBase64Files = base64Files;
            this.inputsBase64FilesSet = true;
            return this;
        }

        /**
         * Sets the JSON field {@code inputs.base64_images} - accepts base64
         * image data. Tri-state like {@link #inputsAudio(Boolean)}.
         *
         * @param base64Images the flag, or {@code null} for the provider
         *        default
         * @return this builder
         */
        public Builder inputsBase64Images(Boolean base64Images) {
            this.inputsBase64Images = base64Images;
            this.inputsBase64ImagesSet = true;
            return this;
        }

        /**
         * Sets the JSON field {@code inputs.file_urls} - accepts file URLs.
         * Tri-state like {@link #inputsAudio(Boolean)}.
         *
         * @param fileUrls the flag, or {@code null} for the provider default
         * @return this builder
         */
        public Builder inputsFileUrls(Boolean fileUrls) {
            this.inputsFileUrls = fileUrls;
            this.inputsFileUrlsSet = true;
            return this;
        }

        /**
         * Sets the JSON field {@code inputs.image_urls} - accepts image
         * URLs. Tri-state like {@link #inputsAudio(Boolean)}.
         *
         * @param imageUrls the flag, or {@code null} for the provider
         *        default
         * @return this builder
         */
        public Builder inputsImageUrls(Boolean imageUrls) {
            this.inputsImageUrls = imageUrls;
            this.inputsImageUrlsSet = true;
            return this;
        }

        /**
         * Sets the JSON field {@code reasoning.supported} - whether requests
         * with {@code reasoning} route to this endpoint. Non-nullable in the
         * schema: set-only, no clear leg; unset omits the key.
         *
         * @param supported the flag
         * @return this builder
         */
        public Builder reasoningSupported(Boolean supported) {
            this.reasoningSupported = supported;
            return this;
        }

        /**
         * Sets the JSON field {@code reasoning.return_mechanism} - where the
         * upstream puts reasoning tokens in its response: {@code reasoning}
         * and {@code reasoning-content} are response fields,
         * {@code content-string} is inline in the message, {@code none}
         * returns no reasoning; unknown spellings pass through verbatim.
         * Tri-state: unset keeps the copied/stored value; a value is sent;
         * {@code null} uses the provider default.
         *
         * @param returnMechanism the mechanism, or {@code null} for the
         *        provider default
         * @return this builder
         */
        public Builder reasoningReturnMechanism(String returnMechanism) {
            this.reasoningReturnMechanism = returnMechanism;
            this.reasoningReturnMechanismSet = true;
            return this;
        }

        /**
         * Sets the JSON field {@code tool_choice.auto} - accepts
         * {@code tool_choice: "auto"}. Non-nullable in the schema:
         * set-only, no clear leg; unset omits the key.
         *
         * @param auto the flag
         * @return this builder
         */
        public Builder toolChoiceAuto(Boolean auto) {
            this.toolChoiceAuto = auto;
            return this;
        }

        /**
         * Sets the JSON field {@code tool_choice.function} - accepts a named
         * function in {@code tool_choice}. Non-nullable in the schema:
         * set-only, no clear leg; unset omits the key.
         *
         * @param function the flag
         * @return this builder
         */
        public Builder toolChoiceFunction(Boolean function) {
            this.toolChoiceFunction = function;
            return this;
        }

        /**
         * Sets the JSON field {@code tool_choice.none} - accepts
         * {@code tool_choice: "none"}. Non-nullable in the schema:
         * set-only, no clear leg; unset omits the key.
         *
         * @param none the flag
         * @return this builder
         */
        public Builder toolChoiceNone(Boolean none) {
            this.toolChoiceNone = none;
            return this;
        }

        /**
         * Sets the JSON field {@code tool_choice.required} - accepts
         * {@code tool_choice: "required"}. Non-nullable in the schema:
         * set-only, no clear leg; unset omits the key.
         *
         * @param required the flag
         * @return this builder
         */
        public Builder toolChoiceRequired(Boolean required) {
            this.toolChoiceRequired = required;
            return this;
        }

        /**
         * Builds the settings request value. Enforces the two documented
         * {@code settings_invalid} rules that are decidable inside one
         * object: {@code max_prompt_tokens} may not exceed a
         * {@code context_length} named in the same object, and the two
         * completion APIs may not both be named {@code false}.
         *
         * @return the settings value
         * @throws IllegalArgumentException when a documented combination is
         *         invalid
         */
        public OpenRouterPrivateEndpointSettingsUpdate build() {
            if (maxPromptTokensSet && maxPromptTokens != null
                    && contextLengthSet && contextLength != null
                    && maxPromptTokens > contextLength) {
                throw new IllegalArgumentException(
                        "max_prompt_tokens (" + maxPromptTokens + ") cannot exceed context_length ("
                                + contextLength + ")");
            }
            if (Boolean.FALSE.equals(completionsChat) && Boolean.FALSE.equals(completionsText)) {
                throw new IllegalArgumentException(
                        "completions.chat and completions.text cannot both be off (400 settings_invalid)");
            }
            JSONObject json = new JSONObject();
            if (contextLengthSet) {
                json.put("context_length", contextLength == null ? JSONObject.NULL : contextLength);
            }
            if (maxPromptTokensSet) {
                json.put("max_prompt_tokens", maxPromptTokens == null ? JSONObject.NULL : maxPromptTokens);
            }
            if (maxCompletionTokensSet) {
                json.put("max_completion_tokens",
                        maxCompletionTokens == null ? JSONObject.NULL : maxCompletionTokens);
            }
            if (quantization != null) {
                json.put("quantization", quantization);
            }
            if (excludedParameters != null) {
                json.put("excluded_parameters", new JSONArray(excludedParameters));
            }
            JSONObject completions = new JSONObject();
            if (completionsChatSet) {
                completions.put("chat", completionsChat == null ? JSONObject.NULL : completionsChat);
            }
            if (completionsTextSet) {
                completions.put("text", completionsText == null ? JSONObject.NULL : completionsText);
            }
            if (!completions.isEmpty()) {
                json.put("completions", completions);
            }
            JSONObject inputs = new JSONObject();
            if (inputsAudioSet) {
                inputs.put("audio", inputsAudio == null ? JSONObject.NULL : inputsAudio);
            }
            if (inputsBase64FilesSet) {
                inputs.put("base64_files", inputsBase64Files == null ? JSONObject.NULL : inputsBase64Files);
            }
            if (inputsBase64ImagesSet) {
                inputs.put("base64_images", inputsBase64Images == null ? JSONObject.NULL : inputsBase64Images);
            }
            if (inputsFileUrlsSet) {
                inputs.put("file_urls", inputsFileUrls == null ? JSONObject.NULL : inputsFileUrls);
            }
            if (inputsImageUrlsSet) {
                inputs.put("image_urls", inputsImageUrls == null ? JSONObject.NULL : inputsImageUrls);
            }
            if (!inputs.isEmpty()) {
                json.put("inputs", inputs);
            }
            JSONObject reasoning = new JSONObject();
            if (reasoningSupported != null) {
                reasoning.put("supported", reasoningSupported);
            }
            if (reasoningReturnMechanismSet) {
                reasoning.put("return_mechanism",
                        reasoningReturnMechanism == null ? JSONObject.NULL : reasoningReturnMechanism);
            }
            if (!reasoning.isEmpty()) {
                json.put("reasoning", reasoning);
            }
            JSONObject toolChoice = new JSONObject();
            if (toolChoiceAuto != null) {
                toolChoice.put("auto", toolChoiceAuto);
            }
            if (toolChoiceFunction != null) {
                toolChoice.put("function", toolChoiceFunction);
            }
            if (toolChoiceNone != null) {
                toolChoice.put("none", toolChoiceNone);
            }
            if (toolChoiceRequired != null) {
                toolChoice.put("required", toolChoiceRequired);
            }
            if (!toolChoice.isEmpty()) {
                json.put("tool_choice", toolChoice);
            }
            return new OpenRouterPrivateEndpointSettingsUpdate(json);
        }
    }
}
