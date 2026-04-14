# Airbnb Selenium TestNG Automation

Maintainable black-box functional test suite for the public Airbnb guest-facing website,
extended with an authenticated user-journey layer covering login, search, listing interaction,
wishlist save/remove, and logout.

Tech stack: **Java 17 · Maven · Selenium WebDriver 4 · TestNG 7 · Page Object Model**

---

## Project Structure

```
src/
├── main/java/airbnb/
│   ├── base/           BaseTest.java
│   │                   AuthenticatedBaseTest.java
│   ├── constants/      FrameworkConstants.java
│   ├── drivers/        DriverFactory.java
│   ├── listeners/      TestListener.java
│   ├── pages/          HomePage, SearchPanelComponent, DatePickerComponent,
│   │                   GuestSelectorComponent, SearchResultsPage,
│   │                   FiltersComponent, ListingDetailsPage,
│   │                   HeaderComponent, FooterComponent,
│   │                   AccountMenuComponent, LoginPage,
│   │                   WishlistPage
│   └── utils/          ConfigReader, WaitUtils, ScreenshotUtils, ElementUtils,
│                       CredentialHelper
└── test/
    ├── java/airbnb/tests/
    │   ├── HomePageSmokeTests.java          (public browsing)
    │   ├── DestinationSearchTests.java      (public browsing)
    │   ├── DatePickerTests.java             (public browsing)
    │   ├── GuestSelectorTests.java          (public browsing)
    │   ├── SearchResultsTests.java          (public browsing)
    │   ├── FilterTests.java                 (public browsing)
    │   ├── ListingDetailsTests.java         (public browsing)
    │   ├── NavigationFooterTests.java       (public browsing)
    │   ├── LoginTests.java                  (authenticated)
    │   ├── AuthenticatedSearchTests.java    (authenticated / e2e)
    │   ├── WishlistTests.java               (wishlist)
    │   └── LogoutTests.java                 (authenticated)
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
| `browser` | `firefox` | `chrome` or `firefox` |
| `headless` | `false` | Set `true` for CI |
| `explicit.wait` | `15` | Seconds for WebDriverWait |

---

## Test Credentials

Authenticated tests use `CredentialHelper` which resolves credentials in this priority order:

| Priority | Source | Example |
|----------|--------|---------|
| 1 | Environment variable | `export AIRBNB_TEST_EMAIL=you@example.com` |
| 2 | Java system property | `mvn test -Dtest.email=you@example.com` |
| 3 | Hard-coded fallback | Pre-set in `CredentialHelper.java` for local use |

To override credentials at run time:
```bash
mvn test -Dtest.email=you@example.com -Dtest.password=YourPass
```

---

## Running Tests

**Pre-requisites:** Java 17+, Maven 3.8+, Google Chrome or Firefox installed.
WebDriverManager downloads the matching driver binary automatically.

```bash
# Run the full suite (all 57 tests)
mvn test

# Run the public browsing layer only
mvn test -Dgroups=smoke,regression,functional

# Run only authenticated / user-journey tests
mvn test -Dgroups=authenticated

# Run only wishlist tests
mvn test -Dgroups=wishlist

# Run the full e2e flow (login → search → listing → wishlist)
mvn test -Dgroups=e2e

# Run headless (no browser window — ideal for CI)
mvn test -Dheadless=true

# Run a single test class
mvn test -Dtest=LoginTests

# Run with Firefox
mvn test -Dbrowser=firefox
```

Reports appear in `target/surefire-reports/`.
Failure screenshots are saved to `target/screenshots/`.

---

## Test Classes & Groups

### Public Browsing Layer (baseline — 37 tests)

| Class | Group(s) | Focus |
|-------|----------|-------|
| `HomePageSmokeTests` | smoke | Homepage loads, header, footer, title |
| `DestinationSearchTests` | functional | Search field, suggestions, clear |
| `DatePickerTests` | functional | Calendar open, day selection, past-date block |
| `GuestSelectorTests` | functional | Adults/children steppers, summary |
| `SearchResultsTests` | regression | Results page URL/structure |
| `FilterTests` | functional | Filters panel open/apply/clear |
| `ListingDetailsTests` | regression | Card click, details/gallery visible |
| `NavigationFooterTests` | smoke / functional | Logo, header, footer links |

### Authenticated User-Journey Layer (new — 20 tests)

| Class | Group(s) | Focus |
|-------|----------|-------|
| `LoginTests` | authenticated, smoke, functional, regression | Valid login, error on bad credentials, avatar present, session persists, logout |
| `AuthenticatedSearchTests` | authenticated, e2e, functional | Search while logged in, results page, open listing, destination in URL, popup handling |
| `WishlistTests` | authenticated, wishlist, e2e, functional | Save from results, save from details, toggle, saved item on /wishlists page, unsave |
| `LogoutTests` | authenticated, smoke, regression | Logged-out state restored, avatar gone, account page blocked, refresh, /wishlists access |

---

## Likely Flaky Selectors — Verify First

1. **Search panel `data-testid`** — Airbnb A/B tests the homepage UI frequently.
   If `structured-search-input-field-query` is absent, inspect the live page and update `SearchPanelComponent`.

2. **Account menu button** — Primary selector: `[data-testid='cypress-headernav-profile']`.
   Fallbacks: `button[aria-label='Account']`, `button[aria-label='Open user menu']`.
   Verify on the live page if login flows fail at the menu-open step.

3. **Login modal flow** — Airbnb may show auth provider options before the email field.
   `LoginPage.selectEmailOption()` handles this automatically.  If login times out on the password step, check whether a CAPTCHA or rate-limit is present.

4. **Heart / save button** — Selectors target `button[aria-label*='wishlist' i]` and
   `button[aria-label*='Save to' i]`.  Saved state is detected via `aria-pressed` or label text.

5. **Date-picker day cells** — Selector falls back to `td[class*='CalendarDay']`.

6. **Listing cards** — `a[href*='/rooms/']` is the most stable fallback.

7. **New-tab on listing click** — Some Airbnb builds open listings in a new tab.  All classes that open listings include a `switchToNewTabIfOpened()` helper.

---

## Out of Scope

Payment automation, booking completion (including the booking-request form),
inbox/messages, CAPTCHA bypass, exact price assertions, exact result counts or ordering.
