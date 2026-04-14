package airbnb.tests;

import airbnb.base.BaseTest;
import airbnb.constants.FrameworkConstants;
import airbnb.pages.AccountMenuComponent;
import airbnb.pages.LoginPage;
import airbnb.pages.PopupHandler;
import airbnb.utils.CredentialHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Verifies the login flow: valid credentials succeed, invalid credentials show
 * an error, and post-login state is correctly reflected in the UI.
 *
 * <p>
 * These tests start from an unauthenticated state and exercise the login
 * path explicitly, so they extend {@link BaseTest} directly rather than
 * {@link airbnb.base.AuthenticatedBaseTest}.
 *
 * <p>
 * Groups: {@code authenticated}, {@code smoke}, {@code functional},
 * {@code regression}
 */
public class LoginTests extends BaseTest {

    private AccountMenuComponent accountMenu;
    private LoginPage loginPage;

    @BeforeMethod(alwaysRun = true)
    public void initPages() {
        CredentialHelper.requireCredentials();
        accountMenu = new AccountMenuComponent(driver);
        loginPage = new LoginPage(driver);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Opens the login modal and runs the full credential entry flow. */
    private void performLogin(String email, String password) {
        PopupHandler.dismissAll(driver);
        accountMenu.openMenuAndClickLogin();
        loginPage.login(email, password);
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test(groups = { "authenticated",
            FrameworkConstants.GROUP_SMOKE }, description = "Valid test credentials log the user in and return a positive success signal")
    public void tc001_validLoginSucceeds() {
        performLogin(CredentialHelper.getTestEmail(), CredentialHelper.getTestPassword());
        boolean succeeded = loginPage.waitForLoginSuccess();
        Assert.assertTrue(succeeded,
                "Login with valid credentials should succeed. " +
                        "Check that the test account '" + CredentialHelper.getTestEmail() +
                        "' is active and the password is current. " +
                        "Current URL: " + driver.getCurrentUrl());
    }

    @Test(groups = { "authenticated",
            FrameworkConstants.GROUP_REGRESSION }, description = "Submitting an incorrect password shows a visible error message")
    public void tc002_invalidPasswordShowsError() {
        performLogin(CredentialHelper.getTestEmail(), "INVALID_PW_99!");
        String errorMsg = loginPage.getErrorMessage();
        Assert.assertFalse(errorMsg.isEmpty(),
                "An incorrect password should produce a visible error message. " +
                        "No error text was detected. Current URL: " + driver.getCurrentUrl());
    }

    @Test(groups = { "authenticated",
            FrameworkConstants.GROUP_SMOKE }, description = "User avatar or authenticated indicator is present in the header after login")
    public void tc003_authenticatedIndicatorVisibleAfterLogin() {
        performLogin(CredentialHelper.getTestEmail(), CredentialHelper.getTestPassword());
        loginPage.waitForLoginSuccess();

        // Navigate to homepage to confirm the indicator persists across a navigation
        navigateTo("/");
        PopupHandler.dismissAll(driver);

        boolean loggedIn = accountMenu.isLoggedIn();
        Assert.assertTrue(loggedIn,
                "After a successful login the account menu should reflect an authenticated " +
                        "state (user avatar visible or 'Log out' present in the dropdown). " +
                        "Current URL: " + driver.getCurrentUrl());
    }

    @Test(groups = { "authenticated",
            FrameworkConstants.GROUP_FUNCTIONAL }, description = "The authenticated session persists after navigating to a different page")
    public void tc004_sessionPersistsAfterNavigation() {
        performLogin(CredentialHelper.getTestEmail(), CredentialHelper.getTestPassword());
        loginPage.waitForLoginSuccess();

        // Visit a different section and return to homepage
        navigateTo("/help");
        PopupHandler.dismissAll(driver);
        navigateTo("/");
        PopupHandler.dismissAll(driver);

        boolean stillLoggedIn = accountMenu.isLoggedIn();
        Assert.assertTrue(stillLoggedIn,
                "The authenticated session should survive page navigations within the same browser session.");
    }

    @Test(groups = { "authenticated",
            FrameworkConstants.GROUP_FUNCTIONAL }, description = "Logging out from an authenticated session restores the 'Log in' option in the menu")
    public void tc005_logoutFromAuthenticatedSession() {
        performLogin(CredentialHelper.getTestEmail(), CredentialHelper.getTestPassword());
        loginPage.waitForLoginSuccess();

        // Log out via the account menu
        accountMenu.logout();
        PopupHandler.dismissAll(driver);

        // Open the menu and confirm the session has been cleared
        accountMenu.openMenu();
        boolean loginOptionVisible = accountMenu.isLoginOptionVisible();
        // Dismiss menu
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);

        Assert.assertTrue(loginOptionVisible,
                "After logging out, the account menu should offer 'Log in', " +
                        "confirming the session has been fully cleared.");
    }
}
