package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Escape-hatch implementation of {@link OpenRouterServerTool}: emits the
 * {@code type} discriminator plus any extra fields verbatim. Use it for
 * server-tool types without a typed implementation in this library (e.g.
 * {@code openrouter:bash}, {@code openrouter:image_generation},
 * {@code openrouter:advisor}, {@code openrouter:subagent},
 * {@code openrouter:fusion}, {@code openrouter:files}) or for types OpenRouter
 * introduces later.
 * <p>
 * Field values are serialized with their Java type: strings as JSON strings,
 * numbers as JSON numbers, booleans as JSON booleans. A {@code parameters}
 * object can be passed verbatim via {@link #withOption(String, Object)}.
 */
public final class OpenRouterGenericServerTool implements OpenRouterServerTool {

    private final String type;
    private final Map<String, Object> fields;

    /**
     * Creates a generic server tool emitting only the {@code type} field.
     *
     * @param type the server-tool discriminator (e.g. {@code "openrouter:bash"})
     */
    public OpenRouterGenericServerTool(String type) {
        this(type, Map.of());
    }

    /**
     * Creates a generic server tool emitting the {@code type} field plus the
     * given extra fields verbatim.
     *
     * @param type the server-tool discriminator
     * @param fields extra tool fields (may be empty; must not contain {@code "type"})
     */
    public OpenRouterGenericServerTool(String type, Map<String, Object> fields) {
        Objects.requireNonNull(type, "type must not be null");
        if (fields.containsKey("type")) {
            throw new IllegalArgumentException("The 'type' field is set from the constructor argument and must not appear in the extra fields");
        }
        this.type = type;
        this.fields = new LinkedHashMap<>(fields);
    }

    /**
     * Returns a copy of this server tool with one additional extra field.
     *
     * @param key the field name (e.g. {@code "parameters"})
     * @param value the field value (String, Number, Boolean, org.json types or null)
     * @return a new server tool with the field added
     */
    public OpenRouterGenericServerTool withOption(String key, Object value) {
        Map<String, Object> copy = new LinkedHashMap<>(this.fields);
        copy.put(key, value);
        return new OpenRouterGenericServerTool(this.type, copy);
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", type);
        for (Map.Entry<String, Object> e : fields.entrySet()) {
            json.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }
        return json;
    }
}
