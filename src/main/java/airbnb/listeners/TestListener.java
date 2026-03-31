package airbnb.listeners;

import airbnb.drivers.DriverFactory;
import airbnb.utils.ScreenshotUtils;
import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG listener attached in testng.xml.
 * Captures a screenshot whenever a test method fails.
 */
public class TestListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        WebDriver driver = DriverFactory.getDriver();
        if (driver != null) {
            String testName = result.getTestClass().getRealClass().getSimpleName()
                    + "_" + result.getMethod().getMethodName();
            String path = ScreenshotUtils.captureScreenshot(driver, testName);
            if (!path.isEmpty()) {
                System.out.println("[TestListener] Screenshot for failed test [" + testName + "]: " + path);
            }
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("[TestListener] STARTING: "
                + result.getTestClass().getRealClass().getSimpleName()
                + "#" + result.getMethod().getMethodName());
    }
}
