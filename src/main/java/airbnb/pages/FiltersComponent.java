package airbnb.pages;

import airbnb.utils.WaitUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

/**
 * Filters panel accessible from search results via "Filters" button.
 *
 * Strategy: assert UI-state changes (button active state, badge count,
 * dialog open/closed) rather than exact result counts.
 */
public class FiltersComponent {

    private final WebDriver driver;

    // "Filters" trigger button on results page
    private static final By FILTERS_BTN =
            By.cssSelector("[data-testid='category-bar-filter-button']," +
                           "button[aria-label*='ilters']," +
                           "button[data-testid*='filter']");

    // Filters dialog/panel
    private static final By FILTERS_PANEL =
            By.cssSelector("[data-testid='modal-container']," +
                           "section[aria-label*='ilter']," +
                           "div[role='dialog']");

    // Close / X button inside filters panel
    private static final By FILTERS_CLOSE =
            By.cssSelector("[data-testid='modal-container'] button[aria-label*='lose']," +
                           "button[aria-label*='lose']");

    // "Show results" / Apply button at the bottom of the filters panel
    private static final By APPLY_BTN =
            By.cssSelector("[data-testid='filter-panel-show-results']," +
                           "button[data-testid*='show']," +
                           "a[data-testid*='show-results']");

    // "Clear all" button inside filters panel
    private static final By CLEAR_ALL_BTN =
            By.cssSelector("button[data-testid*='clear']," +
                           "button[aria-label*='lear all']," +
                           "a[data-testid*='clear']");

    // Stable filter option: "Entire home" type chip
    private static final By ROOM_TYPE_CHIP =
            By.cssSelector("[data-testid*='entire-home']," +
                           "button[aria-label*='Entire home']," +
                           "label[for*='entire']");

    // Filter-active badge / chip on the trigger button
    private static final By ACTIVE_FILTER_CHIP =
            By.cssSelector("[data-testid='category-bar-filter-count']," +
                           "button[data-testid*='filter'] span[class*='badge']," +
                           "button[aria-label*='ilter'] span");

    public FiltersComponent(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void openFilters() {
        WaitUtils.waitForClickable(driver, FILTERS_BTN).click();
    }

    public boolean isFiltersPanelOpen() {
        try {
            return WaitUtils.waitForVisible(driver, FILTERS_PANEL).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isFilterButtonVisible() {
        // Use waitForPresence so the button is given time to render after search results load
        return WaitUtils.waitForPresence(driver, FILTERS_BTN);
    }

    public void selectEntireHomeFilter() {
        List<WebElement> chips = driver.findElements(ROOM_TYPE_CHIP);
        if (!chips.isEmpty()) {
            WaitUtils.waitForClickable(driver, chips.get(0)).click();
        }
    }

    public void applyFilters() {
        try {
            WaitUtils.waitForClickable(driver, APPLY_BTN).click();
        } catch (TimeoutException e) {
            // Some layouts close automatically — acceptable fallback
            System.out.println("[FiltersComponent] Apply button not found; panel may have closed.");
        }
    }

    public void clearAllFilters() {
        try {
            WaitUtils.waitForClickable(driver, CLEAR_ALL_BTN).click();
        } catch (TimeoutException ignored) {}
    }

    public boolean isActiveFilterBadgeVisible() {
        return WaitUtils.isVisible(driver, ACTIVE_FILTER_CHIP);
    }

    public void closeFilterPanel() {
        try {
            WaitUtils.waitForClickable(driver, FILTERS_CLOSE).click();
        } catch (TimeoutException ignored) {}
    }
}
