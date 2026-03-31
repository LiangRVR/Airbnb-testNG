package airbnb.constants;

public final class FrameworkConstants {

    private FrameworkConstants() {}

    // Config keys
    public static final String BASE_URL_KEY       = "base.url";
    public static final String BROWSER_KEY        = "browser";
    public static final String IMPLICIT_WAIT_KEY  = "implicit.wait";
    public static final String EXPLICIT_WAIT_KEY  = "explicit.wait";
    public static final String HEADLESS_KEY       = "headless";

    // Defaults
    public static final String DEFAULT_BASE_URL   = "https://www.airbnb.com";
    public static final String DEFAULT_BROWSER    = "chrome";
    public static final int    DEFAULT_EXPLICIT_WAIT = 15;
    public static final int    DEFAULT_IMPLICIT_WAIT = 0;

    // Screenshot directory
    public static final String SCREENSHOT_DIR     = "target/screenshots/";

    // Test groups
    public static final String GROUP_SMOKE        = "smoke";
    public static final String GROUP_REGRESSION   = "regression";
    public static final String GROUP_FUNCTIONAL   = "functional";
}
