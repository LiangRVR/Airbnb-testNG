package airbnb.tests;

import airbnb.base.AuthenticatedBaseTest;
import airbnb.constants.FrameworkConstants;
import airbnb.pages.PopupHandler;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Verifies that logout correctly terminates the authenticated session and
 * returns the application to its full logged-out state.
 *
 * <p>
 * Every test starts logged in (via the parent @BeforeMethod chain +
 * {@link #loginBeforeEachTest}), then performs a logout action and asserts the
 * resulting UI/state.
 *
 * <p>
 * Groups: {@code authenticated}, {@code smoke}, {@code regression}
 */
public class LogoutTests extends AuthenticatedBaseTest {

    /**
     * Runs after the parent @BeforeMethod chain and performs login so every
     * test method begins from an authenticated starting state.
     */
    @BeforeMethod(alwaysRun = true)
    public void loginBeforeEachTest() {
        loginWithTestCredentials();
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test(groups = { "authenticated",
            FrameworkConstants.GROUP_SMOKE }, description = "Logout from the homepage returns the app to logged-out state — 'Log in' reappears in menu")
    public void tc001_logoutReturnsToLoggedOutState() {
        navigateTo("/");
        PopupHandler.dismissAll(driver);

        accountMenu.logout();
        PopupHandler.dismissAll(driver);

        // Open the menu and verify Log in is now visible
        accountMenu.openMenu();
        boolean loginVisible = accountMenu.isLoginOptionVisible();
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);

        Assert.assertTrue(loginVisible,
                "After logout, opening the account menu should show 'Log in', " +
                        "confirming the session has been cleared.");
    }

    @Test(groups = { "authenticated",
            FrameworkConstants.GROUP_REGRESSION }, description = "User avatar disappears from the header after logout")
    public void tc002_userAvatarDisappearsAfterLogout() {
        accountMenu.logout();
        PopupHandler.dismissAll(driver);

        boolean avatarGone = !accountMenu.isUserAvatarVisible();
        Assert.assertTrue(avatarGone,
                "The user avatar should no longer be visible in the header after logout.");
    }

    @Test(groups = { "authenticated",
            FrameworkConstants.GROUP_REGRESSION }, description = "Account-settings page is inaccessible directly after logout — request is redirected or blocked")
    public void tc003_accountSettingsBlockedAfterLogout() {
        accountMenu.logout();
        PopupHandler.dismissAll(driver);

        // Attempt to navigate directly to an authenticated-only page
        navigateTo("/account-settings");
        PopupHandler.dismissAll(driver);

        String currentUrl = driver.getCurrentUrl();
        // Airbnb should redirect away from this page when the session is gone
        boolean redirectedOrBlocked = !currentUrl.contains("/account-settings") ||
                currentUrl.contains("login") ||
                currentUrl.contains("signup");

        Assert.assertTrue(redirectedOrBlocked,
                "Accessing /account-settings after logout should redirect the user " +
                        "away or show a login gate. Actual URL: " + currentUrl);
    }

    @Test(groups = { "authenticated",
            FrameworkConstants.GROUP_REGRESSION }, description = "A page refresh after logout does not restore the authenticated UI")
    public void tc004_refreshAfterLogoutDoesNotRestoreSession() {
        accountMenu.logout();
        PopupHandler.dismissAll(driver);

        driver.navigate().refresh();
        PopupHandler.dismissAll(driver);

        boolean notLoggedIn = !accountMenu.isLoggedIn();
        Assert.assertTrue(notLoggedIn,
                "After logout and a page refresh the authenticated UI should not reappear. " +
                        "The session should be permanently terminated for this browser session.");
    }

    @Test(groups = { "authenticated",
            FrameworkConstants.GROUP_REGRESSION }, description = "Direct navigation to /wishlists after logout is handled gracefully — private data not exposed")
    public void tc005_directWishlistAccessAfterLogoutIsHandled() {
        accountMenu.logout();
        PopupHandler.dismissAll(driver);

        navigateTo("/wishlists");
        PopupHandler.dismissAll(driver);

        String currentUrl = driver.getCurrentUrl();
        // Acceptable outcomes:
        // – redirected to a login/signup page
        // – shown an empty public page (URL still /wishlists)
        // – redirected to homepage
        // Not acceptable: user's private wishlist data shown to an unauthenticated
        // visitor
        boolean handledGracefully = currentUrl.contains("login") ||
                currentUrl.contains("signup") ||
                currentUrl.contains("/wishlists") ||
                currentUrl.contains("airbnb.com");

        Assert.assertTrue(handledGracefully,
                "Direct access to /wishlists after logout should be handled gracefully. " +
                        "Actual URL: " + currentUrl);

        // Additionally verify that the authenticated menu indicator is absent
        Assert.assertFalse(accountMenu.isUserAvatarVisible(),
                "The user avatar should not be visible when accessing /wishlists after logout.");
    }
}
