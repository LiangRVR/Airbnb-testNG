package airbnb.utils;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Central explicit-wait helpers. All methods use WebDriverWait so
 * Thread.sleep is never needed in page/test code.
 */
public final class WaitUtils {

    private WaitUtils() {
    }

    public static WebDriverWait getWait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getExplicitWait()));
    }

    public static WebDriverWait getWait(WebDriver driver, int seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }

    // ── Visibility / clickability ─────────────────────────────────────────────

    public static WebElement waitForVisible(WebDriver driver, By locator) {
        return getWait(driver).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForVisible(WebDriver driver, WebElement element) {
        return getWait(driver).until(ExpectedConditions.visibilityOf(element));
    }

    public static WebElement waitForClickable(WebDriver driver, By locator) {
        return getWait(driver).until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static WebElement waitForClickable(WebDriver driver, WebElement element) {
        return getWait(driver).until(ExpectedConditions.elementToBeClickable(element));
    }

    // ── Presence ──────────────────────────────────────────────────────────────

    public static boolean waitForPresence(WebDriver driver, By locator) {
        try {
            getWait(driver).until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public static boolean waitForPresence(WebDriver driver, By locator, int timeoutSeconds) {
        try {
            getWait(driver, timeoutSeconds).until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public static List<WebElement> waitForPresenceOfAll(WebDriver driver, By locator) {
        return getWait(driver).until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
    }

    // ── URL conditions ────────────────────────────────────────────────────────

    public static boolean waitForUrlContains(WebDriver driver, String fragment) {
        return getWait(driver).until(ExpectedConditions.urlContains(fragment));
    }

    public static boolean waitForUrlMatches(WebDriver driver, String regex) {
        try {
            return getWait(driver).until(ExpectedConditions.urlMatches(regex));
        } catch (TimeoutException e) {
            return false;
        }
    }

    // ── Page readiness ────────────────────────────────────────────────────────

    public static void waitForPageReady(WebDriver driver) {
        getWait(driver).until(d -> "complete".equals(((JavascriptExecutor) d)
                .executeScript("return document.readyState")));
    }

    // ── Convenience visibility check (no throw) ───────────────────────────────

    public static boolean isVisible(WebDriver driver, By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }
}
