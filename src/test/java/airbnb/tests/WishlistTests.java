package airbnb.tests;

import airbnb.base.AuthenticatedBaseTest;
import airbnb.constants.FrameworkConstants;
import airbnb.pages.ListingDetailsPage;
import airbnb.pages.PopupHandler;
import airbnb.pages.SearchResultsPage;
import airbnb.pages.WishlistPage;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Tests for the wishlist / heart-save feature using an authenticated session.
 *
 * <p>
 * Test coverage:
 * <ul>
 * <li>Save a listing from the results page — verify saved state</li>
 * <li>Save a listing from the listing details page — verify saved state</li>
 * <li>Toggle: save then unsave returns to unsaved state</li>
 * <li>A saved listing causes at least one wishlist to exist on /wishlists</li>
 * <li>Unsave / remove a listing — verify returned to unsaved state</li>
 * </ul>
 *
 * <h2>Cleanup</h2>
 * {@link #cleanUpWishlist} runs after every test. If a listing was saved
 * during the test it attempts to unsave it, so the account stays clean
 * between runs and tests remain isolated.
 *
 * <p>
 * Groups: {@code authenticated}, {@code wishlist}, {@code e2e},
 * {@code functional}
 */
public class WishlistTests extends AuthenticatedBaseTest {

    /**
     * A stable pre-built results URL used as the test pre-condition.
     * Using a direct URL avoids re-running the full search flow in every test.
     */
    private static final String RESULTS_URL = "/s/Lisbon--Portugal/homes";

    private SearchResultsPage resultsPage;
    private ListingDetailsPage detailsPage;
    private WishlistPage wishlistPage;

    /**
     * Flag used by {@link #cleanUpWishlist} to decide whether a cleanup attempt
     * is necessary. Each test that intentionally saves a listing sets this to
     * {@code true}; tests that clean up themselves reset it back to
     * {@code false}.
     */
    private boolean listingSavedDuringTest = false;

    /**
     * Runs after the parent @BeforeMethod chain. Initialises page objects,
     * resets the cleanup flag, and performs login.
     */
    @BeforeMethod(alwaysRun = true)
    public void initPagesAndLogin(Method method) {
        resultsPage = new SearchResultsPage(driver);
        detailsPage = new ListingDetailsPage(driver);
        wishlistPage = new WishlistPage(driver);
        listingSavedDuringTest = false;
        loginWithTestCredentials();
        System.out.println("[WishlistTests] Starting: " + method.getName());
    }

    /**
     * Runs after every test in this class (before {@link BaseTest#tearDown}
     * so the driver is still alive). If a listing was saved during the test,
     * navigates back to the results page and attempts to unsave it.
     */
    @AfterMethod(alwaysRun = true)
    public void cleanUpWishlist(Method method) {
        if (!listingSavedDuringTest)
            return;
        try {
            System.out.println("[WishlistTests] Cleanup: attempting to unsave listing after "
                    + method.getName());
            navigateTo(RESULTS_URL);
            PopupHandler.dismissAll(driver);
            if (wishlistPage.isFirstListingSaved()) {
                wishlistPage.clickFirstSaveButton();
                wishlistPage.confirmWishlistDialog();
                System.out.println("[WishlistTests] Cleanup: listing unsaved successfully.");
            }
        } catch (Exception e) {
            System.out.println("[WishlistTests] Cleanup warning (non-fatal): " + e.getMessage());
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void navigateToResults() {
        navigateTo(RESULTS_URL);
        PopupHandler.dismissAll(driver);
        Assert.assertTrue(resultsPage.waitUntilLoaded(),
                "Pre-condition: results page for Lisbon must load before wishlist tests. " +
                        "Actual URL: " + driver.getCurrentUrl());
    }

    private String switchToNewTabIfOpened(String originalHandle) {
        Set<String> handles = driver.getWindowHandles();
        if (handles.size() > 1) {
            return handles.stream()
                    .filter(h -> !h.equals(originalHandle))
                    .findFirst()
                    .orElse(originalHandle);
        }
        return originalHandle;
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test(groups = { "authenticated", "wishlist",
            FrameworkConstants.GROUP_FUNCTIONAL }, description = "Authenticated user can save a listing from the results page — heart reflects saved state")
    public void tc001_saveListingFromResultsPage() {
        navigateToResults();

        String labelBefore = wishlistPage.clickFirstSaveButton();
        listingSavedDuringTest = true;
        wishlistPage.confirmWishlistDialog();
        PopupHandler.dismissAll(driver);

        boolean nowSaved = wishlistPage.isFirstListingSaved();
        Assert.assertTrue(nowSaved,
                "After clicking the heart on the results page the listing should reflect " +
                        "a saved state (aria-pressed=true or aria-label containing 'Saved'). " +
                        "Label before click was: '" + labelBefore + "'.");
    }

    @Test(groups = { "authenticated", "wishlist",
            FrameworkConstants.GROUP_FUNCTIONAL }, description = "Authenticated user can save a listing from the listing details page — heart reflects saved state")
    public void tc002_saveListingFromDetailsPage() {
        navigateToResults();
        String originalHandle = driver.getWindowHandle();
        resultsPage.clickFirstListing();
        String activeHandle = switchToNewTabIfOpened(originalHandle);
        if (!activeHandle.equals(originalHandle)) {
            driver.switchTo().window(activeHandle);
        }
        Assert.assertTrue(detailsPage.isOnListingPage(),
                "Pre-condition: must be on a listing detail page before attempting to save. " +
                        "Actual URL: " + driver.getCurrentUrl());

        String labelBefore = wishlistPage.clickFirstSaveButton();
        listingSavedDuringTest = true;
        wishlistPage.confirmWishlistDialog();
        PopupHandler.dismissAll(driver);

        boolean nowSaved = wishlistPage.isFirstListingSaved();
        Assert.assertTrue(nowSaved,
                "After clicking the heart on the listing details page the listing should " +
                        "reflect a saved state. Label before click was: '" + labelBefore + "'.");
    }

    @Test(groups = { "authenticated", "wishlist",
            FrameworkConstants.GROUP_FUNCTIONAL }, description = "Save toggles correctly: save then unsave returns the listing to unsaved state")
    public void tc003_saveToggleUnsave() {
        navigateToResults();

        // Step 1 — save
        wishlistPage.clickFirstSaveButton();
        wishlistPage.confirmWishlistDialog();
        PopupHandler.dismissAll(driver);
        Assert.assertTrue(wishlistPage.isFirstListingSaved(),
                "After the first heart click the listing should be in saved state.");

        // Step 2 — unsave (toggle off)
        wishlistPage.clickFirstSaveButton();
        wishlistPage.confirmWishlistDialog();
        PopupHandler.dismissAll(driver);

        boolean nowUnsaved = !wishlistPage.isFirstListingSaved();
        Assert.assertTrue(nowUnsaved,
                "After the second heart click the listing should return to unsaved state " +
                        "(toggle behaviour: click again = remove).");
        // Listing was unsaved by the test itself — no cleanup needed
        listingSavedDuringTest = false;
    }

    @Test(groups = { "authenticated", "wishlist", "e2e",
            FrameworkConstants.GROUP_FUNCTIONAL }, description = "A saved listing causes at least one wishlist collection to appear on the /wishlists page")
    public void tc004_savedListingAppearsOnWishlistsPage() {
        navigateToResults();
        wishlistPage.clickFirstSaveButton();
        listingSavedDuringTest = true;
        wishlistPage.confirmWishlistDialog();
        PopupHandler.dismissAll(driver);

        // Navigate to the wishlists index
        wishlistPage.navigateToWishlists();
        PopupHandler.dismissAll(driver);

        Assert.assertTrue(wishlistPage.isOnWishlistsPage(),
                "Should have landed on the /wishlists page after navigation.");
        Assert.assertTrue(wishlistPage.hasAtLeastOneWishlist(),
                "At least one wishlist collection should exist after saving a listing. " +
                        "The /wishlists page showed 0 collections.");
    }

    @Test(groups = { "authenticated", "wishlist",
            FrameworkConstants.GROUP_FUNCTIONAL }, description = "User can unsave/remove a listing — heart returns to unsaved state after removal")
    public void tc005_unsaveListing() {
        navigateToResults();

        // Pre-condition: save the listing first
        wishlistPage.clickFirstSaveButton();
        wishlistPage.confirmWishlistDialog();
        PopupHandler.dismissAll(driver);
        Assert.assertTrue(wishlistPage.isFirstListingSaved(),
                "Pre-condition: the listing must be in saved state before the unsave test.");

        // Now unsave it
        wishlistPage.clickFirstSaveButton();
        wishlistPage.confirmWishlistDialog();
        PopupHandler.dismissAll(driver);

        boolean nowUnsaved = !wishlistPage.isFirstListingSaved();
        Assert.assertTrue(nowUnsaved,
                "After clicking the heart a second time the listing should be in unsaved state.");
        // Test cleaned itself up
        listingSavedDuringTest = false;
    }
}
