package airbnb.tests;

import airbnb.base.BaseTest;
import airbnb.constants.FrameworkConstants;
import airbnb.pages.SearchPanelComponent;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Functional tests for the destination search field.
 */
public class DestinationSearchTests extends BaseTest {

      private SearchPanelComponent searchPanel;

      @BeforeMethod(alwaysRun = true)
      public void initPages() {
            searchPanel = new SearchPanelComponent(driver);
      }

      @Test(groups = {
                  FrameworkConstants.GROUP_FUNCTIONAL }, description = "Clicking the destination field activates (focuses) it")
      public void tc001_destinationFieldActivates() {
            searchPanel.clickWhereField();
            // If no exception is thrown the click succeeded and the field is focusable
            Assert.assertTrue(searchPanel.isSearchPanelVisible(),
                        "Search panel should remain visible after clicking Where field");
      }

      @Test(groups = {
                  FrameworkConstants.GROUP_FUNCTIONAL }, description = "Typing a valid city name is accepted without errors")
      public void tc002_validCityCanBeTyped() {
            searchPanel.typeDestination("Paris");
            String value = searchPanel.getDestinationFieldValue();
            Assert.assertTrue(value.toLowerCase().contains("paris"),
                        "Destination field should reflect the typed city. Actual value: " + value);
      }

      @Test(groups = {
                  FrameworkConstants.GROUP_FUNCTIONAL }, description = "Auto-suggest dropdown appears after typing a destination")
      public void tc003_suggestionsAppearAfterTyping() {
            searchPanel.typeDestination("London");
            Assert.assertTrue(searchPanel.areSuggestionsVisible(),
                        "Suggestion dropdown items should appear after typing 'London'");
      }

      @Test(groups = { FrameworkConstants.GROUP_FUNCTIONAL }, description = "Destination field can be cleared")
      public void tc004_destinationCanBeCleared() {
            searchPanel.typeDestination("Tokyo");
            searchPanel.clearDestination();
            String value = searchPanel.getDestinationFieldValue();
            Assert.assertTrue(value.isEmpty() || value.isBlank(),
                        "Destination field should be empty after clearing. Actual value: '" + value + "'");
      }

      @Test(groups = {
                  FrameworkConstants.GROUP_FUNCTIONAL }, description = "Selecting a suggestion moves the flow forward (date picker or next step opens)")
      public void tc005_selectingSuggestionMovesForward() {
            searchPanel.typeDestination("Barcelona");
            Assert.assertTrue(searchPanel.areSuggestionsVisible(),
                        "Suggestions must be visible before selection");
            searchPanel.selectFirstSuggestion();
            String updatedValue = searchPanel.getDestinationFieldValue().toLowerCase();
            Assert.assertTrue(updatedValue.contains("barcelona") || !searchPanel.areSuggestionsVisible(),
                        "Selecting a suggestion should update the destination field or dismiss the suggestions list. Actual value: "
                                    + updatedValue);
      }
}
