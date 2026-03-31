package airbnb.tests;

import airbnb.base.BaseTest;
import airbnb.constants.FrameworkConstants;
import airbnb.pages.FooterComponent;
import airbnb.pages.HeaderComponent;
import airbnb.utils.ConfigReader;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Tests for header navigation and footer links.
 * Assertions are structural (elements visible, URLs valid) — no login required.
 */
public class NavigationFooterTests extends BaseTest {

    private HeaderComponent header;
    private FooterComponent footer;

    @BeforeMethod(alwaysRun = true)
    public void initPages() {
        header = new HeaderComponent(driver);
        footer = new FooterComponent(driver);
    }

    @Test(groups = {FrameworkConstants.GROUP_SMOKE},
          description = "Clicking the Airbnb logo navigates back to the homepage")
    public void tc001_logoClickReturnsHome() {
        // Navigate away first
        driver.get(ConfigReader.getBaseUrl() + "/help");
        Assert.assertTrue(header.isLogoVisible(), "Logo should be visible on /help page");
        header.clickLogo();
        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("airbnb.com"),
                "Logo click should navigate to airbnb.com. Actual URL: " + url);
    }

    @Test(groups = {FrameworkConstants.GROUP_SMOKE},
          description = "Site header is visible and the logo element is present")
    public void tc002_headerNavigationVisible() {
        Assert.assertTrue(header.isHeaderVisible(),
                "Header container should be visible on the homepage");
        Assert.assertTrue(header.isLogoVisible(),
                "Airbnb logo/home link should be present in the header");
    }

    @Test(groups = {FrameworkConstants.GROUP_FUNCTIONAL},
          description = "Support / Help section is visible in the footer")
    public void tc003_supportFooterSectionVisible() {
        Assert.assertTrue(footer.isFooterVisible(),
                "Footer should be visible after scrolling");
        Assert.assertTrue(footer.isSupportSectionVisible(),
                "Support/Help section heading should be visible in the footer");
    }

    @Test(groups = {FrameworkConstants.GROUP_FUNCTIONAL},
          description = "Company / About Airbnb section is visible in the footer")
    public void tc004_companyFooterSectionVisible() {
        Assert.assertTrue(footer.isFooterVisible(),
                "Footer should be visible after scrolling");
        Assert.assertTrue(footer.isCompanySectionVisible(),
                "Company/About section heading should be visible in the footer");
    }

    @Test(groups = {FrameworkConstants.GROUP_FUNCTIONAL},
          description = "A footer link opens a valid public Airbnb destination (non-empty URL)")
    public void tc005_footerLinkOpensValidDestination() {
        footer.isFooterVisible(); // ensures scroll to bottom
        List<WebElement> links = footer.getFooterLinks();
        Assert.assertFalse(links.isEmpty(), "Footer should contain at least one anchor link");

        // Find the first link with a meaningful href (not # or javascript:)
        String targetHref = "";
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href != null && !href.isBlank()
                    && !href.startsWith("#")
                    && !href.startsWith("javascript")
                    && href.contains("airbnb.com")) {
                targetHref = href;
                break;
            }
        }

        Assert.assertFalse(targetHref.isEmpty(),
                "Should find at least one footer link pointing to airbnb.com");

        driver.get(targetHref);
        Assert.assertTrue(driver.getCurrentUrl().contains("airbnb.com"),
                "Footer link should open an airbnb.com page. URL: " + driver.getCurrentUrl());
    }
}
