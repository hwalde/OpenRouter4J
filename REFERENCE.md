# OpenRouter API Reference

Übersicht der OpenRouter API für Entwickler von OpenRouter4J.

**Tipp:** Um die Markdown-Variante einer Dokumentationsseite zu lesen, hänge `.md` an die URL an.

---

## API Reference (Kernressourcen)

| URL | Beschreibung |
|-----|--------------|
| https://openrouter.ai/docs/api-reference/overview | Überblick über die gesamte API-Referenz |
| https://openrouter.ai/docs/api/reference/overview | Detaillierte API-Referenz mit Endpoints |
| https://openrouter.ai/docs/api/api-reference/chat/send-chat-completion-request | **Chat Completion Endpoint** - POST /api/v1/chat/completions |
| https://openrouter.ai/docs/api/reference/streaming | Streaming-Modus mit Server-Sent Events (SSE) |
| https://openrouter.ai/docs/api/reference/authentication | Bearer Token Authentifizierung (Authorization Header) |
| https://openrouter.ai/docs/api/reference/parameters | Alle Request-Parameter (temperature, top_p, tools, etc.) |
| https://openrouter.ai/docs/api/reference/errors-and-debugging | Error Codes (400-503) und Debugging |

---

## Features

| URL | Beschreibung |
|-----|--------------|
| https://openrouter.ai/docs/guides/features/tool-calling | Function Calling / Tool Use (tools Array, tool_choice) |
| https://openrouter.ai/docs/guides/features/structured-outputs | JSON Schema für strukturierte Antworten (response_format) |
| https://openrouter.ai/docs/guides/features/message-transforms | Nachrichtentransformationen |
| https://openrouter.ai/docs/guides/features/model-routing | Modell-Routing zwischen verschiedenen Providern |
| https://openrouter.ai/docs/guides/features/presets | Vordefinierte Konfigurationen |
| https://openrouter.ai/docs/guides/features/zero-completion-insurance | Versicherung gegen fehlgeschlagene Completions |
| https://openrouter.ai/docs/guides/features/zdr | Zero Data Retention |

---

## Routing & Provider Selection

| URL | Beschreibung |
|-----|--------------|
| https://openrouter.ai/docs/guides/routing/auto-model-selection | Automatische Modellauswahl |
| https://openrouter.ai/docs/guides/routing/model-fallbacks | Fallback-Modelle bei Fehlern |
| https://openrouter.ai/docs/guides/routing/provider-selection | Provider-Auswahl (z.B. google-ai-studio, openai) |

---

## Overview & Basics

| URL | Beschreibung |
|-----|--------------|
| https://openrouter.ai/docs/quickstart | Schnellstart-Guide mit Code-Beispielen |
| https://openrouter.ai/docs/guides/overview/principles | Grundprinzipien von OpenRouter |
| https://openrouter.ai/docs/guides/overview/models | Verfügbare Modelle |
| https://openrouter.ai/docs/faq | FAQ - inkl. Rate Limits und Free Models |
| https://openrouter.ai/docs/app-attribution | App-Attribution Header (HTTP-Referer, X-Title) |

---

## Community & SDKs

| URL | Beschreibung |
|-----|--------------|
| https://openrouter.ai/docs/guides/community/frameworks-and-integrations-overview | Frameworks und Integrationen |
| https://openrouter.ai/docs/sdks/typescript/overview | TypeScript SDK Dokumentation |

---

## Wichtige API-Details

### Base URL
```
https://openrouter.ai/api/v1
```

### Haupt-Endpoint
```
POST /api/v1/chat/completions
```

### Authentication
```
Authorization: Bearer <OPENROUTER_API_KEY>
```

### Optionale Header (für Rankings)
```
HTTP-Referer: <YOUR_SITE_URL>
X-Title: <YOUR_SITE_NAME>
```

### Provider-Auswahl
```json
{
  "provider": {
    "order": ["google-ai-studio", "openai"]
  }
}
```

---

## API-Unterschiede: OpenRouter vs. Gemini

| Aspekt | Gemini | OpenRouter |
|--------|--------|------------|
| Base URL | generativelanguage.googleapis.com | openrouter.ai/api/v1 |
| Auth Header | ?key=API_KEY (Query) | Authorization: Bearer (Header) |
| Endpoint | /v1beta/models/{model}:generateContent | /chat/completions |
| Model Format | gemini-1.5-flash | google/gemini-2.5-flash |
| Response | candidates[].content.parts[].text | choices[].message.content |
| Finish Reason | candidates[].finishReason | choices[].finish_reason |
| Tool Response | functionResponse | tool role message |
