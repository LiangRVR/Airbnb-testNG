package airbnb.pages;

import airbnb.utils.WaitUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * The hamburger / account-menu button in Airbnb's top-right header nav.
 *
 * <p>When unauthenticated the dropdown contains "Log in" and "Sign up".
 * When authenticated it contains user-specific links such as "Profile",
 * "Account", and "Log out".
 *
 * <p>Selectors are layered defensively: the primary {@code data-testid}
 * is tried first; aria-label fallbacks handle A/B variants.
 */
public class AccountMenuComponent extends BasePage {

    // ── Locators ──────────────────────────────────────────────────────────────

    /** The clickable button that opens the dropdown (hamburger + avatar). */
    private static final By MENU_BUTTON = By.cssSelector(
        "[data-testid='cypress-headernav-profile']," +
        "button[aria-label='Account']," +
        "button[aria-label='Open user menu']," +
        "button[aria-label='Account menu']," +
        "div[data-testid='header-profile-menu-btn']"
    );

    /**
     * The login link — Airbnb currently shows a single "Log in or sign up"
     * header link (href /signup_login) that is always visible when the user
     * is unauthenticated.
     */
    private static final By LOGIN_ITEM = By.xpath(
        "//a[normalize-space()='Log in or sign up']" +
        " | //a[contains(@href,'signup_login')]" +
        " | //a[normalize-space()='Log in']" +
        " | //button[normalize-space()='Log in']" +
        " | //*[@data-testid='header-login-link']"
    );

    /**
     * Broader fallback — matches any login/signup link in the page including
     * older dropdown variants.
     */
    private static final By LOGIN_OR_SIGNUP_ITEM = By.xpath(
        "//a[contains(normalize-space(),'Log in') or contains(normalize-space(),'Sign up')]" +
        " | //*[@role='menuitem'][contains(normalize-space(),'Log in') or contains(normalize-space(),'Sign up')]" +
        " | //a[contains(@href,'/login')]" +
        " | //a[contains(@href,'/signup')]"
    );

    /** User avatar image — present in the header button only when authenticated. */
    private static final By USER_AVATAR = By.cssSelector(
        "img[data-testid='user-avatar']," +
        "[data-testid='cypress-headernav-profile'] img," +
        "[data-testid='account-menu'] img"
    );

    /** "Log out" link/item inside the open dropdown — authenticated only. */
    private static final By LOGOUT_ITEM = By.xpath(
        "//a[normalize-space()='Log out']" +
        " | //button[normalize-space()='Log out']" +
        " | //*[@role='menuitem' and normalize-space()='Log out']"
    );

    public AccountMenuComponent(WebDriver driver) {
        super(driver);
    }

    // ── Opening the menu ──────────────────────────────────────────────────────

    /**
     * Clicks the hamburger/account button to open the dropdown, then waits
     * briefly for the dropdown content to appear.
     */
    public void openMenu() {
        click(MENU_BUTTON);
        // Give the dropdown a moment to render
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5)).until(d ->
                !d.findElements(LOGIN_ITEM).isEmpty() ||
                !d.findElements(LOGOUT_ITEM).isEmpty()
            );
        } catch (TimeoutException ignored) {
            // Dropdown may render slowly — callers check content themselves
        }
    }

    /**
     * Navigates to the Airbnb login/signup page.
     *
     * <p>Strategy (in order):
     * <ol>
     *   <li>Click the visible "Log in or sign up" header link directly — no
     *       menu interaction needed on the current Airbnb UI.</li>
     *   <li>If not found within 5 s, open the hamburger menu and look for a
     *       login item inside the dropdown.</li>
     *   <li>Last resort: navigate to {@code /signup_login} directly.</li>
     * </ol>
     */
    public void openMenuAndClickLogin() {
        // Strategy 1: click the always-visible header login link
        try {
            WebElement loginLink = new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> {
                for (WebElement el : d.findElements(LOGIN_ITEM)) {
                    if (el.isDisplayed()) return el;
                }
                return null;
            });
            loginLink.click();
            return;
        } catch (TimeoutException ignored) { /* fall through */ }

        // Strategy 2: open hamburger menu, then find login item
        try {
            openMenu();
            WaitUtils.waitForClickable(driver, LOGIN_ITEM).click();
            return;
        } catch (TimeoutException ignored) { /* fall through */ }

        // Strategy 3: navigate directly
        driver.navigate().to(airbnb.utils.ConfigReader.getBaseUrl() + "/signup_login");
    }

    // ── State detection ───────────────────────────────────────────────────────

    /**
     * Returns {@code true} when the user avatar image is visible inside the
     * header button — the quickest indicator of an authenticated session without
     * opening the menu.
     */
    public boolean isUserAvatarVisible() {
        return WaitUtils.isVisible(driver, USER_AVATAR);
    }

    /**
     * Returns {@code true} when the session is authenticated.
     *
     * <p>First checks for the avatar without opening the menu (fast path).
     * If that fails, opens the menu and looks for "Log out" (slower but
     * thorough), then closes the menu again with Escape.
     */
    public boolean isLoggedIn() {
        if (isUserAvatarVisible()) return true;
        try {
            openMenu();
            boolean hasLogout = !driver.findElements(LOGOUT_ITEM).isEmpty();
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            return hasLogout;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns {@code true} when the "Log in" option is visible in the currently
     * open dropdown — indicating an unauthenticated state.
     * The caller must have already opened the menu.
     */
    public boolean isLoginOptionVisible() {
        return !driver.findElements(LOGIN_ITEM).isEmpty() ||
               !driver.findElements(LOGIN_OR_SIGNUP_ITEM).isEmpty();
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    /**
     * Opens the account menu and clicks "Log out".  After clicking, waits up
     * to 10 seconds for the user avatar to disappear (confirming the session
     * was terminated).
     *
     * <p>Airbnb renders two "Log out" elements: a visible {@code <a>} link in
     * the dropdown and a hidden {@code <button type="submit">} inside an
     * off-screen CSRF form.  We prefer the visible anchor link and use
     * {@link #jsClick(WebElement)} to bypass any overlay interceptors.
     *
     * @throws NoSuchElementException if "Log out" cannot be located after opening
     *                                the menu (user may already be logged out)
     */
    public void logout() {
        openMenu();

        // Find the logout element, preferring the visible dropdown <a> link over
        // any hidden form submit button that may appear earlier in DOM order.
        WebElement logoutEl = null;
        try {
            List<WebElement> candidates = new WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(d -> {
                        List<WebElement> els = d.findElements(LOGOUT_ITEM);
                        return els.isEmpty() ? null : els;
                    });
            // Prefer <a> links (the visible dropdown item) first
            for (WebElement el : candidates) {
                if ("a".equalsIgnoreCase(el.getTagName()) && el.isDisplayed()) {
                    logoutEl = el;
                    break;
                }
            }
            // Fallback: any visible candidate
            if (logoutEl == null) {
                for (WebElement el : candidates) {
                    if (el.isDisplayed()) {
                        logoutEl = el;
                        break;
                    }
                }
            }
            // Last resort: first candidate regardless of visibility
            if (logoutEl == null) {
                logoutEl = candidates.get(0);
            }
        } catch (TimeoutException e) {
            throw new NoSuchElementException(
                    "Log out option not found in account menu. Is the user currently logged in?");
        }

        // Use jsClick to bypass any overlay that might intercept the native click
        jsClick(logoutEl);

        // Wait for the page to settle into logged-out state (avatar gone)
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
                d.findElements(USER_AVATAR).isEmpty()
            );
        } catch (TimeoutException ignored) {
            // Proceed — logout may have completed without triggering these signals
        }
    }
}
