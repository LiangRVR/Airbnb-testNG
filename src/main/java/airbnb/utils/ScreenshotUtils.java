package airbnb.utils;

import airbnb.constants.FrameworkConstants;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScreenshotUtils {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ScreenshotUtils() {}

    /**
     * Captures a screenshot and saves it to {@code target/screenshots/}.
     *
     * @param driver   active WebDriver instance
     * @param testName used as part of the file name
     * @return absolute path of the saved file, or empty string on failure
     */
    public static String captureScreenshot(WebDriver driver, String testName) {
        try {
            Path dir = Paths.get(FrameworkConstants.SCREENSHOT_DIR);
            Files.createDirectories(dir);

            String timestamp = LocalDateTime.now().format(FMT);
            String safeName  = testName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
            Path   target    = dir.resolve(safeName + "_" + timestamp + ".png");

            byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Files.write(target, bytes);

            System.out.println("[ScreenshotUtils] Saved: " + target.toAbsolutePath());
            return target.toAbsolutePath().toString();
        } catch (IOException e) {
            System.err.println("[ScreenshotUtils] Failed to save screenshot: " + e.getMessage());
            return "";
        }
    }
}
