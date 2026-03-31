package airbnb.pages;

import airbnb.utils.ElementUtils;
import airbnb.utils.WaitUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

/**
 * Footer component available on the Airbnb homepage and many static pages.
 */
public class FooterComponent {

    private final WebDriver driver;

    // Outer footer wrapper — Airbnb renders a <footer> element
    private static final By FOOTER_LOCATOR = By.cssSelector("footer");

    // Support section heading — Airbnb renders <h3>Support</h3> in the footer
    private static final By SUPPORT_SECTION =
            By.xpath("//footer//h3[contains(., 'Support') or contains(., 'Help')]");

    // Company / about section — Airbnb renders <h3>Airbnb</h3> in the footer
    private static final By COMPANY_SECTION =
            By.xpath("//footer//h3[contains(., 'Airbnb')]");

    @FindBy(css = "footer")
    private WebElement footerElement;

    public FooterComponent(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public boolean isFooterVisible() {
        ElementUtils.scrollToBottom(driver);
        try {
            return WaitUtils.waitForVisible(driver, FOOTER_LOCATOR).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isSupportSectionVisible() {
        ElementUtils.scrollToBottom(driver);
        return WaitUtils.waitForPresence(driver, SUPPORT_SECTION);
    }

    public boolean isCompanySectionVisible() {
        ElementUtils.scrollToBottom(driver);
        return WaitUtils.waitForPresence(driver, COMPANY_SECTION);
    }

    /**
     * Finds all anchor tags inside the footer and returns them.
     * Caller can pick any stable one to click.
     */
    public List<WebElement> getFooterLinks() {
        ElementUtils.scrollToBottom(driver);
        return driver.findElements(By.cssSelector("footer a[href]"));
    }
}
