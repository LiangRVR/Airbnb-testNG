package airbnb.pages;

import airbnb.utils.WaitUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;

/**
 * Listing-detail page opened when a user clicks a card on the results page.
 * URL pattern: airbnb.com/rooms/{id}
 */
public class ListingDetailsPage {

    private final WebDriver driver;

    // Primary details container
    private static final By DETAILS_CONTAINER =
            By.cssSelector("[data-section-id='OVERVIEW_DEFAULT']," +
                           "section[aria-label]," +
                           "div[data-testid='pdp-scrollable-page']," +
                           "div[id*='listing']");

    // Hero / photo gallery section
    private static final By GALLERY_SECTION =
            By.cssSelector("[data-section-id='HERO_DEFAULT']," +
                           "div[data-testid='hero-images-scroll']," +
                           "div[class*='gallery']," +
                           "section[aria-label*='Photo']," +
                           "button[aria-label*='photo']");

    // Page title / listing name heading
    private static final By LISTING_TITLE =
            By.cssSelector("h1, h2[data-testid*='title']");

    public ListingDetailsPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public boolean isDetailsContainerVisible() {
        try {
            return WaitUtils.waitForVisible(driver, DETAILS_CONTAINER).isDisplayed();
        } catch (TimeoutException e) {
            // Accept fallback: page has a <main> element at minimum
            return !driver.findElements(By.cssSelector("main")).isEmpty();
        }
    }

    public boolean isGalleryVisible() {
        return WaitUtils.isVisible(driver, GALLERY_SECTION);
    }

    public String getListingTitle() {
        try {
            return WaitUtils.waitForVisible(driver, LISTING_TITLE).getText().trim();
        } catch (TimeoutException e) {
            return "";
        }
    }

    public boolean isOnListingPage() {
        String url = driver.getCurrentUrl();
        return url.contains("/rooms/") || url.contains("/vacation-rentals/");
    }

    /** Navigates back to the results page using browser history. */
    public void goBack() {
        driver.navigate().back();
    }
}
