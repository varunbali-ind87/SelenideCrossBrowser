@UI @Test
Feature: Verify the footer links on the home page

  Scenario: Verify the About Us link
    When the user clicks the "About us" link
    Then he should be taken to the About Us page

  Scenario: Verify the SiteMap link
    When the user clicks the "Sitemap" link
    Then he should be taken to the SiteMap page