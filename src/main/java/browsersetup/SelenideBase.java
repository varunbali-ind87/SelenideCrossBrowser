package browsersetup;

import io.cucumber.java.Scenario;

public class SelenideBase
{
    private static final ThreadLocal<Scenario> scenarioThreadLocal = new ThreadLocal<>();

    private SelenideBase()
    {
    }

    public static synchronized void removeScenarioThread()
    {
        scenarioThreadLocal.remove();
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
