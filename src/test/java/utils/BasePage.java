package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.DriverUtils;
import enums.SelectorType;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;

public abstract class BasePage extends DriverUtils {
    public static final int WAIT_TIME_SECONDS = 5;
    private static final int TIME_OUT_SECONDS = 500;
    private static final int POLLING_SECONDS = 2;
    private static final Logger LOGGER = LogManager.getLogger(BasePage.class);

    protected static String getAttribute(@NotNull String selector, @NotNull SelectorType selectorType, @NotNull String attribute) {
        return findElement(selector, selectorType).getAttribute(attribute);
    }

    /**
     * This returns any text content that an element possesses.
     *
     * @param selector     This should be a CSS or XPATH selector which is used to identify which elements text is to be retrieved.
     * @param selectorType This is the type of selector that the argument selector is.
     * @return The specified elements text contents.
     */
    protected static String getText(@NotNull String selector, @NotNull SelectorType selectorType) {
        return findElement(selector, selectorType).getText();
    }

    protected static String getText(@NotNull String selector) {
        return getText(selector, SelectorType.CSS);
    }


    public static String getTextFromNestedElement(WebElement webElement, String selector) {
        return webElement.findElement(By.xpath(selector)).getText();
    }

    protected static void untilElementWithText(ChronoUnit unit, long duration) {
        new FluentWait<>(getDriver())
                .ignoreAll(Arrays.asList(NoSuchElementException.class, StaleElementReferenceException.class))
                .withTimeout(Duration.of(duration, unit))
                .pollingEvery(Duration.of(500, ChronoUnit.MILLIS))
                .until(driver -> driver.findElement(by("//h1[@class='govuk-heading-xl']", SelectorType.XPATH)).getText().equalsIgnoreCase("Permit fee"));
    }

    protected static boolean isTextPresent(String locator) {
        boolean itsFound = true;
        try {
            new WebDriverWait(getDriver(), Duration.ofSeconds(3)).
                    until(WebDriver ->
                            visibilityOf(findElement(String.format("//*[contains(text(),'%s')]", locator), SelectorType.XPATH)));
        } catch (Exception e) {
            return false;
        }
        return itsFound;
    }

    public static boolean isErrorMessagePresent() {
        boolean hasError = false;

        String ERROR_MESSAGE_HEADING = "Please correct the following errors";
        String ERROR_CLASS = ".govuk-error-message";
        if (isTextPresent(ERROR_MESSAGE_HEADING) || isElementPresent(ERROR_CLASS, SelectorType.CSS)) hasError = true;

        return hasError;
    }

    protected static void scrollAndEnterField(@NotNull String selector, @NotNull SelectorType selectorType, @NotNull String text, boolean append) {
        WebElement field = findElement(selector, selectorType);

        if (!append)
            field.clear();

        new Actions(getDriver()).moveToElement(field).sendKeys(field, text).perform();
    }

    protected static void scrollAndEnterField(@NotNull String selector, @NotNull SelectorType selectorType, @NotNull String text) {
        scrollAndEnterField(selector, selectorType, text, false);
    }

    protected static void scrollAndEnterField(@NotNull String selector, @NotNull String text) {
        scrollAndEnterField(selector, SelectorType.CSS, text);
    }

    public static void clickByLinkText(@NotNull String selector) {
        findElement(selector, SelectorType.PARTIALLINKTEXT).click();
    }

    protected static void clickByXPath(@NotNull String selector) {
        findElement(selector, SelectorType.XPATH).click();
    }

    protected static void clickById(@NotNull String selector) {
        findElement(selector, SelectorType.ID).click();
    }

    protected static void clickByName(@NotNull String selector) {
        findElement(selector, SelectorType.NAME).click();
    }

    protected static void selectValueFromDropDown(@NotNull String selector, @NotNull SelectorType selectorType, @NotNull String listValue) {
        Select selectItem = new Select(findElement(selector, selectorType));
        selectItem.selectByVisibleText(listValue);
    }

    protected static void selectValueFromDropDownByIndex(@NotNull String selector, @NotNull SelectorType selectorType, @NotNull int listValue) {
        Select selectItem = new Select(findElement(selector, selectorType));
        selectItem.selectByIndex(listValue);
    }

    public void cycleThroughPaginationUntilElementIsDisplayed(String linkTextArgument) {
        List<WebElement> pagination = getDriver().findElements(By.xpath("//ul[@class='pagination right-aligned']"));
        int pagination_count = pagination.size() + 1;
        outsideloop:
        while (!isElementPresent(linkTextArgument, SelectorType.LINKTEXT))
            for (int i = 0; i <= pagination_count; i++) {
                isElementNotPresent(linkTextArgument, SelectorType.LINKTEXT);
                scrollAndClick("Next", SelectorType.LINKTEXT);
                if (isElementPresent(linkTextArgument, SelectorType.LINKTEXT)) {
                    break outsideloop;
                }
            }
    }

    public static String selectRandomValueFromDropDown(String idArgument) {
        Select select = new Select(getDriver().findElement(By.id(idArgument)));
        Random random = new Random();
        List<WebElement> dropdown = select.getOptions();
        int size = dropdown.size();
        int randomNo = random.nextInt(size);
        String ownerName = findElement(String.format("//*[@id='%s']/option[%s]", idArgument, randomNo), SelectorType.XPATH).getText();
        selectValueFromDropDown(idArgument, SelectorType.ID, ownerName);
        return ownerName;
    }

    public void selectRandomRadioBtnFromDataTable() {
        List<WebElement> rows_table = getDriver().findElements(By.tagName("tr"));
        int rows_count = rows_table.size();
        outsideloop:
        for (int row = 0; row < rows_count; row++) {
            List<WebElement> Columns_row = rows_table.get(row).findElements(By.tagName("td"));
            int columns_count = Columns_row.size();
            for (int column = 0; column < columns_count; ) {
                List<WebElement> options = findElements(String.format("//tbody//td[%s]", columns_count), SelectorType.XPATH);
                Random random = new Random();
                int size = options.size();
                int index = random.nextInt(size);
                options.get(index).click();
                break outsideloop;
            }
        }
    }

    protected static boolean isLinkPresent(String locator, int duration) {
        Wait<WebDriver> wait = new FluentWait<>(getDriver())
                .withTimeout(ofSeconds(duration))
                .pollingEvery(ofSeconds(POLLING_SECONDS))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);

        boolean itsFound = true;
        try {
            wait.until(WebDriver ->
                    wait.until(ExpectedConditions.visibilityOf(
                            (findElement(locator, SelectorType.PARTIALLINKTEXT)))));
        } catch (Exception e) {
            return false;
        }
        return itsFound;
    }

    public void selectRandomCheckBoxOrRadioBtn(String typeArgument) {
        List<WebElement> checkbox = findElements(String.format("//input[@type='%s']", typeArgument), SelectorType.XPATH);
        Random random = new Random();
        int index = random.nextInt(checkbox.size());
        checkbox.get(index).click();
    }

    protected static boolean isTitlePresent(String locator, int duration) {
        boolean itsFound = true;
        try {
            new WebDriverWait(getDriver(), Duration.ofSeconds(duration))
                    .until(WebDriver ->
                            visibilityOf(findElement(String.format("//h1[contains(text(),\"%s\")]", locator), SelectorType.XPATH)));
        } catch (Exception e) {
            return false;
        }
        return itsFound;
    }

    public static void click(@NotNull String selector, @NotNull SelectorType selectorType) {
        findElement(selector, selectorType).click();
    }

    protected static void scrollAndClick(@NotNull String selector, @NotNull SelectorType selectorType) {
        try {
            new Actions(getDriver())
                    .moveToElement(findElement(selector, selectorType))
                    .click()
                    .perform();
        } catch (StaleElementReferenceException e) {
            if (isElementPresent(selector, selectorType)) {
                new Actions(getDriver())
                        .moveToElement(findElement(selector, selectorType))
                        .click()
                        .perform();
            } else {
                throw new NoSuchElementException("Element was removed from the DOM and not replaced");
            }
        }
    }

    protected static void scrollAndClick(@NotNull String selector) {
        scrollAndClick(selector, SelectorType.CSS);
    }

    public static String nameAttribute(@NotNull String element, @NotNull String value) {
        return String.format("%s[%s=\"%s\"]", element, "name", value);
    }

    protected static List<WebElement> findAll(@NotNull String selector, @NotNull SelectorType selectorType) {
        return findElements(selector, selectorType);
    }

    protected static int size(@NotNull String selector, @NotNull SelectorType selectorType) {
        return findElements(selector, selectorType).size();
    }

    protected static boolean isElementNotPresent(@NotNull String selector, SelectorType selectorType) {
        return !isElementPresent(selector, selectorType);
    }

    protected static boolean isElementPresent(@NotNull String selector, SelectorType selectorType) {
        boolean isElementPresent = true;

        try {
            findElement(selector, selectorType);
        } catch (NoSuchElementException e) {
            isElementPresent = false;
        }

        return isElementPresent;
    }

    protected static boolean isElementVisible(@NotNull String selector, long duration) {
        boolean visible = true;

        try {
            untilVisible(selector, SelectorType.XPATH, duration, TimeUnit.SECONDS);
        } catch (Exception ex) {
            visible = false;
        }

        return visible;
    }

    protected static boolean isElementPresentWithin(@NotNull String selector) {
        boolean isInDOM = true;

        try {
            waitForElementToBePresent(selector);
        } catch (TimeoutException e) {
            isInDOM = false;
        }

        return isInDOM;
    }

    public static void untilVisible(@NotNull String selector, @NotNull SelectorType selectorType, long duration, TimeUnit timeUnit) {
        By by = by(selector, selectorType);
        until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public static void until(ExpectedCondition<?> expectedCondition) {
        new FluentWait<>(getDriver())
                .withTimeout(ofSeconds(TIME_OUT_SECONDS))
                .pollingEvery(ofSeconds(POLLING_SECONDS))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(ElementClickInterceptedException.class)
                .until(WebDriver ->
                        expectedCondition);
    }



    public static String getElementValueByText(@NotNull String selector, @NotNull SelectorType selectorType) {
        return getText(selector, selectorType);
    }

    public static void untilUrlMatches(String needle, long duration, ChronoUnit unit) {
        new FluentWait<>(getDriver())
                .pollingEvery(Duration.of(500, ChronoUnit.MILLIS))
                .withTimeout(Duration.of(duration, unit))
                .until(WebDriver -> ExpectedConditions.urlMatches(needle));
    }

    public static boolean isPath(@NotNull String path) {
        Pattern p = Pattern.compile(path);
        Matcher m = p.matcher(getURL().getPath());

        return m.find();
    }

    public static URL getURL() {
        URL url = null;
        try {
            url = new URL(getDriver().getCurrentUrl());
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Malformed URL");
        }

        return url;
    }

    public static boolean isElementEnabled(@NotNull String selector, @NotNull SelectorType selectorType) {
        return findElement(selector, selectorType).isEnabled();
    }

    public static void waitForElementToBeClickable(@NotNull String selector, @NotNull SelectorType selectorType) {
        Wait<WebDriver> wait = new FluentWait<>(getDriver())
                .withTimeout(ofSeconds(TIME_OUT_SECONDS))
                .pollingEvery(ofSeconds(POLLING_SECONDS))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);

        wait.until(WebDriver ->
                elementToBeClickable(by(selector, selectorType)));
    }

    public static void waitAndSelectValueFromDropDown(@NotNull String selector, @NotNull SelectorType selectorType, @NotNull String listValue) {
        final Wait<WebDriver> wait = new FluentWait<>(getDriver())
                .withTimeout(ofSeconds(TIME_OUT_SECONDS))
                .pollingEvery(ofSeconds(POLLING_SECONDS))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);

        wait.until(WebDriver -> wait.until(elementToBeClickable
                        (getDriver().findElement(By.xpath(selector)))));
        Select selectItem = new Select(findElement(selector, selectorType));
        selectItem.selectByVisibleText(listValue);
    }

    public static void waitAndSelectByIndex(@NotNull final String selector, @NotNull SelectorType selectorType, @NotNull final int listValue) {
        final Wait<WebDriver> wait = new FluentWait<>(getDriver())
                .withTimeout(ofSeconds(TIME_OUT_SECONDS))
                .pollingEvery(ofSeconds(POLLING_SECONDS))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);

        wait.until(driver ->
                wait.until(elementToBeClickable(
                        by(selector, selectorType))));
        Select selectItem = new Select(findElement(selector, selectorType));
        selectItem.selectByIndex(listValue);
    }

    public static void waitAndClick(@NotNull String selector, @NotNull SelectorType selectorType) {
        Wait<WebDriver> wait = new FluentWait<>(getDriver())
                .withTimeout(Duration.ofSeconds(TIME_OUT_SECONDS))
                .pollingEvery(Duration.ofSeconds(POLLING_SECONDS))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(ElementClickInterceptedException.class)
                .ignoring(ElementNotInteractableException.class);

        wait.until(driver ->
                wait.until(elementToBeClickable(
                        by(selector, selectorType))));
        findElement(selector, selectorType).click();
    }

    public static void waitForTextToBePresent(@NotNull String selector) {
        waitForElementToBePresent(String.format("//*[contains(text(),'%s')]", selector));
    }

    public static void waitForTitleToBePresent(@NotNull String selector) {
        waitForElementToBePresent(String.format("//h1[contains(text(),'%s')]", selector));
    }
    public static void waitForTitleToBePresent(@NotNull String htmlTag, @NotNull String selector) {
        waitForElementToBePresent(String.format("//%s[contains(text(),'%s')]", htmlTag, selector));
    }
    public static void waitForElementToBePresent(@NotNull String selector) {
        Wait<WebDriver> wait = new FluentWait<>(getDriver())
                .withTimeout(ofSeconds(TIME_OUT_SECONDS))
                .pollingEvery(ofSeconds(POLLING_SECONDS))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);

        wait.until(webDriver ->
                visibilityOf(getDriver().findElement(By.xpath(
                        selector))));
    }

    public static void waitAndEnterText(@NotNull String selector, @NotNull SelectorType selectorType, @NotNull String textValue) {
        Wait<WebDriver> wait = new FluentWait<>(getDriver())
                .withTimeout(ofSeconds(TIME_OUT_SECONDS))
                .pollingEvery(ofSeconds(POLLING_SECONDS))
                .ignoring(NoSuchElementException.class)
                .ignoring(InvalidElementStateException.class)
                .ignoring(StaleElementReferenceException.class);

                wait.until(elementToBeClickable(by(selector, selectorType)));
        findElement(selector, selectorType).sendKeys(textValue);
    }

    public static boolean isFieldEnabled(String field, SelectorType selectorType) {
        return Boolean.parseBoolean(findElement(field, selectorType).getAttribute("disabled"));
    }

    public static Object javaScriptExecutor(String jsScript) {
        return ((JavascriptExecutor) getDriver()).executeScript(jsScript);
    }

    public static void enterText(@NotNull String selector, @NotNull SelectorType selectorType, @NotNull String textValue) {
        findElement(selector, selectorType).sendKeys(textValue);
    }

    public static void enterText(@NotNull String selector, @NotNull int intValue, @NotNull SelectorType selectorType) {
        enterText(selector, selectorType, String.valueOf(intValue));
    }

    public void replaceText(String selector, SelectorType selectorType, String text) {
        findElement(selector, selectorType).clear();
        waitAndEnterText(selector, selectorType, text);
    }

    public static int returnTableRows(@NotNull String selector, @NotNull SelectorType selectorType) {
        return findElements(selector, selectorType).size();
    }

    public static void findSelectAllRadioButtonsByValue(String value) {
        List<WebElement> radioButtons = findElements("//*[@type='radio']", SelectorType.XPATH);
        radioButtons.stream().
                filter(x -> x.getAttribute("value").equals(value)).
                filter(isChecked -> !isChecked.isSelected()).
                forEach(WebElement::click);
    }

    public void selectFirstValueInList(String selector) {
        findElements(selector, SelectorType.XPATH).stream().findFirst().get().click();
    }

    public boolean checkForFullMatch(String searchTerm) {
        return findElements("//table/tbody/tr[*]", SelectorType.XPATH).stream().allMatch(w -> w.getText().contains(searchTerm));
    }

    public boolean checkForPartialMatch(String searchTerm) {
        return findElements("//table/tbody/tr[*]", SelectorType.XPATH).stream().anyMatch(w -> w.getText().contains(searchTerm));
    }

    public void enterTextIntoMultipleFields(@NotNull String selector, @NotNull SelectorType selectorType, @NotNull String text) {
        Wait<WebDriver> wait = new FluentWait<>(getDriver())
                .withTimeout(ofSeconds(TIME_OUT_SECONDS))
                .pollingEvery(ofSeconds(POLLING_SECONDS))
                .ignoring(NoSuchElementException.class)
                .ignoring(InvalidElementStateException.class)
                .ignoring(StaleElementReferenceException.class);

                wait.until(elementToBeClickable(by(selector, selectorType)));
        findElements(selector, selectorType).forEach(x -> x.sendKeys(text));
    }

    public String getValue(String selector, SelectorType selectorType) {
        return findElement(selector, selectorType).getAttribute("value");
    }

    public void waitForPageLoad() {
        Wait<WebDriver> wait = new FluentWait<>(getDriver())
                .withTimeout(ofSeconds(TIME_OUT_SECONDS))
                .pollingEvery(ofSeconds(POLLING_SECONDS))
                .ignoring(java.util.NoSuchElementException.class);
        ExpectedCondition<Boolean> expect = driver -> ((JavascriptExecutor) driver).executeScript("return document.readyState").toString().equals("complete");
        wait.until(WebDriver -> expect);
        try {
            assertEquals("complete", javaScriptExecutor("return document.readyState").toString());
        } catch (Exception e) {
            LOGGER.info("Page timed out trying to load.");
        }
    }

    public void enterDateFieldsByPartialId(String regex, HashMap<String, String> hashMapDate) {
        replaceText(regex.concat("_day"), SelectorType.ID, hashMapDate.get("day"));
        replaceText(regex.concat("_month"), SelectorType.ID, hashMapDate.get("month"));
        replaceText(regex.concat("_year"), SelectorType.ID, hashMapDate.get("year"));
    }

    public void clickAllCheckboxes() {
        getDriver().findElements(By.xpath("//*[@type='checkbox']")).forEach(WebElement::click);
    }

    public static void refreshPageUntilElementAppears(String selector, SelectorType selectorType) {
        boolean elementFound = false;
        long kickOutTime = System.currentTimeMillis() + 120 * 2000;
        while (!elementFound && System.currentTimeMillis() < kickOutTime) {
            javaScriptExecutor("location.reload(true)");
            elementFound = isElementPresent(selector, selectorType);
        }
    }

    public void waitForTabsToLoad(int expectedNumberOfTabs, int seconds) {
        ArrayList<String> tabs;
        long kickOut = System.currentTimeMillis() + (seconds * 1000L);
        do {
            tabs = new ArrayList<>(getWindowHandles());
            if (kickOut < System.currentTimeMillis()) {
                throw new TimeoutException("Incorrect number of tabs or tabs failed to load.");
            }
        } while (tabs.size() != expectedNumberOfTabs);
    }

    public void closeTabAndFocusTab(int tab) {
        closeTab();
        ArrayList<String> tabs = new ArrayList<String>(getWindowHandles());
        switchToWindow(tabs.get(tab));
    }

    public static void uploadFile(@NotNull String inputBoxSelector, @NotNull String file, String jScript, @NotNull SelectorType selectorType) {
        if (jScript != null) {
            javaScriptExecutor(jScript);
        }

        enterText(inputBoxSelector, selectorType, file);
    }

    public static void untilNotInDOM(@NotNull String selector, int seconds) {
        new WebDriverWait(getDriver(), Duration.ofSeconds(seconds)).until(webDriver ->
                (ExpectedConditions.presenceOfAllElementsLocatedBy(by(selector, SelectorType.CSS))));
    }

    public static void switchToPopWindow() {
        String parentWindow = getDriver().getWindowHandle();
        Set<String> windowHandles = getDriver().getWindowHandles();
        Iterator<String> iterator = windowHandles.iterator();
        while (iterator.hasNext()) {
            String handle = iterator.next();
            if (!handle.contains(parentWindow)) {
                getDriver().switchTo().window(handle);
            }
        }
    }

    public static String getSelectedTextFromDropDown(@NotNull String selector, @NotNull SelectorType selectorType) {
        Select option = new Select(findElement(selector, selectorType));
        return option.getFirstSelectedOption().getText();
    }
}