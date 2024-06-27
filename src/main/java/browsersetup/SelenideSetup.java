package browsersetup;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import ffmpegintegration.FFMPEGRunner;
import ffmpegintegration.FFMPEGSetup;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import jsonreader.JsonUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.time.Duration;

import static com.codeborne.selenide.Selenide.*;

@Log4j2
public class SelenideSetup
{

    private static final String ABSOLUTE_URL = "https://action.deloitte.com/";

    @BeforeAll
    public static synchronized void setupCommonConfig() throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException
    {
        Configuration.fastSetValue = true;
        Configuration.downloadsFolder = System.getProperty("java.io.tmpdir");
        log.info("Initial config parameters set..");
        FFMPEGSetup.setup();
    }


    @Before
    public void setup(Scenario scenario) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, InterruptedException, ConfigurationException, URISyntaxException
    {
        SelenideBase.setScenario(scenario);
        var jsonUtils = new JsonUtils();
        String browser = jsonUtils.getRandomBrowser();
        var driverFactory = new DriverFactory();
        WebDriver driver = driverFactory.getWebDriver(browser);
        WebDriverRunner.setWebDriver(driver);
        open(ABSOLUTE_URL);
        scenario.log(String.format("Visiting %s", ABSOLUTE_URL));
        WebDriverRunner.getWebDriver().manage().window().maximize();
        FFMPEGRunner.startVideoCapture(browser);
        $(By.id("onetrust-reject-all-handler")).shouldBe(Condition.visible, Duration.ofSeconds(10)).click();
    }

    @After
    public void closeBrowser(Scenario scenario) throws IOException, InterruptedException
    {
        FFMPEGRunner.stopVideoCapture();
        if (WebDriverRunner.hasWebDriverStarted())
        {
            clearBrowserLocalStorage();
            scenario.log("Browser storage cleared.");
            closeWebDriver();
            scenario.log("Browser closed.");
            SelenideBase.removeScenarioThread();
            scenario.log("Cleared thread.");
        }
    }
}
