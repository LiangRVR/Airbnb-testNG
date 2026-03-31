package airbnb.utils;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.util.List;

/**
 * Low-level element interaction helpers shared across page objects.
 */
public final class ElementUtils {

    private ElementUtils() {
    }

    /**
     * Scrolls element into view then clicks via JS — useful for off-screen or
     * overlay-blocked elements.
     */
    public static void jsClick(WebDriver driver, WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", element);
        js.executeScript("arguments[0].click();", element);
    }

    /** Clears and types text into an input field. */
    public static void clearAndType(WebElement element, String text) {
        element.clear();
        element.sendKeys(text);
    }

    /** Returns true if the element exists in the DOM (not necessarily visible). */
    public static boolean exists(WebDriver driver, By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    /** Safe getText — returns empty string rather than throwing. */
    public static String safeGetText(WebDriver driver, By locator) {
        List<WebElement> elements = driver.findElements(locator);
        return elements.isEmpty() ? "" : elements.get(0).getText().trim();
    }

    /** Moves mouse to element using Actions (helps trigger hover-only menus). */
    public static void hoverOver(WebDriver driver, WebElement element) {
        new Actions(driver).moveToElement(element).perform();
    }

    /** Scrolls the page to the bottom. */
    public static void scrollToBottom(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
    }
}
