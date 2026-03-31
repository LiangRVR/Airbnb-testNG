package airbnb.utils;

import airbnb.constants.FrameworkConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads configuration from config.properties on the test classpath.
 * Falls back to hardcoded defaults if a key is missing.
 */
public final class ConfigReader {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException e) {
            System.err.println("[ConfigReader] Could not load config.properties: " + e.getMessage());
        }
    }

    private ConfigReader() {}

    public static String get(String key) {
        return PROPS.getProperty(key, "");
    }

    public static String getBaseUrl() {
        String v = PROPS.getProperty(FrameworkConstants.BASE_URL_KEY, "").trim();
        return v.isEmpty() ? FrameworkConstants.DEFAULT_BASE_URL : v;
    }

    public static String getBrowser() {
        String v = PROPS.getProperty(FrameworkConstants.BROWSER_KEY, "").trim();
        return v.isEmpty() ? FrameworkConstants.DEFAULT_BROWSER : v.toLowerCase();
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(PROPS.getProperty(FrameworkConstants.HEADLESS_KEY, "false").trim());
    }

    public static int getExplicitWait() {
        try {
            return Integer.parseInt(PROPS.getProperty(FrameworkConstants.EXPLICIT_WAIT_KEY, "").trim());
        } catch (NumberFormatException e) {
            return FrameworkConstants.DEFAULT_EXPLICIT_WAIT;
        }
    }
}
