package airbnb.utils;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Central explicit-wait helpers. All methods use WebDriverWait so
 * Thread.sleep is never needed in page/test code.
 */
public final class WaitUtils {

    private WaitUtils() {}

    public static WebDriverWait getWait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(airbnb.utils.ConfigReader.getExplicitWait()));
    }

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

    public static boolean waitForUrlContains(WebDriver driver, String fragment) {
        return getWait(driver).until(ExpectedConditions.urlContains(fragment));
    }

    public static boolean waitForPresence(WebDriver driver, By locator) {
        try {
            getWait(driver).until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Waits until at least one element matching the locator is present in DOM. */
    public static java.util.List<WebElement> waitForPresenceOfAll(WebDriver driver, By locator) {
        return getWait(driver).until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
    }

    public static boolean isVisible(WebDriver driver, By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }
}
