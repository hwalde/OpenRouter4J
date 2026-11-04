package de.entwicklertraining.openrouter4j.vault;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A typed view of one entry of {@code github.apps[]} in the vault routing
 * table: a GitHub App the vault can act as, with its installations and the
 * member links it may use.
 *
 * <p>{@link #scope()} is {@code workspace} (applies to every intern of the
 * workspace) or {@code intern} (this intern alone); unknown values are
 * passed through verbatim. {@link #appId()} / {@link #appSlug()} are
 * {@code null} for an App the vault cannot identify.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} / -empty
 * convention; use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterVaultGithubApp {

    private final JSONObject json;

    OpenRouterVaultGithubApp(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw app object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code app_id} - the GitHub App id.
     *
     * @return the id, or {@code null} when absent, not a string or JSON
     *         {@code null}
     */
    public String appId() {
        Object value = json.opt("app_id");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code app_slug} - the GitHub App slug.
     *
     * @return the slug, or {@code null} when absent, not a string or JSON
     *         {@code null}
     */
    public String appSlug() {
        Object value = json.opt("app_slug");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code scope} - {@code workspace} applies to every intern
     * of the workspace; {@code intern} to this intern alone. Unknown values
     * are passed through verbatim.
     *
     * @return the scope, or {@code null} when absent or not a string
     */
    public String scope() {
        Object value = json.opt("scope");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code installations[]} - the App installations the vault
     * can inject an installation token for. Non-object entries are
     * dropped.
     *
     * @return the installations, empty when absent or not an array
     */
    public List<OpenRouterVaultGithubInstallation> installations() {
        List<OpenRouterVaultGithubInstallation> result = new ArrayList<>();
        JSONArray installations = json.optJSONArray("installations");
        if (installations == null) {
            return result;
        }
        for (int i = 0; i < installations.length(); i++) {
            JSONObject entry = installations.optJSONObject(i);
            if (entry != null) {
                result.add(new OpenRouterVaultGithubInstallation(entry));
            }
        }
        return result;
    }

    /**
     * JSON path: {@code member_links[]} - members whose own account the
     * vault would act as on this App's accounts. Non-object entries are
     * dropped.
     *
     * @return the member links, empty when absent or not an array
     */
    public List<OpenRouterVaultGithubMemberLink> memberLinks() {
        List<OpenRouterVaultGithubMemberLink> result = new ArrayList<>();
        JSONArray links = json.optJSONArray("member_links");
        if (links == null) {
            return result;
        }
        for (int i = 0; i < links.length(); i++) {
            JSONObject entry = links.optJSONObject(i);
            if (entry != null) {
                result.add(new OpenRouterVaultGithubMemberLink(entry));
            }
        }
        return result;
    }
}
