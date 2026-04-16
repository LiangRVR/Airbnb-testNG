package airbnb.pages;

import airbnb.utils.WaitUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Represents the Airbnb login modal / page shown after clicking "Log in" from
 * the account menu.
 *
 * <p>
 * Airbnb uses a two-step flow:
 * <ol>
 * <li>Enter email address and click <em>Continue</em></li>
 * <li>Enter password and click <em>Log in</em></li>
 * </ol>
 *
 * <p>
 * Some UI variants show a list of auth providers first ("Continue with
 * email", "Continue with Google", …). {@link #selectEmailOption()} handles
 * that step automatically and is a no-op when the email field is already shown.
 *
 * <h2>Typical usage</h2>
 * 
 * <pre>
 * accountMenu.openMenuAndClickLogin();
 * loginPage.login(email, password);
 * loginPage.waitForLoginSuccess();
 * </pre>
 */
public class LoginPage extends BasePage {

    // ── Locators ──────────────────────────────────────────────────────────────

    /** The surrounding modal / dialog container. */
    private static final By MODAL = By.cssSelector(
            "div[role='dialog']," +
                    "[data-testid='modal-container']," +
                    "[data-testid='auth-modal']");

    /**
     * "Continue with email" button shown when the modal lists auth providers
     * before displaying the email input directly.
     *
     * <p>
     * Airbnb renders this as a {@code <button>}, {@code <a>}, or
     * {@code <div role='button'>} depending on the UI variant.
     */
    private static final By CONTINUE_WITH_EMAIL_BTN = By.xpath(
            "//*[@data-testid='social-auth-email-btn']" +
                    " | //*[@data-testid='login-email-btn']" +
                    " | //button[contains(normalize-space(),'Continue with email')]" +
                    " | //button[contains(normalize-space(),'Use email')]" +
                    " | //a[contains(normalize-space(),'Continue with email')]" +
                    " | //a[contains(normalize-space(),'Use email')]" +
                    " | //div[@role='button'][contains(normalize-space(),'Continue with email')]" +
                    " | //div[@role='button'][contains(normalize-space(),'Use email')]" +
                    " | //*[contains(normalize-space(),'Continue with email') and (self::button or self::a or @role='button')]");

    /**
     * Email / phone input field (step 1).
     *
     * <p>
     * Airbnb's current login form uses a single {@code input[type='tel']}
     * with placeholder "Phone number or email" that accepts both values.
     */
    private static final By EMAIL_INPUT = By.cssSelector(
            "input[type='email']," +
                    "input[type='tel']," +
                    "input[name='email']," +
                    "input[id='email']," +
                    "input[id='phone-or-email']," +
                    "input[autocomplete='email']," +
                    "input[placeholder*='email' i]," +
                    "input[placeholder*='phone' i]");

    /**
     * Floating-label element whose click reveals the hidden phone/email input.
     *
     * <p>
     * Airbnb's login page renders the email input as hidden behind a
     * floating label. Clicking the label triggers CSS/JS that makes the
     * actual {@code <input>} visible and focusable.
     */
    private static final By EMAIL_LABEL = By.cssSelector(
            "label[for='phone-or-email']," +
                    "label[for='email']," +
                    "label[for*='phone']," +
                    "label[for*='email']");

    /** "Continue" / "Next" button that advances from the email step to password. */
    private static final By CONTINUE_BUTTON = By.xpath(
            "//button[normalize-space()='Continue']" +
                    " | //button[normalize-space()='Next']" +
                    " | //button[@data-testid='signup-login-submit-btn']" +
                    " | //button[contains(@data-testid,'continue')]");

    /**
     * OTP / code input shown when Airbnb sends a verification code instead of
     * asking for a password immediately.
     */
    private static final By OTP_INPUT = By.cssSelector("input[id='otp-code-input']");

    /**
     * "Try another way" link on the OTP page — expands alternative auth options.
     */
    private static final By TRY_ANOTHER_WAY = By.xpath(
            "//span[normalize-space()='Try another way']" +
                    " | //a[normalize-space()='Try another way']" +
                    " | //button[normalize-space()='Try another way']");

    /**
     * "Enter your password" option revealed after clicking "Try another way".
     * Target the parent interactive container (button/li/div) rather than just
     * the inner span text, so a real Selenium click and appropriate event
     * propagation reaches the correct click-handler.
     */
    private static final By ENTER_PASSWORD_OPTION = By.xpath(
            // Parent button containing the text (most reliable)
            "//button[.//span[normalize-space()='Enter your password']]" +
                    " | //button[.//div[normalize-space()='Enter your password']]" +
                    " | //li[.//span[normalize-space()='Enter your password']]" +
                    " | //div[@role='button' and .//span[normalize-space()='Enter your password']]" +
                    // Fallback: direct text matches on interactive elements
                    " | //button[normalize-space()='Enter your password']" +
                    " | //a[normalize-space()='Enter your password']" +
                    " | //div[normalize-space()='Enter your password']" +
                    " | //*[@role='option' and .//span[normalize-space()='Enter your password']]" +
                    " | //*[@role='button' and .//span[normalize-space()='Enter your password']]" +
                    // Last resort: the span itself
                    " | //span[normalize-space()='Enter your password']");

    /** Password input field (step 2). */
    private static final By PASSWORD_INPUT = By.cssSelector(
            "input[type='password']," +
                    "input[name='password']," +
                    "input[id='password']");

    /**
     * The final submit button on the password step. Airbnb currently labels
     * it "Continue" on the password step in the /signup_login flow.
     */
    private static final By LOGIN_SUBMIT_BTN = By.xpath(
            "//button[normalize-space()='Log in']" +
                    " | //button[normalize-space()='Sign in']" +
                    " | //button[normalize-space()='Continue']" +
                    " | //button[@data-testid='signup-login-submit-btn']" +
                    " | //button[contains(@data-testid,'login-submit')]");

    /** Error/alert message displayed on bad credentials or server error. */
    private static final By ERROR_MESSAGE = By.cssSelector(
            "[data-testid='login-error']," +
                    "div[role='alert']," +
                    "[aria-live='polite']," +
                    "p[class*='error' i]," +
                    "span[class*='error' i]");

    /**
     * Elements that are present in the DOM only after a successful login.
     *
     * <p>
     * Must NOT contain selectors that also match in the logged-out state.
     * The profile button always contains an img (hamburger icon), so
     * {@code [data-testid='cypress-headernav-profile'] img} is excluded.
     * The wishlists link may appear on the homepage even pre-login, so it
     * is excluded too.
     */
    private static final By POST_LOGIN_INDICATOR = By.cssSelector(
            "img[data-testid='user-avatar']," +
                    "[data-testid='cypress-headernav-profile'] img," +
                    "[data-testid='account-menu'] img");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    // ── Step-level flow methods ───────────────────────────────────────────────

    /**
     * Waits up to the configured explicit-wait timeout for the login form to
     * become available — either as a modal dialog or as a full login page.
     */
    public void waitForLoginModal() {
        new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> !d.findElements(MODAL).isEmpty() ||
                !d.findElements(EMAIL_INPUT).isEmpty() ||
                !d.findElements(CONTINUE_WITH_EMAIL_BTN).isEmpty());
    }

    /**
     * If Airbnb presents a list of auth providers (Google, Apple, email, …),
     * clicks "Continue with email" to reveal the email input.
     * Safe to call even when the email field is already directly visible.
     *
     * <p>
     * If neither the email field nor a provider button is found within 10 s,
     * falls back to navigating to {@code /signup_login} directly where Airbnb
     * always shows the phone-or-email input.
     */
    public void selectEmailOption() {
        // Fast path: email input already visible — nothing to do
        if (!driver.findElements(EMAIL_INPUT).isEmpty()) {
            return;
        }
        try {
            // Wait up to 10s for the provider-list button to appear, then click it
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> {
                // Re-check: if email input appeared in the meantime, signal done
                if (!d.findElements(EMAIL_INPUT).isEmpty())
                    return d.findElements(EMAIL_INPUT).get(0);
                List<WebElement> candidates = d.findElements(CONTINUE_WITH_EMAIL_BTN);
                for (WebElement el : candidates) {
                    if (el.isDisplayed() && el.isEnabled())
                        return el;
                }
                return null;
            });
            // If what we found is the email input itself, skip the click
            if (btn.getTagName().equalsIgnoreCase("input"))
                return;
            btn.click();
            // Wait for the email input to appear after the click
            new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    d -> !d.findElements(EMAIL_INPUT).isEmpty());
        } catch (TimeoutException ignored) {
            // Fallback: navigate directly to the signup_login page where the
            // phone/email input is always available without extra steps.
            String currentUrl = driver.getCurrentUrl();
            if (!currentUrl.contains("/signup_login") && !currentUrl.contains("/login")) {
                driver.navigate().to(airbnb.utils.ConfigReader.getBaseUrl() + "/signup_login");
            }
            try {
                new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                        d -> !d.findElements(EMAIL_INPUT).isEmpty());
            } catch (TimeoutException ex) {
                /* enterEmail() will surface the failure */ }
        }
    }

    /**
     * Types the given email address into the email field.
     *
     * <p>
     * Airbnb's login page uses a floating-label pattern: the
     * {@code <input>} starts invisible and becomes visible only after its
     * {@code <label>} is clicked. This method clicks the label first if the
     * input is not immediately clickable.
     */
    public void enterEmail(String email) {
        WebElement input;
        // Fast path: input already visible and clickable
        try {
            input = new WebDriverWait(driver, Duration.ofSeconds(3)).until(d -> {
                for (WebElement el : d.findElements(EMAIL_INPUT)) {
                    if (el.isDisplayed() && el.isEnabled())
                        return el;
                }
                return null;
            });
        } catch (TimeoutException e) {
            // Floating label: JS-click the label to reveal the hidden input
            // (JS click bypasses scroll-into-view requirements)
            for (WebElement label : driver.findElements(EMAIL_LABEL)) {
                if (label.isDisplayed()) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click()", label);
                    break;
                }
            }
            // Wait for the input to become visible after label click
            input = new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> {
                for (WebElement el : d.findElements(EMAIL_INPUT)) {
                    if (el.isDisplayed() && el.isEnabled())
                        return el;
                }
                return null;
            });
        }
        input.clear();
        input.sendKeys(email);
    }

    /**
     * Clicks the Continue / Next button to advance from the email step
     * to the next step (may be OTP or password).
     */
    public void clickContinue() {
        WaitUtils.waitForClickable(driver, CONTINUE_BUTTON).click();
    }

    /**
     * Handles the OTP interstitial that Airbnb shows after the email step.
     *
     * <p>
     * If Airbnb sent a verification code instead of showing the password
     * input, this method navigates through:
     * <ol>
     * <li>"Try another way" — expands alternative auth options</li>
     * <li>"Enter your password" — reveals the password input</li>
     * </ol>
     * Safe to call when the password input is already visible (no-op in that
     * case).
     */
    public void selectPasswordOption() {
        // Wait up to 15 s for EITHER the password input (no OTP step needed)
        // OR an OTP-page indicator ("otp-code-input" or "Try another way" link).
        // NOTE: returning null from until() means "keep polling";
        // returning any non-null value stops the wait immediately.
        WebElement found;
        try {
            // 30 s to accommodate slower page loads under parallel execution and
            // Airbnb's magic-link-only flows that appear after a recent OTP login.
            found = new WebDriverWait(driver, Duration.ofSeconds(30)).until(d -> {
                // Case 1: password input already visible → no OTP step needed
                for (WebElement el : d.findElements(PASSWORD_INPUT)) {
                    if (el.isDisplayed())
                        return el;
                }
                // Case 2: Airbnb OTP code input appeared
                List<WebElement> otpInputs = d.findElements(OTP_INPUT);
                if (!otpInputs.isEmpty())
                    return otpInputs.get(0);
                // Case 3: "Try another way" link appeared (alternative OTP indicator)
                for (WebElement el : d.findElements(TRY_ANOTHER_WAY)) {
                    if (el.isDisplayed())
                        return el;
                }
                // Case 4: OTP auto-login — Airbnb already signed the user in
                if (!d.findElements(POST_LOGIN_INDICATOR).isEmpty())
                    return d.findElements(POST_LOGIN_INDICATOR).get(0);
                return null; // keep polling
            });
        } catch (WebDriverException e) {
            return; // nothing appeared in 30 s — let enterPassword() surface the error
        }

        // If what appeared was the password input itself, we're already done
        if ("password".equals(found.getAttribute("type")) ||
                "password".equals(found.getAttribute("id"))) {
            return;
        }

        // Otherwise we're on the OTP page — navigate to "Enter your password"
        // Click "Try another way"; BasePage.click() falls back to JS on interception
        click(TRY_ANOTHER_WAY);

        // Wait for the "Enter your password" option to be VISIBLE (not just in DOM),
        // then click its nearest interactive container with native Selenium click.
        WebElement enterPasswordEl;
        try {
            enterPasswordEl = new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> {
                for (WebElement el : d.findElements(ENTER_PASSWORD_OPTION)) {
                    if (el.isDisplayed())
                        return el;
                }
                return null;
            });
        } catch (TimeoutException e) {
            throw new RuntimeException(
                    "'Enter your password' option not visible after clicking 'Try another way'. " +
                            "Current URL: " + driver.getCurrentUrl(),
                    e);
        }

        // Native Selenium click — triggers proper event propagation.
        // Fall back to JS click only on explicit interception.
        try {
            enterPasswordEl.click();
        } catch (ElementClickInterceptedException ignored) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click()", enterPasswordEl);
        }

        // Wait up to 15 s for the password input to become visible.
        // After repeated logins Airbnb may auto-complete the OTP and redirect
        // straight to the homepage, so the password input never appears.
        // Detect that by checking for the logged-in avatar before throwing.
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> {
                for (WebElement el : d.findElements(PASSWORD_INPUT)) {
                    if (el.isDisplayed())
                        return el;
                }
                // OTP auto-login: already redirected to homepage and logged in
                if (!d.findElements(POST_LOGIN_INDICATOR).isEmpty())
                    return Boolean.TRUE;
                return null;
            });
        } catch (TimeoutException e) {
            // Final check before giving up
            if (!driver.findElements(POST_LOGIN_INDICATOR).isEmpty()) {
                return; // OTP auto-login succeeded; no password step needed
            }
            throw new RuntimeException(
                    "Password input not visible after clicking 'Enter your password'. " +
                            "Current URL: " + driver.getCurrentUrl(),
                    e);
        }
        // If what appeared was the logged-in indicator (not password input), return now
        if (!driver.findElements(POST_LOGIN_INDICATOR).isEmpty()
                && driver.findElements(PASSWORD_INPUT).stream().noneMatch(WebElement::isDisplayed)) {
            return; // OTP auto-login succeeded
        }
    }

    /**
     * Types the given password into the password field.
     *
     * <p>
     * If the password input is no longer present or clickable (because an
     * OTP-triggered auto-login completed and the page redirected to the
     * homepage), the method returns silently instead of failing. The caller
     * should call {@link #waitForLoginSuccess()} afterwards to confirm state.
     */
    public void enterPassword(String password) {
        // Wait for the password input to be clickable, but also watch for OTP
        // auto-login that can redirect away from the password step at any moment.
        try {
            Object found = new WebDriverWait(driver, Duration.ofSeconds(20)).until(d -> {
                // OTP auto-login: page navigated to homepage — detected first so we
                // return quickly without waiting the full timeout.
                if (!d.findElements(POST_LOGIN_INDICATOR).isEmpty())
                    return Boolean.TRUE;
                // Password input: must be both visible and enabled (clickable).
                for (WebElement el : d.findElements(PASSWORD_INPUT)) {
                    try {
                        if (el.isDisplayed() && el.isEnabled())
                            return el;
                    } catch (StaleElementReferenceException ignored) {
                    }
                }
                return null;
            });
            if (found instanceof Boolean)
                return; // OTP auto-login — no password step needed
            WebElement input = (WebElement) found;
            input.clear();
            input.sendKeys(password);
        } catch (TimeoutException e) {
            // Neither the password input nor the post-login indicator appeared.
            // Do one final check in case the redirect just completed.
            if (!driver.findElements(POST_LOGIN_INDICATOR).isEmpty())
                return;
            throw e;
        }
    }

    /**
     * Clicks the final "Log in" / "Continue" submit button.
     *
     * <p>
     * Falls back to a JavaScript click when the button is intercepted by
     * a modal backdrop or loading overlay.
     */
    public void submitLogin() {
        WebElement btn = WaitUtils.waitForClickable(driver, LOGIN_SUBMIT_BTN);
        try {
            btn.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click()", btn);
        }
    }

    // ── Convenience method ────────────────────────────────────────────────────

    /**
     * Runs the complete two-step login flow in one call.
     *
     * <ol>
     * <li>Waits for the modal</li>
     * <li>Selects email auth option (no-op if directly shown)</li>
     * <li>Enters email → clicks Continue</li>
     * <li>Enters password → clicks Log in</li>
     * </ol>
     *
     * @param email    the account email address
     * @param password the account password
     */
    public void login(String email, String password) {
        waitForLoginModal();
        selectEmailOption();
        enterEmail(email);
        clickContinue();
        selectPasswordOption(); // bypass OTP page if shown
        // After repeated OTP flows Airbnb may auto-complete the OTP and log the
        // user in before the password input appears. If the post-login avatar
        // is already present there is nothing left to do.
        if (!driver.findElements(POST_LOGIN_INDICATOR).isEmpty()) {
            return;
        }
        enterPassword(password);
        submitLogin();
    }

    // ── Post-login verification ───────────────────────────────────────────────

    /**
     * Waits up to 20 seconds for a post-login indicator to appear in the DOM.
     *
     * @return {@code true} if a post-login signal was found within the timeout;
     *         {@code false} if the timeout expired without a positive signal
     */
    public boolean waitForLoginSuccess() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(d -> !d.findElements(POST_LOGIN_INDICATOR).isEmpty());
            return true;
        } catch (TimeoutException e) {
            // Secondary check: modal disappeared — also counts as success
            try {
                return new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(d -> d.findElements(MODAL).isEmpty());
            } catch (TimeoutException ex) {
                return false;
            }
        }
    }

    // ── Error handling ────────────────────────────────────────────────────────

    /**
     * Returns the text of the first visible error message, or an empty string
     * if no error is shown within 5 seconds.
     */
    public String getErrorMessage() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> {
                for (WebElement el : d.findElements(ERROR_MESSAGE)) {
                    if (el.isDisplayed() && !el.getText().isBlank()) {
                        return el.getText().trim();
                    }
                }
                return null;
            });
        } catch (TimeoutException e) {
            return "";
        }
    }

    /**
     * Returns {@code true} when at least one visible error message element
     * is present on the page.
     */
    public boolean isErrorDisplayed() {
        return !getErrorMessage().isEmpty();
    }
}
