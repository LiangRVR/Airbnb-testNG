package airbnb.tests;

import airbnb.base.AuthenticatedBaseTest;
import airbnb.constants.FrameworkConstants;
import airbnb.pages.ListingDetailsPage;
import airbnb.pages.PopupHandler;
import airbnb.pages.SearchPanelComponent;
import airbnb.pages.SearchResultsPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Set;

/**
 * End-to-end search flow tests executed inside an authenticated session.
 *
 * <p>
 * Each test starts with a fresh browser, navigates to the homepage, and
 * performs login before exercising the search-to-listing path. All assertions
 * target business-visible state (URL pattern, listing count, page reachability)
 * rather than ephemeral element visibility.
 *
 * <p>
 * Groups: {@code authenticated}, {@code e2e}, {@code functional}
 */
public class AuthenticatedSearchTests extends AuthenticatedBaseTest {

    private static final String DESTINATION = "Barcelona";

    private SearchPanelComponent searchPanel;
    private SearchResultsPage resultsPage;
    private ListingDetailsPage detailsPage;

    /**
     * Runs after the parent {@code @BeforeMethod} chain
     * (BaseTest.setUp → AuthenticatedBaseTest.setUpAuthComponents).
     * Initialises remaining page objects and logs in.
     */
    @BeforeMethod(alwaysRun = true)
    public void initPagesAndLogin() {
        searchPanel = new SearchPanelComponent(driver);
        resultsPage = new SearchResultsPage(driver);
        detailsPage = new ListingDetailsPage(driver);
        loginWithTestCredentials();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void doSearch(String destination) {
        searchPanel.typeDestination(destination);
        searchPanel.selectFirstSuggestion();
        searchPanel.clickSearchButton();
        Assert.assertTrue(resultsPage.waitUntilLoaded(),
                "Search for '" + destination + "' should load a results page. " +
                        "Actual URL: " + driver.getCurrentUrl());
    }

    /** Switches to the most recently opened tab if a new tab was opened. */
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

    @Test(groups = { "authenticated", "e2e",
            FrameworkConstants.GROUP_FUNCTIONAL }, description = "An authenticated user can search by destination and reach a results page")
    public void tc001_loggedInUserCanSearch() {
        doSearch(DESTINATION);
        Assert.assertTrue(resultsPage.isOnResultsPage(),
                "Authenticated search for '" + DESTINATION + "' should land on a results-page URL. " +
                        "Actual URL: " + driver.getCurrentUrl());
    }

    @Test(groups = { "authenticated", "e2e",
            FrameworkConstants.GROUP_FUNCTIONAL }, description = "Results page shows at least one listing card after an authenticated search")
    public void tc002_resultsPageContainsListings() {
        doSearch(DESTINATION);
        int count = resultsPage.getListingCount();
        Assert.assertTrue(count > 0,
                "Search results for '" + DESTINATION + "' should contain at least one listing card. " +
                        "Detected count: " + count);
    }

    @Test(groups = { "authenticated", "e2e",
            FrameworkConstants.GROUP_FUNCTIONAL }, description = "Authenticated user can open a listing detail page from the results page")
    public void tc003_canOpenListingFromResults() {
        doSearch(DESTINATION);
        String originalHandle = driver.getWindowHandle();
        resultsPage.clickFirstListing();
        String activeHandle = switchToNewTabIfOpened(originalHandle);
        if (!activeHandle.equals(originalHandle)) {
            driver.switchTo().window(activeHandle);
        }
        Assert.assertTrue(detailsPage.isOnListingPage(),
                "Clicking a result card should navigate to a listing URL (/rooms/). " +
                        "Actual URL: " + driver.getCurrentUrl());
    }

    @Test(groups = { "authenticated", "e2e",
            FrameworkConstants.GROUP_FUNCTIONAL }, description = "The destination keyword is preserved in the results page URL after search")
    public void tc004_destinationPreservedInResultsUrl() {
        doSearch(DESTINATION);
        String currentUrl = driver.getCurrentUrl().toLowerCase();
        // Airbnb encodes the city name in the URL path (e.g. /s/Barcelona--Spain/homes)
        boolean destinationReflected = currentUrl.contains("barcelona");
        Assert.assertTrue(destinationReflected,
                "The destination '" + DESTINATION + "' should appear in the results URL. " +
                        "Actual URL: " + driver.getCurrentUrl());
    }

    @Test(groups = { "authenticated", "e2e",
            FrameworkConstants.GROUP_FUNCTIONAL }, description = "Popup/modal interruptions are handled correctly in an authenticated search flow")
    public void tc005_popupHandlingInAuthFlow() {
        // Dismiss any modals that appeared after login before starting the search
        PopupHandler.dismissAll(driver);
        doSearch(DESTINATION);
        // Dismiss any modals that appeared on the results page
        PopupHandler.dismissAll(driver);

        Assert.assertTrue(resultsPage.isOnResultsPage(),
                "After popup dismissal the results page should remain accessible. " +
                        "Actual URL: " + driver.getCurrentUrl());
        Assert.assertTrue(resultsPage.getListingCount() > 0,
                "After popup dismissal at least one listing card should be visible.");
    }
}
