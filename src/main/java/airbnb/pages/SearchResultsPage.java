package airbnb.pages;

import airbnb.utils.ElementUtils;
import airbnb.utils.WaitUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

/**
 * Represents the search-results listing page (URL typically contains /s/…/homes).
 */
public class SearchResultsPage {

    private final WebDriver driver;

    // Results container — Airbnb uses a data-testid on the list panel
    private static final By RESULTS_CONTAINER =
            By.cssSelector("[data-testid='card-container']," +
                           "div[itemprop='itemList']," +
                           "div[data-testid='listing-card']," +
                           "[data-testid='explore-byway-explore']");

    // Individual listing cards
    private static final By LISTING_CARDS =
            By.cssSelector("[data-testid='card-container']," +
                           "a[href*='/rooms/']," +
                           "div[itemprop='itemListElement']");

    // Map / split-view panel
    private static final By MAP_REGION =
            By.cssSelector("[data-testid='map-toggle']," +
                           "div[class*='mapContainer']," +
                           "div[aria-label*='Map']," +
                           "section[aria-label*='Map']");

    public SearchResultsPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public boolean isResultsContainerVisible() {
        try {
            return WaitUtils.waitForVisible(driver, RESULTS_CONTAINER).isDisplayed();
        } catch (TimeoutException e) {
            // Fall back: check if at least one card exists
            return !driver.findElements(LISTING_CARDS).isEmpty();
        }
    }

    public boolean isMapVisible() {
        return WaitUtils.isVisible(driver, MAP_REGION);
    }

    public List<WebElement> getListingCards() {
        return WaitUtils.waitForPresenceOfAll(driver, LISTING_CARDS);
    }

    /** Clicks the first visible listing card and returns the title
     *  from the href for basic smoke verification. */
    public String clickFirstListing() {
        List<WebElement> cards = getListingCards();
        if (cards.isEmpty()) throw new NoSuchElementException("No listing cards found on results page");
        WebElement first = cards.get(0);
        String href = first.getAttribute("href");
        ElementUtils.jsClick(driver, first);
        return href != null ? href : "";
    }

    public boolean isOnResultsPage() {
        String url = driver.getCurrentUrl();
        return url.contains("/s/") || url.contains("search") || url.contains("homes");
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
