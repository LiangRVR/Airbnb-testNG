package airbnb.utils;

/**
 * Centralised credential provider for all authenticated test flows.
 *
 * <p>
 * Resolution order (highest priority first):
 * <ol>
 * <li>Environment variable – {@code AIRBNB_TEST_EMAIL} /
 * {@code AIRBNB_TEST_PASSWORD}</li>
 * <li>Java system property – {@code test.email} / {@code test.password}
 * (e.g. {@code mvn test -Dtest.email=you@example.com})</li>
 * <li>Hard-coded fallback – for local / class-project use</li>
 * </ol>
 *
 * <p>
 * <b>To use environment variables in CI / CD:</b>
 * 
 * <pre>
 *   export AIRBNB_TEST_EMAIL=testsuit2026@gmail.com
 *   export AIRBNB_TEST_PASSWORD=TN-GS@2026
 * </pre>
 *
 * <p>
 * <b>To override at Maven run time:</b>
 * 
 * <pre>
 *   mvn test -Dtest.email=you@example.com -Dtest.password=YourPass
 * </pre>
 *
 * <p>
 * No credential values are ever logged or printed by this class.
 */
public final class CredentialHelper {

    /** Environment variable for the test account email address. */
    public static final String ENV_EMAIL = "AIRBNB_TEST_EMAIL";

    /** Environment variable for the test account password. */
    public static final String ENV_PASSWORD = "AIRBNB_TEST_PASSWORD";

    // Java system property names (alternative to env vars)
    private static final String PROP_EMAIL = "test.email";
    private static final String PROP_PASSWORD = "test.password";

    // Fallback values – used when neither env var nor system property is set.
    // Move to CI secrets or a .env file for production use.
    private static final String FALLBACK_EMAIL = "testsuit2026@gmail.com";
    private static final String FALLBACK_PASSWORD = "TN-GS@2026";

    private CredentialHelper() {
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns the resolved test email address. Never {@code null} or blank
     * when the fallback is in place.
     */
    public static String getTestEmail() {
        return resolve(ENV_EMAIL, PROP_EMAIL, FALLBACK_EMAIL);
    }

    /**
     * Returns the resolved test account password. Never {@code null} or blank
     * when the fallback is in place.
     */
    public static String getTestPassword() {
        return resolve(ENV_PASSWORD, PROP_PASSWORD, FALLBACK_PASSWORD);
    }

    /**
     * Verifies that both credentials resolve to non-blank values.
     *
     * <p>
     * Call this once at the start of any authenticated test class so
     * misconfiguration surfaces immediately with a readable error message
     * rather than a cryptic login failure deep inside a test.
     *
     * @throws IllegalStateException if either credential is blank after
     *                               resolution (all three sources exhausted)
     */
    public static void requireCredentials() {
        if (getTestEmail().isBlank()) {
            throw new IllegalStateException(
                    "Test email is not configured. " +
                            "Set environment variable " + ENV_EMAIL +
                            " or pass -D" + PROP_EMAIL + "=<value> to Maven.");
        }
        if (getTestPassword().isBlank()) {
            throw new IllegalStateException(
                    "Test password is not configured. " +
                            "Set environment variable " + ENV_PASSWORD +
                            " or pass -D" + PROP_PASSWORD + "=<value> to Maven.");
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static String resolve(String envKey, String propKey, String fallback) {
        // 1. Environment variable
        String v = System.getenv(envKey);
        if (v != null && !v.isBlank())
            return v.trim();
        // 2. Java system property
        v = System.getProperty(propKey);
        if (v != null && !v.isBlank())
            return v.trim();
        // 3. Hard-coded fallback
        return fallback;
    }
}
