package airbnb.pages;

import airbnb.utils.ElementUtils;
import airbnb.utils.WaitUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;

/**
 * Base class for all page objects. Centralises common WebDriver interactions
 * so individual page classes stay focused on page-specific behaviour.
 *
 * Every page object should extend this class and call {@code super(driver)}.
 */
public abstract class BasePage {

    protected final WebDriver driver;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    // ── Click helpers ─────────────────────────────────────────────────────────

    /**
     * Waits for the element to be clickable, then clicks it.
     *
     * <p>
     * Falls back to a JavaScript click when a blocking overlay intercepts
     * the normal click event ({@link ElementClickInterceptedException}).
     */
    protected void click(By locator) {
        WebElement el = WaitUtils.waitForClickable(driver, locator);
        try {
            el.click();
        } catch (ElementClickInterceptedException e) {
            ElementUtils.jsClick(driver, el);
        }
    }

    /**
     * Scrolls into view then clicks via JavaScript — use for intercepted or
     * off-screen elements.
     */
    protected void jsClick(By locator) {
        WebElement el = WaitUtils.waitForVisible(driver, locator);
        ElementUtils.jsClick(driver, el);
    }

    protected void jsClick(WebElement element) {
        ElementUtils.jsClick(driver, element);
    }

    // ── Input helpers ─────────────────────────────────────────────────────────

    /** Waits for the element, clears it, then types the provided text. */
    protected void clearAndType(By locator, String text) {
        WebElement el = WaitUtils.waitForClickable(driver, locator);
        el.clear();
        el.sendKeys(text);
    }

    // ── Text retrieval ────────────────────────────────────────────────────────

    /** Waits for visibility, then returns trimmed text. */
    protected String getText(By locator) {
        return WaitUtils.waitForVisible(driver, locator).getText().trim();
    }

    // ── Visibility / presence ─────────────────────────────────────────────────

    protected boolean isVisible(By locator) {
        return WaitUtils.isVisible(driver, locator);
    }

    protected boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    protected WebElement waitForVisible(By locator) {
        return WaitUtils.waitForVisible(driver, locator);
    }

    protected WebElement waitForClickable(By locator) {
        return WaitUtils.waitForClickable(driver, locator);
    }

}
