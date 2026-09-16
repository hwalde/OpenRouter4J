package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.activity.OpenRouterAnalyticsMetaResponse;
import de.entwicklertraining.openrouter4j.providers.OpenRouterProvider;
import de.entwicklertraining.openrouter4j.providers.OpenRouterProvidersResponse;
import de.entwicklertraining.openrouter4j.providers.OpenRouterZdrEndpoint;
import de.entwicklertraining.openrouter4j.providers.OpenRouterZdrEndpointsResponse;

import java.util.List;

/**
 * Demonstrates the routing-discovery and analytics-meta read endpoints:
 *
 * - GET /providers (the slugs the provider-routing options accept)
 * - GET /endpoints/zdr (the ZDR endpoint picture before enabling {@code zdr(true)})
 * - GET /analytics/meta (the metric/dimension names the analytics query
 *   builder needs; management key required)
 */
public class OpenRouterRoutingDiscoveryExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // 1. All providers: the discovery counterpart of providerOrder/
        //    providerOnly/providerIgnore - these slugs are what those options
        //    accept.
        OpenRouterProvidersResponse providers = client.providers().execute();
        List<OpenRouterProvider> providerList = providers.items();
        System.out.println("Providers: " + providerList.size());
        for (OpenRouterProvider provider : providerList.stream().limit(5).toList()) {
            System.out.printf("  %s (%s), datacenters %s%n",
                    provider.slug(),
                    provider.name(),
                    provider.datacenters());
        }

        // 2. ZDR endpoints: check before enabling zdr(true) that enough
        //    zero-data-retention endpoints remain for the models in play.
        OpenRouterZdrEndpointsResponse zdr = client.zdrEndpoints().execute();
        List<OpenRouterZdrEndpoint> endpoints = zdr.items();
        System.out.println("ZDR endpoints: " + endpoints.size());
        for (OpenRouterZdrEndpoint endpoint : endpoints.stream().limit(5).toList()) {
            System.out.printf("  %s (%s, context %d, uptime 30m %.1f%%)%n",
                    endpoint.name(),
                    endpoint.providerName(),
                    endpoint.contextLength(),
                    endpoint.uptimeLast30m());
        }

        // 3. Analytics meta: the metric/dimension names the analyticsQuery()
        //    builder accepts (management key required). Remember the id
        //    trap: filters on enriched dimensions use the underlying id
        //    (permaslug for model, workspace UUID for workspace).
        OpenRouterAnalyticsMetaResponse meta = client.analyticsMeta().execute();
        System.out.println("Analytics metrics: " + meta.metrics().size()
                + ", dimensions: " + meta.dimensions().size()
                + ", operators: " + meta.operators().size()
                + ", granularities: " + meta.granularities().size());
        meta.metrics().forEach(metric ->
                System.out.println("  metric: " + metric.name() + " (" + metric.displayLabel() + ")"));
        meta.dimensions().forEach(dimension ->
                System.out.println("  dimension: " + dimension.name() + " (" + dimension.displayLabel() + ")"));
    }
}
