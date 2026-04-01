package airbnb.base;

import airbnb.drivers.DriverFactory;
import airbnb.pages.PopupHandler;
import airbnb.utils.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

/**
 * All test classes extend this. Handles driver lifecycle so each test
 * method gets a clean browser state (single-driver-per-test strategy).
 *
 * After navigation, PopupHandler.dismissAll() is called to clear any
 * cookie banners, locale dialogs, or sign-in prompts that would block tests.
 */
public class BaseTest {

    protected WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setUp(Method method) {
        System.out.println("[BaseTest] STARTING: "
                + getClass().getSimpleName() + "#" + method.getName());
        DriverFactory.initDriver(ConfigReader.getBrowser());
        driver = DriverFactory.getDriver();
        driver.get(ConfigReader.getBaseUrl());
        PopupHandler.dismissAll(driver);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(Method method) {
        System.out.println("[BaseTest] FINISHED: "
                + getClass().getSimpleName() + "#" + method.getName());
        if (driver != null) {
            DriverFactory.quitDriver();
        }
    }

    /**
     * Navigates to a path relative to the configured base URL.
     * Example: {@code navigateTo("/s/New-York--NY--United-States/homes")}
     */
    protected void navigateTo(String path) {
        String url = ConfigReader.getBaseUrl() + (path.startsWith("/") ? path : "/" + path);
        driver.get(url);
        PopupHandler.dismissAll(driver);
    }
}
