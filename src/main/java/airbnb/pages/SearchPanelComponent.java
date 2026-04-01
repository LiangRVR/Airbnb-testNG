package airbnb.pages;

import airbnb.utils.WaitUtils;
import org.openqa.selenium.*;

import java.util.List;

/**
 * Represents the unified search bar / panel on the Airbnb homepage.
 *
 * Selector strategy:
 * - Prefer data-testid and aria attributes (more stable than generated class
 * names).
 * - Fallback CSS selectors are ordered from most-specific to least-specific.
 * - The overly broad {@code div[data-index]} selector has been removed from
 * SUGGESTIONS.
 */
public class SearchPanelComponent extends BasePage {

    // The "Where" / destination input field
    private static final By WHERE_INPUT = By.cssSelector(
            "input[data-testid='structured-search-input-field-query']," +
                    "input[placeholder*='Search destinations']," +
                    "input[name='query']");

    // Suggestion panel wrapper shown under the destination field
    private static final By SUGGESTION_PANEL = By.cssSelector(
            "[data-testid='structured-search-input-field-query-panel']," +
                    "[role='listbox']");

    // Clickable suggestion rows inside the panel
    private static final By CLICKABLE_SUGGESTIONS = By.cssSelector(
            "[data-testid='structured-search-input-field-query-panel'] button," +
                    "[data-testid='structured-search-input-field-query-panel'] [role='button']," +
                    "[role='listbox'] [role='option']," +
                    "li[role='option']");

    // Title/text nodes within suggestion rows; kept as a fallback signal only
    private static final By SUGGESTION_TITLES = By.cssSelector(
            "[data-testid='option-title']," +
                    "[data-testid='structured-search-input-field-query-panel'] strong");

    // Clear/erase button for the destination field (appears after typing)
    private static final By CLEAR_BTN = By.cssSelector(
            "[data-testid='structured-search-input-field-query-close-button']," +
                    "button[aria-label*='lear']");

    public SearchPanelComponent(WebDriver driver) {
        super(driver);
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
            jsClick(inputs.get(0));
        }
    }

    public void clickWhereField() {
        click(WHERE_INPUT);
    }

    public void typeDestination(String destination) {
        clearAndType(WHERE_INPUT, destination);
    }

    /**
     * Waits for suggestion items to appear in the DOM and returns true if at least
     * one is present.
     */
    public boolean areSuggestionsVisible() {
        return WaitUtils.waitForPresence(driver, SUGGESTION_PANEL, 5)
                || WaitUtils.waitForPresence(driver, CLICKABLE_SUGGESTIONS, 5)
                || WaitUtils.waitForPresence(driver, SUGGESTION_TITLES, 5);
    }

    /**
     * Waits for suggestions to be visible, then selects the first one.
     * Uses BasePage safeClick path via WaitUtils to handle StaleElement retries.
     */
    public void selectFirstSuggestion() {
        // Wait for any suggestion UI to appear first
        if (!areSuggestionsVisible()) {
            throw new NoSuchElementException("No destination suggestions became visible");
        }

        List<WebElement> clickableRows = driver.findElements(CLICKABLE_SUGGESTIONS);
        for (WebElement row : clickableRows) {
            if (row.isDisplayed()) {
                jsClick(row);
                return;
            }
        }

        List<WebElement> titleNodes = driver.findElements(SUGGESTION_TITLES);
        for (WebElement titleNode : titleNodes) {
            if (titleNode.isDisplayed()) {
                jsClick(titleNode);
                return;
            }
        }

        // Final fallback: use keyboard navigation on the destination input.
        // Airbnb's suggestion rows are occasionally rendered in non-semantic wrappers,
        // but ArrowDown + Enter still selects the first highlighted result reliably.
        List<WebElement> inputs = driver.findElements(WHERE_INPUT);
        if (!inputs.isEmpty()) {
            WebElement input = inputs.get(0);
            input.sendKeys(Keys.ARROW_DOWN);
            input.sendKeys(Keys.ENTER);
            return;
        }

        throw new NoSuchElementException("Suggestion panel appeared, but no clickable suggestion row was found");
    }

    public void clearDestination() {
        try {
            jsClick(CLEAR_BTN);
        } catch (TimeoutException ignored) {
            // Clear button may not appear; fall back to keyboard shortcut
            WebElement input = driver.findElement(WHERE_INPUT);
            input.sendKeys(Keys.CONTROL + "a");
            input.sendKeys(Keys.DELETE);
        }
    }

    public void clickSearchButton() {
        // JS click avoids modal-container intercept and stale-element races
        ((JavascriptExecutor) driver).executeScript(
                "var b = document.querySelector('[data-testid=\"structured-search-input-search-button\"]');" +
                        "if (b) b.click();");
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
