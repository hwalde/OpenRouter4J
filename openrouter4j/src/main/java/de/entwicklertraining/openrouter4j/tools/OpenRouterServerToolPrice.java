package de.entwicklertraining.openrouter4j.tools;

import org.json.JSONObject;

/**
 * A typed view of one entry of {@code engines[].pricing[]} on a server-tool
 * catalog entry (schema {@code ServerToolPrice}): one price row OpenRouter
 * bills for this engine.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterServerToolPrice {

    private final JSONObject json;

    OpenRouterServerToolPrice(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw price row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code mode} - the {@code parameters.mode} this price
     * applies to; {@code null} for prices that do not depend on the mode. A
     * request bills exactly one {@code request} row.
     *
     * @return the mode, or {@code null} when absent, not a string or JSON
     *         {@code null}
     */
    public String mode() {
        if (!json.has("mode") || json.isNull("mode")) {
            return null;
        }
        Object value = json.opt("mode");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code unit} - what the price is per (e.g. {@code request}).
     *
     * @return the unit, or {@code null} when absent or not a string
     */
    public String unit() {
        Object value = json.opt("unit");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code price} - USD per unit, as a decimal string. Do not
     * parse to {@code double}; use {@code BigDecimal}.
     *
     * @return the price, or {@code null} when absent or not a string
     */
    public String price() {
        Object value = json.opt("price");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code max_units_per_call} - the most units one call can
     * bill.
     *
     * @return the limit, or {@code null} when absent or not a number
     */
    public Long maxUnitsPerCall() {
        Object value = json.opt("max_units_per_call");
        return value instanceof Number number ? number.longValue() : null;
    }
}