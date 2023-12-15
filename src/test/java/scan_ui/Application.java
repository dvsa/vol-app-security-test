package scan_ui;

import activesupport.IllegalBrowserException;
import apiCalls.actions.CreateApplication;
import apiCalls.actions.GetUserDetails;
import apiCalls.actions.RegisterUser;
import apiCalls.enums.OperatorType;
import apiCalls.enums.UserType;
import org.apache.hc.core5.http.HttpException;

import java.net.MalformedURLException;


public class Application {

    private String applicationId;
    private String username;
    public void createApplicationViaAPI(String password) throws MalformedURLException, IllegalBrowserException, HttpException {
        ApplicationJourneys applicationJourneys = new ApplicationJourneys();
        RegisterUser registerUser = new RegisterUser();
        registerUser.registerUser();
        applicationJourneys.navigateToExternalSite();
        applicationJourneys.loginIntoExternalSite(registerUser.getUserName(), registerUser.getEmailAddress());
        GetUserDetails userDetails = new GetUserDetails();
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
        createApplication.submitVehicleDeclaration();
        createApplication.addFinancialHistory();
        createApplication.addApplicationSafetyAndComplianceDetails();
        createApplication.addSafetyInspector();
        createApplication.addConvictionsDetails();
        createApplication.addLicenceHistory();
        createApplication.applicationReviewAndDeclare();
        setApplicationId(createApplication.getApplicationId());
        setUsername(registerUser.getUserName());
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
