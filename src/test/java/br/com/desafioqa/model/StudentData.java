package br.com.desafioqa.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Dados imutáveis usados para preencher e validar o Practice Form.
 */
public final class StudentData {

    private final String firstName;
    private final String lastName;
    private final String email;
    private final String gender;
    private final String mobile;
    private final LocalDate dateOfBirth;
    private final String subject;
    private final String hobby;
    private final String pictureFileName;
    private final String currentAddress;
    private final String state;
    private final String city;

    public StudentData(
            String firstName,
            String lastName,
            String email,
            String gender,
            String mobile,
            LocalDate dateOfBirth,
            String subject,
            String hobby,
            String pictureFileName,
            String currentAddress,
            String state,
            String city) {
        this.firstName = requireText(firstName, "firstName");
        this.lastName = requireText(lastName, "lastName");
        this.email = requireText(email, "email");
        this.gender = requireText(gender, "gender");
        this.mobile = requireText(mobile, "mobile");
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth, "dateOfBirth não pode ser nulo");
        this.subject = requireText(subject, "subject");
        this.hobby = requireText(hobby, "hobby");
        this.pictureFileName = requireText(pictureFileName, "pictureFileName");
        this.currentAddress = requireText(currentAddress, "currentAddress");
        this.state = requireText(state, "state");
        this.city = requireText(city, "city");
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public String getMobile() {
        return mobile;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getSubject() {
        return subject;
    }

    public String getHobby() {
        return hobby;
    }

    public String getPictureFileName() {
        return pictureFileName;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public String getState() {
        return state;
    }

    public String getCity() {
        return city;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " não pode ser nulo ou vazio");
        }
        return value;
    }
}
