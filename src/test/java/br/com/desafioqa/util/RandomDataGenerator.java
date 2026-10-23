package br.com.desafioqa.util;

import br.com.desafioqa.model.StudentData;
import br.com.desafioqa.model.WebTableRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomDataGenerator {

    private static final List<String> FIRST_NAMES =
            List.of("Alice", "Bruno", "Carla", "Daniel", "Elisa", "Felipe", "Gabriela", "Henrique");
    private static final List<String> LAST_NAMES =
            List.of("Almeida", "Barbosa", "Campos", "Duarte", "Esteves", "Ferreira", "Gomes", "Lima");
    private static final List<String> GENDERS = List.of("Male", "Female", "Other");
    private static final List<String> SUBJECTS =
            List.of("Maths", "English", "Computer Science", "Physics", "Chemistry");
    private static final List<String> HOBBIES = List.of("Sports", "Reading", "Music");
    private static final List<String> DEPARTMENTS =
            List.of("Quality Assurance", "Engineering", "Finance", "Operations", "Support");
    private static final Map<String, List<String>> CITIES_BY_STATE = Map.of(
            "NCR", List.of("Delhi", "Gurgaon", "Noida"),
            "Uttar Pradesh", List.of("Agra", "Lucknow", "Merrut"),
            "Haryana", List.of("Karnal", "Panipat"),
            "Rajasthan", List.of("Jaipur", "Jaiselmer")
    );

    private RandomDataGenerator() {
    }

    public static StudentData createStudentData() {
        String firstName = randomFrom(FIRST_NAMES);
        String lastName = randomFrom(LAST_NAMES);
        String state = randomFrom(List.copyOf(CITIES_BY_STATE.keySet()));
        String city = randomFrom(CITIES_BY_STATE.get(state));

        return new StudentData(
                firstName,
                lastName,
                uniqueEmail(firstName, lastName),
                randomFrom(GENDERS),
                generateMobile(),
                generateBirthDate(),
                randomFrom(SUBJECTS),
                randomFrom(HOBBIES),
                "upload.txt",
                "Rua das Flores, " + randomInt(10, 999) + " - Centro",
                state,
                city
        );
    }

    public static WebTableRecord createWebTableRecord() {
        String firstName = randomFrom(FIRST_NAMES);
        String lastName = randomFrom(LAST_NAMES);
        return new WebTableRecord(
                firstName,
                lastName,
                uniqueEmail(firstName, lastName),
                randomInt(18, 65),
                randomInt(30_000, 150_000),
                randomFrom(DEPARTMENTS)
        );
    }

    public static WebTableRecord createUpdatedWebTableRecord(WebTableRecord original) {
        String firstName = randomDifferent(FIRST_NAMES, original.getFirstName());
        String department = randomDifferent(DEPARTMENTS, original.getDepartment());
        int salary;
        do {
            salary = randomInt(30_000, 150_000);
        } while (salary == original.getSalary());

        return new WebTableRecord(
                firstName,
                original.getLastName(),
                original.getEmail(),
                original.getAge(),
                salary,
                department
        );
    }

    public static String generateUsername() {
        return "qa_" + System.currentTimeMillis() + "_" + shortUuid();
    }

    public static String generatePassword() {
        int digit = randomInt(0, 9);
        return "Qa!" + digit + shortUuid() + "x";
    }

    private static String uniqueEmail(String firstName, String lastName) {
        return (firstName + "." + lastName + "." + System.currentTimeMillis() + "." + shortUuid()
                + "@example.com").toLowerCase();
    }

    private static String generateMobile() {
        StringBuilder mobile = new StringBuilder();
        mobile.append(randomInt(1, 9));
        for (int index = 1; index < 10; index++) {
            mobile.append(randomInt(0, 9));
        }
        return mobile.toString();
    }

    private static LocalDate generateBirthDate() {
        long minimum = LocalDate.of(1980, 1, 1).toEpochDay();
        long maximum = LocalDate.of(2005, 12, 31).toEpochDay();
        return LocalDate.ofEpochDay(ThreadLocalRandom.current().nextLong(minimum, maximum + 1));
    }

    private static String shortUuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private static int randomInt(int minimum, int maximum) {
        return ThreadLocalRandom.current().nextInt(minimum, maximum + 1);
    }

    private static String randomFrom(List<String> values) {
        return values.get(ThreadLocalRandom.current().nextInt(values.size()));
    }

    private static String randomDifferent(List<String> values, String currentValue) {
        String value;
        do {
            value = randomFrom(values);
        } while (value.equals(currentValue));
        return value;
    }
}
