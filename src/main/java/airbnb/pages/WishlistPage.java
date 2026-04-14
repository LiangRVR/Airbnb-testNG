package airbnb.pages;

import airbnb.utils.ConfigReader;
import airbnb.utils.WaitUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Represents the Airbnb Wishlists / Saved-listings area.
 *
 * <p>
 * Responsibilities:
 * <ul>
 * <li>Navigating to {@code /wishlists} and verifying the page is loaded</li>
 * <li>Inspecting wishlist collection cards on the index page</li>
 * <li>Interacting with the heart / save button that appears on listing cards
 * and listing detail pages (consolidated here because it belongs to the
 * wishlist feature regardless of which page the button lives on)</li>
 * <li>Confirming the "create / select wishlist" dialog that Airbnb may show
 * after the first save action</li>
 * </ul>
 */
public class WishlistPage extends BasePage {

    // ── Wishlists index page ──────────────────────────────────────────────────

    /** Heading element present on the /wishlists index page. */
    private static final By PAGE_HEADING = By.cssSelector(
            "h1, h2," +
                    "[data-testid='wishlist-title']," +
                    "[data-testid='page-heading']");

    /** Individual wishlist collection cards on the index page. */
    private static final By WISHLIST_CARDS = By.cssSelector(
            "[data-testid='wishlist-card']," +
                    "a[href*='/wishlists/']");

    /** Listing cards inside an open wishlist collection. */
    private static final By SAVED_LISTING_CARDS = By.cssSelector(
            "a[href*='/rooms/']," +
                    "[data-testid='listing-card']," +
                    "[data-testid='card-container']");

    /** Empty-state indicator shown when there are no saved wishlists yet. */
    private static final By EMPTY_STATE = By.cssSelector(
            "[data-testid='empty-wishlist']," +
                    "[data-testid='no-wishlists']");

    // ── Wishlist creation / selection dialog ──────────────────────────────────

    /**
     * Confirmation button in the wishlist dialog that appears after saving a
     * listing for the first time. Airbnb may label it "Done", "Create",
     * "Save", "Add", or "View wishlist" depending on the UI variant.
     */
    private static final By WISHLIST_DIALOG_CONFIRM = By.xpath(
            "//button[normalize-space()='Done']" +
                    " | //button[normalize-space()='Create']" +
                    " | //button[normalize-space()='View wishlist']" +
                    " | //button[contains(@data-testid,'wishlist-done')]" +
                    " | //button[contains(@data-testid,'create-wishlist')]" +
                    " | //button[contains(@data-testid,'confirm')]");

    // ── Heart / save button (shared across results page and listing detail) ───

    /**
     * The heart / save button present on listing cards (results page) and on
     * the listing detail page header area.
     *
     * <p>
     * Aria-label values vary by locale and Airbnb version; all known forms
     * are listed here defensively.
     */
    private static final By SAVE_BUTTON = By.cssSelector(
            "button[aria-label*='wishlist' i]," +
                    "button[aria-label*='Save to' i]," +
                    "button[aria-label*='Save listing' i]," +
                    "button[aria-label*='Add to wishlist' i]," +
                    "button[aria-label*='Saved' i]," + // saved state e.g. "Saved: Lisbon Invites"
                    "button[aria-label*='Remove from' i]," + // saved state e.g. "Remove from Favourites"
                    "[data-testid='listing-card-save-button']");

    public WishlistPage(WebDriver driver) {
        super(driver);
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    /**
     * Navigates directly to the wishlists index page and waits for the page
     * heading to appear.
     */
    public void navigateToWishlists() {
        driver.get(ConfigReader.getBaseUrl() + "/wishlists");
        WaitUtils.waitForPageReady(driver);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(8)).until(d -> d.getCurrentUrl().contains("/wishlists") &&
                    !d.findElements(PAGE_HEADING).isEmpty());
        } catch (TimeoutException ignored) {
            // Proceed — page may load without triggering both conditions
        }
    }

    // ── Page state ────────────────────────────────────────────────────────────

    /** Returns {@code true} when the current URL contains {@code /wishlists}. */
    public boolean isOnWishlistsPage() {
        return driver.getCurrentUrl().contains("/wishlists");
    }

    /**
     * Returns {@code true} when both the URL and page heading confirm the page
     * loaded.
     */
    public boolean isPageLoaded() {
        return isOnWishlistsPage() && isPresent(PAGE_HEADING);
    }

    /**
     * Returns {@code true} when at least one wishlist collection card is present.
     */
    public boolean hasAtLeastOneWishlist() {
        return !driver.findElements(WISHLIST_CARDS).isEmpty();
    }

    /** Returns the number of wishlist collection cards visible on the index. */
    public int getWishlistCount() {
        return driver.findElements(WISHLIST_CARDS).size();
    }

    /** Returns {@code true} when the empty-state element is present. */
    public boolean isEmpty() {
        return isPresent(EMPTY_STATE);
    }

    // ── Listing cards inside an open wishlist ─────────────────────────────────

    /** Returns all listing cards currently visible inside the open wishlist. */
    public List<WebElement> getSavedListingCards() {
        return driver.findElements(SAVED_LISTING_CARDS);
    }

    /** Returns the count of saved listing cards currently visible. */
    public int getSavedListingCount() {
        return getSavedListingCards().size();
    }

    /**
     * Clicks into the first wishlist collection card on the index page so
     * its saved listings become visible.
     */
    public void openFirstWishlist() {
        List<WebElement> cards = driver.findElements(WISHLIST_CARDS);
        if (cards.isEmpty()) {
            throw new NoSuchElementException(
                    "No wishlist collection cards found on the wishlists index page.");
        }
        jsClick(cards.get(0));
        WaitUtils.waitForPageReady(driver);
    }

    /**
     * Returns {@code true} when any visible saved-listing card contains the
     * given title fragment (case-insensitive).
     *
     * @param titleSubstring substring to search for in card text
     */
    public boolean containsListingWithTitle(String titleSubstring) {
        for (WebElement card : getSavedListingCards()) {
            if (card.getText().toLowerCase().contains(titleSubstring.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    // ── Wishlist dialog helpers ───────────────────────────────────────────────

    /**
     * After clicking the heart button, Airbnb shows a multi-step dialog: first an
     * optional <em>selection step</em> listing existing wishlists (buttons each
     * containing "X saved"), then a <em>confirm step</em> with a "Create", "Done",
     * or "View wishlist" button.
     *
     * <p>
     * This method loops up to 3 times, waiting 5 s per attempt. In each
     * iteration it clicks the highest-priority visible element:
     * <ol>
     * <li>A confirm/submit button (Done / Create / View wishlist)</li>
     * <li>An existing wishlist card button (contains " saved")</li>
     * <li>A "Create new wishlist" button</li>
     * </ol>
     * After each click it waits up to 3 s to see whether the dialog closed.
     * If the dialog is still open it loops again, which naturally handles the
     * transition from the selection step to the naming form.
     *
     * <p>
     * Safe to call when no dialog appears — all waits are silently suppressed.
     */
    public void confirmWishlistDialog() {
        final By DIALOG_CARD = By.xpath(
                "//button[contains(normalize-space(.), ' saved')]");
        final By CREATE_NEW_BTN = By.xpath(
                "//button[normalize-space()='Create new wishlist']"
                        + " | //button[contains(normalize-space(),'Create new wishlist')]");
        // All locators that indicate an open dialog; used for "is dialog still open"
        // check
        final By ALL_DIALOG_ELEMENTS = By.xpath(
                "//button[contains(normalize-space(.), ' saved')]"
                        + " | //button[normalize-space()='Create new wishlist']"
                        + " | //button[contains(normalize-space(),'Create new wishlist')]"
                        + " | //button[normalize-space()='Done']"
                        + " | //button[normalize-space()='Create']"
                        + " | //button[normalize-space()='View wishlist']");

        for (int attempt = 0; attempt < 4; attempt++) {
            // ── Find the best visible element in this dialog step ────────────
            WebElement found;
            try {
                found = new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> {
                    // Priority 1: confirm button (Create / Done / View wishlist) — visible only,
                    // NOT checking isEnabled() because React may keep the button in a DOM-disabled
                    // state even when the name field is pre-filled; we trigger validation below.
                    for (WebElement el : d.findElements(WISHLIST_DIALOG_CONFIRM)) {
                        if (el.isDisplayed())
                            return el;
                    }
                    // Priority 2: existing wishlist card in the selection dialog
                    for (WebElement el : d.findElements(DIALOG_CARD)) {
                        if (el.isDisplayed())
                            return el;
                    }
                    // Priority 3: "Create new wishlist" button
                    for (WebElement el : d.findElements(CREATE_NEW_BTN)) {
                        if (el.isDisplayed())
                            return el;
                    }
                    return null;
                });
            } catch (TimeoutException e) {
                break; // No dialog element found in 5 s — dialog already closed or silent save
            }

            // If there is a visible name input, interact with it to fire React's
            // onChange validation so the "Create" button becomes enabled.
            try {
                List<WebElement> nameInputs = driver.findElements(By.cssSelector(
                        "input[aria-label='Name'],input[aria-label='Wishlist name']," +
                                "input[aria-label*='ame' i]"));
                for (WebElement ni : nameInputs) {
                    if (ni.isDisplayed()) {
                        ni.click();
                        String val = ni.getAttribute("value");
                        if (val == null || val.trim().isEmpty()) {
                            ni.sendKeys("My Wishlist");
                        } else {
                            // Send a no-op keystroke to fire onChange so React re-evaluates
                            ni.sendKeys(org.openqa.selenium.Keys.END);
                        }
                        break;
                    }
                }
            } catch (Exception ignored) {
                /* name input optional */ }

            try {
                found.click();
            } catch (org.openqa.selenium.ElementClickInterceptedException ex) {
                jsClick(found);
            }

            // ── Check whether the dialog closed after this click ──────────────
            // Wait up to 3 s; if ANY dialog element is still visible, loop again.
            try {
                new WebDriverWait(driver, Duration.ofSeconds(3)).until(d -> d.findElements(ALL_DIALOG_ELEMENTS).stream()
                        .noneMatch(org.openqa.selenium.WebElement::isDisplayed)
                                ? Boolean.TRUE
                                : null);
                break; // Dialog closed — we are done
            } catch (TimeoutException ignored) {
                // Dialog still has visible elements — loop to handle next step
            }
        }
    }

    // ── Heart / save button helpers (usable from any page context) ────────────

    /**
     * Clicks the first visible heart / save button on the current page.
     * Works on both the search-results page and a listing-detail page.
     *
     * @return the {@code aria-label} of the button <em>before</em> clicking,
     *         useful for before/after state comparison; empty string if absent
     */
    public String clickFirstSaveButton() {
        WebElement btn = WaitUtils.waitForClickable(driver, SAVE_BUTTON);
        String labelBefore = btn.getAttribute("aria-label");
        jsClick(btn);
        return labelBefore != null ? labelBefore : "";
    }

    /**
     * Returns the current {@code aria-label} of the first heart/save button,
     * which Airbnb updates from "Save to wishlist" → "Saved to wishlist"
     * (or similar) when the listing is saved.
     *
     * @return label text, or empty string if no save button is found
     */
    public String getFirstSaveButtonLabel() {
        List<WebElement> buttons = driver.findElements(SAVE_BUTTON);
        if (buttons.isEmpty())
            return "";
        String label = buttons.get(0).getAttribute("aria-label");
        return label != null ? label : "";
    }

    /**
     * Returns {@code true} when the first save button reflects a saved state,
     * determined by either:
     * <ul>
     * <li>{@code aria-pressed="true"}</li>
     * <li>an {@code aria-label} containing "saved" or "remove"
     * (case-insensitive)</li>
     * </ul>
     */
    public boolean isFirstListingSaved() {
        // Poll for up to 4 seconds to allow post-dialog UI state update
        try {
            return Boolean.TRUE.equals(
                    new WebDriverWait(driver, Duration.ofSeconds(4)).until(d -> {
                        List<WebElement> buttons = d.findElements(SAVE_BUTTON);
                        if (buttons.isEmpty())
                            return null;
                        WebElement btn = buttons.get(0);
                        String pressed = btn.getAttribute("aria-pressed");
                        String label = btn.getAttribute("aria-label");
                        boolean saved = "true".equalsIgnoreCase(pressed) ||
                                (label != null && (label.toLowerCase().contains("saved") ||
                                        label.toLowerCase().contains("remove") ||
                                        label.toLowerCase().contains("added"))); // e.g. "Added to wishlist"
                        return saved ? Boolean.TRUE : null;
                    }));
        } catch (TimeoutException e) {
            return false;
        }
    }
}
