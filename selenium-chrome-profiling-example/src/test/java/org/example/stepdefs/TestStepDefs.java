package org.example.stepdefs;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.example.utility.DevToolUtility;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;

public class TestStepDefs {
    private static final String SYS_PROP_CHROME_DRIVER = "webdriver.chrome.driver";
    private static final String SYS_PROP_OS_NAME = "os.name";
    private WebDriver driver;
    private DevToolUtility devToolUtility;
    private boolean profilingEnabled;

    @Before
    public void setUp(Scenario scenario) throws Exception {
        if(System.getProperty(SYS_PROP_CHROME_DRIVER)==null){
            var osName = System.getProperty(SYS_PROP_OS_NAME);
            if (osName.toLowerCase().contains("win")) {
                System.setProperty("webdriver.chrome.driver", "resources/chromedriver-114.0.5735.90-win.exe");
            } else if (osName.toLowerCase().contains("mac")) {
                System.setProperty("webdriver.chrome.driver", "resources/chromedriver-114.0.5735.90-mac");
            } else {
                System.setProperty("webdriver.chrome.driver", "resources/chromedriver-114.0.5735.90-nix");
            }
        }

        Thread.sleep(1000);

        ChromeOptions chromeOptions = new ChromeOptions();
        driver = new ChromeDriver(chromeOptions);

        profilingEnabled = "true".equalsIgnoreCase(System.getProperty("profiling.enabled", "false"));
        if (profilingEnabled) {
            devToolUtility = new DevToolUtility((ChromeDriver) driver);
            devToolUtility.startTracing(scenario.getName());
        }
    }

    //region : step-defs
    @Given("Open website {string}")
    public void open_website(String url) throws Throwable {
        driver.get(url);
        Thread.sleep(2000);
    }

    @Given("Click \"Accept All\" button if exists")
    public void click_button_if_exists() {
        driver.findElements(By.xpath("//div[//div[//h1[text() = 'Before you continue to Google']]]//div[text() = 'Accept all']")).forEach(WebElement::click);
    }


    @Given("Enter search text {string}")
    public void enter_search_text(String text) {
        var elements = driver.findElement(By.xpath("//*[@aria-label=\"Search\"]"));
        elements.clear();
        elements.sendKeys(text);
        elements.sendKeys(Keys.ESCAPE);
    }

    @Then("The page title should contain {string}")
    public void page_title_should_contain(String expectedTitle) {
        Assert.assertTrue(driver.getTitle().contains(expectedTitle), "Validate page title");
    }

    @Given("Get Images results")
    public void get_images_results() {
        waitUntilUrlChanged(() -> driver.findElement(By.linkText("Images")).click(), 10);
    }

    @Given("Click search button")
    public void click_search_button() {
        waitUntilUrlChanged(() -> driver.findElement(By.xpath("//input[@value=\"Google Search\"]")).submit(), 10);
    }

    @Given("Wait {int} seconds")
    public void wait_seonds(int waitSec) throws Exception{
        Thread.sleep(waitSec* 1000L);
    }

    @Given("Click picture {int}")
    public void click_first_picture(int nth) {
        driver.findElements(By.xpath("//div[@role='listitem']//a[@role='button']//img")).get(nth-1).click();
    }
    //endregion

    @After
    public void teardown() throws Exception {
        if (profilingEnabled) {
            devToolUtility.stopTracing();
        }
        driver.quit();
    }

    public void waitUntilUrlChanged(Runnable pageNavigationFunction, long waitSec) {
        var currentUrl = driver.getCurrentUrl();
        pageNavigationFunction.run();
        new WebDriverWait(driver, Duration.ofSeconds(waitSec)).until(ExpectedConditions.not(ExpectedConditions.urlToBe(currentUrl)));
    }

}