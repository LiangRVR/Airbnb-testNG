package airbnb.listeners;

import airbnb.drivers.DriverFactory;
import airbnb.utils.ScreenshotUtils;
import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG listener attached in testng.xml.
 * Captures a screenshot and logs diagnostic context whenever a test fails.
 */
public class TestListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        WebDriver driver = DriverFactory.getDriver();
        String testName = result.getTestClass().getRealClass().getSimpleName()
                + "_" + result.getMethod().getMethodName();

        if (driver != null) {
            // Diagnostic context — URL and title help pinpoint where the failure occurred
            try {
                System.out.println("[TestListener] FAILURE URL   : " + driver.getCurrentUrl());
                System.out.println("[TestListener] FAILURE TITLE : " + driver.getTitle());
            } catch (Exception ignored) {
            }

            String path = ScreenshotUtils.captureScreenshot(driver, testName);
            if (!path.isEmpty()) {
                System.out.println("[TestListener] Screenshot saved: " + path);
            }
        }

        // Truncated exception message for console readability
        Throwable cause = result.getThrowable();
        if (cause != null) {
            String msg = cause.getMessage();
            if (msg != null && msg.length() > 200)
                msg = msg.substring(0, 200) + "…";
            System.out.println("[TestListener] FAILURE CAUSE : " + msg);
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("[TestListener] STARTING: "
                + result.getTestClass().getRealClass().getSimpleName()
                + "#" + result.getMethod().getMethodName());
    }
}
