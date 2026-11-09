package de.entwicklertraining.openrouter4j.privateendpoints;

import java.util.List;

/**
 * Shared loud-validation rules for the private-endpoint request builders.
 * Package-private on purpose: this is plumbing, not API.
 */
final class OpenRouterPrivateEndpointRules {

    private OpenRouterPrivateEndpointRules() {
    }

    /**
     * Validates a required non-blank string (the {@code minLength: 1}
     * contract of the documented request fields).
     *
     * @param value the value to check
     * @param name the field name for the error message
     * @return the value
     * @throws IllegalArgumentException when the value is null or empty
     */
    static String requireNonBlank(String value, String name) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be null or empty");
        }
        return value;
    }

    /**
     * Validates a required path-segment string: non-blank and no dot
     * segment. {@link java.net.URLEncoder} does not encode {@code "."}, so
     * an id of {@code "."} or {@code ".."} would build a request target like
     * {@code /private-endpoints/..} that any normalizing hop resolves
     * against the namespace root instead of the named resource (the
     * tools/external-users convention). A dot-segment id cannot address a
     * private endpoint, so it is rejected loudly here.
     *
     * @param value the value to check
     * @param name the field name for the error message
     * @return the value
     * @throws IllegalArgumentException when the value is null or empty or a
     *         dot segment
     */
    static String requirePathSegment(String value, String name) {
        requireNonBlank(value, name);
        if (value.equals(".") || value.equals("..")) {
            throw new IllegalArgumentException(
                    name + " of \".\" or \"..\" cannot be addressed by path");
        }
        return value;
    }

    /**
     * Validates the documented parameter rule: {@code tool_choice} may only
     * be declared when {@code tools} is tested or declared as well (otherwise
     * the API answers 400 {@code parameters_invalid}). On create nothing is
     * tested yet, so the rule is exact. On PATCH the endpoint's stored
     * {@code tested_parameters} may already cover {@code tools} - the library
     * cannot see stored state and rejects the combination anyway; declare
     * {@code tools} alongside (tested parameters stay).
     *
     * @param declared the declared parameter names being sent
     * @throws IllegalArgumentException when {@code tool_choice} is declared
     *         without {@code tools}
     */
    static void requireToolChoiceCovered(List<String> declared) {
        if (declared != null && declared.contains("tool_choice") && !declared.contains("tools")) {
            throw new IllegalArgumentException(
                    "declared parameters name tool_choice without tools: tool_choice requires tools to be "
                            + "tested or declared (400 parameters_invalid) - declare tools alongside");
        }
    }
}
