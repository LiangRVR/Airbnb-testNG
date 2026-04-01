package airbnb.utils;

import airbnb.constants.FrameworkConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads configuration with the following priority order:
 * 1. Java system property (-Dkey=value on the command line)
 * 2. config.properties (src/test/resources/config.properties)
 * 3. Framework default (FrameworkConstants)
 *
 * This means {@code mvn test -Dheadless=true} and
 * {@code mvn test -Dbrowser=firefox}
 * work without touching any file.
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

    private ConfigReader() {
    }

    /**
     * Resolves a key: system property > config.properties > empty string.
     */
    public static String get(String key) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank())
            return sysProp.trim();
        return PROPS.getProperty(key, "");
    }

    public static String getBaseUrl() {
        String v = get(FrameworkConstants.BASE_URL_KEY);
        return v.isEmpty() ? FrameworkConstants.DEFAULT_BASE_URL : v;
    }

    public static String getBrowser() {
        String v = get(FrameworkConstants.BROWSER_KEY);
        return v.isEmpty() ? FrameworkConstants.DEFAULT_BROWSER : v.toLowerCase();
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(get(FrameworkConstants.HEADLESS_KEY).isEmpty()
                ? "false"
                : get(FrameworkConstants.HEADLESS_KEY));
    }

    public static int getExplicitWait() {
        try {
            String v = get(FrameworkConstants.EXPLICIT_WAIT_KEY);
            return v.isEmpty() ? FrameworkConstants.DEFAULT_EXPLICIT_WAIT : Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return FrameworkConstants.DEFAULT_EXPLICIT_WAIT;
        }
    }
}
