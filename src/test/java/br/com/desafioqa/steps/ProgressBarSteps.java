package br.com.desafioqa.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.desafioqa.driver.DriverManager;
import br.com.desafioqa.pages.HomePage;
import br.com.desafioqa.pages.ProgressBarPage;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

public class ProgressBarSteps {

    private ProgressBarPage progressBarPage;
    private int stoppedValue;
    private int completedValue;

    @Dado("que acesso a funcionalidade Progress Bar")
    public void accessProgressBar() {
        HomePage homePage = new HomePage(DriverManager.getDriver());
        homePage.open();
        homePage.selectCard("Widgets");
        homePage.selectSubmenu("Progress Bar");

        progressBarPage = new ProgressBarPage(DriverManager.getDriver());
        assertTrue(progressBarPage.isLoaded(), "Progress Bar page should be loaded");
        assertEquals(0, progressBarPage.getProgressValue(), "Progress should start at zero");
    }

    @Quando("inicio e interrompo a barra ao atingir pelo menos {int} por cento")
    public void startAndStopAtMinimum(int minimum) {
        stoppedValue = progressBarPage.startAndStopAtOrAbove(minimum, 25);
    }

    @Entao("a barra deve permanecer parada acima de zero e em no máximo {int} por cento")
    public void progressMustRemainInsideSafeInterval(int maximum) {
        int confirmedStableValue = progressBarPage.waitUntilStable(4);
        assertEquals(
                stoppedValue,
                confirmedStableValue,
                "Progress value should no longer change after Stop");
        assertTrue(stoppedValue > 0, "Stopped progress should be greater than zero");
        assertTrue(
                stoppedValue <= maximum,
                "Stopped progress should be at most " + maximum + "%, but was " + stoppedValue + "%");
    }

    @Quando("reinicio a barra e aguardo a conclusão")
    public void resumeAndWaitForCompletion() {
        completedValue = progressBarPage.resumeAndWaitUntilComplete();
    }

    @Entao("a barra deve atingir exatamente {int} por cento")
    public void progressMustReachExactValue(int expected) {
        assertEquals(expected, completedValue, "Unexpected final Progress Bar value");
        assertEquals(expected, progressBarPage.getProgressValue(), "Visible progress value should match");
    }

    @Quando("aciono o reset da barra de progresso")
    public void resetProgressBar() {
        progressBarPage.reset();
    }

    @Entao("a barra de progresso deve voltar para {int} por cento")
    public void progressMustReturnTo(int expected) {
        assertEquals(expected, progressBarPage.getProgressValue(), "Unexpected value after reset");
    }
}
