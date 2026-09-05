package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Escape-hatch implementation of {@link OpenRouterPlugin}: emits the plugin
 * {@code id} plus any extra fields verbatim. Use it for plugin ids without a
 * typed implementation in this library (e.g. {@code file-parser},
 * {@code moderation}, {@code context-compression}, {@code auto-router},
 * {@code fusion}) or for ids OpenRouter introduces later.
 * <p>
 * Field values are serialized with their Java type: strings as JSON strings,
 * numbers as JSON numbers, booleans as JSON booleans.
 */
public final class OpenRouterGenericPlugin implements OpenRouterPlugin {

    private final String id;
    private final Map<String, Object> fields;

    /**
     * Creates a generic plugin emitting only the {@code id} field.
     *
     * @param id the plugin discriminator (e.g. {@code "file-parser"})
     */
    public OpenRouterGenericPlugin(String id) {
        this(id, Map.of());
    }

    /**
     * Creates a generic plugin emitting the {@code id} field plus the given
     * extra fields verbatim.
     *
     * @param id the plugin discriminator
     * @param fields extra plugin fields (may be empty; must not contain {@code "id"})
     */
    public OpenRouterGenericPlugin(String id, Map<String, Object> fields) {
        Objects.requireNonNull(id, "id must not be null");
        if (fields.containsKey("id")) {
            throw new IllegalArgumentException("The 'id' field is set from the constructor argument and must not appear in the extra fields");
        }
        this.id = id;
        this.fields = new LinkedHashMap<>(fields);
    }

    /**
     * Returns a copy of this plugin with one additional extra field.
     *
     * @param key the field name
     * @param value the field value (String, Number, Boolean, org.json types or null)
     * @return a new plugin with the field added
     */
    public OpenRouterGenericPlugin withOption(String key, Object value) {
        Map<String, Object> copy = new LinkedHashMap<>(this.fields);
        copy.put(key, value);
        return new OpenRouterGenericPlugin(this.id, copy);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        for (Map.Entry<String, Object> e : fields.entrySet()) {
            json.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }
        return json;
    }
}
