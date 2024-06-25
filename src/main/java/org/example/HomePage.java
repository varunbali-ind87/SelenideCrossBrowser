package org.example;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.WebDriverRunner;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;

import java.time.Duration;

import static browsersetup.SelenideBase.getScenario;
import static com.codeborne.selenide.Selenide.$;

@Log4j2
public class HomePage
{

    public HomePage verifyOverviewIsVisible()
    {
        $(By.xpath("//*[@class='tab-1']")).shouldBe(Condition.visible, Duration.ofSeconds(10));
        return this;
    }

    public HomePage verifyOurTeamIsVisible()
    {
        $(By.xpath("//*[@class='tab-2']")).shouldBe(Condition.visible, Duration.ofSeconds(10));
        return this;
    }

    public HomePage verifyOurServicesIsVisible()
    {
        $(By.xpath("//*[@class='tab-3']")).shouldBe(Condition.visible, Duration.ofSeconds(10));
        return this;
    }

    public HomePage verifyKeyFeaturesIsVisible()
    {
        $(By.xpath("//*[@class='tab-4']")).shouldBe(Condition.visible, Duration.ofSeconds(10));
        return this;
    }

    public HomePage clickLink(String linkToClick)
    {
        $(By.xpath("//*[@id = 'id_footer_links' and @title = '" + linkToClick + "']")).shouldBe(Condition.visible, Duration.ofSeconds(10)).click();
        getScenario().log("Clicked on: " + linkToClick);
        return this;
    }

    public void verifySitemapWindowIsOpen(String parentWindow)
    {
        var windows = WebDriverRunner.getWebDriver().getWindowHandles();
        var targetWindow = windows.stream().filter(window -> !window.equals(parentWindow)).findFirst().orElseThrow();
        WebDriverRunner.getWebDriver().switchTo().window(targetWindow);
        $(By.xpath("//*[@id='__next']/main/div/h1")).shouldHave(Condition.text("Sitemap"));
        getScenario().log("Sitemap was found.");
        WebDriverRunner.getWebDriver().close();
        WebDriverRunner.getWebDriver().switchTo().window(parentWindow);
    }
}
