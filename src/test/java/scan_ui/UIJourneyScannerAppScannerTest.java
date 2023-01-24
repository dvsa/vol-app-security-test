package scan_ui;

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
    public void setUp() {
        Browser.setIpAddress(String.valueOf(IP_ADDRESS));
        Browser.setPortNumber(String.valueOf(PROXY_PORT));
    }

    @Test
    public void fileUploadScan() throws Exception {
        String urlToScan = URL.build(ApplicationType.EXTERNAL, env).toString();

        String contextURLRegex = String.format("https://ssweb.%s.olcs.dev-dvsacloud.uk/.*", env);
        String loginRequestData  = "username={%username%}&password={%password%}";

        //register user with vol-api-calls
        application.createApplicationViaAPI(newPassword);
        //navigate to vol website
        refreshPageWithJavascript();
        Browser.navigate().findElement(By.partialLinkText(application.getApplicationId())).click();
        applicationJourneys.uploadFinancialEvidence();
        applicationJourneys.saveAndReturn();
        applicationJourneys.addFinancialHistory();
        applicationJourneys.saveAndReturn();
        Browser.navigate().findElement(By.partialLinkText("Review")).click();
        Browser.navigate().findElement(By.xpath("//*[contains(text(),'Print')]")).click();
        applicationJourneys.payForApplication();

        //scan with zap
        scanner.createContext(CONTEXT_NAME);
        scanner.includeInContext(CONTEXT_NAME, contextURLRegex);
        scanner.setScannerAttackStrength(SCAN_POLICY, SCAN_ATTACK_STRENGTH);
        scanner.setAuthenticationMethod(urlToScan, loginRequestData, "formBasedAuthentication");
        scanner.loggedInIndicator("<a href=\"/your-account/\" class=\"govuk-header__link\">Your account</a>");
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