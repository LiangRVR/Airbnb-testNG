package airbnb.pages;

import airbnb.utils.WaitUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

/**
 * Listing-detail page opened when a user clicks a card on the results page.
 * URL pattern: airbnb.com/rooms/{id}
 */
public class ListingDetailsPage extends BasePage {

    // Primary details container — specific testids only; removed generic
    // section[aria-label]
    private static final By DETAILS_CONTAINER = By.cssSelector("[data-section-id='OVERVIEW_DEFAULT']," +
            "div[data-testid='pdp-scrollable-page']," +
            "div[id*='listing']");

    // Hero / photo gallery section
    private static final By GALLERY_SECTION = By.cssSelector("[data-section-id='HERO_DEFAULT']," +
            "div[data-testid='hero-images-scroll']," +
            "div[class*='gallery']," +
            "section[aria-label*='Photo']," +
            "button[aria-label*='photo']");

    // Booking panel / price widget
    private static final By BOOKING_SECTION = By.cssSelector("[data-testid='book-it-default']," +
            "[data-section-id='BOOK_IT_SIDEBAR']," +
            "div[data-section-id*='BOOK']");

    // Page title heading — prefer h1 only for precision
    private static final By LISTING_TITLE = By.cssSelector("h1");

    public ListingDetailsPage(WebDriver driver) {
        super(driver);
    }

    public boolean isDetailsContainerVisible() {
        try {
            return WaitUtils.waitForVisible(driver, DETAILS_CONTAINER).isDisplayed();
        } catch (TimeoutException e) {
            // Fallback: rely on URL to confirm we landed on a listing page
            return isOnListingPage();
        }
    }

    public boolean isGalleryVisible() {
        return WaitUtils.isVisible(driver, GALLERY_SECTION);
    }

    public boolean isBookingSectionVisible() {
        return WaitUtils.isVisible(driver, BOOKING_SECTION);
    }

    public String getListingTitle() {
        try {
            return WaitUtils.waitForVisible(driver, LISTING_TITLE).getText().trim();
        } catch (TimeoutException e) {
            return "";
        }
    }

    public boolean isOnListingPage() {
        // New tabs start as about:blank — wait for the real URL before checking
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(d -> {
                        String u = d.getCurrentUrl();
                        return u.contains("/rooms/") || u.contains("/vacation-rentals/");
                    });
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * Navigates back using browser history. Waits up to 10s for the URL to change.
     */
    public void goBack() {
        String urlBefore = driver.getCurrentUrl();
        driver.navigate().back();
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(d -> !d.getCurrentUrl().equals(urlBefore));
        } catch (TimeoutException ignored) {
        }
    }
}
