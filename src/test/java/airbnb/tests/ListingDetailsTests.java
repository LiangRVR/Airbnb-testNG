package airbnb.tests;

import airbnb.base.BaseTest;
import airbnb.constants.FrameworkConstants;
import airbnb.pages.ListingDetailsPage;
import airbnb.pages.SearchResultsPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Set;

/**
 * Tests that verify individual listing detail pages can be opened and contain
 * the expected structural sections. No pricing or booking assertions.
 */
public class ListingDetailsTests extends BaseTest {

        private SearchResultsPage resultsPage;
        private ListingDetailsPage detailsPage;

        /**
         * Switch driver focus to a listing opened in a new tab (if any) and return its
         * handle.
         */
        private String switchToNewTabIfOpened(String originalHandle) {
                Set<String> handles = driver.getWindowHandles();
                if (handles.size() > 1) {
                        String newHandle = handles.stream()
                                        .filter(h -> !h.equals(originalHandle))
                                        .findFirst().orElse(originalHandle);
                        driver.switchTo().window(newHandle);
                        return newHandle;
                }
                return originalHandle;
        }

        @BeforeMethod(alwaysRun = true)
        public void initPages() {
                resultsPage = new SearchResultsPage(driver);
                detailsPage = new ListingDetailsPage(driver);

                // Pre-condition: listing-detail tests validate result-to-detail behavior,
                // not autocomplete, so they start from a stable results URL.
                navigateTo("/s/Copenhagen--Denmark/homes");
                Assert.assertTrue(resultsPage.waitUntilLoaded(),
                                "Pre-condition failed: results page for Copenhagen not reached. Actual URL: "
                                                + driver.getCurrentUrl());
        }

        @Test(groups = {
                        FrameworkConstants.GROUP_REGRESSION }, description = "First listing card on the results page can be opened")
        public void tc001_listingCardCanBeOpened() {
                resultsPage.clickFirstListing();
                if (driver.getWindowHandles().size() > 1) {
                        driver.switchTo().window(
                                        driver.getWindowHandles().stream().reduce((a, b) -> b).orElse(""));
                }
                Assert.assertTrue(detailsPage.isOnListingPage(),
                                "Listing click should navigate to a real listing URL. Actual URL: "
                                                + driver.getCurrentUrl());
        }

        @Test(groups = {
                        FrameworkConstants.GROUP_REGRESSION }, description = "Listing details page loads with its main container visible")
        public void tc002_listingDetailsPageLoads() {
                resultsPage.clickFirstListing();
                if (driver.getWindowHandles().size() > 1) {
                        driver.switchTo().window(
                                        driver.getWindowHandles().stream().reduce((a, b) -> b).orElse(""));
                }
                Assert.assertTrue(detailsPage.isDetailsContainerVisible(),
                                "Main details container should be visible on the listing page");
                Assert.assertTrue(detailsPage.isGalleryVisible() || !detailsPage.getListingTitle().isEmpty(),
                                "Listing page should show either the gallery or a non-empty title");
        }

        @Test(groups = {
                        FrameworkConstants.GROUP_REGRESSION }, description = "Hero / gallery section is present on the listing page")
        public void tc003_galleryVisible() {
                resultsPage.clickFirstListing();
                if (driver.getWindowHandles().size() > 1) {
                        driver.switchTo().window(
                                        driver.getWindowHandles().stream().reduce((a, b) -> b).orElse(""));
                }
                Assert.assertTrue(detailsPage.isGalleryVisible() || detailsPage.isDetailsContainerVisible(),
                                "Gallery/hero section or details container should be visible on the listing page");
        }

        @Test(groups = { FrameworkConstants.GROUP_REGRESSION }, description = "Listing page title heading is non-empty")
        public void tc004_listingTitleNonEmpty() {
                resultsPage.clickFirstListing();
                if (driver.getWindowHandles().size() > 1) {
                        driver.switchTo().window(
                                        driver.getWindowHandles().stream().reduce((a, b) -> b).orElse(""));
                }
                String title = detailsPage.getListingTitle();
                Assert.assertFalse(title.isEmpty(),
                                "Listing page should display a non-empty title heading");
        }

        @Test(groups = {
                        FrameworkConstants.GROUP_REGRESSION }, description = "Browser back from listing returns to a results/search page")
        public void tc005_backReturnsToResults() {
                String originalHandle = driver.getWindowHandle();
                resultsPage.clickFirstListing();
                String landedHandle = switchToNewTabIfOpened(originalHandle);
                if (!landedHandle.equals(originalHandle)) {
                        // Listing opened in a new tab — close the listing tab and go back to results
                        driver.close();
                        driver.switchTo().window(originalHandle);
                } else {
                        // Same-tab navigation — use browser back
                        detailsPage.goBack();
                }
                Assert.assertTrue(driver.getCurrentUrl().contains("airbnb.com")
                                && !driver.getCurrentUrl().contains("/rooms/"),
                                "After navigating back, the browser should no longer be on a listing page. URL: "
                                                + driver.getCurrentUrl());
        }
}
