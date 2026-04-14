package airbnb.base;

import airbnb.pages.AccountMenuComponent;
import airbnb.pages.LoginPage;
import airbnb.pages.PopupHandler;
import airbnb.utils.CredentialHelper;
import org.openqa.selenium.Cookie;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;

import java.util.Set;

/**
 * Base class for all test classes that operate inside an authenticated session.
 *
 * <p>
 * Extends {@link BaseTest}, which handles driver lifecycle (one browser per
 * test method) and navigates to the homepage before each test.
 *
 * <p>
 * This class:
 * <ul>
 * <li>Initialises the {@link AccountMenuComponent} and {@link LoginPage}
 * page objects after the driver is ready</li>
 * <li>Provides {@link #loginWithTestCredentials()} for subclass use</li>
 * <li>Provides {@link #logout()} for post-test cleanup where needed</li>
 * </ul>
 *
 * <h2>TestNG @BeforeMethod execution order</h2>
 * TestNG runs parent @BeforeMethod before child @BeforeMethod. The effective
 * order for any class that extends this one is:
 * <ol>
 * <li>{@link BaseTest#setUp} — driver init + homepage navigation</li>
 * <li>{@link #setUpAuthComponents} — page object init + credential check</li>
 * <li>Subclass @BeforeMethod — search panel init, login call, etc.</li>
 * </ol>
 *
 * <p>
 * Subclasses control <em>when</em> {@code loginWithTestCredentials()} is
 * called — either in their own {@code @BeforeMethod} or at the start of
 * individual test methods where only some tests require a logged-in state.
 */
public abstract class AuthenticatedBaseTest extends BaseTest {

    /**
     * Shared session cookies captured after the first successful OTP login.
     * Stored statically so all test instances within one JVM run can reuse them
     * rather than each going through the OTP → password flow again.
     */
    private static volatile Set<Cookie> sharedSession = null;

    /**
     * Top-right account menu / hamburger button, available after
     * {@link #setUpAuthComponents}.
     */
    protected AccountMenuComponent accountMenu;

    /** Login modal page object, available after {@link #setUpAuthComponents}. */
    protected LoginPage loginPage;

    /**
     * Runs after {@link BaseTest#setUp} has initialised the WebDriver and loaded
     * the homepage. Instantiates auth-related page objects and verifies that
     * test credentials are resolvable so any misconfiguration surfaces early.
     */
    @BeforeMethod(alwaysRun = true)
    public void setUpAuthComponents() {
        CredentialHelper.requireCredentials();
        accountMenu = new AccountMenuComponent(driver);
        loginPage = new LoginPage(driver);
    }

    // ── Auth helpers ──────────────────────────────────────────────────────────

    /**
     * Performs a complete login using the centralised test credentials.
     *
     * <p>
     * Steps:
     * <ol>
     * <li>Dismiss any active popups or modals</li>
     * <li>Open the account menu and click "Log in"</li>
     * <li>Enter email, click Continue, enter password, click Log in</li>
     * <li>Wait for a post-login indicator</li>
     * <li>Dismiss any popups that may appear after login</li>
     * </ol>
     */
    protected void loginWithTestCredentials() {
        PopupHandler.dismissAll(driver);

        // ── Attempt to restore a saved session (avoids repeat OTP flows) ─────
        if (sharedSession != null && !sharedSession.isEmpty()) {
            try {
                for (Cookie c : sharedSession) {
                    try { driver.manage().addCookie(c); } catch (Exception ignored) {}
                }
                driver.navigate().refresh();
                PopupHandler.dismissAll(driver);
                if (accountMenu.isLoggedIn()) {
                    // Refresh sharedSession with any new tokens Airbnb may have issued
                    // on this navigation — keeps the session valid across long test runs.
                    sharedSession = driver.manage().getCookies();
                    System.out.println("[AuthenticatedBaseTest] Session restored from cookies — skipping OTP flow");
                    return;
                }
                System.out.println("[AuthenticatedBaseTest] Saved session expired; falling back to full login");
            } catch (Exception e) {
                System.out.println("[AuthenticatedBaseTest] Cookie restore error: " + e.getMessage());
            }
            sharedSession = null;
        }

        // ── Full login flow ───────────────────────────────────────────────────
        // Clear any stale/invalid cookies (e.g. from a previous logout that
        // invalidated the server-side session) and reload the home page so that
        // the Airbnb header renders cleanly in a logged-out state before we look
        // for the "Log in" link.
        driver.manage().deleteAllCookies();
        driver.navigate().to(airbnb.utils.ConfigReader.getBaseUrl());
        airbnb.utils.WaitUtils.waitForPageReady(driver);
        PopupHandler.dismissAll(driver);

        accountMenu.openMenuAndClickLogin();
        try {
            loginPage.login(
                    CredentialHelper.getTestEmail(),
                    CredentialHelper.getTestPassword());
        } catch (Exception loginEx) {
            // Airbnb may skip the password step after a recent OTP verification,
            // redirecting the browser to the homepage before we can enter a password.
            // If the user IS actually logged in, treat this as a successful auto-login.
            if (!accountMenu.isLoggedIn()) {
                throw new RuntimeException(
                    "loginPage.login() threw and user is still not authenticated: "
                    + loginEx.getMessage(), loginEx);
            }
            System.out.println("[AuthenticatedBaseTest] Login form threw '"
                + loginEx.getClass().getSimpleName()
                + "' but user is authenticated — OTP/magic-link auto-login.");
        }
        boolean success = loginPage.waitForLoginSuccess();
        PopupHandler.dismissAll(driver);
        // Fail fast so @BeforeMethod gives a clear FAILED (not silent skip)
        Assert.assertTrue(success,
                "Login could not be confirmed after the complete login flow. " +
                "If the OTP path failed, check LoginPage.selectPasswordOption(). " +
                "Current URL: " + driver.getCurrentUrl());

        // Persist cookies for the remaining tests in this run
        sharedSession = driver.manage().getCookies();
        System.out.println("[AuthenticatedBaseTest] Logged in as "
                + CredentialHelper.getTestEmail()
                + "; session cached (" + sharedSession.size() + " cookies)");
    }

    // @AfterMethod invalidation removed: the session restore in
    // loginWithTestCredentials() already falls back to a full login when
    // isLoggedIn() returns false, so clearing sharedSession here just forces
    // an extra OTP login (triggering Airbnb's rate-limit).  Stale sessions
    // are handled gracefully by the restore logic itself.

    /**
     * Logs out via the account menu.
     *
     * <p>
     * Dismisses popups first to avoid them blocking the menu.
     * Exceptions are caught and logged so a logout failure during teardown
     * does not mask the actual test result.
     */
    protected void logout() {
        try {
            PopupHandler.dismissAll(driver);
            accountMenu.logout();
            // Invalidate the cached session — server-side logout makes those cookies
            // useless; the next test must do a fresh OTP login.
            sharedSession = null;
            System.out.println("[AuthenticatedBaseTest] Logged out successfully; session cache cleared.");
        } catch (Exception e) {
            System.out.println("[AuthenticatedBaseTest] Logout attempt encountered an issue " +
                    "(user may already be logged out): " + e.getMessage());
        }
    }
}
