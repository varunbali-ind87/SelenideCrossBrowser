import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import lombok.extern.log4j.Log4j2;
import org.testng.ITestContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;

import java.io.File;

@CucumberOptions(features = {"src/test/resources/features"}, glue = {"browsersetup", "stepdefinitions"},
        monochrome = true, plugin = {
        "pretty",
        "json:target/cucumber.json",
        "junit:target/cucumber.xml",
        "rerun:target/failedscenarios.txt"},
        tags = "@Test")

@Log4j2
public class TestRunner extends AbstractTestNGCucumberTests
{

    @AfterTest
    public void afterTest()
    {
        log.info("Wrapping up test..");
    }

    @BeforeTest
    public void beforeTest(ITestContext context)
    {
        /*
         * Valid values of jobtype = eclipse, jenkins. If below code is anything but
         * jenkins then the below code will not be executed. This is helpful if you want
         * to use your IDE If value = jenkins then it will be executed for Jenkins
         * environment
         */
        System.setProperty("log4j2.configurationFile", System.getProperty("user.dir") + File.separator + "src/main/resources/log4j2.xml");
    }

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios()
    {
        return super.scenarios();
    }
}
