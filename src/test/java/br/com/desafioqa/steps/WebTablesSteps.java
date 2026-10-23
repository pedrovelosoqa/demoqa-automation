package br.com.desafioqa.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.desafioqa.driver.DriverManager;
import br.com.desafioqa.model.WebTableRecord;
import br.com.desafioqa.pages.HomePage;
import br.com.desafioqa.pages.WebTablesPage;
import br.com.desafioqa.util.RandomDataGenerator;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WebTablesSteps {

    private WebTablesPage webTablesPage;
    private WebTableRecord createdRecord;
    private WebTableRecord updatedRecord;
    private final List<WebTableRecord> recordsCreatedByScenario = new ArrayList<>();

    @Dado("que acesso a funcionalidade Web Tables")
    public void accessWebTables() {
        HomePage homePage = new HomePage(DriverManager.getDriver());
        homePage.open();
        homePage.selectCard("Elements");
        homePage.selectSubmenu("Web Tables");

        webTablesPage = new WebTablesPage(DriverManager.getDriver());
        assertTrue(webTablesPage.isLoaded(), "Web Tables page should be loaded");
    }

    @Quando("cadastro um registro válido na Web Tables")
    public void createValidRecord() {
        createdRecord = RandomDataGenerator.createWebTableRecord();
        webTablesPage.addRecord(createdRecord);
    }

    @Entao("o registro deve ser encontrado pelo e-mail cadastrado")
    public void recordMustBeFoundByEmail() {
        webTablesPage.search(createdRecord.getEmail());
        assertEquals(
                createdRecord,
                webTablesPage.getRecordByEmail(createdRecord.getEmail()),
                "The row should contain all submitted values");
        webTablesPage.clearSearch();
    }

    @Quando("edito o registro criado com novos dados válidos")
    public void editCreatedRecord() {
        updatedRecord = RandomDataGenerator.createUpdatedWebTableRecord(createdRecord);
        webTablesPage.search(createdRecord.getEmail());
        webTablesPage.openEditForm(createdRecord.getEmail());
        assertTrue(
                webTablesPage.formContains(createdRecord),
                "Edit form should be populated with the selected row values");

        webTablesPage.fillRecordForm(updatedRecord);
        webTablesPage.submitRecordForm();
        webTablesPage.clearSearch();
        webTablesPage.waitForRecord(updatedRecord.getEmail(), true);
        if (!createdRecord.getEmail().equals(updatedRecord.getEmail())) {
            webTablesPage.waitForRecord(createdRecord.getEmail(), false);
        }
    }

    @Entao("a Web Tables deve exibir somente os novos dados para o registro")
    public void tableMustShowUpdatedData() {
        webTablesPage.search(updatedRecord.getEmail());
        assertEquals(
                updatedRecord,
                webTablesPage.getRecordByEmail(updatedRecord.getEmail()),
                "The edited row should contain all updated values");
        webTablesPage.clearSearch();
    }

    @Quando("excluo o registro criado pelo seu e-mail")
    public void deleteCreatedRecordByEmail() {
        String email = updatedRecord == null
                ? createdRecord.getEmail()
                : updatedRecord.getEmail();
        webTablesPage.search(email);
        webTablesPage.deleteRecord(email);
    }

    @Entao("o registro excluído não deve mais ser encontrado")
    public void deletedRecordMustNotBeFound() {
        String email = updatedRecord == null
                ? createdRecord.getEmail()
                : updatedRecord.getEmail();
        assertFalse(webTablesPage.hasRecord(email), "Deleted row should not be present");
        webTablesPage.clearSearch();
    }

    @Quando("cadastro {int} registros aleatórios na Web Tables")
    public void createRandomRecords(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("The quantity must be greater than zero");
        }

        recordsCreatedByScenario.clear();
        Set<String> generatedEmails = new HashSet<>();
        while (recordsCreatedByScenario.size() < quantity) {
            WebTableRecord candidate = RandomDataGenerator.createWebTableRecord();
            if (generatedEmails.add(candidate.getEmail())) {
                webTablesPage.addRecord(candidate);
                recordsCreatedByScenario.add(candidate);
            }
        }
    }

    @Entao("cada registro criado deve ser localizado e validado pelo e-mail")
    public void validateEveryCreatedRecordByEmail() {
        assertFalse(recordsCreatedByScenario.isEmpty(), "No scenario-owned records were created");
        for (WebTableRecord expected : recordsCreatedByScenario) {
            webTablesPage.search(expected.getEmail());
            assertEquals(
                    expected,
                    webTablesPage.getRecordByEmail(expected.getEmail()),
                    "Unexpected data for row with e-mail " + expected.getEmail());
            webTablesPage.clearSearch();
        }
        assertTrue(webTablesPage.isSearchEmpty(), "Search should be cleared after bulk validation");
    }

    @Quando("excluo somente os registros criados neste cenário")
    public void deleteOnlyScenarioOwnedRecords() {
        java.util.List<String> emails = recordsCreatedByScenario.stream()
                .map(WebTableRecord::getEmail)
                .toList();

        webTablesPage.deleteRecordsByEmails(emails);
    }

    @Entao("nenhum registro criado neste cenário deve permanecer na Web Tables")
    public void scenarioOwnedRecordsMustBeAbsent() {
        for (WebTableRecord record : recordsCreatedByScenario) {
            webTablesPage.search(record.getEmail());
            assertFalse(
                    webTablesPage.hasRecord(record.getEmail()),
                    "Scenario-owned row was not deleted: " + record.getEmail());
            webTablesPage.clearSearch();
        }
        assertTrue(webTablesPage.isSearchEmpty(), "Search should be cleared after bulk cleanup");
    }
}
