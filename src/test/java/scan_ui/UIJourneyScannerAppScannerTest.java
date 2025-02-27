package scan_ui;

import activesupport.IllegalBrowserException;
import activesupport.config.Configuration;
import activesupport.driver.Browser;
import activesupport.system.Properties;
import com.typesafe.config.Config;
import enums.SelectorType;
import org.apache.commons.codec.DecoderException;
import activesupport.aws.s3.SecretsManager;
import org.apache.hc.core5.http.HttpException;
import org.dvsa.testing.lib.url.utils.EnvironmentType;
import org.dvsa.testing.lib.url.webapp.utils.ApplicationType;
import org.dvsa.testing.lib.url.webapp.webAppURL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scanner.ScannerMethods;
import utils.BasePage;

import java.net.MalformedURLException;

import static utils.Utils.refreshPageWithJavascript;

public class UIJourneyScannerAppScannerTest extends BasePage {

    private static EnvironmentType env = EnvironmentType.getEnum(Properties.get("env", true));

    private static Config config = new Configuration().getConfig();


    private static final String IP_ADDRESS = "127.0.0.1";
    private final String CONTEXT_NAME = "VOL_API";
    private final String SCAN_REPORT_NAME = "Penetration_Results";
    private final String SCAN_POLICY = "Default policy";
    private static final int PROXY_PORT = 8090;
    public static String username = SecretsManager.getSecretValue("username");
    public static String password = SecretsManager.getSecretValue("intPassword");
    public static String newPassword = SecretsManager.getSecretValue("adminPassword");

    private static final ScannerMethods scanner = new ScannerMethods(IP_ADDRESS, PROXY_PORT);
    private final ApplicationJourneys applicationJourneys = new ApplicationJourneys();
    private static final Application application = new Application();

    @BeforeAll
    public static void setUp() throws MalformedURLException, IllegalBrowserException, HttpException, DecoderException {
        Browser.setIpAddress(IP_ADDRESS);
        Browser.setPortNumber(String.valueOf(PROXY_PORT));
        if (!env.toString().equals("int")) {
            application.createApplicationViaAPI(newPassword);
        } else {
            application.createApplicationViaUI(username, password);
        }
    }

    @Test
    public void fileUploadScan() throws Exception {
        String contextURLRegex;
        String urlToScan = webAppURL.build(ApplicationType.EXTERNAL, env).toString();
        if (env.toString().equals("int")) {
            contextURLRegex = String.format("https://ssweb.%s.olcs.dvsacloud.uk/*", env);
        } else {
            contextURLRegex = String.format("https://ssweb.%s.olcs.dev-dvsacloud.uk/*", env);
        }
        String loginRequestData = "username={%username%}&password={%password%}";

        //navigate to vol website
        refreshPageWithJavascript();
        if (isLinkPresent(application.getApplicationId(), 20)) {
            clickByLinkText(application.getApplicationId());
        }
        applicationJourneys.addFinancialHistory();
        applicationJourneys.saveAndReturn();

        //scan with zap
        scanner.createContext(CONTEXT_NAME);
        scanner.enableAllActiveScanners(SCAN_POLICY);
        scanner.includeInContext(CONTEXT_NAME, contextURLRegex);
        scanner.setAuthenticationMethod(urlToScan, loginRequestData, "formBasedAuthentication");
        scanner.loggedInIndicator("<a href=\"/auth/logout/\" class=\"govuk-header__link\">Sign out</a>");
        if (env.toString().equals("int")) {
            scanner.authenticateUser(username, password);
        } else {
            scanner.authenticateUser(application.getUsername(), newPassword);
        }
        scanner.performSpiderCrawlAsUser(urlToScan);
        scanner.performActiveAttackAsUser(urlToScan,SCAN_POLICY);
        scanner.createReport(SCAN_REPORT_NAME, urlToScan);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        clickByLinkText("Back");
        waitForTitleToBePresent("Apply for a new licence");
        clickByLinkText("Cancel application");
        waitForTitleToBePresent("h2", "Cancel application");
        waitAndClick("form-actions[submit]", SelectorType.ID);
        Browser.closeBrowser();
        scanner.stopZap();
    }
}