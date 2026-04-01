package airbnb.pages;

import airbnb.utils.WaitUtils;
import org.openqa.selenium.*;

import java.util.List;

/**
 * Filters panel accessible from search results via the "Filters" button.
 *
 * Strategy: assert UI-state changes (chip selected state, badge count,
 * dialog open/closed) rather than exact result counts.
 */
public class FiltersComponent extends BasePage {

    // "Filters" trigger button on results page
    private static final By FILTERS_BTN = By.cssSelector("[data-testid='category-bar-filter-button']," +
            "button[aria-label*='ilters']," +
            "button[data-testid*='filter']");

    // Filters dialog/panel
    private static final By FILTERS_PANEL = By.cssSelector("[data-testid='modal-container']," +
            "section[aria-label*='ilter']," +
            "div[role='dialog']");

    // Close / X button inside filters panel
    private static final By FILTERS_CLOSE = By
            .cssSelector("[data-testid='modal-container'] button[aria-label*='lose']," +
                    "button[aria-label*='lose']");

    // "Show results" / Apply button at the bottom of the filters panel
    private static final By APPLY_BTN = By.cssSelector("[data-testid='filter-panel-show-results']," +
            "button[data-testid*='show']," +
            "a[data-testid*='show-results']");

    // "Clear all" button inside filters panel
    private static final By CLEAR_ALL_BTN = By.cssSelector("button[data-testid*='clear']," +
            "button[aria-label*='lear all']," +
            "a[data-testid*='clear']");

    // "Entire home" is rendered as a radio option in the "Type of place" section.
    private static final By ROOM_TYPE_CHIP = By.xpath(
            "//div[@role='radio' and (@aria-describedby='room-filter-description-Entire home' " +
                    "or .//span[normalize-space()='Entire home'])]");

    private static final By ROOM_TYPE_CHIP_SELECTED = By.xpath(
            "//div[@role='radio' and @aria-checked='true' and (@aria-describedby='room-filter-description-Entire home' "
                    +
                    "or .//span[normalize-space()='Entire home'])]");

    private static final By ANY_TYPE_CHIP = By.xpath(
            "//div[@role='radio' and (@aria-describedby='room-filter-description-Any type' " +
                    "or .//span[normalize-space()='Any type'])]");

    // Filter-active badge / chip count on the trigger button
    private static final By ACTIVE_FILTER_CHIP = By.cssSelector("[data-testid='category-bar-filter-count']," +
            "button[data-testid*='filter'] span[class*='badge']," +
            "button[aria-label*='ilter'] span");

    public FiltersComponent(WebDriver driver) {
        super(driver);
    }

    public void openFilters() {
        PopupHandler.dismissAll(driver);
        try {
            WaitUtils.waitForClickable(driver, FILTERS_BTN).click();
        } catch (ElementClickInterceptedException e) {
            PopupHandler.dismissAll(driver);
            jsClick(FILTERS_BTN);
        }
    }

    public boolean isFiltersPanelOpen() {
        try {
            return WaitUtils.waitForVisible(driver, FILTERS_PANEL).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isFilterButtonVisible() {
        return WaitUtils.waitForPresence(driver, FILTERS_BTN);
    }

    public void selectEntireHomeFilter() {
        List<WebElement> chips = driver.findElements(ROOM_TYPE_CHIP);
        for (WebElement chip : chips) {
            if (!chip.isDisplayed()) {
                continue;
            }
            try {
                WaitUtils.waitForClickable(driver, chip).click();
            } catch (ElementClickInterceptedException e) {
                jsClick(chip);
            }
            return;
        }
    }

    /**
     * Returns true when the "Entire home" chip has an active/pressed state.
     * Checks both {@code aria-pressed} and {@code aria-checked} attributes.
     */
    public boolean isEntireHomeSelected() {
        if (!driver.findElements(ROOM_TYPE_CHIP_SELECTED).isEmpty()) {
            return true;
        }

        List<WebElement> chips = driver.findElements(ROOM_TYPE_CHIP);
        for (WebElement chip : chips) {
            if ("true".equals(chip.getAttribute("aria-checked"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the text of the apply button (e.g. "Show 42 places") for richer
     * assertions.
     */
    public String getApplyButtonText() {
        List<WebElement> btns = driver.findElements(APPLY_BTN);
        return btns.isEmpty() ? "" : btns.get(0).getText().trim();
    }

    public void applyFilters() {
        try {
            WaitUtils.waitForClickable(driver, APPLY_BTN).click();
        } catch (ElementClickInterceptedException e) {
            jsClick(APPLY_BTN);
        } catch (TimeoutException e) {
            // Some layouts close automatically — acceptable fallback
            System.out.println("[FiltersComponent] Apply button not found; panel may have closed.");
        }
    }

    public void clearAllFilters() {
        try {
            WaitUtils.waitForClickable(driver, CLEAR_ALL_BTN).click();
        } catch (ElementClickInterceptedException e) {
            jsClick(CLEAR_ALL_BTN);
        } catch (TimeoutException ignored) {
        }

        if (isEntireHomeSelected()) {
            List<WebElement> defaultOptions = driver.findElements(ANY_TYPE_CHIP);
            for (WebElement defaultOption : defaultOptions) {
                if (!defaultOption.isDisplayed()) {
                    continue;
                }
                try {
                    WaitUtils.waitForClickable(driver, defaultOption).click();
                } catch (ElementClickInterceptedException e) {
                    jsClick(defaultOption);
                }
                return;
            }
        }
    }

    public boolean isActiveFilterBadgeVisible() {
        return WaitUtils.isVisible(driver, ACTIVE_FILTER_CHIP);
    }

    public void closeFilterPanel() {
        try {
            WaitUtils.waitForClickable(driver, FILTERS_CLOSE).click();
        } catch (TimeoutException ignored) {
        }
    }
}
