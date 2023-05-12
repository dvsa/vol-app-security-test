package scan_ui;

import activesupport.IllegalBrowserException;
import activesupport.system.Properties;
import activesupport.config.Configuration;
import activesupport.driver.Browser;

import com.typesafe.config.Config;

import org.dvsa.testing.lib.url.utils.EnvironmentType;
import org.dvsa.testing.lib.url.webapp.URL;
import org.dvsa.testing.lib.url.webapp.utils.ApplicationType;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import scanner.ScannerMethods;
import utils.BasePage;

import java.net.MalformedURLException;

import static utils.Utils.newPassword;
import static utils.Utils.refreshPageWithJavascript;

public class UIJourneyScannerAppScannerTest {

    private EnvironmentType env = EnvironmentType.getEnum(Properties.get("env", true));

    private static Config config = new Configuration().getConfig();


    private static final String IP_ADDRESS = config.getString("ipAddress");
    private final String CONTEXT_NAME = config.getString("contextName");
    private final String SCAN_REPORT_NAME = config.getString("reportName");
    private final String SCAN_POLICY = config.getString("policy");
    private final String SCAN_ATTACK_STRENGTH = config.getString("attackStrength");
    private static final int PROXY_PORT = config.getInt("proxyPort");

    private static final ScannerMethods scanner = new ScannerMethods(IP_ADDRESS, PROXY_PORT);
    private final ApplicationJourneys applicationJourneys = new ApplicationJourneys();
    private final Application application = new Application();

    @BeforeAll
    public static void setUp() throws MalformedURLException, IllegalBrowserException {
        Browser.setIpAddress(IP_ADDRESS);
        Browser.setPortNumber(String.valueOf(PROXY_PORT));
    }

    @Test
    public void fileUploadScan() throws Exception {
        application.createApplicationViaAPI(newPassword);
        String urlToScan = URL.build(ApplicationType.EXTERNAL, env).toString();

        String contextURLRegex = String.format("https://ssweb.%s.olcs.dev-dvsacloud.uk/*", env);
        String loginRequestData = "username={%username%}&password={%password%}";

        //navigate to vol website
        refreshPageWithJavascript();
        if (BasePage.isLinkPresent(application.getApplicationId(), 20))
            Browser.navigate().findElement(By.partialLinkText(application.getApplicationId())).click();
        applicationJourneys.addFinancialHistory();
        applicationJourneys.saveAndReturn();
        Browser.navigate().findElement(By.partialLinkText("Review")).click();
        Browser.navigate().findElement(By.xpath("//*[contains(text(),'Print')]")).click();

        //scan with zap
        scanner.createContext(CONTEXT_NAME);
        scanner.enableAllPassiveScanners();
        scanner.enableAllActiveScanners(SCAN_POLICY);
        scanner.includeInContext(CONTEXT_NAME, contextURLRegex);
        scanner.setAuthenticationMethod(urlToScan, loginRequestData, "formBasedAuthentication");
        scanner.loggedInIndicator("<a href=\"/auth/logout/\" class=\"govuk-header__link\">Sign out</a>");
        scanner.authenticateUser(application.getUsername(), newPassword);
        scanner.performSpiderCrawlAsUser(urlToScan);
        scanner.performActiveAttackAsUser(urlToScan);
        scanner.createReport(SCAN_REPORT_NAME, urlToScan);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        Browser.closeBrowser();
        scanner.stopZap();
    }
}