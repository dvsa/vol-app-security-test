package scan_ui;

import activesupport.IllegalBrowserException;
import activesupport.aws.s3.S3;
import activesupport.config.Configuration;
import activesupport.driver.Browser;
import com.typesafe.config.Config;

import enums.SelectorType;
import org.dvsa.testing.lib.url.webapp.URL;
import org.dvsa.testing.lib.url.webapp.utils.ApplicationType;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebElement;
import utils.BasePage;
import utils.Utils;

import java.io.File;
import java.net.MalformedURLException;
import java.time.Duration;

import static activesupport.driver.Browser.navigate;
import static utils.Utils.refreshPageWithJavascript;

public class ApplicationJourneys extends BasePage {

    EnvironmentSetUp setUp = new EnvironmentSetUp();

    public void navigateToExternalSite() {
        String myURL = URL.build(ApplicationType.EXTERNAL, setUp.getEnv()).toString();

        if (Browser.isBrowserOpen()) {
            navigate().manage().deleteAllCookies();
            navigate().manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
        }
        try {
            navigate().get(myURL);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    public void loginIntoExternalSite(String username, String email) throws MalformedURLException, IllegalBrowserException {
        Config config = new Configuration().getConfig();
        String newPassword = config.getString("password");
        String password = S3.getTempPassword(email, config.getString("bucketName"));

        try {
            if (navigate().getCurrentUrl().contains("login")) {
                signIn(username, password);
            }
        } catch (Exception e) {
            //User is already registered
            signIn(username, newPassword);
        } finally {
            navigate().findElement(By.name("newPassword")).sendKeys(newPassword);
            navigate().findElement(By.name("confirmPassword")).sendKeys(newPassword);
            navigate().findElement(By.name("submit")).submit();
            setUp.setPassword(newPassword);
        }
    }

    public void applyForALicence(){
        clickByLinkText("Apply");
        waitAndClick("//*[contains(text(),'Great')]", SelectorType.XPATH);
        waitAndClick("//*[contains(text(),'Goods')]", SelectorType.XPATH);
        waitAndClick("//*[contains(text(),'Standard National')]", SelectorType.XPATH);
        waitAndClick("form-actions[saveAndContinue]", SelectorType.ID);
    }

    public void addFinancialHistory() {
        refreshPageWithJavascript();
        Utils utils = new Utils();
        File file = new File("/src/test/resources/vulnerability_files/injection.html");

        clickByLinkText("Financial history");
        click("//*[@id=\"data[financialHistoryConfirmation][insolvencyConfirmation]\"]", SelectorType.XPATH);
        utils.selectAllRadioButtonsByValue("Y");

        if (System.getProperty("platform") == null) {
            uploadFile("//*[@id='data[file][file]']", System.getProperty("user.dir").concat(String.valueOf(file)), "document.getElementById('data[file][file]').style.left = 0", SelectorType.XPATH);
        } else {
            WebElement addFile = getDriver().findElement(By.xpath("//*[@id='data[file][file]']"));
            ((RemoteWebElement) addFile).setFileDetector(new LocalFileDetector());
            addFile.sendKeys(System.getProperty("user.dir").concat(String.valueOf(file)));
        }
        String xmlInjectionLaugh = "?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE root [<!ENTITY ha \"Ha !\">\n" +
                "<!ENTITY ha2 \"&ha; &ha;\"><!ENTITY ha3 \"&ha2; &ha2;\">\n" +
                "<!ENTITY ha4 \"&ha3; &ha3;\">\n" +
                "<!ENTITY ha5 \"&ha4; &ha4;\">\n" +
                "...<!ENTITY ha128 \"&ha127; &ha127;\">\n" +
                "]>\n" +
                "<root>&ha128;</root>";
        findElement("data[insolvencyDetails]", SelectorType.ID).sendKeys(xmlInjectionLaugh);
        waitAndClick("form-actions[saveAndContinue]", SelectorType.ID);
    }

    private void signIn(@NotNull String emailAddress, @NotNull String password) throws IllegalBrowserException, MalformedURLException {
        navigate().findElement(By.name("username")).sendKeys(emailAddress);
        navigate().findElement(By.name("password")).sendKeys(password);
        navigate().findElement(By.name("submit")).click();
    }
}