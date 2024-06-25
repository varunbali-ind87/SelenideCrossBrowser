package browsersetup;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.time.Duration;

import static com.codeborne.selenide.Selenide.*;

@Log4j2
public class SelenideSetup
{

    private static final String ABSOLUTE_URL = "https://action.deloitte.com/";

    @BeforeAll
    public static synchronized void setupCommonConfig()
    {
        Configuration.fastSetValue = true;
        Configuration.downloadsFolder = System.getProperty("java.io.tmpdir");
        log.info("Initial config parameters set..");
    }


    @Before
    public void setup(Scenario scenario) throws IOException
    {
        SelenideBase.setScenario(scenario);
        var driverFactory = new DriverFactory();
        WebDriver driver = driverFactory.getWebDriver();
        WebDriverRunner.setWebDriver(driver);
        open(ABSOLUTE_URL);
        scenario.log(String.format("Visiting %s", ABSOLUTE_URL));
        WebDriverRunner.getWebDriver().manage().window().maximize();
        $(By.id("onetrust-reject-all-handler")).shouldBe(Condition.visible, Duration.ofSeconds(10)).click();
    }

    @After
    public void closeBrowser(Scenario scenario)
    {
        clearBrowserLocalStorage();
        scenario.log("Browser storage cleared.");
        closeWebDriver();
        scenario.log("Browser closed.");
        SelenideBase.removeScenarioThread();
        scenario.log(String.format("Cleared thread. %sWrapping up the test", System.lineSeparator()));
    }
}
