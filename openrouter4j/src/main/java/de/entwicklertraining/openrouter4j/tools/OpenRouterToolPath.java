package de.entwicklertraining.openrouter4j.tools;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Package-private helper for the {@code name} path segment of
 * GET /tools/{name}: validates the segment and URL-encodes it (the keys
 * surface convention).
 */
final class OpenRouterToolPath {

    private OpenRouterToolPath() {
    }

    /**
     * Validates a tool name for use as a path segment: non-null, non-blank
     * and not {@code "."} or {@code ".."} - a normalizing hop between
     * client and API would resolve those against the {@code /tools/}
     * prefix instead of addressing a tool.
     *
     * @param name the canonical {@code openrouter:*} name or an accepted
     *        alias
     */
    static void requireValid(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
        if (name.equals(".") || name.equals("..")) {
            throw new IllegalArgumentException(
                    "a tool name of \".\" or \"..\" cannot be addressed by path - "
                            + "look the tool up in the list response of GET /tools");
        }
    }

    /**
     * Validates and URL-encodes a tool name for use as a path segment. Note
     * that the form encoding renders a space as {@code +}, which a
     * path-decoding server reads as a literal plus - the same trap as on
     * the by-user external-user routes.
     *
     * @param name the canonical {@code openrouter:*} name or an accepted
     *        alias
     * @return the URL-encoded path segment
     */
    static String encode(String name) {
        requireValid(name);
        return URLEncoder.encode(name, StandardCharsets.UTF_8);
    }
}