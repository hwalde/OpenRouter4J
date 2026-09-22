package de.entwicklertraining.openrouter4j.vault;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Local validation helpers shared by the vault requests. Only the cheap,
 * unambiguous rules are enforced loudly here (the schema's name pattern, the
 * entry counts, empty values); everything the API documents as server-side
 * normalization or error handling stays documented in the javadoc instead of
 * being re-implemented.
 */
final class OpenRouterVaultValidation {

    /**
     * The schema's secret-name pattern: 1-255 chars, lowercase start, only
     * lowercase letters, digits and single underscores, no trailing
     * underscore, no {@code __}.
     */
    private static final Pattern SECRET_NAME =
            Pattern.compile("^(?!.*__)[a-z]([a-z0-9_]*[a-z0-9])?$");

    private OpenRouterVaultValidation() {
    }

    static String requireValidSecretName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("secret name is required");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("secret name must be at most 255 characters");
        }
        if (!SECRET_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "secret name must start with a lowercase letter, use only lowercase"
                            + " letters, digits and single underscores, have no trailing"
                            + " underscore and no '__': " + name);
        }
        return name;
    }

    static List<String> requireValidHosts(List<String> hosts) {
        if (hosts == null || hosts.isEmpty()) {
            throw new IllegalArgumentException("at least one host is required");
        }
        if (hosts.size() > 100) {
            throw new IllegalArgumentException("at most 100 hosts are allowed");
        }
        for (String host : hosts) {
            if (host == null || host.isBlank()) {
                throw new IllegalArgumentException("hosts must not contain empty entries");
            }
        }
        return hosts;
    }

    static String requireValidValue(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("secret value is required");
        }
        if (value.length() > 65_536) {
            throw new IllegalArgumentException(
                    "secret value must be at most 65536 characters");
        }
        return value;
    }

    static List<String> requireValidCopyNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            throw new IllegalArgumentException("at least one secret name is required");
        }
        if (names.size() > 100) {
            throw new IllegalArgumentException("at most 100 secret names are allowed");
        }
        for (String name : names) {
            requireValidSecretName(name);
        }
        return names;
    }
}
