package br.com.desafioqa.pages;

import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object da área Browser Windows.
 */
public class BrowserWindowsPage extends BasePage {

    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);
    private static final By NEW_WINDOW_BUTTON = By.id("windowButton");
    private static final By SAMPLE_HEADING = By.id("sampleHeading");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public BrowserWindowsPage(WebDriver driver) {
        super(driver);
        this.driver = Objects.requireNonNull(driver, "O WebDriver não pode ser nulo");
        this.wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    public String getCurrentWindowHandle() {
        return driver.getWindowHandle();
    }

    public Set<String> getWindowHandles() {
        return new HashSet<>(driver.getWindowHandles());
    }

    public BrowserWindowsPage clickNewWindowButton() {
        scrollTo(NEW_WINDOW_BUTTON);
        safeClick(NEW_WINDOW_BUTTON);
        return this;
    }

    public String waitForNewWindow(Set<String> previousHandles) {
        Objects.requireNonNull(previousHandles, "A coleção de janelas anteriores não pode ser nula");
        wait.until(ExpectedConditions.numberOfWindowsToBe(previousHandles.size() + 1));
        return driver.getWindowHandles().stream()
                .filter(handle -> !previousHandles.contains(handle))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("A nova janela não foi encontrada"));
    }

    public BrowserWindowsPage switchToWindow(String windowHandle) {
        driver.switchTo().window(windowHandle);
        return this;
    }

    public String getSampleHeadingText() {
        return getText(SAMPLE_HEADING);
    }

    public boolean isWindowOpen(String windowHandle) {
        return driver.getWindowHandles().contains(windowHandle);
    }

    public BrowserWindowsPage closeWindowAndReturn(String windowToClose, String returnWindow) {
        Objects.requireNonNull(windowToClose, "O handle da janela a fechar não pode ser nulo");
        Objects.requireNonNull(returnWindow, "O handle da janela de retorno não pode ser nulo");

        try {
            if (driver.getWindowHandles().contains(windowToClose)) {
                driver.switchTo().window(windowToClose);
                driver.close();
            }
        } finally {
            if (!driver.getWindowHandles().contains(returnWindow)) {
                throw new IllegalStateException("A janela original não está mais disponível");
            }
            driver.switchTo().window(returnWindow);
        }
        return this;
    }
}
