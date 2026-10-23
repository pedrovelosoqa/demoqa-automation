package br.com.desafioqa.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Set;

import br.com.desafioqa.driver.DriverManager;
import br.com.desafioqa.pages.BrowserWindowsPage;
import br.com.desafioqa.pages.HomePage;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public class BrowserWindowsSteps {

    private HomePage homePage;
    private BrowserWindowsPage browserWindowsPage;
    private String originalWindow;
    private String newWindow;

    @Dado("que acesso a página Browser Windows")
    public void accessBrowserWindowsPage() {
        homePage = new HomePage(DriverManager.getDriver());
        browserWindowsPage = new BrowserWindowsPage(DriverManager.getDriver());

        homePage.open()
                .selectCard("Alerts, Frame & Windows")
                .selectSubmenu("Browser Windows");
    }

    @Quando("abro uma nova janela")
    public void openNewWindow() {
        originalWindow = browserWindowsPage.getCurrentWindowHandle();
        Set<String> previousHandles = browserWindowsPage.getWindowHandles();

        browserWindowsPage.clickNewWindowButton();
        newWindow = browserWindowsPage.waitForNewWindow(previousHandles);
        assertNotEquals(originalWindow, newWindow, "Uma janela diferente deveria ter sido aberta");
        browserWindowsPage.switchToWindow(newWindow);
    }

    @Então("a nova janela deve exibir a mensagem {string}")
    public void newWindowShouldShowMessage(String expectedMessage) {
        assertEquals(expectedMessage, browserWindowsPage.getSampleHeadingText());
    }

    @E("fecho a nova janela e retorno para a janela original")
    public void closeNewWindowAndReturnToOriginalWindow() {
        browserWindowsPage.closeWindowAndReturn(newWindow, originalWindow);

        assertEquals(originalWindow, browserWindowsPage.getCurrentWindowHandle());
        assertFalse(browserWindowsPage.isWindowOpen(newWindow), "A nova janela deveria estar fechada");
    }
}
