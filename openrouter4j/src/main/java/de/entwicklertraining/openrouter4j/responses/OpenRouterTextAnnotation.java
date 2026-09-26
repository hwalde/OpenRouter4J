package de.entwicklertraining.openrouter4j.responses;

import org.json.JSONObject;

/**
 * A typed view of one Responses-API text annotation (the
 * {@code OpenAIResponsesAnnotation} union on {@code ResponseOutputText}):
 * the citations that attribute a span of the answer text to its source.
 *
 * <p>Three documented annotation types are surfaced:
 * {@link #urlCitation()} ({@code url_citation} - the web-search citation
 * surface), {@link #fileCitation()} ({@code file_citation}) and
 * {@link #filePath()} ({@code file_path}). The output-message union
 * additionally shows a container-file citation form ({@code type: "file"},
 * {@code file.{name, hash, content[]}}) for files a code-interpreter/bash
 * run cited; that long tail is reachable verbatim through {@link #json()}.
 *
 * <p>Positional use: {@code start_index}/{@code end_index} of a url citation
 * are character offsets into the annotated text part's {@code text}, so a
 * citation span is only meaningful together with the part text it annotates.
 *
 * <p>Accessors follow the swallow-and-return-null convention; the
 * discriminated views are {@code null} when the annotation is of another
 * type.
 */
public final class OpenRouterTextAnnotation {

    private final JSONObject raw;

    private OpenRouterTextAnnotation(JSONObject raw) {
        this.raw = raw;
    }

    /**
     * Creates a typed annotation view over the given raw annotation JSON.
     *
     * @param raw the annotation object ({@code type} discriminator plus the
     *            type's fields)
     * @return the typed view
     */
    public static OpenRouterTextAnnotation of(JSONObject raw) {
        return new OpenRouterTextAnnotation(raw);
    }

    /** @return the raw annotation JSON - the escape hatch for annotation
     *         forms the library does not type (e.g. the container-file
     *         {@code file} form) */
    public JSONObject json() {
        return raw;
    }

    /**
     * @return the JSON field {@code type} - {@code url_citation},
     *         {@code file_citation}, {@code file_path}, {@code file}
     *         (container-file citation), or whatever the provider sends
     */
    public String type() {
        return raw.optString("type", null);
    }

    /**
     * @return {@code true} when this is a {@code url_citation} annotation
     */
    public boolean isUrlCitation() {
        return "url_citation".equals(type());
    }

    /**
     * @return {@code true} when this is a {@code file_citation} annotation
     */
    public boolean isFileCitation() {
        return "file_citation".equals(type());
    }

    /**
     * @return {@code true} when this is a {@code file_path} annotation
     */
    public boolean isFilePath() {
        return "file_path".equals(type());
    }

    /**
     * The {@code url_citation} form - the web-search citation surface.
     *
     * @return the typed url-citation view, or {@code null} when this
     *         annotation is of another type
     */
    public UrlCitation urlCitation() {
        return isUrlCitation() ? new UrlCitation(raw) : null;
    }

    /**
     * The {@code file_citation} form - a citation of a stored file.
     *
     * @return the typed file-citation view, or {@code null} when this
     *         annotation is of another type
     */
    public FileCitation fileCitation() {
        return isFileCitation() ? new FileCitation(raw) : null;
    }

    /**
     * The {@code file_path} form - a path to a file the run produced.
     *
     * @return the typed file-path view, or {@code null} when this annotation
     *         is of another type
     */
    public FilePath filePath() {
        return isFilePath() ? new FilePath(raw) : null;
    }

    /**
     * A typed view of the {@code url_citation} annotation fields.
     */
    public static final class UrlCitation {

        private final JSONObject raw;

        private UrlCitation(JSONObject raw) {
            this.raw = raw;
        }

        /**
         * @return the JSON field {@code url} - the cited source URL
         */
        public String url() {
            return raw.optString("url", null);
        }

        /**
         * @return the JSON field {@code title} - the cited source title
         */
        public String title() {
            return raw.optString("title", null);
        }

        /**
         * @return the JSON field {@code start_index} - the character offset
         *         where the cited span starts, in the annotated text part's
         *         {@code text}
         */
        public Long startIndex() {
            return optLong("start_index");
        }

        /**
         * @return the JSON field {@code end_index} - the character offset
         *         where the cited span ends (exclusive), in the annotated
         *         text part's {@code text}
         */
        public Long endIndex() {
            return optLong("end_index");
        }

        /**
         * @return the JSON field {@code content} - the cited content
         *         excerpt, or {@code null} when absent
         */
        public String content() {
            return raw.optString("content", null);
        }

        private Long optLong(String key) {
            if (!raw.has(key) || raw.isNull(key)) {
                return null;
            }
            Object value = raw.get(key);
            return value instanceof Number number ? number.longValue() : null;
        }
    }

    /**
     * A typed view of the {@code file_citation} annotation fields.
     */
    public static final class FileCitation {

        private final JSONObject raw;

        private FileCitation(JSONObject raw) {
            this.raw = raw;
        }

        /**
         * @return the JSON field {@code file_id} - the cited file's id
         */
        public String fileId() {
            return raw.optString("file_id", null);
        }

        /**
         * @return the JSON field {@code filename} - the cited file's name
         */
        public String filename() {
            return raw.optString("filename", null);
        }

        /**
         * @return the JSON field {@code index} - the cited index within the
         *         file, or {@code null} when absent
         */
        public Long index() {
            if (!raw.has("index") || raw.isNull("index")) {
                return null;
            }
            Object value = raw.get("index");
            return value instanceof Number number ? number.longValue() : null;
        }
    }

    /**
     * A typed view of the {@code file_path} annotation fields.
     */
    public static final class FilePath {

        private final JSONObject raw;

        private FilePath(JSONObject raw) {
            this.raw = raw;
        }

        /**
         * @return the JSON field {@code file_id} - the produced file's id
         */
        public String fileId() {
            return raw.optString("file_id", null);
        }

        /**
         * @return the JSON field {@code index} - the index of the file path
         *         entry, or {@code null} when absent
         */
        public Long index() {
            if (!raw.has("index") || raw.isNull("index")) {
                return null;
            }
            Object value = raw.get("index");
            return value instanceof Number number ? number.longValue() : null;
        }
    }
}
