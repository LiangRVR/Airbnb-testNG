package airbnb.pages;

import airbnb.utils.ElementUtils;
import airbnb.utils.WaitUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;

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
public class DatePickerComponent {

    private final WebDriver driver;

    // Triggers the date picker — primary: results page 'little-search-date',
    // fallback: homepage expanded modal calendar tab
    private static final By CHECKIN_BTN = By.cssSelector("[data-testid='little-search-date']," +
            "[data-testid='expanded-searchbar-dates-calendar-tab']," +
            "[data-testid='structured-search-input-field-split-dates-0']," +
            "button[data-testid*='checkin']");

    // Checkout re-uses same trigger since calendar stays open after check-in click
    private static final By CHECKOUT_BTN = CHECKIN_BTN;

    // No container data-testid exists — detect by presence of day cells
    private static final By CALENDAR_CONTAINER = CHECKIN_BTN; // kept for isCalendarOpen() fallback

    // Expanded-panel date tab that appears after clicking the compact
    // 'little-search-date' trigger
    private static final By EXPANDED_DATE_TAB = By.cssSelector(
            "[data-testid='expanded-searchbar-dates-calendar-tab']," +
                    "[data-testid='structured-search-input-field-split-dates-0']");

    // Day cells: aria-label format "1, Sunday, March 2026. <status>"
    // Matched by presence of ", " (day/dayname separator) AND " 202" (year prefix)
    private static final By DAY_CELLS = By
            .xpath("//button[contains(@aria-label, ', ') and contains(@aria-label, ' 202')]");

    // Available (not blocked) day cells — does NOT contain "can't be selected"
    private static final By AVAILABLE_DAYS = By
            .xpath("//button[contains(@aria-label, ', ') and contains(@aria-label, ' 202') " +
                    "and not(contains(@aria-label, \"can't be selected\")) " +
                    "and not(@disabled)]");

    // Past/blocked days contain "Past dates" in aria-label (avoids apostrophe
    // encoding issues)
    private static final By DISABLED_DAYS = By
            .xpath("//button[contains(@aria-label, ', ') and contains(@aria-label, ' 202') " +
                    "and contains(@aria-label, 'Past dates')]");

    public DatePickerComponent(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void openCheckIn() {
        WebElement btn = WaitUtils.waitForVisible(driver, CHECKIN_BTN);
        ElementUtils.jsClick(driver, btn);
        // Quick check: did the click open the calendar directly?
        // (e.g. already on the expanded panel, or the calendar tab was itself clicked)
        if (!WaitUtils.waitForPresence(driver, DAY_CELLS, 4)) {
            // Fallback: the compact 'little-search-date' trigger only expanded the
            // search panel — wait for the date tab to become clickable, then click it.
            try {
                WebElement dateTab = WaitUtils.waitForClickable(driver, EXPANDED_DATE_TAB);
                ElementUtils.jsClick(driver, dateTab);
            } catch (TimeoutException ignored) {
                // Expanded date tab not found; nothing more we can do
            }
            // Final wait for calendar cells with full configured timeout
            WaitUtils.waitForPresence(driver, DAY_CELLS);
        }
    }

    public void openCheckOut() {
        // Calendar stays open after check-in selection; this re-triggers if needed
        List<WebElement> btns = driver.findElements(CHECKIN_BTN);
        if (!btns.isEmpty()) {
            ElementUtils.jsClick(driver, btns.get(0));
        }
    }

    public boolean isCalendarOpen() {
        // Calendar is open when day-cell buttons are present in the DOM
        return !driver.findElements(DAY_CELLS).isEmpty();
    }

    /**
     * Clicks the Nth available (not disabled) day cell. Index is 0-based.
     * Returns the aria-label of the clicked cell for assertion.
     */
    public String clickAvailableDayByIndex(int index) {
        List<WebElement> available = WaitUtils.waitForPresenceOfAll(driver, AVAILABLE_DAYS);
        if (index < available.size()) {
            String label = available.get(index).getAttribute("aria-label");
            // JS click bypasses any overlapping modal backdrop
            ElementUtils.jsClick(driver, available.get(index));
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
