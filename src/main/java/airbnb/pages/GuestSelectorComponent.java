package airbnb.pages;

import airbnb.utils.WaitUtils;
import org.openqa.selenium.*;

import java.util.List;

/**
 * Guest-selector panel (Adults / Children / Infants / Pets).
 * Only "Adults" and "Children" increment/decrement are exercised in tests
 * because Airbnb sometimes hides or reorders the other categories.
 */
public class GuestSelectorComponent extends BasePage {

    // "Who" / Guests trigger button — prefer testid-based selectors; removed
    // the overly broad button[aria-label*='guest'] fallback
    private static final By GUESTS_BTN = By.cssSelector(
            "[data-testid='little-search-guests']," +
            "[data-testid='structured-search-input-field-guests-button']," +
            "div[data-testid*='guests']");

    // Stepper buttons — identified by stable aria-label pattern
    private static final By ADULTS_INCREASE = By.cssSelector(
            "[data-testid='stepper-adults-increase-button']," +
            "button[aria-label*='Increase number of adults']");
    private static final By ADULTS_DECREASE = By.cssSelector(
            "[data-testid='stepper-adults-decrease-button']," +
            "button[aria-label*='Decrease number of adults']");
    private static final By ADULTS_VALUE = By.cssSelector(
            "[data-testid='stepper-adults-value']," +
            "span[data-testid*='adults-value']");

    private static final By CHILDREN_INCREASE = By.cssSelector(
            "[data-testid='stepper-children-increase-button']," +
            "button[aria-label*='Increase number of children']");
    private static final By CHILDREN_VALUE = By.cssSelector(
            "[data-testid='stepper-children-value']," +
            "span[data-testid*='children-value']");

    // Accessible label showing count while panel is open — prefer the specific a11y label
    private static final By GUESTS_SUMMARY = By.cssSelector(
            "[data-testid='stepper-adults-a11y-value-label']," +
            "[data-testid='stepper-adults-value']");

    public GuestSelectorComponent(WebDriver driver) {
        super(driver);
    }

    public void openGuestsPanel() {
        WebElement btn = WaitUtils.waitForVisible(driver, GUESTS_BTN);
        jsClick(btn);
    }

    public boolean isPanelOpen() {
        try {
            WaitUtils.waitForVisible(driver, ADULTS_INCREASE);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void incrementAdults() {
        jsClick(WaitUtils.waitForVisible(driver, ADULTS_INCREASE));
    }

    public void decrementAdults() {
        jsClick(WaitUtils.waitForVisible(driver, ADULTS_DECREASE));
    }

    public int getAdultsCount() {
        return parseCounter(driver.findElements(ADULTS_VALUE));
    }

    public void incrementChildren() {
        jsClick(WaitUtils.waitForVisible(driver, CHILDREN_INCREASE));
    }

    public int getChildrenCount() {
        return parseCounter(driver.findElements(CHILDREN_VALUE));
    }

    /**
     * Returns true when the Adults decrement button is disabled (minimum boundary reached).
     */
    public boolean isAdultsDecrementDisabled() {
        List<WebElement> btns = driver.findElements(ADULTS_DECREASE);
        if (btns.isEmpty()) return false;
        WebElement btn = btns.get(0);
        return "true".equals(btn.getAttribute("disabled"))
                || "true".equals(btn.getAttribute("aria-disabled"));
    }

    public String getGuestsSummaryText() {
        List<WebElement> spans = driver.findElements(GUESTS_SUMMARY);
        return spans.isEmpty() ? "" : spans.get(0).getText().trim();
    }

    private int parseCounter(List<WebElement> elements) {
        if (elements.isEmpty()) return -1;
        String text = elements.get(0).getText().trim();
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            // Try aria-valuenow as fallback (some Airbnb stepper implementations use it)
            String ariaValue = elements.get(0).getAttribute("aria-valuenow");
            if (ariaValue != null) {
                try { return Integer.parseInt(ariaValue.trim()); } catch (NumberFormatException ignored) {}
            }
            return -1;
        }
    }
}
