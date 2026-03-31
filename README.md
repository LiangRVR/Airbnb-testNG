# Airbnb Selenium TestNG Automation

Black-box functional test suite for the public Airbnb guest-facing website.
Tech stack: **Java 17 · Maven · Selenium WebDriver 4 · TestNG 7 · Page Object Model**

---

## Project Structure

```
src/
├── main/java/airbnb/
│   ├── base/           BaseTest.java
│   ├── constants/      FrameworkConstants.java
│   ├── drivers/        DriverFactory.java
│   ├── listeners/      TestListener.java
│   ├── pages/          HomePage, SearchPanelComponent, DatePickerComponent,
│   │                   GuestSelectorComponent, SearchResultsPage,
│   │                   FiltersComponent, ListingDetailsPage,
│   │                   HeaderComponent, FooterComponent
│   └── utils/          ConfigReader, WaitUtils, ScreenshotUtils, ElementUtils
└── test/
    ├── java/airbnb/tests/
    │   ├── HomePageSmokeTests.java
    │   ├── DestinationSearchTests.java
    │   ├── DatePickerTests.java
    │   ├── GuestSelectorTests.java
    │   ├── SearchResultsTests.java
    │   ├── FilterTests.java
    │   ├── ListingDetailsTests.java
    │   └── NavigationFooterTests.java
    └── resources/
        ├── config.properties
        └── testng.xml
```

---

## Configuration

Edit `src/test/resources/config.properties`:

| Key | Default | Notes |
|-----|---------|-------|
| `base.url` | `https://www.airbnb.com` | Target URL |
| `browser` | `chrome` | `chrome` or `firefox` |
| `headless` | `false` | Set `true` for CI |
| `explicit.wait` | `15` | Seconds for WebDriverWait |

---

## Running Tests

**Pre-requisites:** Java 17+, Maven 3.8+, Google Chrome installed.
WebDriverManager downloads the matching ChromeDriver automatically.

```bash
# Run the full suite (testng.xml)
mvn test

# Run a single test class
mvn test -Dtest=HomePageSmokeTests

# Run a group (smoke | regression | functional)
mvn test -Dgroups=smoke

# Run headless (no browser window)
mvn test -Dheadless=true

# Run with Firefox
mvn test -Dbrowser=firefox
```

Reports appear in `target/surefire-reports/`.
Failure screenshots are saved to `target/screenshots/`.

---

## Test Classes & Groups

| Class | Group(s) | Focus |
|-------|----------|-------|
| `HomePageSmokeTests` | smoke | Homepage loads, header, footer, title |
| `DestinationSearchTests` | functional | Search field, suggestions, clear |
| `DatePickerTests` | functional | Calendar open, day selection, past-date block |
| `GuestSelectorTests` | functional | Adults/children steppers, summary |
| `SearchResultsTests` | regression | Results page URL/structure |
| `FilterTests` | functional | Filters panel open/apply/clear |
| `ListingDetailsTests` | regression | Card click, details/gallery visible |
| `NavigationFooterTests` | smoke/functional | Logo, header, footer links |

---

## Likely Flaky Selectors — Verify First

1. **Search panel `data-testid`** — Airbnb A/B tests the homepage UI frequently.
   If `structured-search-input-field-query` is absent, inspect the live page and update `SearchPanelComponent`.

2. **Date-picker day cells** — Selector falls back to `td[class*='CalendarDay']`; verify selector in `DatePickerComponent` if tests timeout.

3. **Guest stepper `aria-label`** — Exact label text (`"Increase number of adults"`) may vary by locale.

4. **Filters `data-testid`** — `category-bar-filter-button` and `modal-container` are the primary targets; confirm on the live results page.

5. **Listing cards** — `a[href*='/rooms/']` is the most stable fallback; the primary `data-testid='card-container'` may change.

6. **New-tab on listing click** — Some Airbnb builds open listings in a new tab; `ListingDetailsTests` handles both cases but verify the window-handle logic on your Chrome version.

---

## Out of Scope

Login, signup, payment, booking completion, inbox/messages, CAPTCHA bypass, exact price assertions, exact result counts or ordering.
