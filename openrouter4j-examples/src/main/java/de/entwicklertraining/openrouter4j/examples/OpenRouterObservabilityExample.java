package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.observability.OpenRouterObservabilityDestination;
import de.entwicklertraining.openrouter4j.observability.OpenRouterObservabilityDestinationCreateResponse;
import de.entwicklertraining.openrouter4j.observability.OpenRouterObservabilityDestinationsListResponse;

import java.util.List;

/**
 * Demonstrates the observability destination endpoints. All of them require a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * - a normal inference key is rejected with an authorization error.
 *
 * <p>Destinations are where traces are broadcast to; requests opt into
 * tracing via the {@code trace} request object ({@code OpenRouterTraceConfig}).
 * The {@code config} object is destination-type-specific and built here with
 * the {@code configOption(key, value)} escape hatch. Secrets inside the config
 * (e.g. a Langfuse {@code secretKey}) are masked by the API in every response.
 */
public class OpenRouterObservabilityExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // 1. List the destinations of the account.
        OpenRouterObservabilityDestinationsListResponse list =
                client.observability().destinations().list().execute();
        List<OpenRouterObservabilityDestination> destinations = list.items();
        System.out.println("Destinations: " + destinations.size());
        for (OpenRouterObservabilityDestination destination : destinations) {
            System.out.printf("  %s (%s), enabled: %s, regions: %s%n",
                    destination.name(), destination.type(), destination.enabled(), destination.regions());
        }

        // 2. Create a Langfuse destination - type, name and config are
        //    required; the config keys depend on the type.
        OpenRouterObservabilityDestinationCreateResponse created =
                client.observability().destinations().create()
                        .type("langfuse")
                        .name("Example Langfuse")
                        .configOption("baseUrl", "https://us.cloud.langfuse.com")
                        .configOption("publicKey", "pk-lf-example")
                        .configOption("secretKey", "sk-lf-example-do-not-log")
                        .privacyMode(true)
                        .samplingRate(1.0)
                        .execute();
        String id = created.data() != null ? created.data().id() : null;
        System.out.println("Created destination " + id);

        // 3. Read one destination by id (config secrets arrive masked).
        OpenRouterObservabilityDestination fetched =
                client.observability().destinations().get(id).execute().data();
        System.out.println("Fetched destination " + fetched.name()
                + ", sampling rate " + fetched.samplingRate());

        // 4. Disable it.
        client.observability().destinations().update(id)
                .enabled(false)
                .execute();
        System.out.println("Disabled destination " + id);

        // 5. Delete it (permanent).
        Boolean deleted = client.observability().destinations().delete(id).execute().deleted();
        System.out.println("Deleted: " + deleted);
    }
}
