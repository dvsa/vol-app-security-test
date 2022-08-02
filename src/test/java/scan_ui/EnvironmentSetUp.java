package scan_ui;

import activesupport.system.Properties;
import org.dvsa.testing.lib.url.utils.EnvironmentType;

public class EnvironmentSetUp {
    private final EnvironmentType env
            = EnvironmentType.getEnum(Properties.get("env", true));

    public void setPassword(String newPassword) {
    }

    public EnvironmentType getEnv() {
        return env;
    }
}