package scan_ui;

import activesupport.IllegalBrowserException;
import apiCalls.actions.CreateApplication;
import apiCalls.actions.UserDetails;
import apiCalls.actions.RegisterUser;
import apiCalls.enums.OperatorType;
import apiCalls.enums.UserType;
import enums.SelectorType;
import org.apache.commons.codec.DecoderException;
import org.apache.hc.core5.http.HttpException;
import utils.BasePage;

import java.net.MalformedURLException;


public class Application extends BasePage {

    private String applicationId;
    private String username;

    ApplicationJourneys applicationJourneys = new ApplicationJourneys();
    public void createApplicationViaAPI(String password) throws MalformedURLException, IllegalBrowserException, HttpException, DecoderException {
        RegisterUser registerUser = new RegisterUser();
        registerUser.registerUser();
        applicationJourneys.navigateToExternalSite();
        applicationJourneys.loginIntoExternalSite(registerUser.getUserName(), registerUser.getEmailAddress());
        UserDetails userDetails = new UserDetails();
        CreateApplication createApplication = new CreateApplication(registerUser, userDetails);
        createApplication.setOperatorType(OperatorType.GOODS.name());
        userDetails.getUserDetails(UserType.EXTERNAL.asString(), registerUser.getUserId(), registerUser.getUserName(), password);
        createApplication.startApplication();
        createApplication.addBusinessType();
        createApplication.addBusinessDetails();
        createApplication.addAddressDetails();
        createApplication.addDirectors();
        createApplication.submitTaxiPhv();
        createApplication.addOperatingCentre();
        createApplication.updateOperatingCentre();
        createApplication.addFinancialEvidence();
        createApplication.addTransportManager();
        createApplication.submitTransport();
        createApplication.addTmResponsibilities();
        createApplication.submitTmResponsibilities();
        createApplication.addVehicleDetails();
        createApplication.addFinancialHistory();
        createApplication.addApplicationSafetyAndComplianceDetails();
        createApplication.addSafetyInspector();
        createApplication.addConvictionsDetails();
        createApplication.addLicenceHistory();
        createApplication.applicationReviewAndDeclare();
        setApplicationId(createApplication.getApplicationId());
        setUsername(registerUser.getUserName());
    }

    public void createApplicationViaUI(String username, String password) {
        applicationJourneys.navigateToExternalSite();
        applicationJourneys.signIn(username, password);
        waitAndClick("//*[contains(text(),'Apply for a new licence')]", SelectorType.XPATH);
        waitForTitleToBePresent( "Type of licence");
        waitAndClick("//*[contains(text(),'Goods vehicles')]", SelectorType.XPATH);
        waitAndClick("//*[contains(text(),'Standard National')]", SelectorType.XPATH);
        clickAndContinue();
        waitForTitleToBePresent( "Apply for a new licence");
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
