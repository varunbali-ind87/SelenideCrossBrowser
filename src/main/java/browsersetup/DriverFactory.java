package browsersetup;

import assertionhandler.CustomAssertion;
import jsonreader.JsonUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.safari.SafariDriver;

import java.io.IOException;

public class DriverFactory
{
    public WebDriver getWebDriver() throws IOException
    {
        var jsonUtils = new JsonUtils();
        String browser = jsonUtils.getRandomBrowser();
        WebDriver driver = switch(browser)
        {
            case "chrome" -> new ChromeDriver();
            case "edge" -> new EdgeDriver();
            case "firefox" -> new FirefoxDriver();
            case "safari" -> new SafariDriver();
            default -> throw new CustomAssertion("Browser not supported");
        };
        SelenideBase.getScenario().log(String.format("Launching %s", browser));
        return driver;
    }
}
