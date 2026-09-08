package de.entwicklertraining.openrouter4j.chat.completion;

import de.entwicklertraining.api.base.ApiClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the response accessors against recorded JSON shapes of OpenRouter
 * responses (reasoning output, native finish reason, provider name, routing
 * metadata, top-level error object, usage extras).
 */
class OpenRouterChatCompletionResponseTest {

    private OpenRouterChatCompletionResponse responseOf(String json) {
        return new OpenRouterChatCompletionResponse(new JSONObject(json), null);
    }

    @Test
    void reasoningAndReasoningDetailsAreSurfaced() {
        OpenRouterChatCompletionResponse response = responseOf("""
                {
                  "choices": [{
                    "index": 0,
                    "message": {
                      "role": "assistant",
                      "content": "The answer is 4.",
                      "reasoning": "The user asked 2+2. Basic arithmetic: 2+2=4.",
                      "reasoning_details": [
                        {"type": "text", "text": "step 1", "format": "openrouter"},
                        {"type": "reasoning.encrypted", "data": "enc"}
                      ]
                    },
                    "finish_reason": "stop"
                  }]
                }
                """);

        assertThat(response.reasoning()).isEqualTo("The user asked 2+2. Basic arithmetic: 2+2=4.");
        assertThat(response.reasoningDetails()).hasSize(2);
        assertThat(response.reasoningDetails().get(0).getString("type")).isEqualTo("text");
        assertThat(response.reasoningDetails().get(1).getString("type")).isEqualTo("reasoning.encrypted");
    }

    @Test
    void reasoningAccessorsReturnEmptyOrNullWhenAbsent() {
        OpenRouterChatCompletionResponse response = responseOf("""
                {"choices": [{"index": 0, "message": {"role": "assistant", "content": "Hi"}, "finish_reason": "stop"}]}
                """);

        assertThat(response.reasoning()).isNull();
        assertThat(response.reasoningDetails()).isEmpty();
    }

    @Test
    void nativeFinishReasonAndProviderAreSurfaced() {
        OpenRouterChatCompletionResponse response = responseOf("""
                {
                  "model": "anthropic/claude-sonnet-4",
                  "provider": "Anthropic",
                  "choices": [{
                    "index": 0,
                    "message": {"role": "assistant", "content": "Hi"},
                    "finish_reason": "stop",
                    "native_finish_reason": "end_turn"
                  }]
                }
                """);

        assertThat(response.nativeFinishReason()).isEqualTo("end_turn");
        assertThat(response.finishReason()).isEqualTo("stop");
        assertThat(response.provider()).isEqualTo("Anthropic");
    }

    @Test
    void openrouterMetadataIsSurfaced() {
        OpenRouterChatCompletionResponse response = responseOf("""
                {
                  "choices": [],
                  "openrouter_metadata": {
                    "provider_name": "Google AI Studio",
                    "usage": 0.001
                  }
                }
                """);

        assertThat(response.openrouterMetadata()).isNotNull();
        assertThat(response.openrouterMetadata().getString("provider_name")).isEqualTo("Google AI Studio");
    }

    @Test
    void openrouterMetadataIsNullWhenAbsent() {
        OpenRouterChatCompletionResponse response = responseOf("""
                {"choices": [{"index": 0, "message": {"role": "assistant", "content": "Hi"}, "finish_reason": "stop"}]}
                """);

        assertThat(response.openrouterMetadata()).isNull();
    }

    @Test
    void topLevelErrorIsDetectedAndThrowsLoudly() {
        OpenRouterChatCompletionResponse response = responseOf("""
                {
                  "error": {
                    "code": 429,
                    "message": "Rate limit exceeded",
                    "metadata": {"error_type": "rate_limit_exceeded"}
                  }
                }
                """);

        assertThat(response.hasError()).isTrue();
        assertThat(response.errorCode()).isEqualTo(429);
        assertThat(response.errorMessage()).isEqualTo("Rate limit exceeded");
        assertThat(response.error()).isNotNull();

        assertThatThrownBy(response::throwOnError)
                .isInstanceOf(ApiClient.ApiResponseUnusableException.class)
                .hasMessageContaining("Rate limit exceeded");
    }

    @Test
    void malformedTopLevelErrorStillThrowsLoudly() {
        // error present but not an object - must not NPE, must throw loudly
        OpenRouterChatCompletionResponse response = responseOf("{\"error\": \"rate limit\"}");

        assertThat(response.hasError()).isTrue();
        assertThat(response.error()).isNull();
        assertThat(response.errorMessage()).isNull();

        assertThatThrownBy(response::throwOnError)
                .isInstanceOf(ApiClient.ApiResponseUnusableException.class)
                .hasMessageContaining("rate limit");
    }

    @Test
    void nonNumericErrorCodeReturnsNull() {
        OpenRouterChatCompletionResponse response = responseOf("""
                {"error": {"code": "internal", "message": "boom"}}
                """);

        assertThat(response.errorCode()).isNull();
        assertThat(response.errorMessage()).isEqualTo("boom");
    }

    @Test
    void successfulResponseHasNoError() {
        OpenRouterChatCompletionResponse response = responseOf("""
                {"choices": [{"index": 0, "message": {"role": "assistant", "content": "Hi"}, "finish_reason": "stop"}]}
                """);

        assertThat(response.hasError()).isFalse();
        assertThat(response.error()).isNull();
        assertThatCode(response::throwOnError).doesNotThrowAnyException();
    }

    @Test
    void usageExtrasAreSurfaced() {
        OpenRouterChatCompletionResponse response = responseOf("""
                {
                  "choices": [{
                    "index": 0,
                    "message": {"role": "assistant", "content": "Hi"},
                    "finish_reason": "stop"
                  }],
                  "usage": {
                    "prompt_tokens": 10,
                    "completion_tokens": 15,
                    "total_tokens": 25,
                    "cost": 0.0012,
                    "cost_details": {
                      "upstream_inference_cost": null,
                      "upstream_inference_prompt_cost": 0.0008,
                      "upstream_inference_completions_cost": 0.0004
                    },
                    "prompt_tokens_details": {"cached_tokens": 2},
                    "completion_tokens_details": {"reasoning_tokens": 5}
                  }
                }
                """);

        assertThat(response.cost()).isEqualTo(0.0012);
        assertThat(response.costDetails()).isNotNull();
        assertThat(response.costDetails().getDouble("upstream_inference_prompt_cost")).isEqualTo(0.0008);
        assertThat(response.cachedPromptTokens()).isEqualTo(2);
        assertThat(response.reasoningTokens()).isEqualTo(5);
        assertThat(response.promptTokens()).isEqualTo(10);
        assertThat(response.completionTokens()).isEqualTo(15);
    }

    @Test
    void usageExtrasReturnNullWhenAbsent() {
        OpenRouterChatCompletionResponse response = responseOf("""
                {"choices": [{"index": 0, "message": {"role": "assistant", "content": "Hi"}, "finish_reason": "stop"}]}
                """);

        assertThat(response.cost()).isNull();
        assertThat(response.costDetails()).isNull();
        assertThat(response.cachedPromptTokens()).isNull();
        assertThat(response.reasoningTokens()).isNull();
        assertThat(response.acceptedPredictionTokens()).isNull();
        assertThat(response.rejectedPredictionTokens()).isNull();
        assertThat(response.serviceTier()).isNull();
    }

    @Test
    void serviceTierAndPredictionTokenUsageAreSurfaced() {
        OpenRouterChatCompletionResponse response = responseOf("""
                {
                  "service_tier": "flex",
                  "choices": [{
                    "index": 0,
                    "message": {"role": "assistant", "content": "Hi"},
                    "finish_reason": "stop"
                  }],
                  "usage": {
                    "prompt_tokens": 10,
                    "completion_tokens": 15,
                    "total_tokens": 25,
                    "completion_tokens_details": {
                      "reasoning_tokens": 5,
                      "accepted_prediction_tokens": 20,
                      "rejected_prediction_tokens": 3
                    }
                  }
                }
                """);

        assertThat(response.serviceTier()).isEqualTo("flex");
        assertThat(response.acceptedPredictionTokens()).isEqualTo(20);
        assertThat(response.rejectedPredictionTokens()).isEqualTo(3);
        assertThat(response.reasoningTokens()).isEqualTo(5);
    }

    @Test
    void malformedResponseLooksEmptyButNotFailed() {
        OpenRouterChatCompletionResponse response = responseOf("""
                {"not_a_valid_shape": true}
                """);

        assertThat(response.assistantMessage()).isNull();
        assertThat(response.hasError()).isFalse();
        assertThat(response.reasoningDetails()).isEmpty();
    }

    @Test
    void logprobsAndSystemFingerprintAreSurfaced() {
        OpenRouterChatCompletionResponse response = responseOf("""
                {
                  "system_fingerprint": "fp_64829e98a1",
                  "choices": [{
                    "index": 0,
                    "message": {"role": "assistant", "content": "Hi"},
                    "finish_reason": "stop",
                    "logprobs": {
                      "content": [
                        {"token": "Hi", "logprob": -0.02, "bytes": [72, 105],
                         "top_logprobs": [{"token": "Hi", "logprob": -0.02}]}
                      ],
                      "refusal": []
                    }
                  }]
                }
                """);

        assertThat(response.systemFingerprint()).isEqualTo("fp_64829e98a1");
        assertThat(response.logprobs()).isNotNull();
        assertThat(response.logprobs().getJSONArray("content").getJSONObject(0)
                .getString("token")).isEqualTo("Hi");
        assertThat(response.logprobs().getJSONArray("content").getJSONObject(0)
                .getDouble("logprob")).isEqualTo(-0.02);
    }

    @Test
    void logprobsAndSystemFingerprintReturnNullWhenAbsent() {
        OpenRouterChatCompletionResponse response = responseOf("""
                {"choices": [{"index": 0, "message": {"role": "assistant", "content": "Hi"}, "finish_reason": "stop"}]}
                """);

        assertThat(response.logprobs()).isNull();
        assertThat(response.systemFingerprint()).isNull();
    }

    @Test
    void imagesAndImageUrlsAreSurfaced() {
        OpenRouterChatCompletionResponse response = responseOf("""
                {
                  "choices": [{
                    "index": 0,
                    "message": {
                      "role": "assistant",
                      "content": "",
                      "images": [
                        {"image_url": {"url": "https://cdn.example.com/img0.png"}},
                        {"image_url": {"url": "data:image/png;base64,AAAA"}}
                      ]
                    },
                    "finish_reason": "stop"
                  }]
                }
                """);

        assertThat(response.images()).isNotNull();
        assertThat(response.images().length()).isEqualTo(2);
        assertThat(response.imageUrls()).containsExactly(
                "https://cdn.example.com/img0.png",
                "data:image/png;base64,AAAA");
    }

    @Test
    void imageAccessorsReturnEmptyOrNullWhenAbsent() {
        OpenRouterChatCompletionResponse response = responseOf("""
                {"choices": [{"index": 0, "message": {"role": "assistant", "content": "Hi"}, "finish_reason": "stop"}]}
                """);

        assertThat(response.images()).isNull();
        assertThat(response.imageUrls()).isEmpty();
    }

    @Test
    void audioOutputIsSurfaced() {
        OpenRouterChatCompletionResponse response = responseOf("""
                {
                  "choices": [{
                    "index": 0,
                    "message": {
                      "role": "assistant",
                      "content": null,
                      "audio": {
                        "id": "audio_abc123",
                        "data": "SGVsbG8=",
                        "expires_at": 1700000000,
                        "transcript": "Hello"
                      }
                    },
                    "finish_reason": "stop"
                  }]
                }
                """);

        assertThat(response.audio()).isNotNull();
        assertThat(response.audioId()).isEqualTo("audio_abc123");
        assertThat(response.audioData()).isEqualTo("SGVsbG8=");
        assertThat(response.audioExpiresAt()).isEqualTo(1700000000L);
        assertThat(response.audioTranscript()).isEqualTo("Hello");
    }

    @Test
    void audioAccessorsReturnNullWhenAbsent() {
        OpenRouterChatCompletionResponse response = responseOf("""
                {"choices": [{"index": 0, "message": {"role": "assistant", "content": "Hi"}, "finish_reason": "stop"}]}
                """);

        assertThat(response.audio()).isNull();
        assertThat(response.audioId()).isNull();
        assertThat(response.audioData()).isNull();
        assertThat(response.audioExpiresAt()).isNull();
        assertThat(response.audioTranscript()).isNull();
    }
}
