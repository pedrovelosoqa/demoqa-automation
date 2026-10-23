package br.com.desafioqa.driver;

import br.com.desafioqa.config.ConfigManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.util.Locale;

public final class DriverManager {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
    }

    public static void startDriver() {
        if (DRIVER.get() != null) {
            throw new IllegalStateException("Já existe um WebDriver iniciado para esta thread.");
        }

        String browser = ConfigManager.getBrowser().toLowerCase(Locale.ROOT);
        WebDriver driver = switch (browser) {
            case "chrome" -> createChromeDriver();
            case "edge" -> createEdgeDriver();
            default -> throw new IllegalArgumentException(
                    "Navegador não suportado: " + browser + ". Use chrome ou edge."
            );
        };

        if (!ConfigManager.isHeadless()) {
            driver.manage().window().maximize();
        }
        DRIVER.set(driver);
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException(
                    "WebDriver não iniciado. O cenário de interface deve possuir a tag @ui."
            );
        }
        return driver;
    }

    public static boolean hasDriver() {
        return DRIVER.get() != null;
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            return;
        }
        try {
            driver.quit();
        } finally {
            DRIVER.remove();
        }
    }

    private static WebDriver createChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications");
        if (ConfigManager.isHeadless()) {
            options.addArguments("--headless=new", "--window-size=1920,1080");
        }
        return new ChromeDriver(options);
    }

    private static WebDriver createEdgeDriver() {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--disable-notifications");
        if (ConfigManager.isHeadless()) {
            options.addArguments("--headless=new", "--window-size=1920,1080");
        }
        return new EdgeDriver(options);
    }
}
