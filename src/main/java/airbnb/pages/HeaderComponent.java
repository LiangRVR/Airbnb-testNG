package airbnb.pages;

import airbnb.utils.WaitUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;

/**
 * Persistent top-of-page header component present on most Airbnb pages.
 * Selectors target stable aria/role attributes to reduce flakiness.
 */
public class HeaderComponent extends BasePage {

    // Main site logo / home link
    @FindBy(css = "a[aria-label='Airbnb homepage'], a[href='/']")
    private WebElement logoLink;

    // Primary navigation bar wrapper
    @FindBy(css = "header, div[data-testid='header']")
    private WebElement headerContainer;

    public HeaderComponent(WebDriver driver) {
        super(driver);
    }

    public boolean isHeaderVisible() {
        try {
            return WaitUtils.waitForVisible(driver,
                    By.cssSelector("header, div[data-testid='header']")).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void clickLogo() {
        WaitUtils.waitForClickable(driver, logoLink).click();
    }

    public boolean isLogoVisible() {
        try {
            return WaitUtils.waitForVisible(driver, logoLink).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

}
