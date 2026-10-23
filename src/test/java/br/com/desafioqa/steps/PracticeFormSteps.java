package br.com.desafioqa.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import br.com.desafioqa.driver.DriverManager;
import br.com.desafioqa.model.StudentData;
import br.com.desafioqa.pages.HomePage;
import br.com.desafioqa.pages.PracticeFormPage;
import br.com.desafioqa.util.RandomDataGenerator;
import br.com.desafioqa.util.ResourceUtils;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public class PracticeFormSteps {

    private static final DateTimeFormatter SUBMITTED_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMMM,yyyy", Locale.ENGLISH);

    private HomePage homePage;
    private PracticeFormPage practiceFormPage;
    private StudentData student;

    @Dado("que acesso o formulário Practice Form")
    public void accessPracticeForm() {
        homePage = new HomePage(DriverManager.getDriver());
        practiceFormPage = new PracticeFormPage(DriverManager.getDriver());

        homePage.open()
                .selectCard("Forms")
                .selectSubmenu("Practice Form");
    }

    @Quando("preencho e submeto o formulário com dados válidos")
    public void fillAndSubmitValidForm() {
        student = RandomDataGenerator.createStudentData();
        Path uploadFile = ResourceUtils.getResourcePath("files/upload.txt");

        practiceFormPage.fillForm(student, uploadFile)
                .submit();
    }

    @Então("o modal de confirmação deve exibir todos os dados informados")
    public void confirmationModalShouldShowSubmittedData() {
        assertEquals("Thanks for submitting the form", practiceFormPage.getModalTitle());
        assertEquals(student.getFullName(), valueOf("Student Name"));
        assertEquals(student.getEmail(), valueOf("Student Email"));
        assertEquals(student.getGender(), valueOf("Gender"));
        assertEquals(student.getMobile(), valueOf("Mobile"));
        assertEquals(student.getDateOfBirth().format(SUBMITTED_DATE_FORMAT), valueOf("Date of Birth"));
        assertEquals(student.getSubject(), valueOf("Subjects"));
        assertEquals(student.getHobby(), valueOf("Hobbies"));
        assertEquals(student.getPictureFileName(), valueOf("Picture"));
        assertEquals(student.getCurrentAddress(), valueOf("Address"));
        assertEquals(student.getState() + " " + student.getCity(), valueOf("State and City"));
    }

    @E("posso fechar o modal de confirmação")
    public void closeConfirmationModal() {
        practiceFormPage.closeModal();
        assertFalse(practiceFormPage.isModalVisible(), "O modal de confirmação deveria estar fechado");
    }

    private String valueOf(String label) {
        return practiceFormPage.getSubmittedValue(label);
    }
}
