package airbnb.tests;

import airbnb.base.BaseTest;
import airbnb.constants.FrameworkConstants;
import airbnb.pages.FooterComponent;
import airbnb.pages.HeaderComponent;
import airbnb.pages.HomePage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Smoke tests — quick pass/fail checks that the homepage loads correctly.
 */
public class HomePageSmokeTests extends BaseTest {

    private HomePage     homePage;
    private HeaderComponent header;
    private FooterComponent footer;

    @BeforeMethod(alwaysRun = true)
    public void initPages() {
        homePage = new HomePage(driver);
        header   = new HeaderComponent(driver);
        footer   = new FooterComponent(driver);
    }

    @Test(groups = {FrameworkConstants.GROUP_SMOKE},
          description = "Homepage main content area loads and is visible")
    public void tc001_homepageLoads() {
        Assert.assertTrue(homePage.isLoaded(),
                "Homepage <main> content area should be visible after navigation");
    }

    @Test(groups = {FrameworkConstants.GROUP_SMOKE},
          description = "Search entry / search bar is visible on homepage")
    public void tc002_searchBarVisible() {
        Assert.assertTrue(homePage.getSearchPanel().isSearchPanelVisible(),
                "Search panel / Where input should be visible on the homepage");
    }

    @Test(groups = {FrameworkConstants.GROUP_SMOKE},
          description = "Site header is visible on homepage")
    public void tc003_headerVisible() {
        Assert.assertTrue(header.isHeaderVisible(),
                "Site header should be visible on the homepage");
    }

    @Test(groups = {FrameworkConstants.GROUP_SMOKE},
          description = "Site footer is visible after scrolling to the bottom")
    public void tc004_footerVisible() {
        Assert.assertTrue(footer.isFooterVisible(),
                "Site footer should be visible after scrolling to the bottom");
    }

    @Test(groups = {FrameworkConstants.GROUP_SMOKE},
          description = "Page title contains the Airbnb brand name")
    public void tc005_titleContainsBrand() {
        String title = driver.getTitle().toLowerCase();
        Assert.assertTrue(title.contains("airbnb"),
                "Page title should contain 'airbnb'. Actual title: " + driver.getTitle());
    }
}
