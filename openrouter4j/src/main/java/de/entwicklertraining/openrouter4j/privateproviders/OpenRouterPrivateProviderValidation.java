package de.entwicklertraining.openrouter4j.privateproviders;

import java.util.regex.Pattern;

/**
 * Local validation helpers shared by the private-provider requests. Only the
 * cheap, unambiguous rules of the documented field contracts are enforced
 * loudly here; everything the API documents as server-side behaviour (for
 * example the base-URL host restrictions) stays documented in the javadoc
 * instead of being re-implemented.
 */
final class OpenRouterPrivateProviderValidation {

    /** JSON schema pattern of {@code CreatePrivateProviderRequest.name}. */
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9 ._-]*$");

    /** JSON schema pattern of {@code CreatePrivateProviderRequest.slug}. */
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    private OpenRouterPrivateProviderValidation() {
    }

    /**
     * Validates the {@code name} of a new private provider against the
     * documented contract: 2-64 characters out of letters, numbers, spaces,
     * dots, underscores and hyphens, starting with a letter or number
     * (schema pattern {@code ^[A-Za-z0-9][A-Za-z0-9 ._-]*$}).
     *
     * @param name the provider name
     * @return the unchanged name
     * @throws IllegalArgumentException when the contract is violated
     */
    static String requireValidName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is required");
        }
        if (name.length() < 2 || name.length() > 64) {
            throw new IllegalArgumentException("name must be 2-64 characters");
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "name must match ^[A-Za-z0-9][A-Za-z0-9 ._-]*$ "
                            + "(letters, numbers, spaces, dots, underscores or hyphens, "
                            + "starting with a letter or number)");
        }
        return name;
    }

    /**
     * Validates the {@code slug} of a new private provider against the
     * documented contract: 2-64 lowercase letters and numbers, separated by
     * single hyphens (schema pattern {@code ^[a-z0-9]+(?:-[a-z0-9]+)*$}).
     *
     * @param slug the provider slug
     * @return the unchanged slug
     * @throws IllegalArgumentException when the contract is violated
     */
    static String requireValidSlug(String slug) {
        if (slug == null) {
            throw new IllegalArgumentException("slug is required");
        }
        if (slug.length() < 2 || slug.length() > 64) {
            throw new IllegalArgumentException("slug must be 2-64 characters");
        }
        if (!SLUG_PATTERN.matcher(slug).matches()) {
            throw new IllegalArgumentException(
                    "slug must match ^[a-z0-9]+(?:-[a-z0-9]+)*$ "
                            + "(2-64 lowercase letters and numbers separated by single hyphens)");
        }
        return slug;
    }

    /**
     * Validates an optional {@code display_name} against the documented
     * length range (1-80 characters). The API documents no character set.
     *
     * @param displayName the display name
     * @return the unchanged display name
     * @throws IllegalArgumentException when the range is violated
     */
    static String requireValidDisplayName(String displayName) {
        if (displayName == null) {
            throw new IllegalArgumentException("display_name must not be null");
        }
        if (displayName.isEmpty() || displayName.length() > 80) {
            throw new IllegalArgumentException("display_name must be 1-80 characters");
        }
        return displayName;
    }

    /**
     * Validates an optional {@code base_url} against the documented minimum
     * length of 1. The HTTPS scheme requirement and the rejection of private,
     * internal, metadata and OpenRouter hosts are server-side rules and are
     * documented in the setter javadoc instead of being enforced here.
     *
     * @param baseUrl the base URL
     * @return the unchanged base URL
     * @throws IllegalArgumentException when the minimum length is violated
     */
    static String requireValidBaseUrl(String baseUrl) {
        if (baseUrl == null) {
            throw new IllegalArgumentException("base_url must not be null");
        }
        if (baseUrl.isEmpty()) {
            throw new IllegalArgumentException("base_url must not be empty");
        }
        return baseUrl;
    }

    /**
     * Validates the {@code slug} path segment of the by-slug routes. The path
     * parameter is a plain string in the API schema (no pattern, no length
     * range), so only the presence of a value is enforced loudly - a slug that
     * does not exist is the API's 404 to give, not the library's to invent.
     * A dot segment ({@code "."} / {@code ".."}) is rejected regardless: the
     * form encoding does not encode it and a normalizing hop would resolve
     * {@code /private-providers/..} against the namespace root instead of the
     * named provider (the tools/external-users convention).
     *
     * @param slug the provider slug
     * @return the unchanged slug
     * @throws IllegalArgumentException when the slug is null or empty or a
     *         dot segment
     */
    static String requireSlugSegment(String slug) {
        if (slug == null || slug.isEmpty()) {
            throw new IllegalArgumentException("slug is required");
        }
        if (slug.equals(".") || slug.equals("..")) {
            throw new IllegalArgumentException(
                    "slug of \".\" or \"..\" cannot be addressed by path");
        }
        return slug;
    }
}
