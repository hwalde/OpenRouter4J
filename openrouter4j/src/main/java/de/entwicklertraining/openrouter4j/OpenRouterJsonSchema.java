package de.entwicklertraining.openrouter4j;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A structure that helps define the JSON schema for structured outputs in OpenRouter.
 * OpenRouter supports JSON Schema format for response_format and tool parameters.
 * Unlike Gemini, OpenRouter does support additionalProperties.
 */
public sealed interface OpenRouterJsonSchema permits OpenRouterJsonSchemaImpl {

    JSONObject toJson();

    OpenRouterJsonSchema description(String desc);

    OpenRouterJsonSchema property(String name, OpenRouterJsonSchema schema, boolean requiredField);

    OpenRouterJsonSchema items(OpenRouterJsonSchema itemSchema);

    OpenRouterJsonSchema enumValues(String... values);

    OpenRouterJsonSchema additionalProperties(boolean allowed);

    static OpenRouterJsonSchema objectSchema() {
        return new OpenRouterJsonSchemaImpl("object");
    }

    static OpenRouterJsonSchema stringSchema(String description) {
        OpenRouterJsonSchemaImpl schema = new OpenRouterJsonSchemaImpl("string");
        schema.description(description);
        return schema;
    }

    static OpenRouterJsonSchema numberSchema(String description) {
        OpenRouterJsonSchemaImpl schema = new OpenRouterJsonSchemaImpl("number");
        schema.description(description);
        return schema;
    }

    static OpenRouterJsonSchema booleanSchema(String description) {
        OpenRouterJsonSchemaImpl schema = new OpenRouterJsonSchemaImpl("boolean");
        schema.description(description);
        return schema;
    }

    static OpenRouterJsonSchema integerSchema(String description) {
        OpenRouterJsonSchemaImpl schema = new OpenRouterJsonSchemaImpl("integer");
        schema.description(description);
        return schema;
    }

    static OpenRouterJsonSchema arraySchema(OpenRouterJsonSchema itemsSchema) {
        OpenRouterJsonSchemaImpl schema = new OpenRouterJsonSchemaImpl("array");
        schema.items(itemsSchema);
        return schema;
    }

    static OpenRouterJsonSchema enumSchema(String description, String... enumVals) {
        OpenRouterJsonSchemaImpl schema = new OpenRouterJsonSchemaImpl("string");
        schema.description(description);
        schema.enumValues(enumVals);
        return schema;
    }

    static OpenRouterJsonSchema anyOf(OpenRouterJsonSchema... variants) {
        OpenRouterJsonSchemaImpl schema = new OpenRouterJsonSchemaImpl(null);
        schema.setAnyOfMode(true);
        for (OpenRouterJsonSchema variant : variants) {
            schema.getAnyOfSchemas().put(variant.toJson());
        }
        return schema;
    }
}

final class OpenRouterJsonSchemaImpl implements OpenRouterJsonSchema {

    private String type; // "object", "array", "string", etc. May be null in anyOfMode
    private String description;
    private final JSONObject properties;
    private final JSONArray required;
    private final JSONArray enumValues;
    private OpenRouterJsonSchema itemsSchema;
    private final JSONArray anyOfSchemas;
    private boolean additionalProperties;
    private boolean anyOfMode;

    OpenRouterJsonSchemaImpl(String type) {
        this.type = type;
        this.description = null;
        this.properties = new JSONObject();
        this.required = new JSONArray();
        this.enumValues = new JSONArray();
        this.anyOfSchemas = new JSONArray();
        this.additionalProperties = false;
        this.anyOfMode = false;
    }

    @Override
    public OpenRouterJsonSchema description(String desc) {
        this.description = desc;
        return this;
    }

    @Override
    public OpenRouterJsonSchema property(String name, OpenRouterJsonSchema schema, boolean requiredField) {
        if (anyOfMode) {
            throw new IllegalStateException("Cannot add properties in anyOf mode directly.");
        }
        if (!"object".equals(type)) {
            throw new IllegalStateException("properties can only be added to an object schema.");
        }
        this.properties.put(name, schema.toJson());
        if (requiredField) {
            this.required.put(name);
        }
        return this;
    }

    @Override
    public OpenRouterJsonSchema items(OpenRouterJsonSchema itemSchema) {
        if (anyOfMode) {
            throw new IllegalStateException("Cannot set items in anyOf mode.");
        }
        if (!"array".equals(type)) {
            throw new IllegalStateException("items can only be set for array schemas.");
        }
        this.itemsSchema = itemSchema;
        return this;
    }

    @Override
    public OpenRouterJsonSchema enumValues(String... values) {
        if (anyOfMode) {
            throw new IllegalStateException("Cannot set enum in anyOf mode.");
        }
        if (type == null || !"string".equals(type)) {
            throw new IllegalStateException("enum is only supported on string schemas.");
        }
        for (String v : values) {
            enumValues.put(v);
        }
        return this;
    }

    @Override
    public OpenRouterJsonSchema additionalProperties(boolean allowed) {
        this.additionalProperties = allowed;
        return this;
    }

    @Override
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();

        // If we're in anyOf mode, we place "anyOf": [ ... ] and optional "description"
        if (anyOfMode) {
            obj.put("anyOf", anyOfSchemas);
            if (description != null && !description.isBlank()) {
                obj.put("description", description);
            }
            return obj;
        }

        // Otherwise, we have a normal schema
        if (type != null) {
            obj.put("type", type);
        }
        if (properties.length() > 0) {
            obj.put("properties", properties);
        }
        if (required.length() > 0) {
            obj.put("required", required);
        }
        if (enumValues.length() > 0) {
            obj.put("enum", enumValues);
        }
        if ("array".equals(type) && itemsSchema != null) {
            obj.put("items", itemsSchema.toJson());
        }

        // OpenRouter does support additionalProperties
        if ("object".equals(type)) {
            obj.put("additionalProperties", additionalProperties);
        }

        if (description != null && !description.isBlank()) {
            obj.put("description", description);
        }
        return obj;
    }

    void setAnyOfMode(boolean mode) {
        this.anyOfMode = mode;
    }

    JSONArray getAnyOfSchemas() {
        return anyOfSchemas;
    }
}
