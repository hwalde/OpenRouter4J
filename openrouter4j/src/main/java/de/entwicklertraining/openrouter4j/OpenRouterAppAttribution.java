package de.entwicklertraining.openrouter4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * App-attribution data sent as HTTP headers with every request, used by OpenRouter
 * for rankings and analytics:
 * <ul>
 *   <li>{@code HTTP-Referer} - the app URL; the primary identifier for the
 *       OpenRouter leaderboards.</li>
 *   <li>{@code X-OpenRouter-Title} - the app display name shown in rankings and
 *       analytics. (The legacy {@code X-Title} header is still accepted by the API
 *       as a documented alias, but this library always sends
 *       {@code X-OpenRouter-Title}.)</li>
 *   <li>{@code X-OpenRouter-Categories} - comma-separated marketplace categories,
 *       at most 2 per request.</li>
 * </ul>
 *
 * <p>An instance can be configured once on the {@link OpenRouterClient} (then it is
 * sent with every request) and overridden per request via the
 * {@code httpReferer}/{@code appTitle}/{@code appCategories} builder methods.
 *
 * <p>Instances are immutable; use {@link #builder()} to create one.
 */
public final class OpenRouterAppAttribution {

    /** Maximum number of marketplace categories OpenRouter accepts per request. */
    public static final int MAX_CATEGORIES = 2;

    private final String httpReferer;
    private final String appTitle;
    private final List<String> categories;

    private OpenRouterAppAttribution(Builder builder) {
        this.httpReferer = builder.httpReferer;
        this.appTitle = builder.appTitle;
        this.categories = List.copyOf(builder.categories);
    }

    /**
     * @return the app URL sent as {@code HTTP-Referer}, or {@code null} when unset
     */
    public String httpReferer() {
        return httpReferer;
    }

    /**
     * @return the display name sent as {@code X-OpenRouter-Title}, or {@code null} when unset
     */
    public String appTitle() {
        return appTitle;
    }

    /**
     * @return the marketplace categories sent as {@code X-OpenRouter-Categories},
     *         empty when none are set (never {@code null})
     */
    public List<String> categories() {
        return categories;
    }

    /**
     * @return the categories joined with commas as they appear in the
     *         {@code X-OpenRouter-Categories} header, or {@code null} when no
     *         categories are set
     */
    public String categoriesHeader() {
        if (categories.isEmpty()) {
            return null;
        }
        return String.join(",", categories);
    }

    /**
     * @return a new builder for {@code OpenRouterAppAttribution}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link OpenRouterAppAttribution}.
     */
    public static final class Builder {
        private String httpReferer;
        private String appTitle;
        private final List<String> categories = new ArrayList<>();

        private Builder() {
        }

        /**
         * Sets the app URL sent as {@code HTTP-Referer} - the primary identifier
         * for the OpenRouter leaderboards.
         *
         * @param httpReferer the app URL
         * @return this builder
         */
        public Builder httpReferer(String httpReferer) {
            this.httpReferer = httpReferer;
            return this;
        }

        /**
         * Sets the display name sent as {@code X-OpenRouter-Title} (the legacy
         * {@code X-Title} header remains an accepted alias in the API).
         *
         * @param appTitle the app display name
         * @return this builder
         */
        public Builder appTitle(String appTitle) {
            this.appTitle = appTitle;
            return this;
        }

        /**
         * Replaces the marketplace categories (at most 2 per request).
         *
         * @param categories the marketplace categories
         * @return this builder
         * @throws IllegalArgumentException if more than {@value #MAX_CATEGORIES}
         *         categories are given
         */
        public Builder categories(String... categories) {
            return categories(Arrays.asList(categories));
        }

        /**
         * Replaces the marketplace categories (at most 2 per request).
         *
         * @param categories the marketplace categories
         * @return this builder
         * @throws IllegalArgumentException if more than {@value #MAX_CATEGORIES}
         *         categories are given
         */
        public Builder categories(List<String> categories) {
            validateCategoryCount(categories);
            this.categories.clear();
            if (categories != null) {
                this.categories.addAll(categories);
            }
            return this;
        }

        /**
         * Adds a single marketplace category.
         *
         * @param category one marketplace category
         * @return this builder
         * @throws IllegalArgumentException if more than {@value #MAX_CATEGORIES}
         *         categories are registered in total
         */
        public Builder addCategory(String category) {
            List<String> next = new ArrayList<>(this.categories);
            next.add(category);
            validateCategoryCount(next);
            this.categories.add(category);
            return this;
        }

        private static void validateCategoryCount(List<String> categories) {
            if (categories != null && categories.size() > MAX_CATEGORIES) {
                throw new IllegalArgumentException(
                        "At most " + MAX_CATEGORIES + " categories are allowed per request, got "
                                + categories.size());
            }
        }

        /**
         * @return the immutable {@link OpenRouterAppAttribution}
         */
        public OpenRouterAppAttribution build() {
            return new OpenRouterAppAttribution(this);
        }
    }
}
