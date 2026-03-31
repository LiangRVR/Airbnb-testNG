package airbnb.drivers;

import airbnb.utils.ConfigReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 * Creates and manages a thread-local WebDriver instance.
 * Supports chrome (default) and firefox.
 */
public final class DriverFactory {

    private static final ThreadLocal<WebDriver> DRIVER_TL = new ThreadLocal<>();

    private DriverFactory() {}

    public static WebDriver getDriver() {
        if (DRIVER_TL.get() == null) {
            initDriver(ConfigReader.getBrowser());
        }
        return DRIVER_TL.get();
    }

    public static void initDriver(String browser) {
        WebDriver driver;
        boolean headless = ConfigReader.isHeadless();

        switch (browser.toLowerCase()) {
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions ffOpts = new FirefoxOptions();
                if (headless) ffOpts.addArguments("--headless");
                driver = new FirefoxDriver(ffOpts);
            }
            default -> {
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOpts = new ChromeOptions();
                chromeOpts.addArguments("--start-maximized");
                chromeOpts.addArguments("--disable-notifications");
                chromeOpts.addArguments("--disable-popup-blocking");
                // Suppress "Chrome is being controlled by automated software" bar
                chromeOpts.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
                chromeOpts.setExperimentalOption("useAutomationExtension", false);
                if (headless) {
                    chromeOpts.addArguments("--headless=new");
                    chromeOpts.addArguments("--window-size=1920,1080");
                }
                driver = new ChromeDriver(chromeOpts);
            }
        }

        DRIVER_TL.set(driver);
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER_TL.get();
        if (driver != null) {
            driver.quit();
            DRIVER_TL.remove();
        }
    }
}
