package br.com.desafioqa.pages;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import br.com.desafioqa.model.StudentData;

/**
 * Page Object do formulário de cadastro de estudante.
 */
public class PracticeFormPage extends BasePage {

    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    private static final By FIRST_NAME = By.id("firstName");
    private static final By LAST_NAME = By.id("lastName");
    private static final By EMAIL = By.id("userEmail");
    private static final By MOBILE = By.id("userNumber");
    private static final By DATE_OF_BIRTH = By.id("dateOfBirthInput");
    private static final By SUBJECTS = By.id("subjectsInput");
    private static final By PICTURE = By.id("uploadPicture");
    private static final By CURRENT_ADDRESS = By.id("currentAddress");
    private static final By STATE_INPUT = By.id("react-select-3-input");
    private static final By CITY_INPUT = By.id("react-select-4-input");
    private static final By SUBMIT = By.id("submit");
    private static final By ACTIVE_MODAL = By.xpath(
            "//*[@id='example-modal-sizes-title-lg']"
                    + "/ancestor::div[contains(concat(' ', normalize-space(@class), ' '), ' modal ')][1]");
    private static final By CONFIRMATION_TITLE = By.id("example-modal-sizes-title-lg");
    private static final By MODAL_BACKDROP = By.cssSelector(".modal-backdrop.show");
    private static final By MODAL_TITLE = By.id("example-modal-sizes-title-lg");
    private static final By CLOSE_MODAL = By.id("closeLargeModal");
    private static final By MONTH_SELECT = By.cssSelector("select.react-datepicker__month-select");
    private static final By YEAR_SELECT = By.cssSelector("select.react-datepicker__year-select");

    private static final Map<String, String> GENDER_IDS = Map.of(
            "Male", "gender-radio-1",
            "Female", "gender-radio-2",
            "Other", "gender-radio-3");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public PracticeFormPage(WebDriver driver) {
        super(driver);
        this.driver = Objects.requireNonNull(driver, "O WebDriver não pode ser nulo");
        this.wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    public PracticeFormPage fillForm(StudentData student, Path picturePath) {
        Objects.requireNonNull(student, "Os dados do estudante não podem ser nulos");
        Objects.requireNonNull(picturePath, "O caminho da imagem não pode ser nulo");

        fill(FIRST_NAME, student.getFirstName());
        fill(LAST_NAME, student.getLastName());
        fill(EMAIL, student.getEmail());
        selectGender(student.getGender());
        fill(MOBILE, student.getMobile());
        selectDateOfBirth(student.getDateOfBirth());
        selectSubject(student.getSubject());
        selectHobby(student.getHobby());
        uploadPicture(picturePath);
        fill(CURRENT_ADDRESS, student.getCurrentAddress());
        selectStateAndCity(student.getState(), student.getCity());
        return this;
    }

    public PracticeFormPage selectGender(String gender) {
        String inputId = GENDER_IDS.get(gender);
        if (inputId == null) {
            throw new IllegalArgumentException("Gênero não suportado pelo formulário: " + gender);
        }

        By option = By.cssSelector("label[for='" + inputId + "']");
        scrollTo(option);
        safeClick(option);
        return this;
    }

    public PracticeFormPage selectDateOfBirth(LocalDate dateOfBirth) {
        Objects.requireNonNull(dateOfBirth, "A data de nascimento não pode ser nula");

        scrollTo(DATE_OF_BIRTH);
        safeClick(DATE_OF_BIRTH);
        new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(MONTH_SELECT)))
                .selectByValue(String.valueOf(dateOfBirth.getMonthValue() - 1));
        new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(YEAR_SELECT)))
                .selectByVisibleText(String.valueOf(dateOfBirth.getYear()));

        By day = By.xpath("//div[contains(@class,'react-datepicker__day')"
                + " and not(contains(@class,'react-datepicker__day--outside-month'))"
                + " and normalize-space()='" + dateOfBirth.getDayOfMonth() + "']");
        safeClick(day);
        return this;
    }

    public PracticeFormPage selectSubject(String subject) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(SUBJECTS));
        input.sendKeys(subject);
        By suggestion = By.xpath("//div[contains(@class,'subjects-auto-complete__option')"
                + " and normalize-space()='" + subject + "']");
        safeClick(suggestion);
        return this;
    }

    public PracticeFormPage selectHobby(String hobby) {
        By option = By.xpath("//div[@id='hobbiesWrapper']//label[normalize-space()='" + hobby + "']");
        scrollTo(option);
        safeClick(option);
        return this;
    }

    public PracticeFormPage uploadPicture(Path picturePath) {
        driver.findElement(PICTURE).sendKeys(picturePath.toAbsolutePath().toString());
        return this;
    }

    public PracticeFormPage selectStateAndCity(String state, String city) {
        selectReactOption(By.id("state"), STATE_INPUT, state);
        selectReactOption(By.id("city"), CITY_INPUT, city);
        return this;
    }

    public PracticeFormPage submit() {
        scrollTo(SUBMIT);
        safeClick(SUBMIT);
        waitUntilVisible(MODAL_TITLE);
        return this;
    }

    public String getModalTitle() {
        return getText(MODAL_TITLE);
    }

    public String getSubmittedValue(String label) {
        By valueCell = By.xpath("//div[contains(@class,'modal-content')]//tbody/tr"
                + "[td[1][normalize-space()='" + label + "']]/td[2]");
        return getText(valueCell);
    }

    public PracticeFormPage closeModal() {
        waitUntilVisible(CONFIRMATION_TITLE);

        WebElement button = waitUntilClickable(CLOSE_MODAL);
        scrollTo(button);

        try {
            button.click();
        } catch (ElementClickInterceptedException intercepted) {
            safeClick(button);
        }

        boolean closed = waitUntilInvisible(CONFIRMATION_TITLE, Duration.ofSeconds(3));

        if (!closed) {
            WebElement currentButton = waitUntilClickable(CLOSE_MODAL);
            scrollTo(currentButton);
            currentButton.sendKeys(org.openqa.selenium.Keys.ENTER);
            closed = waitUntilInvisible(CONFIRMATION_TITLE, Duration.ofSeconds(3));
        }

        if (!closed) {
            WebElement currentButton = driver.findElement(CLOSE_MODAL);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", currentButton);
            closed = waitUntilInvisible(CONFIRMATION_TITLE, Duration.ofSeconds(3));
        }

        if (!closed) {
            try {
                driver.findElement(CONFIRMATION_TITLE).sendKeys(org.openqa.selenium.Keys.ESCAPE);
            } catch (org.openqa.selenium.ElementNotInteractableException titleNotFocusable) {
                driver.switchTo().activeElement().sendKeys(org.openqa.selenium.Keys.ESCAPE);
            }
        }

        try {
            org.openqa.selenium.support.ui.WebDriverWait closeWait =
                    new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(10));
            closeWait.until(ExpectedConditions.invisibilityOfElementLocated(CONFIRMATION_TITLE));
            closeWait.until(ExpectedConditions.invisibilityOfElementLocated(CLOSE_MODAL));
            closeWait.until(ExpectedConditions.invisibilityOfElementLocated(MODAL_BACKDROP));
        } catch (org.openqa.selenium.TimeoutException timeout) {
            throw new org.openqa.selenium.TimeoutException(
                    "O modal de confirmação do Practice Form continuou aberto após clicar em Close.",
                    timeout);
        }

        return this;
    }

    private boolean waitUntilInvisible(By locator, Duration timeout) {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, timeout)
                    .until(ExpectedConditions.invisibilityOfElementLocated(locator));
            return true;
        } catch (org.openqa.selenium.TimeoutException stillVisible) {
            return false;
        }
    }

    private PracticeFormPage closeModalUsingLegacyClickSequence() {
        wait.until(ExpectedConditions.presenceOfElementLocated(ACTIVE_MODAL));
        WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(CLOSE_MODAL));
        scrollTo(closeButton);

        try {
            closeButton.click();
        } catch (ElementClickInterceptedException interceptedClick) {
            safeClick(closeButton);
        }

        if (!waitForModalToClose(Duration.ofSeconds(3))) {
            closeButton = wait.until(ExpectedConditions.elementToBeClickable(CLOSE_MODAL));
            scrollTo(closeButton);
            try {
                closeButton.click();
            } catch (ElementClickInterceptedException interceptedClick) {
                // O estado é verificado abaixo antes do fallback final.
            }
        }

        if (isModalOpen()) {
            closeButton = wait.until(ExpectedConditions.elementToBeClickable(CLOSE_MODAL));
            scrollTo(closeButton);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", closeButton);
        }

        waitUntilModalClosed();
        return this;
    }

    public boolean isModalVisible() {
        return isModalOpen();
    }

    private boolean waitForModalToClose(Duration timeout) {
        try {
            new WebDriverWait(driver, timeout).until(webDriver -> !isModalOpen());
            return true;
        } catch (TimeoutException modalStillOpen) {
            return false;
        }
    }

    private boolean isModalOpen() {
        return driver.findElements(ACTIVE_MODAL).stream().anyMatch(element -> {
            try {
                String classes = element.getAttribute("class");
                return element.isDisplayed()
                        && classes != null
                        && classes.contains("show");
            } catch (StaleElementReferenceException staleModal) {
                return false;
            }
        });
    }

    private void waitUntilModalClosed() {
        org.openqa.selenium.support.ui.WebDriverWait modalWait =
                new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));

        modalWait.until(ignored -> !isModalOpen());
        modalWait.until(ExpectedConditions.invisibilityOfElementLocated(MODAL_BACKDROP));
    }

    private void waitUntilModalClosedUsingLegacyConditions() {
        wait.withMessage("O modal ativo do Practice Form não desapareceu")
                .until(webDriver -> !isModalOpen());
        wait.withMessage("O backdrop do modal do Practice Form não desapareceu")
                .until(ExpectedConditions.invisibilityOfElementLocated(MODAL_BACKDROP));
    }

    private void selectReactOption(By container, By inputLocator, String value) {
        scrollTo(container);
        safeClick(container);
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(inputLocator));
        input.sendKeys(value);

        By option = By.xpath("//div[contains(@class,'-menu')]"
                + "//div[contains(@class,'-option') and normalize-space()='" + value + "']");
        new Actions(driver).moveToElement(wait.until(ExpectedConditions.elementToBeClickable(option)))
                .click()
                .perform();
    }
}
