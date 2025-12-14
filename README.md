# OpenRouter4J

OpenRouter4J is a fluent Java wrapper for the [OpenRouter API](https://openrouter.ai/docs).
It builds on top of the lightweight [`api-base`](https://github.com/hwalde/api-base) library which
handles HTTP communication, authentication and exponential backoff. The goal is to provide a type safe
and convenient way to access hundreds of AI models through OpenRouter from Java, being as close to the raw API as possible.

> **A word from the author**
>
> I created this library because I was looking for a Java library that interacts with the OpenRouter API while staying as close to the raw API as possible. OpenRouter provides a unified API that gives you access to hundreds of AI models (OpenAI, Anthropic, Google, Meta, Mistral, and many more) through a single endpoint. This implementation is fully compatible with OpenRouter but only includes the features I personally require. I maintain similar libraries for Gemini, DeepSeek and OpenAI, each in its own repository so usage remains explicit.
>
> At the moment the library only covers the parts I need. Chat Completions are implemented with nearly every option, but many specialized endpoints are missing. If you need additional functionality, feel free to implement it yourself or submit a pull request and I will consider adding it.


## Features

* Chat Completions including tool calling, structured outputs and vision inputs
* Access to 200+ AI models through a single unified API
* Provider selection for routing requests to specific providers
* Vision capabilities for image understanding and analysis
* Fluent builder APIs for all requests
* Examples demonstrating each feature

## Installation

Add the dependency from Maven Central:

```xml
<dependency>
    <groupId>de.entwicklertraining</groupId>
    <artifactId>openrouter4j</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Basic Usage

Instantiate an `OpenRouterClient` and use the builders exposed by its fluent API. The
[function calling example](openrouter4j-examples/src/main/java/de/entwicklertraining/openrouter4j/examples/OpenRouterChatCompletionWithFunctionCallingExample.java)
shows how tools can be defined and executed:

```java
OpenRouterToolDefinition weatherFunction = OpenRouterToolDefinition.builder("get_local_weather")
        .description("Get weather information for a location.")
        .parameter("location", OpenRouterJsonSchema.stringSchema("Name of the city"), true)
        .callback(ctx -> {
            String loc = ctx.arguments().getString("location");
            return OpenRouterToolResult.of(new JSONObject().put("weather", "Sunny in " + loc + " with a high of 25°C."));
        })
        .build();

OpenRouterClient client = new OpenRouterClient(); // reads the API key from OPENROUTER_API_KEY

OpenRouterChatCompletionResponse resp = client.chat().completion()
        .model("google/gemini-2.5-flash")
        .provider("google-ai-studio")  // optional: specify the provider
        .systemInstruction("You are a helpful assistant.")
        .addMessage("user", "What's the weather in Berlin?")
        .addTool(weatherFunction)
        .execute();
System.out.println(resp.assistantMessage());
```

### Vision Example

The [vision example](openrouter4j-examples/src/main/java/de/entwicklertraining/openrouter4j/examples/OpenRouterChatCompletionWithVisionUrlExample.java)
demonstrates image analysis capabilities:

```java
OpenRouterClient client = new OpenRouterClient();
OpenRouterChatCompletionResponse response = client.chat().completion()
        .model("google/gemini-2.5-flash")
        .provider("google-ai-studio")
        .addMessage("user", "What's in this image?")
        .addImageByUrl("https://example.com/image.jpg")
        .execute();
System.out.println(response.assistantMessage());
```

See the `openrouter4j-examples` module for more demonstrations including base64 images, structured outputs, and thinking mode.

### Provider Selection

OpenRouter allows you to route requests to specific providers. This is useful when you want to use a specific provider's infrastructure:

```java
client.chat().completion()
        .model("google/gemini-2.5-flash")
        .provider("google-ai-studio")  // Use Google's AI Studio
        .addMessage("user", "Hello!")
        .execute();

// Or use OpenAI's infrastructure
client.chat().completion()
        .model("openai/gpt-4o-mini")
        .provider("openai")
        .addMessage("user", "Hello!")
        .execute();
```

### Configuring the Client

`OpenRouterClient` accepts an `ApiClientSettings` object for fine-grained control over retries and timeouts. The API key can be configured directly and a hook can inspect each request before it is sent:

```java
ApiClientSettings settings = ApiClientSettings.builder()
        .apiKey("your-openrouter-api-key")
        .beforeSend(req -> System.out.println("Sending " + req.getHttpMethod() + " " + req.getRelativeUrl()))
        .build();

OpenRouterClient client = new OpenRouterClient(settings);
```

## Project Structure

The library follows a clear structure:

* **`OpenRouterClient`** – entry point for all API calls. Extends `ApiClient` from *api-base*
  and registers error handling. Currently exposes the chat completion endpoint via `chat()`.
* **Request/Response classes** – located in the `chat.completion` package.
  Each request extends `OpenRouterRequest` and has an inner `Builder` that extends
  `ApiRequestBuilderBase` from *api-base*. Responses extend `OpenRouterResponse`.
* **Tool calling** – defined via `OpenRouterToolDefinition` and handled by
  `OpenRouterToolsCallback` and `OpenRouterToolCallContext`.
* **Structured outputs** – use `OpenRouterJsonSchema` for defining response schemas.

The `openrouter4j-examples` module demonstrates various use cases and can be used as a quick start.

## Extending OpenRouter4J

1. **Create a Request** – subclass `OpenRouterRequest` and implement `getRelativeUrl`,
   `getHttpMethod`, `getBody` and `createResponse`. Provide a nested builder
   extending `ApiRequestBuilderBase`.
2. **Create a Response** – subclass `OpenRouterResponse` and parse the JSON payload
   returned by OpenRouter.
3. **Expose a builder** – add a convenience method in `OpenRouterClient` returning your
   new builder so users can call it fluently.

Thanks to *api-base*, sending the request is handled by calling
`client.sendRequest(request)` or by using the builder's `execute()` method which
internally delegates to `sendRequest` with optional exponential backoff.
See [api-base's Readme](https://github.com/hwalde/api-base) for details on available
settings like retries, timeouts or capture hooks.

## Building

This project uses Maven. Compile the library and run examples with:

```bash
mvn package
```

## License

OpenRouter4J is distributed under the MIT License as defined in the project `pom.xml`.
