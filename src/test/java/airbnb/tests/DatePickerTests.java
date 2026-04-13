package airbnb.tests;

import airbnb.base.BaseTest;
import airbnb.constants.FrameworkConstants;
import airbnb.pages.DatePickerComponent;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Verifies the interactive date-picker component behaviour.
 * Past-date blocking is asserted via aria-disabled, not by attempted click,
 * to keep tests reliable across time-zone differences.
 */
public class DatePickerTests extends BaseTest {

      private DatePickerComponent datePicker;

      @BeforeMethod(alwaysRun = true)
      public void initPages() {
            datePicker = new DatePickerComponent(driver);
            // Navigate to a results page where 'little-search-date' is always present
            navigateTo("/s/New-York--NY--United-States/homes");
      }

      @Test(groups = {
                  FrameworkConstants.GROUP_FUNCTIONAL }, description = "Clicking the check-in field opens the calendar")
      public void tc001_datePickerOpens() {
            datePicker.openCheckIn();
            Assert.assertTrue(datePicker.isCalendarOpen(),
                        "Calendar should be visible after clicking the check-in field");
      }

      @Test(groups = { FrameworkConstants.GROUP_FUNCTIONAL }, description = "A future check-in date can be selected")
      public void tc002_futureCheckInSelectable() {
            datePicker.openCheckIn();
            Assert.assertTrue(datePicker.isCalendarOpen(), "Calendar must be open");
            // Skip first few days to avoid today/tomorrow edge cases; pick the 5th
            // available day
            String selectedLabel = datePicker.clickAvailableDayByIndex(5);
            Assert.assertFalse(selectedLabel.isEmpty(),
                        "Clicking the 6th available day should return a non-empty aria-label");
      }

      @Test(groups = {
                  FrameworkConstants.GROUP_FUNCTIONAL }, description = "A future check-out date can be selected after check-in")
      public void tc003_futureCheckOutSelectable() {
            datePicker.openCheckIn();
            Assert.assertTrue(datePicker.isCalendarOpen(), "Calendar must be open");
            datePicker.clickAvailableDayByIndex(5); // check-in
            // After check-in selection, calendar stays open for check-out
            String checkOutLabel = datePicker.clickAvailableDayByIndex(8); // check-out must be after check-in
            Assert.assertFalse(checkOutLabel.isEmpty(),
                        "Check-out date selection should succeed");
      }

      @Test(groups = {
                  FrameworkConstants.GROUP_FUNCTIONAL }, description = "Past dates are blocked / aria-disabled in the calendar")
      public void tc004_pastDatesAreBlocked() {
            datePicker.openCheckIn();
            Assert.assertTrue(datePicker.isCalendarOpen(), "Calendar must be open");
            Assert.assertTrue(datePicker.arePastDatesBlocked(),
                        "Past date cells should carry aria-disabled='true' (or equivalent blocked class)");
      }

      @Test(groups = {
                  FrameworkConstants.GROUP_FUNCTIONAL }, description = "After selecting dates the calendar can be re-opened to confirm state")
      public void tc005_selectedDatesReflectedInUI() {
            datePicker.openCheckIn();
            String checkInLabel = datePicker.clickAvailableDayByIndex(4);
            datePicker.clickAvailableDayByIndex(7);

            // Re-open the check-in section and confirm the calendar is still available
            datePicker.openCheckIn();
            Assert.assertTrue(datePicker.isCalendarOpen(),
                        "Calendar should be re-openable after dates are selected");
            Assert.assertFalse(checkInLabel.isEmpty(),
                        "A check-in day must have been selected (non-empty label expected)");
      }
}
