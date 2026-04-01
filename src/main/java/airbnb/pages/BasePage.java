package airbnb.pages;

import airbnb.utils.ElementUtils;
import airbnb.utils.WaitUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

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

    /** Waits for the element to be clickable, then clicks it. */
    protected void click(By locator) {
        WaitUtils.waitForClickable(driver, locator).click();
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

    /** Waits for the element, then sends keys without clearing first. */
    protected void type(By locator, String text) {
        WaitUtils.waitForClickable(driver, locator).sendKeys(text);
    }

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

    /**
     * Returns trimmed text of the first matching element, or empty string if none.
     */
    protected String safeGetText(By locator) {
        List<WebElement> elements = driver.findElements(locator);
        return elements.isEmpty() ? "" : elements.get(0).getText().trim();
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

    // ── Scroll ────────────────────────────────────────────────────────────────

    protected void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", element);
    }
}
