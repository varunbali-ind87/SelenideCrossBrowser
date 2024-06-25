package browsersetup;

import jsonreader.JsonUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;

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
            default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
        };
        SelenideBase.getScenario().log(String.format("Launching %s", browser));
        return driver;
    }
}
