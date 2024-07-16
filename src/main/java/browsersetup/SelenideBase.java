package browsersetup;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import io.cucumber.java.Scenario;

import java.util.Objects;

public class SelenideBase
{
    private static final ThreadLocal<Scenario> scenarioThreadLocal = new ThreadLocal<>();

    private SelenideBase()
    {
    }

    public static synchronized void cleanupAndCloseBrowser()
    {
        if (Objects.nonNull(getScenario()) && WebDriverRunner.hasWebDriverStarted())
        {
            scenarioThreadLocal.remove();
            Selenide.clearBrowserLocalStorage();
            Selenide.closeWebDriver();
        }
    }

    public static Scenario getScenario()
    {
        return scenarioThreadLocal.get();
    }

    public static void setScenario(final Scenario scenario)
    {
        scenarioThreadLocal.set(scenario);
    }

}
