package airbnb.tests;

import airbnb.base.BaseTest;
import airbnb.constants.FrameworkConstants;
import airbnb.pages.SearchPanelComponent;
import airbnb.pages.SearchResultsPage;
import airbnb.utils.ConfigReader;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Verifies the search-results page state after performing a destination search.
 * No exact count or ordering assertions — only structural/UI-state checks.
 */
public class SearchResultsTests extends BaseTest {

    private SearchPanelComponent searchPanel;
    private SearchResultsPage    resultsPage;

    @BeforeMethod(alwaysRun = true)
    public void initPages() {
        searchPanel = new SearchPanelComponent(driver);
        resultsPage = new SearchResultsPage(driver);
    }

    /** Helper: perform a quick search to reach the results page. */
    private void doSearch(String destination) {
        searchPanel.typeDestination(destination);
        searchPanel.selectFirstSuggestion();
        searchPanel.clickSearchButton();
    }

    @Test(groups = {FrameworkConstants.GROUP_REGRESSION},
          description = "A valid search navigates to a results / search page")
    public void tc001_validSearchReachesResultsPage() {
        doSearch("Miami");
        Assert.assertTrue(resultsPage.isOnResultsPage(),
                "URL should indicate a results/search page. Actual URL: " + driver.getCurrentUrl());
    }

    @Test(groups = {FrameworkConstants.GROUP_REGRESSION},
          description = "Results container element is visible on the results page")
    public void tc002_resultsContainerVisible() {
        doSearch("Amsterdam");
        Assert.assertTrue(resultsPage.isResultsContainerVisible(),
                "At least one listing card or results container should be visible");
    }

    @Test(groups = {FrameworkConstants.GROUP_REGRESSION},
          description = "URL changes to a search/results state after submitting search")
    public void tc003_urlChangesToResultsState() {
        String homeUrl = driver.getCurrentUrl();
        doSearch("Rome");
        String resultsUrl = driver.getCurrentUrl();
        Assert.assertNotEquals(resultsUrl, homeUrl,
                "URL should change after search. Was: " + homeUrl + " Now: " + resultsUrl);
        Assert.assertTrue(resultsUrl.contains("airbnb.com"),
                "Should remain on airbnb.com. Actual URL: " + resultsUrl);
    }

    @Test(groups = {FrameworkConstants.GROUP_REGRESSION},
          description = "Map region or results list region is present on the results page")
    public void tc004_mapOrResultsRegionVisible() {
        doSearch("Sydney");
        boolean hasResults = resultsPage.isResultsContainerVisible();
        boolean hasMap     = resultsPage.isMapVisible();
        Assert.assertTrue(hasResults || hasMap,
                "Either a results list or a map panel should be visible on the results page");
    }

    @Test(groups = {FrameworkConstants.GROUP_REGRESSION},
          description = "Pressing browser back from results lands on the homepage domain")
    public void tc005_backNavigationPreservesState() {
        doSearch("Berlin");
        Assert.assertTrue(resultsPage.isOnResultsPage(), "Must reach results before navigating back");
        driver.navigate().back();
        // After going back, we should still be on airbnb.com (homepage or prior page)
        Assert.assertTrue(driver.getCurrentUrl().contains("airbnb.com"),
                "Browser back should stay on airbnb.com. Actual URL: " + driver.getCurrentUrl());
    }
}
