package scan_ui;

import activesupport.IllegalBrowserException;
import activesupport.system.Properties;
import activesupport.config.Configuration;
import activesupport.driver.Browser;

import com.typesafe.config.Config;

import org.dvsa.testing.lib.url.utils.EnvironmentType;
import org.dvsa.testing.lib.url.webapp.URL;
import org.dvsa.testing.lib.url.webapp.utils.ApplicationType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openqa.selenium.By;
import scanner.ScannerMethods;
import utils.BasePage;

import java.net.MalformedURLException;

import static utils.Utils.newPassword;
import static utils.Utils.refreshPageWithJavascript;

public class UIJourneyScannerAppScannerTest {

    private EnvironmentType env = EnvironmentType.getEnum(Properties.get("env", true));

    private Config config = new Configuration().getConfig();


    private final String IP_ADDRESS = config.getString("ipAddress");
    private final String CONTEXT_NAME = config.getString("contextName");
    private final String SCAN_REPORT_NAME = config.getString("reportName");
    private final String SCAN_POLICY = config.getString("policy");
    private final String SCAN_ATTACK_STRENGTH = config.getString("attackStrength");
    private final int PROXY_PORT = config.getInt("proxyPort");

    private final ScannerMethods scanner = new ScannerMethods(IP_ADDRESS, PROXY_PORT);
    private final ApplicationJourneys applicationJourneys = new ApplicationJourneys();
    private final Application application = new Application();

    @Before
    public void setUp() throws MalformedURLException, IllegalBrowserException {
        Browser.setIpAddress(String.valueOf(IP_ADDRESS));
        Browser.setPortNumber(String.valueOf(PROXY_PORT));
        application.createApplicationViaAPI(newPassword);
    }

    @Test
    public void fileUploadScan() throws Exception {
        String urlToScan = URL.build(ApplicationType.EXTERNAL, env).toString();

        String contextURLRegex = String.format("https://ssweb.%s.olcs.dev-dvsacloud.uk/*", env);
        String loginRequestData  = "username={%username%}&password={%password%}";

        //navigate to vol website
        refreshPageWithJavascript();
        if(BasePage.isLinkPresent(application.getApplicationId(),20))
        Browser.navigate().findElement(By.partialLinkText(application.getApplicationId())).click();
        applicationJourneys.uploadFinancialEvidence();
        applicationJourneys.saveAndReturn();
        applicationJourneys.addFinancialHistory();
        applicationJourneys.saveAndReturn();
        Browser.navigate().findElement(By.partialLinkText("Review")).click();
        Browser.navigate().findElement(By.xpath("//*[contains(text(),'Print')]")).click();

        //scan with zap
        scanner.createContext(CONTEXT_NAME);
        scanner.enableAllPassiveScanners();
        scanner.enableAllActiveScanners(SCAN_POLICY);
        scanner.excludeUrlFromSpiderScan("^((?!(https://firefox-settings-attachments.cdn.mozilla.net|https://tracking-protection.cdn.mozilla.net|https://content-signature-2.cdn.mozilla.net|https://firefox.settings.services.mozilla.com|https://location.services.mozilla.com)).*)$");
        scanner.excludeUrlFromActiveScan("^((?!(https://firefox-settings-attachments.cdn.mozilla.net|https://tracking-protection.cdn.mozilla.net|https://content-signature-2.cdn.mozilla.net|https://firefox.settings.services.mozilla.com|https://location.services.mozilla.com)).*)$");
        scanner.includeInContext(CONTEXT_NAME, contextURLRegex);
        scanner.setAuthenticationMethod(urlToScan, loginRequestData, "formBasedAuthentication");
        scanner.loggedInIndicator("<a href=\"/auth/logout/\" class=\"govuk-header__link\">Sign out</a>");
        scanner.authenticateUser(application.getUsername(), newPassword);
        scanner.performSpiderCrawlAsUser(urlToScan);
        scanner.performActiveAttackAsUser(urlToScan);
        scanner.createReport(SCAN_REPORT_NAME, urlToScan);
    }

    @After
    public void tearDown() throws Exception {
        Browser.closeBrowser();
        scanner.stopZap();
    }
}