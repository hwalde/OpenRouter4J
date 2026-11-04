package de.entwicklertraining.openrouter4j.vault;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A typed view of one entry of {@code google.accounts[]} in the vault
 * routing table: a Google account the vault can act as for the intern.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} / -empty
 * convention; use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterVaultGoogleAccount {

    private final JSONObject json;

    OpenRouterVaultGoogleAccount(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw account object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code email} - the account's email address.
     *
     * @return the email, or {@code null} when absent or not a string
     */
    public String email() {
        Object value = json.opt("email");
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
     * JSON path: {@code is_delegated_to_intern} - whether the account is
     * delegated to the intern.
     *
     * @return the flag, or {@code null} when absent or not a boolean
     */
    public Boolean isDelegatedToIntern() {
        Object value = json.opt("is_delegated_to_intern");
        return value instanceof Boolean flag ? flag : null;
    }

    /**
     * JSON path: {@code delegated_capabilities[]} - the capabilities
     * delegated to the intern. Non-string entries are dropped.
     *
     * @return the capabilities, empty when absent or not an array
     */
    public List<String> delegatedCapabilities() {
        List<String> result = new ArrayList<>();
        JSONArray capabilities = json.optJSONArray("delegated_capabilities");
        if (capabilities == null) {
            return result;
        }
        for (int i = 0; i < capabilities.length(); i++) {
            Object value = capabilities.opt(i);
            if (value instanceof String capability) {
                result.add(capability);
            }
        }
        return result;
    }

    /**
     * JSON path: {@code holds_grant} - whether the vault holds a grant to
     * act as this account.
     *
     * @return the flag, or {@code null} when absent or not a boolean
     */
    public Boolean holdsGrant() {
        Object value = json.opt("holds_grant");
        return value instanceof Boolean flag ? flag : null;
    }
}
