package de.entwicklertraining.openrouter4j;

import de.entwicklertraining.api.base.ApiRequest;
import de.entwicklertraining.api.base.ApiRequestBuilderBase;

/**
 * Eine abstrakte OpenRouter-spezifische Request-Klasse,
 * die nun von ApiRequest<T> erbt.
 */
public abstract class OpenRouterRequest<T extends OpenRouterResponse<?>> extends ApiRequest<T> {

    /**
     * Header {@code X-OpenRouter-Cache}: opts a request into OpenRouter-level
     * response caching ({@code true}) or out of it ({@code false}, which also
     * overrides a preset's {@code cache_enabled: true}). Caveat: a preset with
     * {@code cache_enabled: false} wins over this header in either direction.
     */
    public static final String HEADER_RESPONSE_CACHE = "X-OpenRouter-Cache";

    /**
     * Header {@code X-OpenRouter-Cache-Clear}: {@code true} deletes the cache
     * entry for this request's cache key and forces a fresh upstream call. Has
     * no effect unless caching is enabled for the request.
     */
    public static final String HEADER_RESPONSE_CACHE_CLEAR = "X-OpenRouter-Cache-Clear";

    /**
     * Header {@code X-OpenRouter-Cache-TTL}: the cache lifetime in seconds
     * (1-86400, default 300). Overrides a preset's {@code cache_ttl_seconds}.
     */
    public static final String HEADER_RESPONSE_CACHE_TTL = "X-OpenRouter-Cache-TTL";

    protected <Y extends ApiRequestBuilderBase<?, ?>> OpenRouterRequest(Y builder) {
        super(builder);
    }

    /**
     * @return z.B. "POST" oder "GET".
     */
    @Override
    public abstract String getHttpMethod();

    /**
     * @return Der JSON-Body (String) für diesen Request.
     */
    @Override
    public abstract String getBody();

    /**
     * Erzeugt die passende OpenRouterResponse-Subklasse aus dem JSON-String.
     */
    @Override
    public abstract T createResponse(String responseBody);

    // Da wir isBinaryResponse, getBodyBytes etc. ggf. überschreiben können,
    // lassen wir sie hier unverändert. Standard-Implementierung reicht oft aus.
}
