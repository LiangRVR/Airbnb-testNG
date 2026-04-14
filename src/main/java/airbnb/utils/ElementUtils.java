package airbnb.utils;

import org.openqa.selenium.*;

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

    /** Scrolls the page to the bottom. */
    public static void scrollToBottom(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
    }
}
