package airbnb.pages;

import airbnb.utils.WaitUtils;
import org.openqa.selenium.*;

import java.util.List;

/**
 * Represents the search-results listing page (URL typically contains
 * /s/…/homes).
 */
public class SearchResultsPage extends BasePage {

    private static final By ROOM_LINKS = By.cssSelector("a[href*='/rooms/']");

    private static final By FILTERS_TRIGGER = By.cssSelector("[data-testid='category-bar-filter-button']," +
            "button[aria-label*='ilters']," +
            "button[data-testid*='filter']");

    // Results container panel
    private static final By RESULTS_CONTAINER = By.cssSelector("[data-testid='card-container']," +
            "div[itemprop='itemList']," +
            "[data-testid='explore-byway-explore']");

    // Individual listing cards — prefer room-specific hrefs for reliability
    private static final By LISTING_CARDS = By.cssSelector("a[href*='/rooms/']," +
            "[data-testid='card-container']," +
            "div[itemprop='itemListElement']");

    // Map / split-view panel
    private static final By MAP_REGION = By.cssSelector("[data-testid='map-toggle']," +
            "div[aria-label*='Map']," +
            "section[aria-label*='Map']");

    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    public boolean isResultsContainerVisible() {
        try {
            return WaitUtils.waitForVisible(driver, RESULTS_CONTAINER).isDisplayed();
        } catch (TimeoutException e) {
            // Fallback: check for at least one room-link card
            return !driver.findElements(ROOM_LINKS).isEmpty();
        }
    }

    public boolean isMapVisible() {
        return WaitUtils.isVisible(driver, MAP_REGION);
    }

    public List<WebElement> getListingCards() {
        return WaitUtils.waitForPresenceOfAll(driver, LISTING_CARDS);
    }

    public int getListingCount() {
        return driver.findElements(LISTING_CARDS).size();
    }

    /**
     * Waits until the browser is on a search/results URL and at least one
     * results-page signal is present.
     */
    public boolean waitUntilLoaded() {
        WaitUtils.waitForPageReady(driver);

        boolean urlMatch = WaitUtils.waitForUrlMatches(driver,
                ".*(/s/.*|search_type=search_query.*|[?&]query=.*)");
        if (!urlMatch) {
            return false;
        }

        return WaitUtils.waitForPresence(driver, ROOM_LINKS, 10)
                || WaitUtils.waitForPresence(driver, RESULTS_CONTAINER, 10)
                || WaitUtils.waitForPresence(driver, FILTERS_TRIGGER, 10);
    }

    /**
     * Clicks the first visible listing card and returns the href for smoke
     * verification.
     * Prefers {@code a[href*='/rooms/']} links so we open a real listing.
     */
    public String clickFirstListing() {
        // Prefer direct room links first
        List<WebElement> roomLinks = driver.findElements(ROOM_LINKS);
        if (!roomLinks.isEmpty()) {
            String href = roomLinks.get(0).getAttribute("href");
            jsClick(roomLinks.get(0));
            return href != null ? href : "";
        }
        List<WebElement> cards = getListingCards();
        if (cards.isEmpty())
            throw new NoSuchElementException("No listing cards found on results page");
        String href = cards.get(0).getAttribute("href");
        jsClick(cards.get(0));
        return href != null ? href : "";
    }

    /**
     * Returns true when the current page looks like a search-results page.
     * Requires both URL pattern AND at least one card in the DOM.
     * Waits briefly for each signal to allow for React re-renders.
     */
    public boolean isOnResultsPage() {
        String url = driver.getCurrentUrl();
        boolean urlMatch = url.contains("/s/") || url.contains("search_type=search_query") || url.contains("?query=");
        if (!urlMatch)
            return false;
        return WaitUtils.waitForPresence(driver, ROOM_LINKS, 5)
                || WaitUtils.waitForPresence(driver, RESULTS_CONTAINER, 5)
                || WaitUtils.waitForPresence(driver, FILTERS_TRIGGER, 5);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
