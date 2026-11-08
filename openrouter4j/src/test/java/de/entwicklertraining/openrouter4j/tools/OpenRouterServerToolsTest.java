package de.entwicklertraining.openrouter4j.tools;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the server-tool catalog requests (GET /tools, GET /tools/{name}) and
 * the typed catalog views against recorded JSON shapes of the OpenRouter
 * tools endpoints.
 */
class OpenRouterServerToolsTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    // ------------------------------------------------------------------
    // GET /tools request shape
    // ------------------------------------------------------------------

    @Test
    void listRequestCarriesNoQueryAndNoBody() {
        OpenRouterToolsListRequest request = new OpenRouterToolsListRequest.Builder(client()).build();
        assertThat(request.getRelativeUrl()).isEqualTo("/tools");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
    }

    @Test
    void listRequestEmitsEveryTypedQueryParameter() {
        assertThat(new OpenRouterToolsListRequest.Builder(client())
                .offset(10).limit(25).apiFormat("chat-completions")
                .build().getRelativeUrl())
                .isEqualTo("/tools?offset=10&limit=25&api_format=chat-completions");
        assertThat(new OpenRouterToolsListRequest.Builder(client()).offset(0).build().getRelativeUrl())
                .isEqualTo("/tools?offset=0");
        assertThat(new OpenRouterToolsListRequest.Builder(client()).limit(1).build().getRelativeUrl())
                .isEqualTo("/tools?limit=1");
        assertThat(new OpenRouterToolsListRequest.Builder(client()).apiFormat("responses").build().getRelativeUrl())
                .isEqualTo("/tools?api_format=responses");
    }

    @Test
    void listRequestPassesUnknownApiFormatsVerbatimAndEncodesValues() {
        assertThat(new OpenRouterToolsListRequest.Builder(client())
                .apiFormat("graphql-future").build().getRelativeUrl())
                .isEqualTo("/tools?api_format=graphql-future");
        assertThat(new OpenRouterToolsListRequest.Builder(client())
                .apiFormat("anthropic messages").build().getRelativeUrl())
                .isEqualTo("/tools?api_format=anthropic+messages");
    }

    @Test
    void listRequestUnsetsTypedParametersOnExplicitNull() {
        assertThat(new OpenRouterToolsListRequest.Builder(client())
                .offset(10).limit(25).apiFormat("responses")
                .offset(null).limit(null).apiFormat(null)
                .build().getRelativeUrl())
                .isEqualTo("/tools");
    }

    @Test
    void listRequestReadsBackItsTypedParameters() {
        OpenRouterToolsListRequest request = new OpenRouterToolsListRequest.Builder(client())
                .offset(5).limit(50).apiFormat("anthropic-messages")
                .build();
        assertThat(request.offset()).isEqualTo(5);
        assertThat(request.limit()).isEqualTo(50);
        assertThat(request.apiFormat()).isEqualTo("anthropic-messages");
        OpenRouterToolsListRequest bare = new OpenRouterToolsListRequest.Builder(client()).build();
        assertThat(bare.offset()).isNull();
        assertThat(bare.limit()).isNull();
        assertThat(bare.apiFormat()).isNull();
    }

    @Test
    void listRequestValidatesRangesAndBlankFormatsLoudly() {
        assertThatThrownBy(() -> new OpenRouterToolsListRequest.Builder(client()).offset(-1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OpenRouterToolsListRequest.Builder(client()).limit(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OpenRouterToolsListRequest.Builder(client()).limit(101))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OpenRouterToolsListRequest.Builder(client()).apiFormat(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OpenRouterToolsListRequest.Builder(client()).apiFormat("   "))
                .isInstanceOf(IllegalArgumentException.class);
        // boundary values are accepted
        assertThat(new OpenRouterToolsListRequest.Builder(client()).offset(0).limit(1).build().getRelativeUrl())
                .isEqualTo("/tools?offset=0&limit=1");
        assertThat(new OpenRouterToolsListRequest.Builder(client()).limit(100).build().getRelativeUrl())
                .isEqualTo("/tools?limit=100");
    }

    // ------------------------------------------------------------------
    // GET /tools/{name} request shape
    // ------------------------------------------------------------------

    @Test
    void getRequestEncodesTheNameIntoThePath() {
        OpenRouterToolGetRequest request = new OpenRouterToolGetRequest.Builder(client(), "openrouter:web_search")
                .build();
        assertThat(request.getRelativeUrl()).isEqualTo("/tools/openrouter%3Aweb_search");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
        assertThat(request.name()).isEqualTo("openrouter:web_search");
        assertThat(new OpenRouterToolGetRequest.Builder(client(), "web_search_preview_2025_03_11")
                .build().getRelativeUrl())
                .isEqualTo("/tools/web_search_preview_2025_03_11");
        // the form-encoding contract of the segment: space renders as '+',
        // a slash is percent-encoded (no way out of the /tools/ prefix)
        assertThat(new OpenRouterToolGetRequest.Builder(client(), "a b/c").build().getRelativeUrl())
                .isEqualTo("/tools/a+b%2Fc");
    }

    @Test
    void getRequestRejectsNullAndBlankNamesLoudly() {
        assertThatThrownBy(() -> new OpenRouterToolGetRequest.Builder(client(), null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OpenRouterToolGetRequest.Builder(client(), ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OpenRouterToolGetRequest.Builder(client(), "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getRequestRejectsDotSegmentsThatEscapeTheToolsPrefix() {
        // "." and ".." survive URLEncoder and would be normalized by any
        // hop in between into /tools/ and /api/v1/ - rejected loudly
        assertThatThrownBy(() -> new OpenRouterToolGetRequest.Builder(client(), "."))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("list response");
        assertThatThrownBy(() -> new OpenRouterToolGetRequest.Builder(client(), ".."))
                .isInstanceOf(IllegalArgumentException.class);
        // neighbouring spellings stay addressable
        assertThat(new OpenRouterToolGetRequest.Builder(client(), "...").build().getRelativeUrl())
                .isEqualTo("/tools/...");
        assertThat(new OpenRouterToolGetRequest.Builder(client(), "a..b").build().getRelativeUrl())
                .isEqualTo("/tools/a..b");
    }

    // ------------------------------------------------------------------
    // GET /tools response
    // ------------------------------------------------------------------

    @Test
    void listResponseSurfacesFullCatalogRows() {
        OpenRouterToolsListResponse<?> response = listResponseOf("""
                {
                  "data": [
                    {
                      "id": "openrouter:web_search",
                      "name": "Web search",
                      "summary": "Search the web and cite the results.",
                      "tool_description": "Search the web for current information.",
                      "status": "active",
                      "docs_url": "https://openrouter.ai/docs/features/web-search",
                      "aliases": [
                        {
                          "type": "web_search_20250305",
                          "api_formats": ["responses", "chat-completions"],
                          "output_names": [
                            {"api_format": "responses", "type": "web_search_call"},
                            {"api_format": "anthropic-messages", "type": "server_tool_use", "name": "web_search"}
                          ]
                        },
                        {"type": "openrouter:web_search", "api_formats": ["responses"], "output_names": []}
                      ],
                      "supported_api_formats": ["responses", "chat-completions", "anthropic-messages"],
                      "default_engine": "native",
                      "fallback_engine": "exa",
                      "parameters_schema": {"type": "object", "properties": {"max_results": {"type": "integer"}}},
                      "input_schema": {"type": "object", "properties": {}},
                      "output_schema": null,
                      "engines": [
                        {
                          "id": "exa",
                          "name": "Exa",
                          "executed_by": "openrouter",
                          "data_regions": ["global", "us"],
                          "byok": "none",
                          "default_mode": "auto",
                          "pricing_source": "openrouter",
                          "pricing_doc_url": null,
                          "pricing": [
                            {"mode": "auto", "unit": "request", "price": "0.007", "max_units_per_call": 10},
                            {"mode": null, "unit": "request", "price": "0.01", "max_units_per_call": 1}
                          ]
                        },
                        {
                          "id": null,
                          "name": "Native",
                          "executed_by": "provider",
                          "data_regions": ["global"],
                          "byok": "required",
                          "default_mode": null,
                          "pricing_source": "byok",
                          "pricing_doc_url": "https://example.com/rates",
                          "pricing": []
                        }
                      ],
                      "native_support": {"endpoint_count": 18, "model_count": 12}
                    }
                  ],
                  "total_count": 12,
                  "links": {"next": "/api/v1/tools?offset=50&limit=50"}
                }
                """);

        assertThat(response.totalCount()).isEqualTo(12L);
        assertThat(response.nextPageUrl()).isEqualTo("/api/v1/tools?offset=50&limit=50");
        assertThat(response.items()).hasSize(1);

        OpenRouterServerToolCatalogEntry tool = response.items().get(0);
        assertThat(tool.id()).isEqualTo("openrouter:web_search");
        assertThat(tool.name()).isEqualTo("Web search");
        assertThat(tool.summary()).isEqualTo("Search the web and cite the results.");
        assertThat(tool.toolDescription()).isEqualTo("Search the web for current information.");
        assertThat(tool.status()).isEqualTo("active");
        assertThat(tool.docsUrl()).isEqualTo("https://openrouter.ai/docs/features/web-search");
        assertThat(tool.supportedApiFormats())
                .containsExactly("responses", "chat-completions", "anthropic-messages");
        assertThat(tool.defaultEngine()).isEqualTo("native");
        assertThat(tool.fallbackEngine()).isEqualTo("exa");
        assertThat(tool.parametersSchema().toString())
                .isEqualTo(new JSONObject("""
                        {"type": "object", "properties": {"max_results": {"type": "integer"}}}
                        """).toString());
        assertThat(tool.inputSchema().toString())
                .isEqualTo(new JSONObject("{\"type\": \"object\", \"properties\": {}}").toString());
        assertThat(tool.outputSchema()).isNull();
        assertThat(tool.json().getString("id")).isEqualTo("openrouter:web_search");

        assertThat(tool.aliases()).hasSize(2);
        OpenRouterServerToolInputName alias = tool.aliases().get(0);
        assertThat(alias.type()).isEqualTo("web_search_20250305");
        assertThat(alias.apiFormats()).containsExactly("responses", "chat-completions");
        assertThat(alias.outputNames()).hasSize(2);
        assertThat(alias.outputNames().get(0).apiFormat()).isEqualTo("responses");
        assertThat(alias.outputNames().get(0).type()).isEqualTo("web_search_call");
        assertThat(alias.outputNames().get(0).name()).isNull();
        assertThat(alias.outputNames().get(1).apiFormat()).isEqualTo("anthropic-messages");
        assertThat(alias.outputNames().get(1).type()).isEqualTo("server_tool_use");
        assertThat(alias.outputNames().get(1).name()).isEqualTo("web_search");
        assertThat(tool.aliases().get(1).outputNames()).isEmpty();

        assertThat(tool.engines()).hasSize(2);
        OpenRouterServerToolEngine engine = tool.engines().get(0);
        assertThat(engine.id()).isEqualTo("exa");
        assertThat(engine.name()).isEqualTo("Exa");
        assertThat(engine.executedBy()).isEqualTo("openrouter");
        assertThat(engine.dataRegions()).containsExactly("global", "us");
        assertThat(engine.byok()).isEqualTo("none");
        assertThat(engine.defaultMode()).isEqualTo("auto");
        assertThat(engine.pricingSource()).isEqualTo("openrouter");
        assertThat(engine.pricingDocUrl()).isNull();
        assertThat(engine.pricing()).hasSize(2);
        assertThat(engine.pricing().get(0).mode()).isEqualTo("auto");
        assertThat(engine.pricing().get(0).unit()).isEqualTo("request");
        assertThat(engine.pricing().get(0).price()).isEqualTo("0.007");
        assertThat(engine.pricing().get(0).maxUnitsPerCall()).isEqualTo(10L);
        assertThat(engine.pricing().get(1).mode()).isNull();

        OpenRouterServerToolEngine second = tool.engines().get(1);
        assertThat(second.id()).isNull();
        assertThat(second.defaultMode()).isNull();
        assertThat(second.pricingSource()).isEqualTo("byok");
        assertThat(second.pricingDocUrl()).isEqualTo("https://example.com/rates");
        assertThat(second.pricing()).isEmpty();

        assertThat(tool.nativeSupport()).isNotNull();
        assertThat(tool.nativeSupport().endpointCount()).isEqualTo(18L);
        assertThat(tool.nativeSupport().modelCount()).isEqualTo(12L);
        // the list rows carry no native_support.models - details shape only
        assertThat(tool.nativeSupport().models()).isEmpty();
        assertThat(tool.nativeSupport().json().getInt("endpoint_count")).isEqualTo(18);
    }

    @Test
    void listResponseReadsThePaginationLegs() {
        assertThat(listResponseOf("""
                {"data": [], "total_count": 0, "links": {"next": null}}
                """).nextPageUrl()).isNull();
        assertThat(listResponseOf("""
                {"data": [], "total_count": 0, "links": {"next": "https://openrouter.ai/api/v1/tools?offset=3"}}
                """).nextPageUrl()).isEqualTo("https://openrouter.ai/api/v1/tools?offset=3");
        assertThat(listResponseOf("""
                {"data": [], "total_count": 0}
                """).nextPageUrl()).isNull();
        assertThat(listResponseOf("""
                {"data": [], "total_count": 0, "links": {"next": 42}}
                """).nextPageUrl()).isNull();
        assertThat(listResponseOf("""
                {"data": [], "total_count": 0, "links": "not-an-object"}
                """).nextPageUrl()).isNull();
    }

    @Test
    void listResponseSwallowsMalformedShapes() {
        OpenRouterToolsListResponse<?> response = listResponseOf("""
                {
                  "data": [
                    "not-a-row",
                    {
                      "id": 42,
                      "name": null,
                      "summary": true,
                      "tool_description": {},
                      "status": 7,
                      "docs_url": [],
                      "aliases": [{"type": 1}, "junk", {"type": "web_search_preview", "api_formats": ["responses", 3], "output_names": ["junk", {"api_format": "responses", "type": 5, "name": 6}]}],
                      "supported_api_formats": ["responses", 9, null],
                      "default_engine": 3,
                      "fallback_engine": false,
                      "parameters_schema": "not-an-object",
                      "input_schema": null,
                      "output_schema": 5,
                      "engines": [
                        "junk",
                        {
                          "id": 1,
                          "name": {},
                          "executed_by": [],
                          "data_regions": ["global", 4],
                          "byok": 2,
                          "default_mode": 3,
                          "pricing_source": null,
                          "pricing_doc_url": 4,
                          "pricing": [{"mode": 1, "unit": 2, "price": 3, "max_units_per_call": "x"}, "junk"]
                        }
                      ],
                      "native_support": "not-an-object"
                    }
                  ],
                  "total_count": "twelve",
                  "links": {"next": null}
                }
                """);

        assertThat(response.items()).hasSize(1);
        OpenRouterServerToolCatalogEntry tool = response.items().get(0);
        assertThat(tool.id()).isNull();
        assertThat(tool.name()).isNull();
        assertThat(tool.summary()).isNull();
        assertThat(tool.toolDescription()).isNull();
        assertThat(tool.status()).isNull();
        assertThat(tool.docsUrl()).isNull();
        assertThat(tool.supportedApiFormats()).containsExactly("responses");
        assertThat(tool.defaultEngine()).isNull();
        assertThat(tool.fallbackEngine()).isNull();
        assertThat(tool.parametersSchema()).isNull();
        assertThat(tool.inputSchema()).isNull();
        assertThat(tool.outputSchema()).isNull();
        assertThat(tool.nativeSupport()).isNull();
        assertThat(response.totalCount()).isNull();

        assertThat(tool.aliases()).hasSize(2);
        OpenRouterServerToolInputName brokenAlias = tool.aliases().get(0);
        assertThat(brokenAlias.type()).isNull();
        assertThat(brokenAlias.apiFormats()).isEmpty();
        assertThat(brokenAlias.outputNames()).isEmpty();
        assertThat(brokenAlias.json().has("type")).isTrue();
        // the good alias row survives with its own malformed parts swallowed
        OpenRouterServerToolInputName alias = tool.aliases().get(1);
        assertThat(alias.type()).isEqualTo("web_search_preview");
        assertThat(alias.apiFormats()).containsExactly("responses");
        assertThat(alias.outputNames()).hasSize(1);
        assertThat(alias.outputNames().get(0).apiFormat()).isEqualTo("responses");
        assertThat(alias.outputNames().get(0).type()).isNull();
        assertThat(alias.outputNames().get(0).name()).isNull();

        assertThat(tool.engines()).hasSize(1);
        OpenRouterServerToolEngine engine = tool.engines().get(0);
        assertThat(engine.id()).isNull();
        assertThat(engine.name()).isNull();
        assertThat(engine.executedBy()).isNull();
        assertThat(engine.dataRegions()).containsExactly("global");
        assertThat(engine.byok()).isNull();
        assertThat(engine.defaultMode()).isNull();
        assertThat(engine.pricingSource()).isNull();
        assertThat(engine.pricingDocUrl()).isNull();
        assertThat(engine.pricing()).hasSize(1);
        assertThat(engine.pricing().get(0).mode()).isNull();
        assertThat(engine.pricing().get(0).unit()).isNull();
        assertThat(engine.pricing().get(0).price()).isNull();
        assertThat(engine.pricing().get(0).maxUnitsPerCall()).isNull();
    }

    @Test
    void listResponseAnswersEmptyWhenBodyAbsent() {
        OpenRouterToolsListResponse<?> response = listResponseOf("{}");
        assertThat(response.items()).isEmpty();
        assertThat(response.totalCount()).isNull();
        assertThat(response.nextPageUrl()).isNull();
        assertThat(listResponseOf("").items()).isEmpty();
    }

    // ------------------------------------------------------------------
    // GET /tools/{name} response
    // ------------------------------------------------------------------

    @Test
    void detailsResponseSurfacesTheNativeModels() {
        OpenRouterToolGetResponse<?> response = getResponseOf("""
                {
                  "data": {
                    "id": "openrouter:web_search",
                    "name": "Web search",
                    "summary": "Search the web and cite the results.",
                    "tool_description": "Search the web for current information.",
                    "status": "deprecated",
                    "docs_url": "https://openrouter.ai/docs/features/web-search",
                    "aliases": [{"type": "web_search_preview", "api_formats": ["responses"], "output_names": []}],
                    "supported_api_formats": ["responses"],
                    "default_engine": null,
                    "fallback_engine": null,
                    "parameters_schema": {"type": "object"},
                    "input_schema": {"type": "object"},
                    "output_schema": {"type": "object"},
                    "engines": [],
                    "native_support": {
                      "endpoint_count": 18,
                      "model_count": 2,
                      "models": [
                        {"slug": "anthropic/claude-4.5-sonnet", "providers": ["Anthropic", "Google"]},
                        "junk",
                        {"slug": 5, "providers": ["OpenAI", 7]}
                      ]
                    }
                  }
                }
                """);

        OpenRouterServerToolCatalogDetails tool = response.data();
        assertThat(tool).isNotNull();
        assertThat(tool.id()).isEqualTo("openrouter:web_search");
        assertThat(tool.status()).isEqualTo("deprecated");
        assertThat(tool.outputSchema()).isNotNull();

        // the non-object row is dropped - two views, not three
        assertThat(tool.nativeModels()).hasSize(2);
        assertThat(tool.nativeModels().get(0).slug()).isEqualTo("anthropic/claude-4.5-sonnet");
        assertThat(tool.nativeModels().get(0).providers()).containsExactly("Anthropic", "Google");
        assertThat(tool.nativeModels().get(0).json().getString("slug"))
                .isEqualTo("anthropic/claude-4.5-sonnet");
        // the malformed second row survives its own swallowed fields
        assertThat(tool.nativeModels().get(1).slug()).isNull();
        assertThat(tool.nativeModels().get(1).providers()).containsExactly("OpenAI");
        // the same rows are reachable through nativeSupport().models()
        assertThat(tool.nativeSupport().models()).hasSize(2);
    }

    @Test
    void detailsResponseSwallowsAbsentAndMalformedData() {
        assertThat(getResponseOf("{}").data()).isNull();
        assertThat(getResponseOf("{\"data\": null}").data()).isNull();
        assertThat(getResponseOf("{\"data\": \"not-an-object\"}").data()).isNull();

        OpenRouterServerToolCatalogDetails empty = getResponseOf("{\"data\": {}}").data();
        assertThat(empty).isNotNull();
        assertThat(empty.id()).isNull();
        assertThat(empty.nativeSupport()).isNull();
        assertThat(empty.nativeModels()).isEmpty();

        OpenRouterServerToolCatalogDetails malformed = getResponseOf("""
                {"data": {"native_support": {"endpoint_count": "x", "model_count": "x", "models": "not-an-array"}}}
                """).data();
        assertThat(malformed.nativeSupport().endpointCount()).isNull();
        assertThat(malformed.nativeSupport().modelCount()).isNull();
        assertThat(malformed.nativeModels()).isEmpty();
    }

    @Test
    void bothResponsesTolerateEmptyAndNullBodies() {
        OpenRouterToolsListRequest listRequest = new OpenRouterToolsListRequest.Builder(client()).build();
        assertThat(listRequest.createResponse("").items()).isEmpty();
        assertThat(listRequest.createResponse((String) null).items()).isEmpty();
        assertThat(listRequest.createResponse((String) null).totalCount()).isNull();

        OpenRouterToolGetRequest getRequest =
                new OpenRouterToolGetRequest.Builder(client(), "openrouter:web_search").build();
        assertThat(getRequest.createResponse("").data()).isNull();
        assertThat(getRequest.createResponse((String) null).data()).isNull();
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private OpenRouterToolsListResponse<?> listResponseOf(String json) {
        OpenRouterToolsListRequest request = new OpenRouterToolsListRequest.Builder(client()).build();
        return request.createResponse(json);
    }

    private OpenRouterToolGetResponse<?> getResponseOf(String json) {
        OpenRouterToolGetRequest request = new OpenRouterToolGetRequest.Builder(client(), "openrouter:web_search")
                .build();
        return request.createResponse(json);
    }
}