package utils;

import activesupport.IllegalBrowserException;
import activesupport.config.Configuration;
import activesupport.dates.Dates;
import activesupport.dates.LocalDateCalendar;
import activesupport.driver.Browser;
import com.typesafe.config.Config;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import scan_ui.ApplicationJourneys;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class Utils {
    public void selectAllRadioButtonsByValue(String value)  {
        List<WebElement> radioButtons = Browser.navigate().findElements(By.xpath("//*[@type='radio']"));
        radioButtons.stream().filter((x) -> x.getAttribute("value").equals(value)).filter((isChecked) -> !isChecked.isSelected()).forEach((x) -> {
            x.click();
        });
    }

    public String readFile(String fileName) throws IOException {
        ClassLoader classLoader = ApplicationJourneys.class.getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource(fileName)).getFile());
        return FileUtils.readFileToString(file, "UTF-8");
    }

    public static InputStream getFileFromResourceAsStream(String fileName) {
        // The class loader that loaded the class
        ClassLoader classLoader = Utils.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }
    }

    public static Object javaScriptExecutor(String jsScript) {
        return ((JavascriptExecutor) Browser.navigate()).executeScript(jsScript);
    }
    public static void refreshPageWithJavascript() {
        javaScriptExecutor("location.reload(true)");
    }

    @NotNull
    public String getString(String dateFormat) {
        Dates sut = new Dates(new LocalDateCalendar());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        LinkedHashMap<String, String> date = sut.getDateHashMap(0, 0, -30);
        return simpleDateFormat.format(date.get("month") + date.get("day") + date.get("year"));
    }
}