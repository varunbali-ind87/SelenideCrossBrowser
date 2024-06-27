package browsersetup;

import assertionhandler.CustomAssertion;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.safari.SafariDriver;

import static browsersetup.SelenideBase.getScenario;

public class DriverFactory
{
    public WebDriver getWebDriver(final String browser)
    {
        WebDriver driver = switch(browser)
        {
            case "chrome" -> new ChromeDriver();
            case "edge" -> new EdgeDriver();
            case "firefox" -> new FirefoxDriver();
            case "safari" -> new SafariDriver();
            default -> throw new CustomAssertion("Browser not supported");
        };
        getScenario().log(String.format("Launching %s", browser));
        return driver;
    }
}
