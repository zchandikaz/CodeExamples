package org.example;

import io.cucumber.testng.*;

@CucumberOptions(
        glue = "org.example.stepdefs",
        features = "src/test/resources/features/test",
        plugin = {
                "pretty",
                "html:reports/tests/cucumber/cucumber-pretty.html",
                "testng:reports/tests/cucumber/testng/cucumber.xml",
                "json:reports/tests/cucumber/json/cucumberTestReport.json"
        }
)
public class RunWebDriverCucumberTests extends AbstractTestNGCucumberTests {}
