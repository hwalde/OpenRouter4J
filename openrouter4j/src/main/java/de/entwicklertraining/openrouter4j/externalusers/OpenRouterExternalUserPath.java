package de.entwicklertraining.openrouter4j.externalusers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Package-private helper for the {@code user} path segment of the by-user
 * external-user routes: validates the segment and URL-encodes it (the keys
 * surface convention).
 */
final class OpenRouterExternalUserPath {

    private OpenRouterExternalUserPath() {
    }

    /**
     * Validates and URL-encodes a tracking string for use as a path segment.
     * The path form cannot address an id of {@code "."} or {@code ".."}
     * (the API resolves no such registration), so those are rejected loudly
     * with a pointer at the list filter.
     *
     * @param user the tracking string
     * @return the URL-encoded path segment
     */
    static String encode(String user) {
        if (user == null || user.isEmpty()) {
            throw new IllegalArgumentException("user must not be null or empty");
        }
        if (user.equals(".") || user.equals("..")) {
            throw new IllegalArgumentException(
                    "an external-user id of \".\" or \"..\" cannot be addressed by path - "
                            + "look it up with the list filter of GET /external-users");
        }
        return URLEncoder.encode(user, StandardCharsets.UTF_8);
    }
}
