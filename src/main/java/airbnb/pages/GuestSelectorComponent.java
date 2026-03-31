package airbnb.pages;

import airbnb.utils.ElementUtils;
import airbnb.utils.WaitUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;

/**
 * Guest-selector panel (Adults / Children / Infants / Pets).
 * Only "Adults" and "Children" increment/decrement are exercised in tests
 * because Airbnb sometimes hides or reorders the other categories.
 */
public class GuestSelectorComponent {

    private final WebDriver driver;

    // "Who" / Guests trigger button — primary: results page little-search bar,
    // fallback: homepage structured search modal
    private static final By GUESTS_BTN = By.cssSelector("[data-testid='little-search-guests']," +
            "[data-testid='structured-search-input-field-guests-button']," +
            "div[data-testid*='guests']," +
            "button[aria-label*='guest']");

    // Guest-panel wrapper — OR the stepper buttons being visible confirms the panel
    // is open
    private static final By PANEL = By.cssSelector("[data-testid='structured-search-input-field-guests-panel']," +
            "[data-testid='stepper-adults-increase-button']," +
            "div[class*='GuestsPopup']," +
            "section[class*='guest']");

    // Stepper buttons — identified by aria-label pattern
    private static final By ADULTS_INCREASE = By.cssSelector("[data-testid='stepper-adults-increase-button']," +
            "button[aria-label*='Increase number of adults']");
    private static final By ADULTS_DECREASE = By.cssSelector("[data-testid='stepper-adults-decrease-button']," +
            "button[aria-label*='Decrease number of adults']");
    private static final By ADULTS_VALUE = By.cssSelector("[data-testid='stepper-adults-value']," +
            "span[data-testid*='adults']");

    private static final By CHILDREN_INCREASE = By.cssSelector("[data-testid='stepper-children-increase-button']," +
            "button[aria-label*='Increase number of children']");
    private static final By CHILDREN_VALUE = By.cssSelector("[data-testid='stepper-children-value']," +
            "span[data-testid*='children']");

    // Summary shown while panel is open — accessible label "X Adults" or the raw
    // value
    private static final By GUESTS_SUMMARY = By.cssSelector("[data-testid='stepper-adults-a11y-value-label']," +
            "[data-testid='stepper-adults-value']," +
            "[data-testid='structured-search-input-field-guests-button'] span," +
            "button[data-testid*='guests'] span");

    public GuestSelectorComponent(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void openGuestsPanel() {
        WebElement btn = WaitUtils.waitForVisible(driver, GUESTS_BTN);
        ElementUtils.jsClick(driver, btn);
    }

    public boolean isPanelOpen() {
        // Confirm the adults stepper button is visible — it's reliable and always
        // present
        // when the guest panel is active (PANEL may contain hidden pre-rendered
        // elements)
        try {
            WaitUtils.waitForVisible(driver, ADULTS_INCREASE);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void incrementAdults() {
        ElementUtils.jsClick(driver, WaitUtils.waitForVisible(driver, ADULTS_INCREASE));
    }

    public void decrementAdults() {
        ElementUtils.jsClick(driver, WaitUtils.waitForVisible(driver, ADULTS_DECREASE));
    }

    public int getAdultsCount() {
        return parseCounter(driver.findElements(ADULTS_VALUE));
    }

    public void incrementChildren() {
        ElementUtils.jsClick(driver, WaitUtils.waitForVisible(driver, CHILDREN_INCREASE));
    }

    public int getChildrenCount() {
        return parseCounter(driver.findElements(CHILDREN_VALUE));
    }

    public String getGuestsSummaryText() {
        java.util.List<WebElement> spans = driver.findElements(GUESTS_SUMMARY);
        return spans.isEmpty() ? "" : spans.get(0).getText().trim();
    }

    private int parseCounter(java.util.List<WebElement> elements) {
        if (elements.isEmpty())
            return -1;
        try {
            return Integer.parseInt(elements.get(0).getText().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
