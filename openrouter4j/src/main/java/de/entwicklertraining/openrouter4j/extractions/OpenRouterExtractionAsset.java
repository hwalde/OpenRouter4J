package de.entwicklertraining.openrouter4j.extractions;

import org.json.JSONObject;

import java.util.regex.Pattern;

/**
 * An image or PDF of an Extractions request - one entry of the
 * {@code assets} map of {@link OpenRouterExtractionsRequest}, keyed by a
 * caller-chosen reference string (schema {@code ExtractionAsset}).
 *
 * <p>Write the reference string wherever the asset belongs in {@code state}
 * or a field {@code instructions}; every asset must be referenced at least
 * once. {@code data_base64} is the file in standard base64 with no
 * {@code data:} prefix. PDF is read by document models only.
 *
 * <p>Traps: a {@code state} that is exactly the sole asset's key carries no
 * text of its own and is read as file-only. A model that reads either text
 * or a document per request, not both (ScaleDown Extract with a PDF), needs
 * that form - any other text around the key is state text and is rejected
 * with 400. The documented {@code format} spellings are {@code png},
 * {@code jpg}, {@code jpeg} and {@code pdf}, and the documented
 * {@code type} spellings are {@code image} and {@code document}; both are
 * passed through verbatim, so unknown spellings are rejected by the API,
 * not by this library.
 *
 * <p>Alpha: this schema belongs to the {@code /api/alpha} namespace and may
 * change or disappear without a major version of the API.
 */
public final class OpenRouterExtractionAsset {

    private static final Pattern BASE64 = Pattern.compile(
            "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$");

    private final JSONObject json;

    private OpenRouterExtractionAsset(JSONObject json) {
        this.json = json;
    }

    /**
     * Creates an asset without the optional {@code type} hint.
     *
     * @param format the file format ({@code png}, {@code jpg}, {@code jpeg}
     *               or {@code pdf}; unknown spellings pass through verbatim)
     * @param dataBase64 the file in standard base64 with no {@code data:}
     *                   prefix (validated loudly: non-empty and well-formed
     *                   standard base64)
     * @return the asset
     */
    public static OpenRouterExtractionAsset of(String format, String dataBase64) {
        return of(format, dataBase64, null);
    }

    /**
     * Creates an asset with the optional {@code type} hint.
     *
     * @param format the file format ({@code png}, {@code jpg}, {@code jpeg}
     *               or {@code pdf}; unknown spellings pass through verbatim)
     * @param dataBase64 the file in standard base64 with no {@code data:}
     *                   prefix (validated loudly: non-empty and well-formed
     *                   standard base64)
     * @param type the asset kind ({@code image} or {@code document}; unknown
     *             spellings pass through verbatim), or {@code null} to omit
     *             the hint
     * @return the asset
     */
    public static OpenRouterExtractionAsset of(String format, String dataBase64, String type) {
        if (format == null || format.isEmpty()) {
            throw new IllegalArgumentException("asset format is required (\"png\", \"jpg\", \"jpeg\" or \"pdf\")");
        }
        if (dataBase64 == null || dataBase64.isEmpty()) {
            throw new IllegalArgumentException("asset data_base64 is required and must not be empty");
        }
        if (!BASE64.matcher(dataBase64).matches()) {
            throw new IllegalArgumentException(
                    "asset data_base64 must be standard base64 (with padding, no \"data:\" prefix)");
        }
        JSONObject json = new JSONObject();
        json.put("format", format);
        json.put("data_base64", dataBase64);
        if (type != null) {
            json.put("type", type);
        }
        return new OpenRouterExtractionAsset(json);
    }

    /**
     * JSON path: {@code format} - the file format as written.
     *
     * @return the format, or {@code null} when not a string
     */
    public String format() {
        Object value = json.opt("format");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code data_base64} - the file in standard base64 with no
     * {@code data:} prefix.
     *
     * @return the base64 payload, or {@code null} when not a string
     */
    public String dataBase64() {
        Object value = json.opt("data_base64");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code type} - the asset kind ({@code image} or
     * {@code document}; unknown spellings pass through verbatim).
     *
     * @return the type hint, or {@code null} when unset or not a string
     */
    public String type() {
        Object value = json.opt("type");
        return value instanceof String text ? text : null;
    }

    /**
     * @return the JSON object emitted as the {@code assets[reference]} value
     */
    public JSONObject toJson() {
        return new JSONObject(json.toString());
    }
}
