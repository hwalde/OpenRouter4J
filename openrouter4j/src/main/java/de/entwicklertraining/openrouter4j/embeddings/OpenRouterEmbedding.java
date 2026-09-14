package de.entwicklertraining.openrouter4j.embeddings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * A typed view of one embedding entry of a POST /embeddings response (the
 * {@code data[]} items): the vector, the input index it belongs to and the
 * object type.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}/empty
 * convention. Use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterEmbedding {

    private final JSONObject json;

    OpenRouterEmbedding(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw response entry behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code index} - the index of the input this embedding was
     * computed for (batch requests return one entry per input).
     *
     * @return the value, or {@code null} when absent
     */
    public Integer index() {
        try {
            if (!json.has("index") || json.isNull("index")) {
                return null;
            }
            return json.optInt("index");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code object} - always {@code "embedding"} on a well-formed
     * entry.
     *
     * @return the value, or {@code null} when absent
     */
    public String object() {
        try {
            return json.optString("object", null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code embedding} read as a float vector. Only populated
     * when the request used {@code encoding_format: "float"} (the default);
     * with {@code "base64"} the field is a string and this returns
     * {@code null} - use {@link #embeddingBase64()} or
     * {@link #vectorFromBase64()} instead. The wire format is IEEE 754
     * float32, so {@code float} is lossless here; all vector accessors of
     * this class return {@code List<Float>} for that reason.
     *
     * @return the embedding vector, or {@code null} when the entry carries a
     *         base64 string instead of an array
     */
    public List<Float> vector() {
        try {
            JSONArray arr = json.optJSONArray("embedding");
            if (arr == null) {
                return null;
            }
            List<Float> vector = new ArrayList<>(arr.length());
            for (int i = 0; i < arr.length(); i++) {
                vector.add((float) arr.optDouble(i));
            }
            return vector;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code embedding} read as a base64 string. Only populated
     * when the request used {@code encoding_format: "base64"}; with the
     * default {@code "float"} the field is an array and this returns
     * {@code null} - use {@link #vector()} instead.
     *
     * @return the base64-encoded vector, or {@code null} when the entry
     *         carries a float array instead of a string
     */
    public String embeddingBase64() {
        try {
            Object value = json.opt("embedding");
            return value instanceof String ? (String) value : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Decodes the base64 form of the embedding (see
     * {@link #embeddingBase64()}) into a float vector. The OpenRouter base64
     * payload is the little-endian IEEE 754 float32 representation of the
     * vector, as produced by the OpenAI-compatible embeddings API.
     *
     * @return the decoded vector, or {@code null} when the entry carries no
     *         base64 string
     */
    public List<Float> vectorFromBase64() {
        String base64 = embeddingBase64();
        if (base64 == null) {
            return null;
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            List<Float> vector = new ArrayList<>(bytes.length / 4);
            for (int offset = 0; offset + 4 <= bytes.length; offset += 4) {
                int bits = (bytes[offset] & 0xFF)
                        | ((bytes[offset + 1] & 0xFF) << 8)
                        | ((bytes[offset + 2] & 0xFF) << 16)
                        | ((bytes[offset + 3] & 0xFF) << 24);
                vector.add(Float.intBitsToFloat(bits));
            }
            return vector;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Convenience accessor that returns the embedding vector regardless of
     * the requested {@code encoding_format}: the float array when present,
     * otherwise the decoded base64 payload.
     *
     * @return the embedding vector as float list, or {@code null} when neither
     *         form is present or decodable
     */
    public List<Float> vectorOrDecoded() {
        List<Float> direct = vector();
        return direct != null ? direct : vectorFromBase64();
    }
}
