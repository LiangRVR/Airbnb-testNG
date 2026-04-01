package airbnb.pages;

import airbnb.utils.ElementUtils;
import airbnb.utils.WaitUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

/**
 * Represents the unified search bar / panel on the Airbnb homepage.
 *
 * Selector strategy:
 * - Prefer data-testid and aria attributes (more stable than generated class
 * names).
 * - Fall-back CSS selectors are ordered from most-specific to least-specific.
 */
public class SearchPanelComponent {

    private final WebDriver driver;

    // The "Where" / destination input field
    private static final By WHERE_INPUT = By.cssSelector("input[data-testid='structured-search-input-field-query']," +
            "input[placeholder*='Search destinations']," +
            "input[name='query']");

    // Auto-suggest dropdown items
    private static final By SUGGESTIONS = By.cssSelector("[data-testid='option-title']," +
            "li[role='option']," +
            "div[data-index]");

    // A clear/erase button for the destination field (appears after typing)
    private static final By CLEAR_BTN = By
            .cssSelector("[data-testid='structured-search-input-field-query-close-button']," +
                    "button[aria-label*='lear']");

    // Main search submit button
    private static final By SEARCH_BTN = By.cssSelector("[data-testid='structured-search-input-search-button']," +
            "button[type='submit']");

    public SearchPanelComponent(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public boolean isSearchPanelVisible() {
        return WaitUtils.waitForPresence(driver, WHERE_INPUT);
    }

    /**
     * Expands the search modal — required before guests/date fields are reachable.
     */
    public void expandSearchModal() {
        List<WebElement> inputs = driver.findElements(WHERE_INPUT);
        if (!inputs.isEmpty()) {
            ElementUtils.jsClick(driver, inputs.get(0));
        }
    }

    public void clickWhereField() {
        WebElement input = WaitUtils.waitForVisible(driver, WHERE_INPUT);
        ElementUtils.jsClick(driver, input);
    }

    public void typeDestination(String destination) {
        WebElement input = WaitUtils.waitForClickable(driver, WHERE_INPUT);
        input.clear();
        input.sendKeys(destination);
    }

    public boolean areSuggestionsVisible() {
        try {
            List<WebElement> items = WaitUtils.waitForPresenceOfAll(driver, SUGGESTIONS);
            return !items.isEmpty();
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * Selects the first auto-suggest item for the typed destination.
     * Retries on StaleElementReferenceException because the suggestion list
     * can re-render between the time it is fetched and the JS click executes.
     */
    public void selectFirstSuggestion() {
        int maxAttempts = 3;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                List<WebElement> suggestions = WaitUtils.waitForPresenceOfAll(driver, SUGGESTIONS);
                if (!suggestions.isEmpty()) {
                    ElementUtils.jsClick(driver, suggestions.get(0));
                }
                return;
            } catch (StaleElementReferenceException e) {
                if (attempt == maxAttempts - 1)
                    throw e;
            }
        }
    }

    public void clearDestination() {
        try {
            WebElement btn = WaitUtils.waitForVisible(driver, CLEAR_BTN);
            ElementUtils.jsClick(driver, btn);
        } catch (TimeoutException ignored) {
            // Clear button may not appear; use keyboard shortcut as fallback
            WebElement input = driver.findElement(WHERE_INPUT);
            input.sendKeys(Keys.CONTROL + "a");
            input.sendKeys(Keys.DELETE);
        }
    }

    public void clickSearchButton() {
        // Pure JS click avoids both modal-container intercept and stale-element races
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "var b = document.querySelector('[data-testid=\"structured-search-input-search-button\"]');" +
                        "if (b) b.click();");
        // Wait for the page to navigate away from the homepage
        try {
            WaitUtils.waitForUrlContains(driver, "/s/");
        } catch (TimeoutException ignored) {
            // Some results pages use different URL patterns — acceptable
        }
    }

    public String getDestinationFieldValue() {
        List<WebElement> inputs = driver.findElements(WHERE_INPUT);
        return inputs.isEmpty() ? "" : inputs.get(0).getAttribute("value");
    }
}
