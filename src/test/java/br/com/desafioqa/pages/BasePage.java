package br.com.desafioqa.pages;

import br.com.desafioqa.config.ConfigManager;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(ConfigManager.getExplicitWaitSeconds())
        );
    }

    protected WebElement waitUntilVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitUntilClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected boolean waitUntilInvisible(By locator) {
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    protected List<WebElement> waitUntilAllVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    protected void safeClick(By locator) {
        try {
            waitUntilClickable(locator).click();
        } catch (ElementClickInterceptedException firstInterception) {
            scrollTo(locator);
            try {
                waitUntilClickable(locator).click();
            } catch (ElementClickInterceptedException secondInterception) {
                // O JavaScript é usado apenas como último recurso quando o DemoQA mantém um overlay sobre o alvo.
                WebElement element = waitUntilClickable(locator);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            }
        }
    }

    protected void safeClick(WebElement element) {
        scrollTo(element);
        WebElement clickableElement = wait.until(ExpectedConditions.elementToBeClickable(element));
        try {
            clickableElement.click();
        } catch (ElementClickInterceptedException firstInterception) {
            scrollTo(clickableElement);
            try {
                wait.until(ExpectedConditions.elementToBeClickable(clickableElement)).click();
            } catch (ElementClickInterceptedException secondInterception) {
                // Fallback final para overlays externos que continuam sobre o elemento após o scroll.
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", clickableElement);
            }
        }
    }

    protected void scrollTo(By locator) {
        scrollTo(waitUntilVisible(locator));
    }

    protected void scrollTo(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});",
                element
        );
    }

    protected void fill(By locator, String value) {
        WebElement element = waitUntilVisible(locator);
        element.clear();
        element.sendKeys(value);
    }

    protected String getText(By locator) {
        return waitUntilVisible(locator).getText().trim();
    }

    protected void waitForUrlContains(String expectedPart) {
        wait.until(ExpectedConditions.urlContains(expectedPart));
    }
}
