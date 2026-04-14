package airbnb.pages;

import airbnb.utils.WaitUtils;
import org.openqa.selenium.*;

import java.util.List;

/**
 * Encapsulates the date-picker panel that appears after clicking the check-in
 * or check-out field in the search bar.
 *
 * Reliability notes:
 * - Past dates carry aria-disabled="true" — we assert that attribute rather
 * than attempting to click them.
 * - Day cells are selected by aria-label which includes the full date string.
 */
public class DatePickerComponent extends BasePage {

    // Check-in trigger: results page 'little-search-date', homepage expanded modal
    // calendar tab,
    // or the split-dates field
    private static final By CHECKIN_BTN = By.cssSelector(
            "[data-testid='little-search-date']," +
                    "[data-testid='expanded-searchbar-dates-calendar-tab']," +
                    "[data-testid='structured-search-input-field-split-dates-0']," +
                    "button[data-testid*='checkin']");

    // Expanded-panel date tab that appears after clicking the compact trigger
    private static final By EXPANDED_DATE_TAB = By.cssSelector(
            "[data-testid='expanded-searchbar-dates-calendar-tab']," +
                    "[data-testid='structured-search-input-field-split-dates-0']");

    // Day cells: aria-label format "1, Sunday, March 2026. <status>"
    private static final By DAY_CELLS = By
            .xpath("//button[contains(@aria-label, ', ') and contains(@aria-label, ' 202')]");

    // Available (not blocked) day cells
    private static final By AVAILABLE_DAYS = By
            .xpath("//button[contains(@aria-label, ', ') and contains(@aria-label, ' 202') " +
                    "and not(contains(@aria-label, \"can't be selected\")) " +
                    "and not(@disabled)]");

    // Past/blocked days contain "Past dates" in aria-label
    private static final By DISABLED_DAYS = By
            .xpath("//button[contains(@aria-label, ', ') and contains(@aria-label, ' 202') " +
                    "and contains(@aria-label, 'Past dates')]");

    public DatePickerComponent(WebDriver driver) {
        super(driver);
    }

    public void openCheckIn() {
        PopupHandler.dismissAll(driver);
        WebElement btn = WaitUtils.waitForVisible(driver, CHECKIN_BTN);
        jsClick(btn);
        if (!WaitUtils.waitForPresence(driver, DAY_CELLS, 4)) {
            try {
                WebElement dateTab = WaitUtils.waitForClickable(driver, EXPANDED_DATE_TAB);
                jsClick(dateTab);
            } catch (TimeoutException ignored) {
            }
            if (!WaitUtils.waitForPresence(driver, DAY_CELLS, 8)) {
                // Popup may have appeared and interrupted the click — dismiss and retry
                PopupHandler.dismissAll(driver);
                btn = WaitUtils.waitForVisible(driver, CHECKIN_BTN);
                jsClick(btn);
            }
            WaitUtils.waitForPresence(driver, DAY_CELLS);
        }
    }

    public boolean isCalendarOpen() {
        return !driver.findElements(DAY_CELLS).isEmpty();
    }

    /**
     * Clicks the Nth available (not disabled) day cell.
     * Index is 0-based. Returns the aria-label of the clicked cell.
     */
    public String clickAvailableDayByIndex(int index) {
        return clickAvailableDayAt(index);
    }

    private String clickAvailableDayAt(int index) {
        List<WebElement> available = WaitUtils.waitForPresenceOfAll(driver, AVAILABLE_DAYS);
        if (index < available.size()) {
            String label = available.get(index).getAttribute("aria-label");
            jsClick(available.get(index));
            return label != null ? label : "";
        }
        return "";
    }

    /** Returns true if any past/blocked day cells exist in the calendar. */
    public boolean arePastDatesBlocked() {
        List<WebElement> disabled = driver.findElements(DISABLED_DAYS);
        return !disabled.isEmpty();
    }
}
