package de.entwicklertraining.openrouter4j;

import de.entwicklertraining.api.base.ApiRequest;
import de.entwicklertraining.api.base.ApiRequestBuilderBase;

/**
 * Eine abstrakte OpenRouter-spezifische Request-Klasse,
 * die nun von ApiRequest<T> erbt.
 */
public abstract class OpenRouterRequest<T extends OpenRouterResponse<?>> extends ApiRequest<T> {

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
