package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import enums.SelectorType;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.time.Duration.ofSeconds;
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

    public static boolean isLinkPresent(String locator, int duration) {
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

    protected static boolean isElementPresent(@NotNull String selector, SelectorType selectorType) {
        boolean isElementPresent = true;

        try {
            findElement(selector, selectorType);
        } catch (NoSuchElementException e) {
            isElementPresent = false;
        }

        return isElementPresent;
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

    public static Object javaScriptExecutor(String jsScript) {
        return ((JavascriptExecutor) getDriver()).executeScript(jsScript);
    }

    public static void enterText(@NotNull String selector, @NotNull SelectorType selectorType, @NotNull String textValue) {
        findElement(selector, selectorType).sendKeys(textValue);
    }

    public static void uploadFile(@NotNull String inputBoxSelector, @NotNull String file, String jScript, @NotNull SelectorType selectorType) {
        if (jScript != null) {
            javaScriptExecutor(jScript);
        }

        enterText(inputBoxSelector, selectorType, file);
    }
}