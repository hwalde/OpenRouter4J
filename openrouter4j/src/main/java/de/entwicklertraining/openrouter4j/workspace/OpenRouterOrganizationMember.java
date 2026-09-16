package de.entwicklertraining.openrouter4j.workspace;

import org.json.JSONObject;

/**
 * A typed view of one organization member (GET /organization/members): id,
 * name, email and organization role.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 */
public final class OpenRouterOrganizationMember {

    private final JSONObject json;

    OpenRouterOrganizationMember(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw JSON row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
 * JSON path: {@code id} - Clerk user id of the organization member.
 */
    public String id() {
        return json.optString("id", null);
    }
    /**
 * JSON path: {@code first_name} - first name of the member, or {@code null}
 * when absent.
 */
    public String firstName() {
        return json.optString("first_name", null);
    }
    /**
 * JSON path: {@code last_name} - last name of the member, or {@code null}
 * when absent.
 */
    public String lastName() {
        return json.optString("last_name", null);
    }
    /**
 * JSON path: {@code email} - email address of the member.
 */
    public String email() {
        return json.optString("email", null);
    }
    /**
 * JSON path: {@code role} - role of the member in the organization
 * ({@code org:admin} or {@code org:member}).
 */
    public String role() {
        return json.optString("role", null);
    }
}
