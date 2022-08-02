package utils;

import activesupport.driver.Browser;
import enums.SelectorType;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public class DriverUtils {

    public static WebDriver getDriver() {
        return Browser.navigate();
    }

    public static void deleteCookies() {
        getDriver().manage().deleteAllCookies();
    }

    public static void refreshPage() {
        getDriver().navigate().refresh();
    }
    public static By by(@NotNull String selector, @NotNull SelectorType selectorType) {
        By bySelector;

        switch (selectorType) {
            case CSS:
                bySelector = By.cssSelector(selector);
                break;
            case XPATH:
                bySelector = By.xpath(selector);
                break;
            case NAME:
                bySelector = By.name(selector);
                break;
            case ID:
                bySelector = By.id(selector);
                break;
            case LINKTEXT:
                bySelector = By.linkText(selector);
                break;
            case PARTIALLINKTEXT:
                bySelector = By.partialLinkText(selector);
                break;
            default:
                throw new IllegalArgumentException("Only CSS, ID, NAME, XPATH, LINKTEXT and PARTIALLINKTEXT selector types are allowed");
        }
        return bySelector;
    }

    public static WebElement findElement(@NotNull String selector, @NotNull SelectorType selectorType) {
        return getDriver().findElement(by(selector, selectorType));
    }

    public static WebElement findElement(@NotNull String selector, @NotNull SelectorType selectorType, long timeOutInSeconds) {
        new WebDriverWait(getDriver(), Duration.ofSeconds(timeOutInSeconds)).until(
                webDriver ->
                        ExpectedConditions.presenceOfElementLocated(by(selector, selectorType)));
        return findElement(selector, selectorType);
    }

    public static List<WebElement> findElements(@NotNull String selector, @NotNull SelectorType selectorType) {
        return getDriver().findElements(by(selector, selectorType));
    }

    public static String getCurrentUrl() {
        return getDriver().getCurrentUrl();
    }

    public static void get(String url) {
        getDriver().get(url);
    }

    public static void switchToWindow(String windowHandle) {
        getDriver().switchTo().window(windowHandle);
    }

    public static Set<String> getWindowHandles() {
        return getDriver().getWindowHandles();
    }

    public static WebDriver.Options manage() {
        return getDriver().manage();
    }

    public static void closeTab() {
        getDriver().close();
    }

    public static String getTitle() {
        return getDriver().getTitle();
    }
}