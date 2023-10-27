package scan_ui;

import activesupport.IllegalBrowserException;
import activesupport.aws.s3.S3;
import activesupport.config.Configuration;
import activesupport.driver.Browser;
import activesupport.system.Properties;
import com.typesafe.config.Config;

import enums.SelectorType;
import org.dvsa.testing.lib.url.utils.EnvironmentType;
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
import java.util.ArrayList;
import java.util.List;

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

    public void applyForALicence() {
        clickByLinkText("Apply");
        waitAndClick("//*[contains(text(),'Great')]", SelectorType.XPATH);
        waitAndClick("//*[contains(text(),'Goods')]", SelectorType.XPATH);
        waitAndClick("//*[contains(text(),'Standard National')]", SelectorType.XPATH);
        waitAndClick("form-actions[saveAndContinue]", SelectorType.ID);
    }

    public void uploadFinancialEvidence() {
        refreshPageWithJavascript();
        clickByLinkText("Financial evidence");
        File file = new File("/src/test/resources/vulnerability_files/disclose.xml");

        click("uploadNowRadio", SelectorType.ID);
        if (System.getProperty("platform") == null) {
            uploadFile("//*[@id='evidence[files][file]']", System.getProperty("user.dir").concat(String.valueOf(file)), "document.getElementById('evidence[files][file]').style.left = 0", SelectorType.XPATH);
        } else {
            WebElement addFile = getDriver().findElement(By.xpath("//*[@id='evidence[files][file]']"));
            ((RemoteWebElement) addFile).setFileDetector(new LocalFileDetector());
            addFile.sendKeys(System.getProperty("user.dir").concat(String.valueOf(file)));
        }
        click("uploadLaterRadio", SelectorType.ID);
        waitAndClick("form-actions[saveAndContinue]", SelectorType.ID);
    }

    public void addFinancialHistory() {
        refreshPageWithJavascript();
        Utils utils = new Utils();
        File injectionFile = new File("/src/test/resources/vulnerability_files/injection.html");
        File discloseFile = new File("/src/test/resources/vulnerability_files/disclose.xml");
        List<File> filesList = new ArrayList<>();
        filesList.add(injectionFile);
        filesList.add(discloseFile);

        clickByLinkText("Financial history");
        utils.selectAllRadioButtonsByValue("Y");

        for(File file : filesList) {
            if (System.getProperty("platform") == null) {
                uploadFile("//*[@id='data[file][file]']", System.getProperty("user.dir").concat(String.valueOf(file)), "document.getElementById('data[file][file]').style.left = 0", SelectorType.XPATH);
            } else {
                WebElement addFile = getDriver().findElement(By.xpath("//*[@id='data[file][file]']"));
                ((RemoteWebElement) addFile).setFileDetector(new LocalFileDetector());
                addFile.sendKeys(System.getProperty("user.dir").concat(String.valueOf(file)));
            }
        }
        String xmlInjectionLaugh = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*" +
                "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*" +
                "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE";
        findElement("data[insolvencyDetails]", SelectorType.ID).sendKeys(xmlInjectionLaugh);
        waitAndClick("form-actions[saveAndContinue]", SelectorType.ID);
    }

    private void signIn(@NotNull String emailAddress, @NotNull String password) {
        navigate().findElement(By.name("username")).sendKeys(emailAddress);
        navigate().findElement(By.name("password")).sendKeys(password);
        navigate().findElement(By.name("submit")).click();
    }

    public void payForApplication() {
        clickById("submitAndPay");
        clickById("form-actions[pay]");
        customerPaymentModule();
    }

    public void customerPaymentModule() {
        EnvironmentType env = EnvironmentType.getEnum(Properties.get("env", true));
        Config config = new activesupport.config.Configuration(env.toString()).getConfig();
        waitForTextToBePresent("Card Number*");
        enterText("//*[@id='scp_cardPage_cardNumber_input']", SelectorType.XPATH, config.getString("cardNumber"));
        enterText("//*[@id='scp_cardPage_expiryDate_input']", SelectorType.XPATH, config.getString("cardExpiryMonth"));
        enterText("//*[@id='scp_cardPage_expiryDate_input2']", SelectorType.XPATH, config.getString("cardExpiryYear"));
        enterText("//*[@id='scp_cardPage_csc_input']", SelectorType.XPATH, "123");
        if (isElementPresent("scp_cardPage_storedCard_payment_input", SelectorType.ID)) {
            click("scp_cardPage_storedCard_payment_input", SelectorType.ID);
        }
        click("//*[@id='scp_cardPage_buttonsNoBack_continue_button']", SelectorType.XPATH);
        enterCardHolderDetails();
        waitForTextToBePresent("Payment Confirmation Page");
        click("//*[@id='scp_confirmationPage_buttons_payment_button']", SelectorType.XPATH);
        if (isElementPresent("//*[@id='scp_storeCardConfirmationPage_buttons_back_button']", SelectorType.XPATH)) {
            waitForTextToBePresent("Online Payments");
            click("//*[@value='Save']", SelectorType.XPATH);
        }
    }

    public void enterCardHolderDetails() {
        waitAndEnterText("scp_tdsv2AdditionalInfoPage_cardholderName_input", SelectorType.ID, "SecurityUser" + " " + "Hacker");
        waitAndEnterText("scp_tdsv2AdditionalInfoPage_address_1_input", SelectorType.ID, "24 VOL WAY");
        if (isElementPresent("scp_tdsv2AdditionalInfoPage_address_2_input", SelectorType.ID)) {
            waitAndEnterText("scp_tdsv2AdditionalInfoPage_address_2_input", SelectorType.ID, "Hacking");
        }
        waitAndEnterText("scp_tdsv2AdditionalInfoPage_city_input", SelectorType.ID, "Nottingham");
        waitAndEnterText("scp_tdsv2AdditionalInfoPage_postcode_input", SelectorType.ID, "NG1 3DV");
        waitAndEnterText("scp_tdsv2AdditionalInfoPage_email_input", SelectorType.ID, "sec@vol.com");
        waitAndClick("_eventId_continue", SelectorType.NAME);
    }

    public void saveAndReturn() {
        Browser.navigate().findElement(By.id("form-actions[save]")).click();
    }
}