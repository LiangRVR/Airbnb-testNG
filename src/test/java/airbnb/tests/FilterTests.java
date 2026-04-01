package airbnb.tests;

import airbnb.base.BaseTest;
import airbnb.constants.FrameworkConstants;
import airbnb.pages.FiltersComponent;
import airbnb.pages.SearchResultsPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for the filters panel accessible from the search-results page.
 * Assertions check UI-state changes, not exact result counts.
 */
public class FilterTests extends BaseTest {

      private FiltersComponent filters;
      private SearchResultsPage resultsPage;

      @BeforeMethod(alwaysRun = true)
      public void initPages() {
            resultsPage = new SearchResultsPage(driver);
            filters = new FiltersComponent(driver);

            // Direct-state setup: filter tests validate filter behaviour, not search-box
            // autocomplete.
            navigateTo("/s/Lisbon--Portugal/homes");
            Assert.assertTrue(resultsPage.isOnResultsPage(),
                        "Pre-condition failed: could not reach results page for Lisbon");
      }

      @Test(groups = {
                  FrameworkConstants.GROUP_FUNCTIONAL }, description = "Filters button is visible on the results page")
      public void tc001_filtersButtonVisible() {
            Assert.assertTrue(filters.isFilterButtonVisible(),
                        "Filters button should be visible on the search results page");
      }

      @Test(groups = {
                  FrameworkConstants.GROUP_FUNCTIONAL }, description = "Clicking Filters button opens the filters panel")
      public void tc002_filtersPanelOpens() {
            filters.openFilters();
            Assert.assertTrue(filters.isFiltersPanelOpen(),
                        "Filters panel/dialog should be visible after clicking Filters button");
      }

      @Test(groups = {
                  FrameworkConstants.GROUP_FUNCTIONAL }, description = "A stable filter (Entire home) can be selected in the panel")
      public void tc003_stableFilterCanBeSelected() {
            filters.openFilters();
            Assert.assertTrue(filters.isFiltersPanelOpen(), "Panel must be open");
            String applyButtonTextBefore = filters.getApplyButtonText();
            filters.selectEntireHomeFilter();
            String applyButtonTextAfter = filters.getApplyButtonText();

            boolean selectionReflectedInUi = filters.isEntireHomeSelected()
                        || !filters.isFiltersPanelOpen()
                        || !applyButtonTextBefore.equals(applyButtonTextAfter);

            Assert.assertTrue(selectionReflectedInUi,
                        "Selecting Entire home should change the filter UI state (selected chip, panel close, or apply-button text change)");
      }

      @Test(groups = {
                  FrameworkConstants.GROUP_FUNCTIONAL }, description = "Applying a filter dismisses the panel and returns to results")
      public void tc004_applyFilterUpdatesState() {
            filters.openFilters();
            filters.selectEntireHomeFilter();
            filters.applyFilters();
            Assert.assertTrue(resultsPage.isOnResultsPage(),
                        "Should remain on a results page after applying filters. URL: " + driver.getCurrentUrl());
            Assert.assertTrue(resultsPage.isResultsContainerVisible(),
                        "Results should still be visible after applying a filter");
      }

      @Test(groups = {
                  FrameworkConstants.GROUP_FUNCTIONAL }, description = "Clear/reset option is available inside the filters panel")
      public void tc005_clearResetIsAvailable() {
            filters.openFilters();
            filters.selectEntireHomeFilter();
            filters.clearAllFilters();
            Assert.assertFalse(filters.isEntireHomeSelected(),
                        "Entire home filter should no longer be selected after Clear All");
      }
}
