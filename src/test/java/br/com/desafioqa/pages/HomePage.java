package br.com.desafioqa.pages;

import java.util.Objects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriver;

/**
 * Page Object da página inicial do DemoQA.
 */
public class HomePage extends BasePage {

    private static final String DEFAULT_BASE_URL = "https://demoqa.com";
    private static final By CATEGORY_CARDS = By.cssSelector("div.category-cards");

    private final WebDriver driver;

    public HomePage(WebDriver driver) {
        super(driver);
        this.driver = Objects.requireNonNull(driver, "O WebDriver não pode ser nulo");
    }

    public HomePage open() {
        driver.get(System.getProperty("base.url", DEFAULT_BASE_URL));
        waitUntilVisible(CATEGORY_CARDS);
        return this;
    }

    public HomePage selectCard(String cardName) {
        By card = By.xpath("//div[contains(@class,'category-cards')]"
                + "//div[contains(@class,'card')][.//h5[normalize-space()="
                + xpathLiteral(cardName) + "]]");
        scrollTo(card);
        safeClick(card);
        return this;
    }

    public HomePage selectSubmenu(String submenuName) {
        if ("Progress Bar".equals(submenuName)) {
            openVisibleProgressBarWithRetry();
            return this;
        }

        By submenu = By.xpath("//div[contains(@class,'element-list')"
                + " and contains(concat(' ', normalize-space(@class), ' '), ' show ')]"
                + "//li[contains(@class,'btn-light')]"
                + "[.//span[normalize-space()=" + xpathLiteral(submenuName) + "]]");
        waitUntilVisible(submenu);
        scrollTo(submenu);
        safeClick(submenu);
        return this;
    }

    private void openVisibleProgressBarWithRetry() {
        new org.openqa.selenium.support.ui.WebDriverWait(
                driver, java.time.Duration.ofSeconds(5))
                .until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("/widgets"));

        By progressBarMenuItem = By.xpath("//span[normalize-space()='Progress Bar']");
        By widgetsHeader = By.xpath(
                "//*[contains(@class,'header-text') and normalize-space()='Widgets']"
                        + "/ancestor::*[contains(@class,'group-header')][1]");

        WebElement progressBar = findVisibleElement(progressBarMenuItem);

        if (progressBar == null) {
            WebElement header = findVisibleElement(widgetsHeader);
            if (header == null) {
                throw new org.openqa.selenium.TimeoutException(
                        "O grupo Widgets não foi encontrado para exibir Progress Bar.");
            }

            scrollTo(header);
            safeClick(header);
            new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(3))
                    .until(currentDriver -> findVisibleElement(progressBarMenuItem) != null);
            progressBar = findVisibleElement(progressBarMenuItem);
        }

        clickVisibleProgressBar(progressBar, false);

        if (!waitBrieflyForUrl("/progress-bar")) {
            progressBar = findVisibleElement(progressBarMenuItem);
            if (progressBar == null) {
                throw new org.openqa.selenium.TimeoutException(
                        "Não foi possível relocalizar Progress Bar para a única retentativa.");
            }

            clickVisibleProgressBar(progressBar, true);
            if (!waitBrieflyForUrl("/progress-bar")) {
                throw new org.openqa.selenium.TimeoutException(
                        "Não foi possível navegar de Widgets para Progress Bar.");
            }
        }
    }

    private WebElement findVisibleElement(By locator) {
        for (WebElement element : driver.findElements(locator)) {
            try {
                if (element.isDisplayed()) {
                    return element;
                }
            } catch (org.openqa.selenium.StaleElementReferenceException updatedMenu) {
                // O chamador relocaliza o elemento na próxima tentativa.
            }
        }

        return null;
    }

    private void clickVisibleProgressBar(WebElement item, boolean javascriptFallback) {
        scrollTo(item);

        if (javascriptFallback) {
            try {
                safeClick(item);
            } catch (org.openqa.selenium.WebDriverException clickFailure) {
                ((org.openqa.selenium.JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", item);
            }
            return;
        }

        safeClick(item);
    }

    private boolean waitBrieflyForUrl(String path) {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(3))
                    .until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains(path));
            return true;
        } catch (org.openqa.selenium.TimeoutException navigationPending) {
            return false;
        }
    }

    private static String xpathLiteral(String value) {
        Objects.requireNonNull(value, "O texto do elemento não pode ser nulo");
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }

        String[] parts = value.split("'", -1);
        StringBuilder literal = new StringBuilder("concat(");
        for (int index = 0; index < parts.length; index++) {
            if (index > 0) {
                literal.append(", \"'\", ");
            }
            literal.append("'").append(parts[index]).append("'");
        }
        return literal.append(')').toString();
    }
}
