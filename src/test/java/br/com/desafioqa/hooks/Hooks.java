package br.com.desafioqa.hooks;

import br.com.desafioqa.driver.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Hooks {

    private static final DateTimeFormatter SCREENSHOT_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    @Before("@ui")
    public void startBrowser() {
        DriverManager.startDriver();
    }

    @After("@ui")
    public void finishBrowser(Scenario scenario) {
        try {
            if (scenario.isFailed() && DriverManager.hasDriver()) {
                captureFailureEvidence(scenario);
            }
        } catch (IOException | WebDriverException exception) {
            scenario.log("Falha ao capturar screenshot: " + exception.getMessage());
        } finally {
            DriverManager.quitDriver();
        }
    }

    private void captureFailureEvidence(Scenario scenario) throws IOException {
        byte[] screenshot = ((TakesScreenshot) DriverManager.getDriver())
                .getScreenshotAs(OutputType.BYTES);

        String fileName = sanitize(scenario.getName())
                + "-"
                + LocalDateTime.now().format(SCREENSHOT_TIME)
                + ".png";
        Path directory = Paths.get("target", "screenshots");
        Files.createDirectories(directory);
        Files.write(directory.resolve(fileName), screenshot);

        scenario.attach(screenshot, "image/png", fileName);
    }

    private String sanitize(String scenarioName) {
        return scenarioName.replaceAll("[^\\p{L}\\p{N}._-]+", "_");
    }
}
