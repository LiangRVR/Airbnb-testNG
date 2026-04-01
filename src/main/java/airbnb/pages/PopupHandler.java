package airbnb.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Fire-and-forget popup/overlay dismissal for Airbnb.
 *
 * Called once per test setup (from BaseTest) after the initial page load.
 * Every attempt uses a short 3-second timeout and is wrapped in a try-catch,
 * so this method never throws and never slows down tests when no popup is
 * present.
 *
 * Handles:
 * - Cookie / consent banners
 * - Sign-in prompts and locale/translation dialogs
 * - Generic modal overlays via the Escape key
 */
public final class PopupHandler {

    private static final int POPUP_TIMEOUT_SECONDS = 3;

    // Cookie / consent accept button
    private static final By COOKIE_ACCEPT = By.cssSelector(
            "[data-testid='accept-btn']," +
                    "button[id*='accept']," +
                    "button[aria-label*='Accept all']," +
                    "button[aria-label*='Accept cookies']");

    // Generic modal close button (sign-in dialog, translation prompt, etc.)
    private static final By MODAL_CLOSE = By.cssSelector(
            "[data-testid='modal-container'] button[aria-label*='lose']," +
                    "button[aria-label='Close']," +
                    "button[aria-label='Dismiss']," +
                    "[data-testid='closeButton']");

    private PopupHandler() {
    }

    /**
     * Attempts to dismiss any active popups or overlays on the current page.
     * Safe to call at any time — all attempts are silent on failure.
     */
    public static void dismissAll(WebDriver driver) {
        dismissCookieBanner(driver);
        dismissModal(driver);
        sendEscapeToBody(driver);
    }

    private static void dismissCookieBanner(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(POPUP_TIMEOUT_SECONDS));
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(COOKIE_ACCEPT));
            btn.click();
        } catch (Exception ignored) {
        }
    }

    private static void dismissModal(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(POPUP_TIMEOUT_SECONDS));
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(MODAL_CLOSE));
            btn.click();
        } catch (Exception ignored) {
        }
    }

    private static void sendEscapeToBody(WebDriver driver) {
        try {
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        } catch (Exception ignored) {
        }
    }
}
