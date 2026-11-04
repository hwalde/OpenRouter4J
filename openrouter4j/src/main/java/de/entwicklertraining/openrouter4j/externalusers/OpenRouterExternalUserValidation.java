package de.entwicklertraining.openrouter4j.externalusers;

/**
 * Local validation helpers shared by the external-user requests. Only the
 * cheap, unambiguous rules of the documented tracking-string contract are
 * enforced loudly here; everything the API documents as server-side
 * behaviour stays documented in the javadoc instead of being re-implemented.
 */
final class OpenRouterExternalUserValidation {

    private OpenRouterExternalUserValidation() {
    }

    /**
     * Validates one external tracking string against the documented
     * contract: 1 to 512 characters (code points, not UTF-16 units),
     * well-formed Unicode (no unpaired surrogates), no NUL, no surrounding
     * whitespace.
     *
     * @param user the tracking string
     * @return the unchanged string
     * @throws IllegalArgumentException when the contract is violated
     */
    static String requireValidUser(String user) {
        if (user == null || user.isEmpty()) {
            throw new IllegalArgumentException("user is required");
        }
        if (user.codePointCount(0, user.length()) > 512) {
            throw new IllegalArgumentException("user must be at most 512 characters");
        }
        if (user.indexOf('\u0000') >= 0) {
            throw new IllegalArgumentException("user must not contain NUL");
        }
        if (!user.equals(user.strip())) {
            throw new IllegalArgumentException("user must not have surrounding whitespace");
        }
        for (int i = 0; i < user.length(); i++) {
            char c = user.charAt(i);
            if (Character.isHighSurrogate(c)) {
                if (i + 1 >= user.length() || !Character.isLowSurrogate(user.charAt(i + 1))) {
                    throw new IllegalArgumentException("user must be well-formed Unicode");
                }
                i++;
            } else if (Character.isLowSurrogate(c)) {
                throw new IllegalArgumentException("user must be well-formed Unicode");
            }
        }
        return user;
    }
}
