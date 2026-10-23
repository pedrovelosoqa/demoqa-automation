package br.com.desafioqa.pages;

import java.util.LinkedHashSet;
import java.util.Set;

import br.com.desafioqa.model.WebTableRecord;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page object for Elements / Web Tables. */
public class WebTablesPage extends BasePage {

    private static final By PAGE_TITLE = By.xpath("//h1[normalize-space()='Web Tables']");
    private static final By ADD_BUTTON = By.id("addNewRecordButton");
    private static final By SEARCH_INPUT = By.id("searchBox");
    private static final By TABLE = By.cssSelector(".web-tables-wrapper table");
    private static final By TABLE_ROWS = By.cssSelector(".web-tables-wrapper tbody tr");
    private static final By ROW_CELLS = By.cssSelector("td");
    private static final By MODAL = By.cssSelector(".modal.show .modal-content");
    private static final By FIRST_NAME_INPUT = By.id("firstName");
    private static final By LAST_NAME_INPUT = By.id("lastName");
    private static final By EMAIL_INPUT = By.id("userEmail");
    private static final By AGE_INPUT = By.id("age");
    private static final By SALARY_INPUT = By.id("salary");
    private static final By DEPARTMENT_INPUT = By.id("department");
    private static final By SUBMIT_BUTTON = By.id("submit");
    private static final By EDIT_IN_ROW = By.cssSelector("[id^='edit-record-']");
    private static final By DELETE_IN_ROW = By.cssSelector("[id^='delete-record-']");

    private static final int FIRST_NAME_COLUMN = 0;
    private static final int LAST_NAME_COLUMN = 1;
    private static final int AGE_COLUMN = 2;
    private static final int EMAIL_COLUMN = 3;
    private static final int SALARY_COLUMN = 4;
    private static final int DEPARTMENT_COLUMN = 5;

    private final WebDriverWait pageWait;

    public WebTablesPage(WebDriver driver) {
        super(driver);
        this.pageWait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public boolean isLoaded() {
        return waitUntilVisible(PAGE_TITLE).isDisplayed()
                && waitUntilVisible(TABLE).isDisplayed();
    }

    public void openNewRecordForm() {
        safeClick(ADD_BUTTON);
        waitUntilVisible(MODAL);
    }

    public void fillRecordForm(WebTableRecord record) {
        fill(FIRST_NAME_INPUT, record.getFirstName());
        fill(LAST_NAME_INPUT, record.getLastName());
        fill(EMAIL_INPUT, record.getEmail());
        fill(AGE_INPUT, Integer.toString(record.getAge()));
        fill(SALARY_INPUT, Integer.toString(record.getSalary()));
        fill(DEPARTMENT_INPUT, record.getDepartment());
    }

    public void submitRecordForm() {
        safeClick(SUBMIT_BUTTON);
        pageWait.withMessage("The Web Tables form did not close after submission")
                .until(driver -> driver.findElements(MODAL).isEmpty());
    }

    public void addRecord(WebTableRecord record) {
        clearSearch();
        openNewRecordForm();
        fillRecordForm(record);
        submitRecordForm();
        // Searching by the unique e-mail also makes records beyond the first
        // pagination page visible during the bulk scenario.
        search(record.getEmail());
        waitForRecord(record.getEmail(), true);
        clearSearch();
    }

    public void search(String value) {
        clearSearch();
        WebElement input = waitUntilVisible(SEARCH_INPUT);
        pageWait.withMessage("The Web Tables search field is not enabled")
                .until(driver -> driver.findElement(SEARCH_INPUT).isEnabled());

        input.click();
        input.sendKeys(Keys.CONTROL, "a");
        input.sendKeys(Keys.DELETE);

        pageWait.withMessage("The Web Tables search field could not be cleared")
                .until(driver -> driver.findElement(SEARCH_INPUT).getAttribute("value").isEmpty());

        input = waitUntilVisible(SEARCH_INPUT);
        input.sendKeys(value);
        pageWait.withMessage("The Web Tables search field was not updated to: " + value)
                .until(driver -> value.equals(driver.findElement(SEARCH_INPUT).getAttribute("value")));
        waitForTableToReflectSearch(value);
    }

    public void clearSearch() {
        WebElement input = waitUntilVisible(SEARCH_INPUT);
        pageWait.withMessage("The Web Tables search field is not enabled")
                .until(driver -> driver.findElement(SEARCH_INPUT).isEnabled());

        input.click();
        input.sendKeys(Keys.CONTROL, "a");
        input.sendKeys(Keys.DELETE);

        pageWait.withMessage("The Web Tables search field was not cleared")
                .until(driver -> driver.findElement(SEARCH_INPUT).getAttribute("value").isEmpty());
        pageWait.withMessage("The Web Tables table did not update after clearing the search")
                .until(driver -> !driver.findElements(TABLE_ROWS).isEmpty());
    }

    public boolean isSearchEmpty() {
        return waitUntilVisible(SEARCH_INPUT).getAttribute("value").isEmpty();
    }

    public boolean hasRecord(String email) {
        return findRowByEmail(email).isPresent();
    }

    private boolean rowExistsByEmail(String email) {
        return findRowByEmail(email).isPresent();
    }

    private Set<String> findEmailsStillPresentInTable(Set<String> candidateEmails) {
        Set<String> found = new LinkedHashSet<>();

        if (candidateEmails.isEmpty()) {
            return found;
        }

        resetSearchFilterForBatch();
        ensureFirstPage();

        for (int page = 0; page < 20; page++) {
            for (WebElement row : driver.findElements(TABLE_ROWS)) {
                List<WebElement> cells = row.findElements(By.tagName("td"));

                if (!isDataRow(cells)) {
                    continue;
                }

                for (WebElement cell : cells) {
                    String value = cell.getText().trim();

                    if (candidateEmails.contains(value)) {
                        found.add(value);
                    }
                }
            }

            if (found.containsAll(candidateEmails)) {
                break;
            }

            WebElement next = findVisibleNextButton();

            if (next == null || !isPaginationButtonEnabled(next)) {
                break;
            }

            goToNextPageAndWaitForChange(next);
        }

        return found;
    }

    private void resetSearchFilterForBatch() {
        By batchSearchBox = By.id("searchBox");
        String temporaryValue = "__batch_reset__";
        WebElement input = waitUntilVisible(batchSearchBox);

        input.click();
        input.sendKeys(org.openqa.selenium.Keys.CONTROL, "a");
        input.sendKeys(temporaryValue);

        wait.until(currentDriver -> temporaryValue.equals(
                currentDriver.findElement(batchSearchBox).getAttribute("value")));

        input = driver.findElement(batchSearchBox);
        input.sendKeys(org.openqa.selenium.Keys.CONTROL, "a");
        input.sendKeys(org.openqa.selenium.Keys.BACK_SPACE);
        input.sendKeys(org.openqa.selenium.Keys.TAB);

        wait.until(currentDriver -> {
            String value = currentDriver.findElement(batchSearchBox).getAttribute("value");
            return value != null && value.isEmpty();
        });

        waitForUnfilteredTableForBatch();
    }

    private void waitForUnfilteredTableForBatch() {
        try {
            wait.until(currentDriver -> {
                long realRows = 0;

                for (WebElement row : currentDriver.findElements(TABLE_ROWS)) {
                    try {
                        java.util.List<WebElement> cells = row.findElements(ROW_CELLS);
                        if (isDataRow(cells)) {
                            realRows++;
                        }
                    } catch (org.openqa.selenium.StaleElementReferenceException updatedTable) {
                        return false;
                    }
                }

                return realRows > 1;
            });
        } catch (org.openqa.selenium.TimeoutException stillFiltered) {
            throw new org.openqa.selenium.TimeoutException(
                    "O SearchBox foi visualmente limpo, mas a Web Tables permaneceu em estado filtrado antes do batch.",
                    stillFiltered);
        }
    }

    public void deleteRecordsByEmails(java.util.Collection<String> emails) {
        java.util.LinkedHashSet<String> remainingEmails =
                new java.util.LinkedHashSet<>(emails);


        while (!remainingEmails.isEmpty()) {

            resetSearchFilterForBatch();
            ensureFirstPage();

            boolean deletedOne = false;

            for (int page = 0; page < 20; page++) {
                WebElement matchedRow = findRowContainingAnyEmail(remainingEmails);

                if (matchedRow != null) {
                    String matchedEmail = findScenarioEmailInRow(
                            matchedRow, remainingEmails);
                    WebElement deleteButton = matchedRow.findElement(
                            By.cssSelector("[id^='delete-record-']"));

                    safeClick(deleteButton);
                    waitForTableRerenderAfterDelete(matchedRow, matchedEmail);
                remainingEmails.remove(matchedEmail);

                // Reconsulta a tabela porque o rerender do DemoQA pode remover
                // mais de um registro criado após um único clique em Delete.
                Set<String> emailsStillPresent =
                        findEmailsStillPresentInTable(remainingEmails);
                remainingEmails.retainAll(emailsStillPresent);


                    deletedOne = true;
                    break;
                }

                WebElement next = findVisibleNextButton();
                if (!isPaginationButtonEnabled(next)) {
                    break;
                }

                goToNextPageAndWaitForChange(next);
            }

        if (!deletedOne) {
            Set<String> emailsStillPresent =
                    findEmailsStillPresentInTable(remainingEmails);
            remainingEmails.retainAll(emailsStillPresent);

            if (remainingEmails.isEmpty()) {
                break;
            }
                throw new AssertionError(
                        "Não foi possível localizar os registros pendentes na Web Tables: "
                                + remainingEmails);
            }
        }

        wait.until(currentDriver -> {
            String value = currentDriver.findElement(By.id("searchBox")).getAttribute("value");
            return value != null && value.isEmpty();
        });
    }

    private void logBatchState(
            String moment,
            java.util.Set<String> remainingEmails) {
        System.out.println("[BONUS-DEBUG] ===== " + moment + " =====");

        By batchSearchBox = By.id("searchBox");
        String searchValue = driver.findElement(batchSearchBox).getAttribute("value");
        System.out.println("[BONUS-DEBUG] search.value='"
                + (searchValue == null ? "" : searchValue) + "'");

        java.util.List<WebElement> rows = driver.findElements(TABLE_ROWS);
        long realRows = 0;
        java.util.LinkedHashSet<String> visibleEmails = new java.util.LinkedHashSet<>();

        for (WebElement row : rows) {
            try {
                java.util.List<WebElement> cells = row.findElements(ROW_CELLS);
                if (isDataRow(cells)) {
                    realRows++;
                }

                for (WebElement cell : cells) {
                    String value = cell.getText().trim();
                    if (value.contains("@")) {
                        visibleEmails.add(value);
                    }
                }
            } catch (org.openqa.selenium.StaleElementReferenceException updatedTable) {
                System.out.println("[BONUS-DEBUG] row.state=STALE_DURING_SNAPSHOT");
            }
        }

        java.util.LinkedHashSet<String> visiblePendingEmails =
                new java.util.LinkedHashSet<>(visibleEmails);
        visiblePendingEmails.retainAll(remainingEmails);

        System.out.println("[BONUS-DEBUG] rows.total=" + rows.size());
        System.out.println("[BONUS-DEBUG] rows.real=" + realRows);
        System.out.println("[BONUS-DEBUG] visibleEmails=" + visibleEmails);
        System.out.println("[BONUS-DEBUG] remaining.count=" + remainingEmails.size());
        System.out.println("[BONUS-DEBUG] remainingEmails=" + remainingEmails);
        System.out.println("[BONUS-DEBUG] visiblePending=" + visiblePendingEmails);

        logPaginationButtonState("previous", findVisiblePreviousButton());
        logPaginationButtonState("next", findVisibleNextButton());

        String currentPage = readVisiblePaginationValue(
                By.cssSelector("input[aria-label='jump to page'], .-pageJump input"),
                "value");
        String totalPages = readVisiblePaginationValue(
                By.cssSelector(".-totalPages"),
                null);
        System.out.println("[BONUS-DEBUG] currentPage=" + currentPage);
        System.out.println("[BONUS-DEBUG] totalPages=" + totalPages);

        String fingerprint;
        try {
            fingerprint = currentVisibleRowsSignature();
        } catch (org.openqa.selenium.StaleElementReferenceException updatedTable) {
            fingerprint = "RERENDERING";
        }
        if (fingerprint.length() > 500) {
            fingerprint = fingerprint.substring(0, 500) + "...";
        }
        System.out.println("[BONUS-DEBUG] fingerprint=" + fingerprint);
    }

    private void logPaginationButtonState(String name, WebElement button) {
        System.out.println("[BONUS-DEBUG] " + name + ".present=" + (button != null));

        if (button == null) {
            System.out.println("[BONUS-DEBUG] " + name + ".enabled=false");
            System.out.println("[BONUS-DEBUG] " + name + ".disabled=null");
            System.out.println("[BONUS-DEBUG] " + name + ".ariaDisabled=null");
            System.out.println("[BONUS-DEBUG] " + name + ".class=null");
            return;
        }

        try {
            System.out.println("[BONUS-DEBUG] " + name + ".enabled=" + button.isEnabled());
            System.out.println("[BONUS-DEBUG] " + name + ".disabled="
                    + button.getAttribute("disabled"));
            System.out.println("[BONUS-DEBUG] " + name + ".ariaDisabled="
                    + button.getAttribute("aria-disabled"));
            System.out.println("[BONUS-DEBUG] " + name + ".class="
                    + button.getAttribute("class"));
        } catch (org.openqa.selenium.StaleElementReferenceException updatedPagination) {
            System.out.println("[BONUS-DEBUG] " + name + ".state=STALE");
        }
    }

    private String readVisiblePaginationValue(By locator, String attribute) {
        for (WebElement element : driver.findElements(locator)) {
            try {
                if (!element.isDisplayed()) {
                    continue;
                }

                String value = attribute == null
                        ? element.getText().trim()
                        : element.getAttribute(attribute);
                return value == null || value.isBlank() ? "UNKNOWN" : value;
            } catch (org.openqa.selenium.StaleElementReferenceException updatedPagination) {
                // Tenta outra ocorrência visível, se existir.
            }
        }

        return "UNKNOWN";
    }

    private WebElement findRowContainingAnyEmail(java.util.Set<String> remainingEmails) {
        for (WebElement row : driver.findElements(TABLE_ROWS)) {
            try {
                if (findScenarioEmailInRow(row, remainingEmails) != null) {
                    return row;
                }
            } catch (org.openqa.selenium.StaleElementReferenceException updatedTable) {
                return null;
            }
        }

        return null;
    }

    private void waitForTableRerenderAfterDelete(WebElement deletedRow, String email) {
        wait.until(currentDriver -> {
            try {
                if (!deletedRow.isDisplayed()) {
                    return true;
                }
            } catch (org.openqa.selenium.StaleElementReferenceException rerendered) {
                return true;
            }

            return findRowByEmail(email).isEmpty();
        });
    }

    private void goToNextPageAndWaitForChange(WebElement nextButton) {
        String previousPageSignature = currentVisibleRowsSignature();
        safeClick(nextButton);
        wait.until(currentDriver ->
                !previousPageSignature.equals(currentVisibleRowsSignature()));
    }

    private String findScenarioEmailInRow(
            WebElement row,
            java.util.Set<String> remainingEmails) {
        for (WebElement cell : row.findElements(By.tagName("td"))) {
            String value = cell.getText().trim();
            if (remainingEmails.contains(value)) {
                return value;
            }
        }

        return null;
    }

    private WebElement findVisibleNextButton() {
        By nextButton = By.xpath(
                "//button[normalize-space()='Next'"
                        + " or @aria-label='Next Page'"
                        + " or @aria-label='Next'"
                        + " or @title='Next']");

        for (WebElement button : driver.findElements(nextButton)) {
            try {
                if (button.isDisplayed()) {
                    return button;
                }
            } catch (org.openqa.selenium.StaleElementReferenceException updatedPagination) {
                // A próxima varredura relocaliza o controle.
            }
        }

        return null;
    }

    private boolean isPaginationButtonEnabled(WebElement button) {
        if (button == null || !button.isEnabled()) {
            return false;
        }

        String classes = button.getAttribute("class");
        return button.getAttribute("disabled") == null
                && !"true".equalsIgnoreCase(button.getAttribute("aria-disabled"))
                && (classes == null
                        || !(" " + classes.trim() + " ").contains(" disabled "));
    }

    private String currentVisibleRowsSignature() {
        StringBuilder signature = new StringBuilder();

        for (WebElement row : driver.findElements(TABLE_ROWS)) {
            for (WebElement cell : row.findElements(By.tagName("td"))) {
                signature.append(cell.getText().trim()).append('|');
            }
            signature.append(';');
        }

        return signature.toString();
    }

    private void ensureFirstPage() {
        for (int attempt = 0; attempt < 10; attempt++) {
            WebElement previous = findVisiblePreviousButton();

            if (previous == null
                    || !previous.isEnabled()
                    || "true".equalsIgnoreCase(previous.getAttribute("aria-disabled"))
                    || previous.getAttribute("disabled") != null) {
                return;
            }

            scrollTo(previous);
            safeClick(previous);
        }

        WebElement previous = findVisiblePreviousButton();
        if (previous != null && previous.isEnabled()
                && !"true".equalsIgnoreCase(previous.getAttribute("aria-disabled"))
                && previous.getAttribute("disabled") == null) {
            throw new org.openqa.selenium.TimeoutException(
                    "Não foi possível retornar a Web Tables para a primeira página após 10 tentativas.");
        }
    }

    private WebElement findVisiblePreviousButton() {
        By previousButton = By.xpath(
                "//button[normalize-space()='Previous'"
                        + " or @aria-label='Previous Page'"
                        + " or @aria-label='Previous'"
                        + " or @title='Previous']");

        for (WebElement button : driver.findElements(previousButton)) {
            try {
                if (button.isDisplayed()) {
                    return button;
                }
            } catch (org.openqa.selenium.StaleElementReferenceException updatedPagination) {
                // A próxima iteração relocaliza o controle.
            }
        }

        return null;
    }

    public void deleteVisibleRecordByEmail(String email) {
        WebElement row = waitForRowByEmail(email);
        WebElement deleteButton = row.findElement(
                By.cssSelector("[id^='delete-record-']"));

        safeClick(deleteButton);
        wait.until(currentDriver -> !rowExistsByEmail(email));
    }

    public boolean isRecordVisibleByEmail(String email) {
        return rowExistsByEmail(email);
    }

    public WebElement waitForRowByEmail(String email) {
        org.openqa.selenium.support.ui.WebDriverWait rowWait =
                new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));

        try {
            return rowWait.until(currentDriver -> findRowByEmail(email).orElse(null));
        } catch (org.openqa.selenium.TimeoutException timeout) {
            throw new org.openqa.selenium.TimeoutException(
                    "Nenhuma linha da Web Tables foi encontrada para o e-mail: " + email,
                    timeout);
        }
    }

    private WebElement waitForRowByEmailUsingLegacyRowText(String email) {
        return pageWait.withMessage("No Web Tables row appeared for e-mail: " + email)
                .ignoring(StaleElementReferenceException.class)
                .until(driver -> findRowByEmail(email).orElse(null));
    }

    public Optional<WebTableRecord> findRecordByEmail(String email) {
        return findRowByEmail(email).map(this::toRecord);
    }

    public WebTableRecord getRecordByEmail(String email) {
        waitForRecord(email, true);
        return findRecordByEmail(email).orElseThrow(() ->
                new NoSuchElementException("No Web Tables row found for e-mail: " + email));
    }

    public List<WebTableRecord> getVisibleRecords() {
        List<WebTableRecord> records = new ArrayList<>();
        for (WebElement row : driver.findElements(TABLE_ROWS)) {
            List<WebElement> cells = row.findElements(ROW_CELLS);
            if (isDataRow(cells)) {
                records.add(toRecord(cells));
            }
        }
        return records;
    }

    public void openEditForm(String email) {
        // Resolve the row by its business key, independently of pagination/order.
        search(email);
        WebElement row = requireRowByEmail(email);
        WebElement editButton = row.findElement(EDIT_IN_ROW);
        safeClick(editButton);
        waitUntilVisible(MODAL);
    }

    public boolean formContains(WebTableRecord expected) {
        waitUntilVisible(MODAL);
        return expected.getFirstName().equals(inputValue(FIRST_NAME_INPUT))
                && expected.getLastName().equals(inputValue(LAST_NAME_INPUT))
                && expected.getEmail().equals(inputValue(EMAIL_INPUT))
                && Integer.toString(expected.getAge()).equals(inputValue(AGE_INPUT))
                && Integer.toString(expected.getSalary()).equals(inputValue(SALARY_INPUT))
                && expected.getDepartment().equals(inputValue(DEPARTMENT_INPUT));
    }

    public void editRecord(String currentEmail, WebTableRecord updatedRecord) {
        openEditForm(currentEmail);
        fillRecordForm(updatedRecord);
        submitRecordForm();

        // Validate each identity under its own e-mail filter. This remains reliable
        // when the table has enough rows to paginate and when an edit changes e-mail.
        search(updatedRecord.getEmail());
        waitForRecord(updatedRecord.getEmail(), true);
        clearSearch();
        if (!currentEmail.equals(updatedRecord.getEmail())) {
            search(currentEmail);
            waitForRecord(currentEmail, false);
            clearSearch();
        }
    }

    private WebElement searchAndRequireRow(String email) {
        org.openqa.selenium.TimeoutException firstFailure = null;

        for (int attempt = 0; attempt < 2; attempt++) {
            clearSearch();
            waitUntilTableReturnsFromFilter();
            search(email);

            try {
                return waitForRowByEmail(email);
            } catch (org.openqa.selenium.TimeoutException notFound) {
                firstFailure = notFound;
            }
        }

        throw new org.openqa.selenium.TimeoutException(
                "Nenhuma linha da Web Tables foi encontrada após reaplicar o filtro para o e-mail: "
                        + email,
                firstFailure);
    }

    private void waitUntilTableReturnsFromFilter() {
        wait.until(currentDriver -> {
            try {
                boolean hasDataRow = currentDriver.findElements(TABLE_ROWS).stream()
                        .map(row -> row.findElements(ROW_CELLS))
                        .anyMatch(cells -> cells.stream()
                                .anyMatch(cell -> !cell.getText().trim().isEmpty()));
                boolean noRowsFound = currentDriver
                        .findElements(By.xpath("//*[normalize-space()='No rows found']"))
                        .stream()
                        .anyMatch(WebElement::isDisplayed);

                return hasDataRow && !noRowsFound;
            } catch (org.openqa.selenium.StaleElementReferenceException tableUpdating) {
                return false;
            }
        });
    }

    public void deleteRecord(String email) {
        WebElement row = searchAndRequireRow(email);
        WebElement deleteButton = row.findElement(By.cssSelector("span[id^='delete-record-']"));
        safeClick(deleteButton);

        org.openqa.selenium.support.ui.WebDriverWait deletionWait =
                new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
        deletionWait.until(currentDriver -> !rowExistsByEmail(email));

        clearSearch();
        waitUntilTableReturnsFromFilter();
    }

    private void deleteRecordUsingLegacyFlow(String email) {
        clearSearch();
        // Filtering first ensures the action targets the e-mail requested even when
        // the table is sorted differently or the row lives on another page.
        search(email);
        WebElement row = waitForRowByEmail(email);
        WebElement deleteButton = row.findElement(DELETE_IN_ROW);
        safeClick(deleteButton);
        waitForRecord(email, false);
        clearSearch();
    }

    public void waitForRecord(String email, boolean expectedToExist) {
        pageWait.withMessage(() -> "Expected row for e-mail '" + email + "' to "
                        + (expectedToExist ? "exist" : "be absent") + " in Web Tables")
                .ignoring(StaleElementReferenceException.class)
                .until(driver -> hasRecord(email) == expectedToExist);
    }

    private WebElement requireRowByEmail(String email) {
        return waitForRowByEmail(email);
    }

    private Optional<WebElement> findRowByEmail(String email) {
        for (WebElement row : driver.findElements(TABLE_ROWS)) {
            List<WebElement> cells = row.findElements(ROW_CELLS);
            if (isDataRow(cells) && email.equals(cells.get(EMAIL_COLUMN).getText().trim())) {
                return Optional.of(row);
            }
        }
        return Optional.empty();
    }

    private void waitForTableToReflectSearch(String email) {
        pageWait.withMessage("The Web Tables table did not reflect the search for: " + email)
                .ignoring(StaleElementReferenceException.class)
                .until(driver -> {
                    List<WebElement> dataRows = driver.findElements(TABLE_ROWS).stream()
                            .filter(row -> isDataRow(row.findElements(ROW_CELLS)))
                            .toList();
                    if (dataRows.isEmpty()) {
                        return true;
                    }
                    return dataRows.stream().allMatch(row -> {
                        List<WebElement> cells = row.findElements(ROW_CELLS);
                        return email.equals(cells.get(EMAIL_COLUMN).getText().trim());
                    });
                });
    }

    private WebTableRecord toRecord(WebElement row) {
        return toRecord(row.findElements(ROW_CELLS));
    }

    private WebTableRecord toRecord(List<WebElement> cells) {
        if (!isDataRow(cells)) {
            throw new IllegalArgumentException("The selected Web Tables row is empty or incomplete");
        }
        return new WebTableRecord(
                textAt(cells, FIRST_NAME_COLUMN),
                textAt(cells, LAST_NAME_COLUMN),
                textAt(cells, EMAIL_COLUMN),
                integerAt(cells, AGE_COLUMN),
                integerAt(cells, SALARY_COLUMN),
                textAt(cells, DEPARTMENT_COLUMN));
    }

    private boolean isDataRow(List<WebElement> cells) {
        return cells.size() > DEPARTMENT_COLUMN
                && !cells.get(EMAIL_COLUMN).getText().trim().isEmpty();
    }

    private String textAt(List<WebElement> cells, int index) {
        return cells.get(index).getText().trim();
    }

    private int integerAt(List<WebElement> cells, int index) {
        String rawValue = textAt(cells, index).replaceAll("[^0-9-]", "");
        return Integer.parseInt(rawValue);
    }

    private String inputValue(By locator) {
        return driver.findElement(locator).getAttribute("value");
    }

}
