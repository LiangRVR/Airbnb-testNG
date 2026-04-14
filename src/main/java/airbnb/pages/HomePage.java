package airbnb.pages;

import airbnb.utils.WaitUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;

/**
 * Airbnb homepage — the entry point for all search flows.
 */
public class HomePage extends BasePage {

    private final SearchPanelComponent searchPanel;
    private final HeaderComponent header;
    private final FooterComponent footer;

    // Hero / main content area
    @FindBy(css = "main, #main, div[id='main-panel']")
    private WebElement mainContent;

    public HomePage(WebDriver driver) {
        super(driver);
        this.searchPanel = new SearchPanelComponent(driver);
        this.header = new HeaderComponent(driver);
        this.footer = new FooterComponent(driver);
    }

    // ---------- Getters for composed components ----------

    public SearchPanelComponent getSearchPanel() {
        return searchPanel;
    }

    public HeaderComponent getHeader() {
        return header;
    }

    public FooterComponent getFooter() {
        return footer;
    }

    // ---------- Page-level assertions ----------

    public boolean isLoaded() {
        try {
            WaitUtils.waitForPageReady(driver);

            boolean hasMain = WaitUtils.waitForPresence(driver,
                    By.cssSelector("main, #main, [role='main']"), 5);
            boolean hasHeader = header.isHeaderVisible();
            boolean hasSearch = searchPanel.isSearchPanelVisible();

            return (hasMain && hasHeader) || (hasHeader && hasSearch);
        } catch (WebDriverException e) {
            return header.isHeaderVisible() && searchPanel.isSearchPanelVisible();
        }
    }

    public String getTitle() {
        return driver.getTitle();
    }

}
