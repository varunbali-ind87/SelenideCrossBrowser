package stepdefinitions;

import com.codeborne.selenide.WebDriverRunner;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.HomePage;

public class HomePageStep
{
    private final HomePage homePage = new HomePage();
    private String parentWindow;

    @When("the user clicks the {string} link")
    public void theUserClicksTheLink(String link)
    {
        parentWindow = WebDriverRunner.getWebDriver().getWindowHandle();
        homePage.clickLink(link);
    }

    @Then("he should be taken to the About Us page")
    public void heShouldBeTakenToTheAboutUsPage()
    {
        homePage.verifyOverviewIsVisible()
                .verifyOurTeamIsVisible()
                .verifyOurServicesIsVisible()
                .verifyKeyFeaturesIsVisible();
    }

    @Then("he should be taken to the SiteMap page")
    public void heShouldBeTakenToTheSiteMapPage()
    {
        homePage.verifySitemapWindowIsOpen(parentWindow);
    }
}
