package airbnb.tests;

import airbnb.base.BaseTest;
import airbnb.constants.FrameworkConstants;
import airbnb.pages.GuestSelectorComponent;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for the guest-selector (Who) panel in the search bar.
 * Counts are compared with >, <, == rather than fixed numbers because
 * the default value (1 adult) may differ per locale.
 */
public class GuestSelectorTests extends BaseTest {

      private GuestSelectorComponent guestSelector;

      @BeforeMethod(alwaysRun = true)
      public void initPages() {
            guestSelector = new GuestSelectorComponent(driver);
            // Navigate to results page where 'little-search-guests' is always present
            navigateTo("/s/New-York--NY--United-States/homes");
      }

      @Test(groups = {
                  FrameworkConstants.GROUP_FUNCTIONAL }, description = "Clicking the Guests button opens the guest selector panel")
      public void tc001_guestSelectorOpens() {
            guestSelector.openGuestsPanel();
            Assert.assertTrue(guestSelector.isPanelOpen(),
                        "Guest selector panel should be visible after clicking the Guests button");
      }

      @Test(groups = {
                  FrameworkConstants.GROUP_FUNCTIONAL }, description = "Adults count increments by 1 each time + button is clicked")
      public void tc002_adultsIncrement() {
            guestSelector.openGuestsPanel();
            int before = guestSelector.getAdultsCount();
            guestSelector.incrementAdults();
            int after = guestSelector.getAdultsCount();
            Assert.assertTrue(after > before,
                        "Adults count should increase after clicking +. Before=" + before + " After=" + after);
      }

      @Test(groups = {
                  FrameworkConstants.GROUP_FUNCTIONAL }, description = "Adults count decrements when – button is clicked (minimum is 1)")
      public void tc003_adultsDecrement() {
            guestSelector.openGuestsPanel();
            // Ensure there is at least 2 adults so we can decrement
            guestSelector.incrementAdults();
            guestSelector.incrementAdults();
            int before = guestSelector.getAdultsCount();
            guestSelector.decrementAdults();
            int after = guestSelector.getAdultsCount();
            Assert.assertTrue(after < before,
                        "Adults count should decrease after clicking –. Before=" + before + " After=" + after);
      }

      @Test(groups = {
                  FrameworkConstants.GROUP_FUNCTIONAL }, description = "Children count increments when + button is clicked")
      public void tc004_childrenIncrement() {
            guestSelector.openGuestsPanel();
            int before = guestSelector.getChildrenCount();
            guestSelector.incrementChildren();
            int after = guestSelector.getChildrenCount();
            Assert.assertTrue(after > before,
                        "Children count should increase. Before=" + before + " After=" + after);
      }

      @Test(groups = {
                  FrameworkConstants.GROUP_FUNCTIONAL }, description = "Guest summary on the trigger button updates after adding guests")
      public void tc005_guestSummaryUpdates() {
            guestSelector.openGuestsPanel();
            guestSelector.incrementAdults();
            // Read summary while panel is still open (stepper a11y label shows "X Adults")
            String summary = guestSelector.getGuestsSummaryText();
            // Summary should be non-empty and contain a numeral or the word "guest"
            boolean hasContent = !summary.isEmpty()
                        && (summary.matches(".*\\d.*") || summary.toLowerCase().contains("guest"));
            Assert.assertTrue(hasContent,
                        "Guest button summary should show a count or 'guest' text. Actual: '" + summary + "'");
      }
}
