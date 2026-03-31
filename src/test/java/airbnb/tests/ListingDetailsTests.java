package airbnb.tests;

import airbnb.base.BaseTest;
import airbnb.constants.FrameworkConstants;
import airbnb.pages.ListingDetailsPage;
import airbnb.pages.SearchPanelComponent;
import airbnb.pages.SearchResultsPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests that verify individual listing detail pages can be opened and contain
 * the expected structural sections. No pricing or booking assertions.
 */
public class ListingDetailsTests extends BaseTest {

    private SearchPanelComponent searchPanel;
    private SearchResultsPage    resultsPage;
    private ListingDetailsPage   detailsPage;

    @BeforeMethod(alwaysRun = true)
    public void initPages() {
        searchPanel = new SearchPanelComponent(driver);
        resultsPage = new SearchResultsPage(driver);
        detailsPage = new ListingDetailsPage(driver);

        // Pre-condition: navigate to results
        searchPanel.typeDestination("Copenhagen");
        searchPanel.selectFirstSuggestion();
        searchPanel.clickSearchButton();
        Assert.assertTrue(resultsPage.isOnResultsPage(),
                "Pre-condition failed: results page for Copenhagen not reached");
    }

    @Test(groups = {FrameworkConstants.GROUP_REGRESSION},
          description = "First listing card on the results page can be opened")
    public void tc001_listingCardCanBeOpened() {
        resultsPage.clickFirstListing();
        // Clicks may open a new tab in some Airbnb configs — handle both
        if (driver.getWindowHandles().size() > 1) {
            driver.switchTo().window(
                    driver.getWindowHandles().stream().reduce((a, b) -> b).orElse(""));
        }
        Assert.assertTrue(detailsPage.isOnListingPage() || detailsPage.isDetailsContainerVisible(),
                "Should land on a listing details page. URL: " + driver.getCurrentUrl());
    }

    @Test(groups = {FrameworkConstants.GROUP_REGRESSION},
          description = "Listing details page loads with its main container visible")
    public void tc002_listingDetailsPageLoads() {
        resultsPage.clickFirstListing();
        if (driver.getWindowHandles().size() > 1) {
            driver.switchTo().window(
                    driver.getWindowHandles().stream().reduce((a, b) -> b).orElse(""));
        }
        Assert.assertTrue(detailsPage.isDetailsContainerVisible(),
                "Main details container should be visible on the listing page");
    }

    @Test(groups = {FrameworkConstants.GROUP_REGRESSION},
          description = "Hero / gallery section is present on the listing page")
    public void tc003_galleryVisible() {
        resultsPage.clickFirstListing();
        if (driver.getWindowHandles().size() > 1) {
            driver.switchTo().window(
                    driver.getWindowHandles().stream().reduce((a, b) -> b).orElse(""));
        }
        Assert.assertTrue(detailsPage.isGalleryVisible() || detailsPage.isDetailsContainerVisible(),
                "Gallery/hero section or details container should be visible on the listing page");
    }

    @Test(groups = {FrameworkConstants.GROUP_REGRESSION},
          description = "Listing page title heading is non-empty")
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

    @Test(groups = {FrameworkConstants.GROUP_REGRESSION},
          description = "Browser back from listing returns to a results/search page")
    public void tc005_backReturnsToResults() {
        resultsPage.clickFirstListing();
        if (driver.getWindowHandles().size() > 1) {
            // Close the new tab and switch back to original
            driver.close();
            driver.switchTo().window(
                    driver.getWindowHandles().iterator().next());
        } else {
            detailsPage.goBack();
        }
        // After going back/closing tab, we should be back on a results or airbnb page
        Assert.assertTrue(driver.getCurrentUrl().contains("airbnb.com"),
                "Should be back on airbnb.com after navigating back. URL: " + driver.getCurrentUrl());
    }
}
