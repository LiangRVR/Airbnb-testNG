package airbnb.pages;

import airbnb.utils.ElementUtils;
import airbnb.utils.WaitUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * Airbnb homepage — the entry point for all search flows.
 */
public class HomePage {

    private final WebDriver driver;

    private final SearchPanelComponent searchPanel;
    private final HeaderComponent      header;
    private final FooterComponent      footer;

    // Hero / main content area
    @FindBy(css = "main, #main, div[id='main-panel']")
    private WebElement mainContent;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.searchPanel = new SearchPanelComponent(driver);
        this.header      = new HeaderComponent(driver);
        this.footer      = new FooterComponent(driver);
        PageFactory.initElements(driver, this);
    }

    // ---------- Getters for composed components ----------

    public SearchPanelComponent getSearchPanel() { return searchPanel; }
    public HeaderComponent      getHeader()      { return header; }
    public FooterComponent      getFooter()      { return footer; }

    // ---------- Page-level assertions ----------

    public boolean isLoaded() {
        try {
            return WaitUtils.waitForVisible(driver,
                    By.cssSelector("main, #main, [role='main']")).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    public String getTitle() {
        return driver.getTitle();
    }

    public boolean isTitleContains(String keyword) {
        return driver.getTitle().toLowerCase().contains(keyword.toLowerCase());
    }
}
